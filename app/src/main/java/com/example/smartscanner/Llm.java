package com.example.smartscanner;

import static com.google.mediapipe.tasks.genai.llminference.LlmInference.Backend.GPU;

import android.content.Context;
import android.util.Log;

import com.google.mediapipe.tasks.genai.llminference.LlmInference;
import com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Llm {

    private LlmInferenceOptions options;
    private LlmInference llmInference;
    private String modelName = "gemma-2b-it-gpu-int4.bin";
    public Llm(Context context) {
        init(context);
    }

    private void init(Context context) {
        try {
            // Копируем модель из assets во временную директорию
            File modelFile = copyModelFromAssets(context, modelName);

            // Указываем путь к скопированной модели
            LlmInferenceOptions options = LlmInferenceOptions.builder()
                    .setModelPath(modelFile.getAbsolutePath()) // Путь к скопированной модели
                    .setMaxTokens(1000)
                    .setMaxTopK(40)
                    .setPreferredBackend(GPU)
                    .build();

            // Инициализируем LlmInference с настроенными опциями
            llmInference = LlmInference.createFromOptions(context, options);
        } catch (IOException e) {
            Log.e("Llm", "Failed to initialize model: " + e.getMessage());
            throw new RuntimeException("Failed to initialize model", e);
        }
    }

    /**
     * Копирует модель из папки assets во временную директорию.
     *
     * @param context   Контекст приложения.
     * @param modelName Имя файла модели в папке assets.
     * @return Файл, представляющий скопированную модель.
     * @throws IOException Если произошла ошибка при копировании.
     */
    private File copyModelFromAssets(Context context, String modelName) throws IOException {
        // Создаем временный файл для модели
        File modelFile = new File(context.getFilesDir(), modelName);

        // Если файл уже существует, не копируем его снова
        if (modelFile.exists()) {
            return modelFile;
        }

        // Открываем поток для чтения файла из assets
        try (InputStream inputStream = context.getAssets().open(modelName);
             FileOutputStream outputStream = new FileOutputStream(modelFile)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        return modelFile;
    }


    /**
     * Функция для получения ответа на текст.
     *
     * @param inputText Входной текст для модели.
     * @return Ответ, сгенерированный моделью.
     */
    public String getResponse(String inputText) {
        if (inputText == null || inputText.isEmpty()) {
            return "Пустой запрос. Пожалуйста, укажите текст.";
        }
        String systemPrompt = "Вы являетесь языковым ассистентом, который предоставляет ответы на вопросы и комментирует на" +
                " русском языке.Старайся писать без ошибок " +
                "Вот текст для которого вы должны предоставить ответ - ";
        String response = llmInference.generateResponse(systemPrompt + "\n\n" + inputText);
        return response != null ? response : "Ошибка генерации ответа.";
    }

    /**
     * Функция для исправления ошибок и восстановления структуры документа с использованием HTML-тегов.
     *
     * @param inputText Входной текст для анализа и исправления.
     * @return Исправленный текст с восстановленной структурой в формате HTML.
     */
    public String fixAndReformatDocumentWithTags(String inputText) {
        if (inputText == null || inputText.isEmpty()) {
            return "Пустой запрос. Пожалуйста, укажите текст.";
        }

        // Системная инструкция для модели: исправить ошибки и восстановить структуру с HTML-тегами
        String systemPrompt = "Исправь все ошибки в тексте, включая грамматические и пунктуационные. "
                + "Восстанови логическую структуру документа, если она нарушена. "
                + "Форматируй текст с использованием HTML-тегов: "
                + "- Используй <h1>, <h2>, <h3> для заголовков разного уровня. "
                + "- Используй <p> для параграфов. "
                + "- Используй <ul> и <li> для неупорядоченных списков. "
                + "- Используй <ol> и <li> для упорядоченных списков. "
                + "- Используй <table>, <tr>, <th>, <td> для таблиц. "
                + "- Используй <a href='...'> для ссылок. "
                + "- И другие подобные теги для оформления текста. "
                + "Текст должен быть читаемым, профессионально оформленным и полностью структурированным.";

        // Генерация исправленного текста с HTML-тегами
        String correctedText = llmInference.generateResponse(systemPrompt + "\n\n" + inputText);

        // Возвращаем исправленный текст
        return correctedText != null ? correctedText : "Ошибка исправления текста.";
    }
}