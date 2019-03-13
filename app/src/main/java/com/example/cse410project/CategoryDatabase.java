package com.example.cse410project;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CategoryDatabase extends SQLiteOpenHelper {

    private String DATABASE_NAME = "categories";
    CategoryDatabase(Context context) {
        super(context, "CATEGORIES_DATABASE", null, 3);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE categories(_id INTEGER PRIMARY KEY, category TEXT)");
        storeCategory(db, "General");
        storeCategory(db, "To Do");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,
                          int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    public void storeCategory(SQLiteDatabase db, String category) {
        ContentValues values = new ContentValues();
        values.put("category", category);
        db.insert(DATABASE_NAME, null, values);
    }
}
