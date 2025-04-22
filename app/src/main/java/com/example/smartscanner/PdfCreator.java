package com.example.smartscanner;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

public class PdfCreator {

    // Создание PDF-файла с изображением
    public static File createPdf(Bitmap bitmap, Context context) {
        // Создаем PDF-документ
        PdfDocument document = new PdfDocument();

        // Размеры страницы A4
        int pageWidth = 595; // A4 width in points
        int pageHeight = 842; // A4 height in points

        // Создаем страницу
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Рассчитываем размеры для отрисовки Bitmap
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();

        // Масштабируем изображение, чтобы оно помещалось на странице
        float scale = Math.min((float) pageWidth / bitmapWidth, (float) pageHeight / bitmapHeight);
        int scaledWidth = (int) (bitmapWidth * scale);
        int scaledHeight = (int) (bitmapHeight * scale);

        // Рисуем Bitmap в центре страницы
        int x = (pageWidth - scaledWidth) / 2;
        int y = (pageHeight - scaledHeight) / 2;
        canvas.drawBitmap(bitmap, null, new android.graphics.Rect(x, y, x + scaledWidth, y + scaledHeight), null);

        // Завершаем страницу
        document.finishPage(page);

        // Сохраняем PDF через MediaStore
        try {
            return savePdfToDocuments(context, document);
        } catch (IOException e) {
            e.printStackTrace();
            return null; // Возвращаем null в случае ошибки
        } finally {
            document.close(); // Закрываем документ
        }
    }

    private static File savePdfToDocuments(Context context, PdfDocument document) throws IOException {
        ContentValues values = new ContentValues();
        String fileName = generateRandomNumericFileName(8) + ".pdf";
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS);

        Uri uri = context.getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
        if (uri == null) {
            throw new IOException("Failed to create new MediaStore record.");
        }

        try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
            document.writeTo(outputStream);
        }

        return new File(uri.getPath());
    }

    private static String generateRandomNumericFileName(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10)); // Генерация цифры от 0 до 9
        }
        return sb.toString();
    }
}