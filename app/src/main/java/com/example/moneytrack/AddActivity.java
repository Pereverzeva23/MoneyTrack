package com.example.moneytrack;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class AddActivity extends Activity {
    private EditText editTextName, editTextGoal, editTextBalance;
    private Button backButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_activity);
        editTextName = findViewById(R.id.editTextName);
        editTextGoal = findViewById(R.id.editTextGoal);
        editTextBalance = findViewById(R.id.editTextBalance);
        Button buttonSave = findViewById(R.id.buttonSave);
        backButton = findViewById(R.id.backButton);

        // Получаем данные из Intent
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String goal = intent.getStringExtra("goal");
        String balance = intent.getStringExtra("balance");
        int position = intent.getIntExtra("position", -1); // Получаем позицию

        // Заполняем поля данными
        editTextName.setText(name);
        editTextGoal.setText(goal);
        editTextBalance.setText(balance);

        backButton.setOnClickListener(v -> {
            finish(); // Закрываем текущую активность
        });

        buttonSave.setOnClickListener(v -> {
            String updatedName = editTextName.getText().toString();
            String updatedGoal = editTextGoal.getText().toString();
            String updatedBalance = editTextBalance.getText().toString();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("name", updatedName);
            resultIntent.putExtra("goal", updatedGoal);
            resultIntent.putExtra("balance", updatedBalance);
            resultIntent.putExtra("position", position); // Возвращаем позицию
            setResult(RESULT_OK, resultIntent);

            // Переход к DetailActivity после сохранения
            Intent detailIntent = new Intent(AddActivity.this, DetailActivity.class);
            detailIntent.putExtra("name", updatedName);
            detailIntent.putExtra("goal", updatedGoal);
            detailIntent.putExtra("balance", updatedBalance);
            detailIntent.putExtra("position", position);
            startActivity(detailIntent); // Запускаем DetailActivity
            finish(); // Закрываем AddActivity
        });
    }
}


