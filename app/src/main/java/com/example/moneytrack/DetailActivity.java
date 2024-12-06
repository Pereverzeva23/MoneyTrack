package com.example.moneytrack;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

import android.widget.Button;
import android.widget.ListView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity implements TransactionDialogFragment.TransactionDialogListener {
    private ArrayList<String> transactions; // Список транзакций
    private ArrayAdapter<String> adapter;
    private double currentBalance = 0; // Начальный баланс
    private double targetBalance; // Целевая сумма для прогресса
    private String piggyBankName; // Имя копилки
    private Button backButton;
    private ProgressBar progressBar;
    private TextView progressTextView;

    private ActivityResultLauncher<Intent> editPiggyBankLauncher; // Лаунчер для редактирования копилки

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity);

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish()); // Закрываем текущую активность
        Button threeDotsButton = findViewById(R.id.threeDotsButton);
        threeDotsButton.setOnClickListener(viewClickListener);

        transactions = new ArrayList<>();
        TextView textViewBalance = findViewById(R.id.textViewBalance);
        TextView textViewPiggyBankName = findViewById(R.id.textViewPiggyBankName); // Получаем ссылку на TextView для названия копилки
        ListView transactionsListView = findViewById(R.id.transactionsListView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, transactions);
        transactionsListView.setAdapter(adapter);

        // Получаем имя копилки, целевую сумму и баланс из Intent
        piggyBankName = getIntent().getStringExtra("name");
        targetBalance = Double.parseDouble(getIntent().getStringExtra("goal")); // Получаем целевую сумму
        String balanceString = getIntent().getStringExtra("balance"); // Получаем баланс

        // Загружаем данные из SharedPreferences
        loadData();

        // Устанавливаем начальный баланс
        if (currentBalance == 0 && balanceString != null) {
            currentBalance = Double.parseDouble(balanceString); // Устанавливаем баланс из Intent, если он не был загружен
        }
        // Устанавливаем название копилки в TextView
        textViewPiggyBankName.setText(piggyBankName);
        // Устанавливаем начальный баланс в TextView
        textViewBalance.setText(formatBalance(currentBalance));

        // Инициализируем ProgressBar и текстовое поле для отображения прогресса
        progressBar = findViewById(R.id.progressBar);
        progressTextView = findViewById(R.id.progressTextView);

        // Обновляем прогресс при создании активности
        updateProgress();

        // Обработка нажатия на кнопку пополнения
        Button depositButton = findViewById(R.id.depositButton);
        depositButton.setOnClickListener(v -> {
            TransactionDialogFragment dialog = TransactionDialogFragment.newInstance(true);
            dialog.show(getSupportFragmentManager(), "TransactionDialog");
        });

        // Обработка нажатия на кнопку вывода
        Button withdrawButton = findViewById(R.id.withdrawButton);
        withdrawButton.setOnClickListener(v -> {
            TransactionDialogFragment dialog = TransactionDialogFragment.newInstance(false);
            dialog.show(getSupportFragmentManager(), "TransactionDialog");
        });

        editPiggyBankLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // Получаем обновленные данные
                        String updatedName = result.getData().getStringExtra("name");
                        String updatedGoal = result.getData().getStringExtra("goal");
                        String updatedBalance = result.getData().getStringExtra("balance");
                        int position = getIntent().getIntExtra("position", -1); // Получаем позицию

                        if (position != -1) {
                            // Обновляем данные копилки
                            piggyBankName = updatedName;
                            targetBalance = Double.parseDouble(updatedGoal);
                            currentBalance = Double.parseDouble(updatedBalance);

                            // Устанавливаем название копилки в TextView
                            textViewPiggyBankName.setText(piggyBankName);

                            // Обновляем отображение баланса
                            textViewBalance.setText(formatBalance(currentBalance)); // Обновляем отображение баланса

                            // Обновляем прогресс
                            updateProgress();

                            // Сохраняем обновленные данные в SharedPreferences
                            saveData();

                            // Передаем обновленные данные обратно в MainActivity
                            Intent intent = new Intent();
                            intent.putExtra("name", updatedName);
                            intent.putExtra("goal", updatedGoal);
                            intent.putExtra("balance", updatedBalance);
                            intent.putExtra("position", position); // Передаем позицию копилки
                            setResult(RESULT_OK, intent); // Устанавливаем результат
                            finish(); // Закрываем DetailActivity
                        }
                    }
                }
        );
    }

    View.OnClickListener viewClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            showPopupMenu(v);
        }
    };

    private void showPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.inflate(R.menu.popup_menu);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.edit1) {
                    editPiggyBank();
                    return true;
                } else if(item.getItemId() == R.id.delete2) {
                    deletePiggyBank();
                    return true;
                } else {
                    return false;
                }
            }

            private void editPiggyBank() {
                Intent intent = new Intent(DetailActivity.this, AddActivity.class);
                intent.putExtra("name", piggyBankName); // Передаем имя копилки
                intent.putExtra("goal", String.valueOf(targetBalance)); // Передаем целевую сумму
                intent.putExtra("balance", String.valueOf(currentBalance)); // Передаем баланс
                intent.putExtra("position", getIntent().getIntExtra("position", -1)); // Передаем позицию копилки
                editPiggyBankLauncher.launch(intent); // Запускаем AddActivity для редактирования
            }


            private void deletePiggyBank() {
                Intent intent = new Intent();
                intent.putExtra("deletedPiggyBank", piggyBankName); // Передаем имя копилки для удаления
                intent.putExtra("position", getIntent().getIntExtra("position", -1)); // Передаем позицию копилки
                setResult(RESULT_OK, intent); // Устанавливаем результат
                finish(); // Закрываем DetailActivity
            }
        });

        popupMenu.setOnDismissListener(menu -> { });
        popupMenu.show();
    }

    @Override
    public void onTransactionSaved(double amount, String description, boolean isDeposit) {
        // Обновляем баланс
        if (isDeposit) {
            currentBalance += amount;
        } else {
            currentBalance -= amount;
        }

        // Обновляем текст в TextView для отображения нового баланса
        TextView textViewBalance = findViewById(R.id.textViewBalance);
        textViewBalance.setText(formatBalance(currentBalance)); // Обновляем отображение баланса

        // Добавляем транзакцию в список
        String transaction = String.format("%s: %s %.2f",
                new SimpleDateFormat("dd MMMM yyyy HH:mm", new Locale("ru")).format(new Date()),
                isDeposit ? "+" : "-", amount);
        transactions.add(transaction + " - " + description);
        adapter.notifyDataSetChanged();

        // Сохраняем данные
        saveData();

        // Обновляем прогресс
        updateProgress();
    }

    private void updateProgress() {
        // Рассчитываем процент достижения целевой суммы
        double progressPercentage = (currentBalance / targetBalance) * 100;

        // Обновляем ProgressBar и текстовое поле, если процент меньше или равен 100%
        if (progressPercentage <= 100) {
            progressBar.setProgress((int) progressPercentage);
            progressTextView.setText(String.format("%.2f%% (Осталось: %.2f)", progressPercentage, targetBalance - currentBalance));
        } else {
            // Если процент больше 100%, устанавливаем 100% и не обновляем
            progressBar.setProgress(100);
            progressTextView.setText("100% (Цель достигнута)");
        }
    }
   private void saveData(){
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("balance_" + piggyBankName, String.valueOf(currentBalance));
       editor.putString("transactions_" + piggyBankName, TextUtils.join(";", transactions)); // Сохраняем транзакции через разделитель
       editor.apply();
   }


    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE);
        currentBalance = Double.parseDouble(sharedPreferences.getString("balance_" + piggyBankName, "0"));
        String transactionsString = sharedPreferences.getString("transactions_" + piggyBankName, "");
        if (!transactionsString.isEmpty()) {
            transactions.addAll(Arrays.asList(transactionsString.split(";"))); // Восстанавливаем транзакции
        }
    }

    private String formatBalance(double balance) {
        // Форматируем баланс для отображения
        if (balance == (int) balance) {
            return String.valueOf((int) balance); // Целое число
        } else {
            return String.valueOf(balance); // С дробной частью
        }
    }

}
