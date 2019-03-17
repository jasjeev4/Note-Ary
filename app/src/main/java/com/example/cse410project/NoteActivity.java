package com.example.cse410project;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
//import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import java.util.Date;

public class NoteActivity extends AppCompatActivity {

    String imagePath = "";
    Button saveNote;
    TextView noteTitle;
    TextView noteContents;
    ImageView noteImage;
    Spinner categoriesSpinner;
    TextView noteDate;
    SQLiteDatabase db;

    boolean isUpdate = false;
    int noteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        // Create a new instance of the NoteTakingDatabase
        NoteTakingDatabase handler = new NoteTakingDatabase(getApplicationContext());
        // Get the writable database
        db = handler.getReadableDatabase();
        initializeSpinner();
        saveNote = (Button) findViewById(R.id.create_note);
        noteTitle = (TextView) findViewById(R.id.note_title);
        noteImage = (ImageView) findViewById(R.id.note_image);
        noteContents = (TextView) findViewById(R.id.note_contents);
        noteDate = (TextView) findViewById(R.id.note_date);

        Date mydate = new Date();
        String thedate = mydate.toString();
        noteDate.setText(thedate);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isUpdate = true;
            noteId = (int) extras.getLong("noteId");
            setNote(noteId);
        }

        saveNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //handle a blank note
                if(noteTitle.getText().toString().equals("") || noteContents.getText().toString().equals("")) {
                    //send a message
                    View constraintView = findViewById(R.id.constraintLayout);

                    //Snackbar snackbar = Snackbar.make(constraintView,"Note cannot be blank!",Snackbar.LENGTH_SHORT);

                    Context context = getApplicationContext();
                    CharSequence text = "Note cannot be blank!";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
                else {
                    if (!isUpdate) {
                        storeNote(imagePath, noteTitle.getText().toString(), noteContents.getText().toString(), categoriesSpinner.getSelectedItem().toString(),noteDate.getText().toString());

                        Log.d("dateeee",(noteDate.getText().toString()).getClass().getName());
                    } else {
                        updateNote(noteId, imagePath, noteTitle.getText().toString(), noteContents.getText().toString(), categoriesSpinner.getSelectedItem().toString());
                    }
                    finish();
                }
            }
        });
        noteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Use the EasyImage library to open up a chooser to pick an image.
                EasyImage.openChooserWithGallery(NoteActivity.this, "Upload an Image", 0);
            }
        });
    }

    private void initializeSpinner() {
        categoriesSpinner = (Spinner) findViewById(R.id.category_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, MainActivity.categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoriesSpinner.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                // TODO error stuff
            }

            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                imagePath = imageFile.getAbsolutePath();
                Bitmap imageBitmap = BitmapFactory.decodeFile(imagePath);
                noteImage.setImageBitmap(imageBitmap);
            }
        });
    }

    private void updateNote(int noteId, String imagePath, String title, String description, String category) {
        // Create a new instance of the NoteTakingDatabase
        NoteTakingDatabase handler = new NoteTakingDatabase(getApplicationContext());
        // Get the writable database
        SQLiteDatabase db = handler.getWritableDatabase();
        // Store the note in the database
        handler.updateNote(db, noteId, imagePath, title, description, category);
        db.close();
    }

    private void setNote(Integer noteId) {
        // Get note by id
        Cursor cursor = db.rawQuery("SELECT * FROM notes WHERE _id = " + noteId, null);
        cursor.moveToFirst();

        // Set note details to view
        String path = cursor.getString(cursor.getColumnIndexOrThrow("noteImage"));
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        noteImage.setImageBitmap(bitmap);

        // Get the note text from the database as a String
        String noteText = cursor.getString(cursor.getColumnIndexOrThrow("noteText"));
        noteTitle.setText(noteText);
        Log.d("fff","yy");
        String contents = cursor.getString(cursor.getColumnIndexOrThrow("noteDescription"));
        noteContents.setText(contents);

//        String noteDescription = cursor.getString(cursor.getColumnIndexOrThrow("noteDescription"));
        String noteCategory = cursor.getString(cursor.getColumnIndexOrThrow("noteCategory"));

        String noteDatee = cursor.getString(cursor.getColumnIndexOrThrow("noteDate"));
        noteDate.setText(noteDatee);


        cursor.close();
    }

    public void storeNote(String path, String title, String description, String category, String date) {
        // Create a new instance of the NoteTakingDatabase
        NoteTakingDatabase handler = new NoteTakingDatabase(getApplicationContext());
        // Get the writable database
        SQLiteDatabase db = handler.getWritableDatabase();
        // Store the note in the database
        handler.storeNote(db, path, title, description, category, date);
        db.close();
    }
}
