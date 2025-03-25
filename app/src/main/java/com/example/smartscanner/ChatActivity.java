package com.example.smartscanner;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChat;
    private EditText editTextMessage;
    private Button buttonSend;
    private ChatAdapter chatAdapter;
    private ArrayList<String> messages = new ArrayList<>();
    private Llm llm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Инициализация Llm
        llm = new Llm(this);

        // Находим элементы интерфейса
        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);

        // Настройка RecyclerView
        chatAdapter = new ChatAdapter(messages);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(chatAdapter);

        // Обработка нажатия на кнопку "Send"
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userMessage = editTextMessage.getText().toString().trim();
                if (!userMessage.isEmpty()) {
                    // Добавляем сообщение пользователя в список
                    messages.add("You: " + userMessage);
                    chatAdapter.notifyItemInserted(messages.size() - 1);

                    // Очищаем поле ввода
                    editTextMessage.setText("");

                    // Получаем ответ от LLM
                    String response = llm.getResponse(userMessage);
                    messages.add("Bot: " + response);
                    chatAdapter.notifyItemInserted(messages.size() - 1);

                    // Прокручиваем RecyclerView до последнего сообщения
                    recyclerViewChat.scrollToPosition(messages.size() - 1);
                }
            }
        });
    }
}