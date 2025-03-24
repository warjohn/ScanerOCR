package com.example.smartscanner;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PdfCreator {
    // Создание PDF-файла
    public static File createPdf(String text, Context context) {
        // Создаем PDF-документ
        PdfDocument document = new PdfDocument();
        Paint paint = new Paint();
        paint.setTextSize(12);

        // Размеры страницы A4
        int pageWidth = 595; // A4 width in points
        int pageHeight = 842; // A4 height in points

        // Создаем страницу
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Рисуем текст
        String[] lines = text.split("\n");
        float y = 20; // Начальная позиция по вертикали
        for (String line : lines) {
            canvas.drawText(line, 20, y, paint);
            y += 20; // Перемещаемся на следующую строку
        }

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
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, "output.pdf");
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
}