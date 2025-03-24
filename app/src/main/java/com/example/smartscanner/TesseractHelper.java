package com.example.smartscanner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;

public class TesseractHelper {
    private static final String DATA_PATH = "/mnt/sdcard/tesseract/";
    private static final String LANG = "rus"; // Укажите нужный язык
    private Context context;
    public void initializeTesseract(Context context) {
        // Определяем путь для хранения данных
        this.context = context;
        String internalDataPath = context.getFilesDir().getAbsolutePath() + "/tesseract/";

        // Создаем директорию, если она не существует
        File tessDataDir = new File(internalDataPath + "tessdata");
        if (!tessDataDir.exists()) {
            tessDataDir.mkdirs();
        }

        // Копируем файлы из assets в эту директорию
        FileUtils.copyAssetsToStorage(context, "tessdata", internalDataPath);

        // Проверяем, что файл rus.traineddata скопирован
        File trainedDataFile = new File(internalDataPath + "tessdata/rus.traineddata");
        if (!trainedDataFile.exists()) {
            System.out.println("Ошибка: Файл rus.traineddata не найден!");
            return;
        }

        // Инициализируем Tesseract
        TessBaseAPI tessBaseAPI = new TessBaseAPI();
        if (!tessBaseAPI.init(internalDataPath, LANG)) {
            System.out.println("Ошибка инициализации Tesseract");
        } else {
            System.out.println("Tesseract успешно инициализирован");
        }
    }
    public String extractTextFromImage(Bitmap bitmap) {
        TessBaseAPI tessBaseAPI = new TessBaseAPI();

        // Используем тот же путь, что и в initializeTesseract
        String internalDataPath = context.getFilesDir().getAbsolutePath() + "/tesseract/";

        // Инициализация Tesseract
        if (!tessBaseAPI.init(internalDataPath, LANG)) {
            return "Ошибка инициализации Tesseract";
        }
        Bitmap bitmap1 = preprocessImage(bitmap);
        tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
        // Установка изображения
        tessBaseAPI.setImage(bitmap1);

        // Получение распознанного текста
        String extractedText = tessBaseAPI.getUTF8Text();

        // Освобождение ресурсов
        tessBaseAPI.end();

        return extractedText;
    }
    private Bitmap preprocessImage(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Преобразование в оттенки серого
        Bitmap grayscaleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(grayscaleBitmap);
        Paint paint = new Paint();
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0); // Убираем цвет
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        paint.setColorFilter(filter);
        canvas.drawBitmap(bitmap, 0, 0, paint);

        return grayscaleBitmap;
    }

}