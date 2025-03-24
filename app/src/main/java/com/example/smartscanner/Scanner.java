package com.example.smartscanner;

import android.content.ContentValues;
import android.content.Context;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner;
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult;

import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Scanner {
    private final WeakReference<AppCompatActivity> activityRef;
    private final GmsDocumentScannerOptions options;
    private final GmsDocumentScanner scanner;
    private final ActivityResultLauncher<IntentSenderRequest> scannerLauncher;
    private final BlockingQueue<Uri> imageQueue = new LinkedBlockingQueue<>();
    private boolean isRunning = true;

    private TesseractHelper tesseractHelper;
    private boolean shouldContinueScanning = true;

    public Scanner(AppCompatActivity activity, TesseractHelper tesseractHelper) {
        this.activityRef = new WeakReference<>(activity);
        this.tesseractHelper = tesseractHelper;

        // Настройка параметров сканера
        options = new GmsDocumentScannerOptions.Builder()
                .setGalleryImportAllowed(false)
                .setPageLimit(2)
                .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG, GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
                .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
                .build();

        // Создание клиента сканера
        scanner = GmsDocumentScanning.getClient(options);

        // Регистрация лаунчера для получения результата
        AppCompatActivity activityInstance = activityRef.get();
        if (activityInstance == null) {
            throw new IllegalStateException("Activity is null. Cannot initialize scanner.");
        }

        // Инициализация scannerLauncher
        scannerLauncher = activityInstance.registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                        // Обработка результата сканирования
                        GmsDocumentScanningResult scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.getData());

                        // Добавляем страницы в очередь
                        for (GmsDocumentScanningResult.Page page : scanningResult.getPages()) {
                            imageQueue.add(page.getImageUri());
                        }
                        startScan();
                    }
                });

        // Запуск фонового потока
        startBackgroundProcessing();
    }

    // Метод для запуска сканера
    public void startScan() {
        AppCompatActivity activity = activityRef.get();
        if (activity == null) return;

        Task<IntentSender> intentSenderTask = scanner.getStartScanIntent(activity);

        intentSenderTask.addOnSuccessListener(intentSender -> {
            IntentSenderRequest request = new IntentSenderRequest.Builder(intentSender).build();
            scannerLauncher.launch(request);
        }).addOnFailureListener(e -> {
            e.printStackTrace();
        });
    }

    private void startBackgroundProcessing() {
        Thread backgroundThread = new Thread(() -> {
            while (isRunning) {
                try {
                    // Берем URI из очереди
                    Uri imageUri = imageQueue.take();

                    // Выполняем анализ и сохранение
                    analyzeAndSaveImage(imageUri);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break; // Прерываем цикл при остановке потока
                }
            }
        });
        backgroundThread.start();
    }

    // Остановка сканирования
    public void stopProcessing() {
        isRunning = false;
    }


    // Анализ и сохранение изображения
    private void analyzeAndSaveImage(Uri imageUri) {
        try {
            AppCompatActivity activity = activityRef.get();
            if (activity == null) return;

            // Преобразуем URI в Bitmap
            InputStream inputStream = activity.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // Распознаем текст
            String recognizedText = tesseractHelper.extractTextFromImage(bitmap);
            System.out.println("Распознанный текст: " + recognizedText);

            // Создаем PDF
            File pdfFile = PdfCreator.createPdf(recognizedText, activity);
            System.out.println("PDF сохранён: " + pdfFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}