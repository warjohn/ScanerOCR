package com.example.smartscanner;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private Scanner scanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TesseractHelper tesseractHelper = null;
        try {
            tesseractHelper = new TesseractHelper();
            tesseractHelper.initializeTesseract(this);
            Log.d("OcrHelper", "OcrHelper initialized successfully");
        } catch (Exception e) {
            Log.d("OcrHelper", "Failed to initialize OcrHelper - " + e.getMessage());
        }

        // Проверка разрешений
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        // Инициализация сканера
        scanner = new Scanner(this, tesseractHelper);

        // Запуск сканера
        scanner.startScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scanner != null) {
            scanner.stopProcessing(); // Останавливаем сканирование при уничтожении активности
        }
    }

}