package com.example.moneytrack;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    private ArrayList<String> piggyBanks; // Список для хранения данных о копилках
    private ArrayAdapter<String> adapter; // Адаптер для отображения списка
    private ActivityResultLauncher<Intent> addPiggyBankLauncher; // Лаунчер для добавления копилки
    private ActivityResultLauncher<Intent> editPiggyBankLauncher; // Лаунчер для редактирования копилки
    private static final int EDIT_PIGGY_BANK_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView listView = findViewById(R.id.listView);
        piggyBanks = new ArrayList<>();

        // Загружаем список из SharedPreferences
        loadPiggyBanks();

        // Создаем адаптер и устанавливаем его в ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getPiggyBankNames());
        listView.setAdapter(adapter);

        // Инициализация лаунчеров
        addPiggyBankLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String name = result.getData().getStringExtra("name");
                        String goal = result.getData().getStringExtra("goal");
                        String balance = result.getData().getStringExtra("balance");

                        if (name != null) {
                            // Добавляем новую копилку в список
                            piggyBanks.add(name + "," + goal + "," + balance);
                            updateAdapter();
                            savePiggyBanks(); // Сохраняем изменения в SharedPreferences
                        }
                    }
                });

        editPiggyBankLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String updatedName = result.getData().getStringExtra("name");
                        String updatedGoal = result.getData().getStringExtra("goal");
                        String updatedBalance = result.getData().getStringExtra("balance");
                        int position = result.getData().getIntExtra("position", -1);

                        if (position != -1) {
                            // Обновляем данные в списке
                            piggyBanks.set(position, updatedName + "," + updatedGoal + "," + updatedBalance);
                            updateAdapter();
                            savePiggyBanks(); // Сохраняем изменения в SharedPreferences
                        }
                    }
                });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (!piggyBanks.isEmpty() && position < piggyBanks.size()) {
                Intent detailIntent = new Intent(MainActivity.this, DetailActivity.class);
                String[] details = piggyBanks.get(position).split(","); // Получаем данные копилки
                detailIntent.putExtra("name", details[0]); // Передаем имя копилки
                detailIntent.putExtra("goal", details[1]); // Передаем целевую сумму
                detailIntent.putExtra("balance", details[2]); // Передаем баланс
                detailIntent.putExtra("position", position); // Передаем позицию копилки
                editPiggyBankLauncher.launch(detailIntent); // Запускаем DetailActivity для редактирования
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent1 = new Intent(this, AddActivity.class);
            addPiggyBankLauncher.launch(intent1); // Запускаем AddActivity для добавления новой копилки
        });
    }

    private void updateAdapter() {
        adapter.clear(); // Очищаем адаптер
        adapter.addAll(getPiggyBankNames()); // Добавляем только названия копилок
        adapter.notifyDataSetChanged(); // Обновляем адаптер
    }

    private void loadPiggyBanks() {
        SharedPreferences sharedPreferences = getSharedPreferences("PiggyBanks", MODE_PRIVATE);
        String savedPiggyBanks = sharedPreferences.getString("piggyBanks", "");
        if (!savedPiggyBanks.isEmpty()) {
            String[] banks = savedPiggyBanks.split(";");
            Collections.addAll(piggyBanks, banks);
        }
    }

    private void savePiggyBanks() {
        SharedPreferences sharedPreferences = getSharedPreferences("PiggyBanks", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        StringBuilder builder = new StringBuilder();
        for (String bank : piggyBanks) {
            builder.append(bank).append(";");
        }
        editor.putString("piggyBanks", builder.toString());
        editor.apply();
    }

    // Метод для получения только названий копилок
    private ArrayList<String> getPiggyBankNames() {
        ArrayList<String> names = new ArrayList<>();
        for (String bank : piggyBanks) {
            String[] details = bank.split(","); // Разделяем данные на части
            names.add(details[0]); // Добавляем только имя копилки в новый список
        }
        return names;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PIGGY_BANK_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // Обработка обновления копилки
            String updatedName = data.getStringExtra("name");
            String updatedGoal = data.getStringExtra("goal");
            String updatedBalance = data.getStringExtra("balance");
            int position = data.getIntExtra("position", -1);

            if (position != -1) {
                piggyBanks.set(position, updatedName + "," + updatedGoal + "," + updatedBalance);
                updateAdapter();
                savePiggyBanks();
            }
        } else if (resultCode == RESULT_OK && data != null && data.hasExtra("deletedPiggyBank")) {
            // Обработка удаления копилки
            int position = data.getIntExtra("position", -1);
            if (position != -1) {
                piggyBanks.remove(position); // Удаляем копилку из списка
                updateAdapter(); // Обновляем адаптер
                savePiggyBanks(); // Сохраняем изменения в SharedPreferences
            }
        }
    }
}


