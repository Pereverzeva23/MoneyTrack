package com.example.moneytrack;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class TransactionDialogFragment extends DialogFragment {
    private EditText amountEditText;
    private EditText descriptionEditText;
    private boolean isDeposit; // true для пополнения, false для вывода

    public interface TransactionDialogListener {
        void onTransactionSaved(double amount, String description, boolean isDeposit);
    }

    public static TransactionDialogFragment newInstance(boolean isDeposit) {
        TransactionDialogFragment fragment = new TransactionDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean("isDeposit", isDeposit);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        isDeposit = getArguments() != null && getArguments().getBoolean("isDeposit", true);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_fragment, null);

        amountEditText = view.findViewById(R.id.amountEditText);
        descriptionEditText = view.findViewById(R.id.descriptionEditText);

        builder.setView(view)
                .setTitle(isDeposit ? "Пополнение" : "Вывод")
                .setPositiveButton("Сохранить", (dialog, id) -> {
                    String amountStr = amountEditText.getText().toString();
                    String description = descriptionEditText.getText().toString();
                    if (!amountStr.isEmpty() && !description.isEmpty()) {
                        double amount = Double.parseDouble(amountStr);
                        TransactionDialogListener listener = (TransactionDialogListener) getActivity();
                        listener.onTransactionSaved(amount, description, isDeposit);
                    } else {
                        Toast.makeText(getActivity(), "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", (dialog, id) -> TransactionDialogFragment.this.getDialog().cancel());

        return builder.create();
    }
}
