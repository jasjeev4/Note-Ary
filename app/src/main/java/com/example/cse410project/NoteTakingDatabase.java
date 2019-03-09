package com.example.cse410project;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by tristan on 1/30/18
 */

public class NoteTakingDatabase extends SQLiteOpenHelper {

    private String DATABASE_NAME = "notes";

    NoteTakingDatabase(Context context) {
        super(context, "NOTES_DATABASE", null, 3);
    }

    // This is where we need to write create table statements.
    // This is called when database is created.
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE notes(_id INTEGER PRIMARY KEY, noteImage TEXT, noteText TEXT, noteDescription TEXT, noteCategory TEXT)");
    }

    void storeNote(SQLiteDatabase db, String path, String title, String description, String category) {
        ContentValues values = new ContentValues();
        values.put("noteImage", path);
        values.put("noteText", title);
        values.put("noteDescription", description);
        values.put("noteCategory", category);

        db.insert(DATABASE_NAME, null, values);
    }

    void updateNote(SQLiteDatabase db, Integer id, String path, String text, String description, String category) {
        ContentValues values = new ContentValues();
        values.put("noteImage", path);
        values.put("noteText", text);
        values.put("noteDescription", description);
        values.put("noteCategory", category);

        db.update(DATABASE_NAME, values, "_id=?", new String[]{String.valueOf(id)});
    }

    void deleteNote(SQLiteDatabase db, String title) {
        this.getWritableDatabase().delete(DATABASE_NAME, "noteText=?", new String[]{title});
    }

    // This method will simply replace the database if it's out of date.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,
                          int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }
}