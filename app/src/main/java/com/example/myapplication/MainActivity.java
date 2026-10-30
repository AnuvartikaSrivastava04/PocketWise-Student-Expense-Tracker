
package com.example.myapplication;

import android.app.AlertDialog;
import android.os.Bundle;
import android.database.Cursor;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends ComponentActivity {

    private DatabaseHelper dbHelper;
    private TextView totalText;
    private TextView emptyText;
    private LinearLayout expenseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        totalText = findViewById(R.id.totalText);
        emptyText = findViewById(R.id.emptyText);
        expenseList = findViewById(R.id.expenseList);
        Button addButton = findViewById(R.id.addButton);

        addButton.setOnClickListener(v -> showAddExpenseDialog());

        loadExpenses();
    }

    private void showAddExpenseDialog() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(32, 8, 32, 8);

        EditText nameInput = new EditText(this);
        nameInput.setHint("Expense name (e.g. Lunch)");
        form.addView(nameInput);

        EditText amountInput = new EditText(this);
        amountInput.setHint("Amount in ₹");
        amountInput.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER
                        | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        form.addView(amountInput);

        EditText categoryInput = new EditText(this);
        categoryInput.setHint("Category (e.g. Food)");
        form.addView(categoryInput);

        new AlertDialog.Builder(this)
                .setTitle("Add Expense")
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String amountText =
                            amountInput.getText().toString().trim();
                    String category =
                            categoryInput.getText().toString().trim();

                    if (name.isEmpty() || amountText.isEmpty()
                            || category.isEmpty()) {
                        Toast.makeText(this,
                                "Please fill in all fields",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double amount = Double.parseDouble(amountText);

                        if (amount <= 0) {
                            Toast.makeText(this,
                                    "Enter an amount greater than zero",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String date = new SimpleDateFormat(
                                "dd MMM yyyy", Locale.getDefault())
                                .format(new Date());

                        long result = dbHelper.addExpense(
                                name, amount, category, date);

                        if (result != -1) {
                            loadExpenses();
                            Toast.makeText(this,
                                    "Expense saved!",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this,
                                    "Could not save expense",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this,
                                "Please enter a valid amount",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void loadExpenses() {
        expenseList.removeAllViews();

        totalText.setText(String.format(
                Locale.getDefault(), "₹%.2f", dbHelper.getTotal()));

        Cursor cursor = dbHelper.getExpenses();

        try {
            boolean hasExpenses = cursor.moveToFirst();
            emptyText.setVisibility(
                    hasExpenses ? View.GONE : View.VISIBLE);

            while (hasExpenses) {
                long id = cursor.getLong(
                        cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(
                        cursor.getColumnIndexOrThrow("name"));
                double amount = cursor.getDouble(
                        cursor.getColumnIndexOrThrow("amount"));
                String category = cursor.getString(
                        cursor.getColumnIndexOrThrow("category"));
                String date = cursor.getString(
                        cursor.getColumnIndexOrThrow("date"));

                addExpenseRow(id, name, amount, category, date);
                hasExpenses = cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
    }

    private void addExpenseRow(long id, String name, double amount,
                               String category, String date) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(16, 14, 16, 14);
        row.setBackgroundColor(Color.WHITE);

        LinearLayout.LayoutParams rowParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 0, 0, 12);
        row.setLayoutParams(rowParams);

        TextView nameView = new TextView(this);
        nameView.setText(name + "  •  ₹"
                + String.format(Locale.getDefault(), "%.2f", amount));
        nameView.setTextSize(17);
        nameView.setTextColor(Color.rgb(23, 37, 84));
        nameView.setGravity(Gravity.START);
        row.addView(nameView);

        TextView detailsView = new TextView(this);
        detailsView.setText(category + "  •  " + date
                + "\nTap this expense to delete it");
        detailsView.setTextSize(13);
        detailsView.setTextColor(Color.DKGRAY);
        row.addView(detailsView);

        row.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Delete Expense")
                        .setMessage("Delete \"" + name + "\"?")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Delete", (dialog, which) -> {
                            dbHelper.deleteExpense(id);
                            loadExpenses();
                            Toast.makeText(this,
                                    "Expense deleted",
                                    Toast.LENGTH_SHORT).show();
                        })
                        .show());

        expenseList.addView(row);
    }
}
