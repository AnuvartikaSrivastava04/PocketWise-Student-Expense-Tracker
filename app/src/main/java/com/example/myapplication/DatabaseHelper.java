
package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "pocketwise.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE expenses (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "amount REAL NOT NULL, " +
                        "category TEXT NOT NULL, " +
                        "date TEXT NOT NULL)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion, int newVersion) {
        // Add database migration code here in future versions.
    }

    public long addExpense(String name, double amount,
                           String category, String date) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("amount", amount);
        values.put("category", category);
        values.put("date", date);

        return getWritableDatabase().insert(
                "expenses", null, values
        );
    }

    public Cursor getExpenses() {
        return getReadableDatabase().rawQuery(
                "SELECT id, name, amount, category, date " +
                        "FROM expenses ORDER BY id DESC", null
        );
    }

    public double getTotal() {
        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT COALESCE(SUM(amount), 0) FROM expenses",
                null)) {
            return cursor.moveToFirst() ? cursor.getDouble(0) : 0;
        }
    }

    public int deleteExpense(long id) {
        return getWritableDatabase().delete(
                "expenses", "id=?",
                new String[]{String.valueOf(id)}
        );
    }
}

