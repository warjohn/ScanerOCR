package com.example.smartscanner;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileUtils {

    public static void copyAssetsToStorage(Context context, String assetPath, String storagePath) {
        try {
            // Получаем список файлов из assets
            String[] files = context.getAssets().list(assetPath);

            if (files != null && files.length > 0) {
                // Создаем директорию tessdata
                File dir = new File(storagePath + "tessdata");
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                // Копируем каждый файл
                for (String file : files) {
                    InputStream in = context.getAssets().open(assetPath + "/" + file);
                    FileOutputStream out = new FileOutputStream(storagePath + "tessdata/" + file);

                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }

                    in.close();
                    out.flush();
                    out.close();

                    System.out.println("Скопирован файл: " + file);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}