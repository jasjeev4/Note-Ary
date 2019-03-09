package com.example.cse410project;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

    Cursor todoCursor;
    ListView noteList;
    NoteAdapter adapter;
    HashSet<Integer> deleteList = new HashSet<>();
    public static String[] categories = { "General", "To Do" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        noteList = (ListView) findViewById(R.id.note_list);
        noteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("ID", "ID HERERR"+id);
                Intent openNote = new Intent(MainActivity.this, NoteActivity.class);
                openNote.putExtra("noteId", id);
                startActivity(openNote);
            }
        });
        if(userHasPermission()) {
            loadNotesFromDatabase();
        }  else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    0);
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        super.dispatchTouchEvent(event);
//        if (event.getAction() != MotionEvent.ACTION_UP && event.getAction() != MotionEvent.ACTION_DOWN) {
//            return true;
//        }
//        super.onTouchEvent(event);
//        Log.d("F","PISSS");
//        boolean someChecked = false;
//        if (noteList != null) {
//            for (int i = 0; i < noteList.getCount(); ++i) {
//                View v = noteList.getChildAt(i);
//                CheckBox cb = (CheckBox) v.findViewById(R.id.checkbox);
//                if (cb.isChecked()) {
//                    someChecked = true;
//                    deleteList.add(i);
//                } else {
//                    deleteList.remove(i);
//                }
//            }
//        }
//        if (someChecked) {
//            findViewById(R.id.delete).setVisibility(View.VISIBLE);
//        } else {
//            findViewById(R.id.delete).setVisibility(View.INVISIBLE);
//        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    loadNotesFromDatabase();
                } else {
                    // TODO tell the user we need permission for our app to work
                }
                break;
        }
    }


    public boolean userHasPermission() {
        return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close database cursor
        if (todoCursor != null) {
            todoCursor.close();
        }
    }

    public void loadNotesFromDatabase() {
        // Create a new instance of the NoteTakingDatabase
        NoteTakingDatabase handler = new NoteTakingDatabase(getApplicationContext());
        // Get the writable database
        SQLiteDatabase db = handler.getWritableDatabase();
        //Get all notes from the database
        todoCursor = db.rawQuery("SELECT * FROM notes", null);

        // Create an instance of the NoteAdapter with our cursor
        adapter = new NoteAdapter(this, todoCursor, 0);

        // Set the NoteAdapter to the ListView (display all notes from DB)
        noteList.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_note:
                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                startActivity(intent);
                return true;
            case R.id.delete:
                deleteItems();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteItems() {
        NoteTakingDatabase handler = new NoteTakingDatabase(getApplicationContext());
        SQLiteDatabase db = handler.getWritableDatabase();
        if (noteList != null) {
            for (int i = 0; i < noteList.getCount(); ++i) {
                View v = noteList.getChildAt(i);
                CheckBox cb = (CheckBox) v.findViewById(R.id.checkbox);
                if (cb.isChecked()) {
                    TextView tv = (TextView) v.findViewById(R.id.noteText);
                    handler.deleteNote(db, tv.getText().toString());
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
