package com.example.cse410project;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

    Cursor todoCursor;
    ListView noteList;
    NoteAdapter adapter;
    SwipeRefreshLayout swipeView;
    HashSet<Integer> deleteList = new HashSet<>();
    String viewingCategory = "";
    public static ArrayList<String> categories = new ArrayList<String>() {};
    BottomNavigationView bottomNavigationView;



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

            loadCategories();
        }  else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    0);
        }
        //Adapted from source: https://www.survivingwithandroid.com/2014/05/android-swiperefreshlayout-tutorial-2.html
        swipeView = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeView.setEnabled(false);
        swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeView.setRefreshing(true);
                ( new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeView.setRefreshing(false);
                    }
                }, 3000);
            }
        });
        noteList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0) {
                    swipeView.setEnabled(true);
                }
                else {
                    swipeView.setEnabled(false);
                }
            }
        });

        bottomNavigationView =  (BottomNavigationView) findViewById(R.id.navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.navigation_title:
                        sortbytitle();
                        return true;
                    case R.id.navigation_category:
                        sortbycategory();
                        return true;
                    case R.id.navigation_date:
                        sortbydate();
                        return true;
                    default:
                        return true;
                }
            }
        });

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

    @Override
    public void onResume(){
        super.onResume();
        // put your code here...
        loadNotesFromDatabase();
    }

    public void loadNotesFromDatabase() {
        // Create a new instance of the NoteTakingDatabase
        NoteTakingDatabase handler = new NoteTakingDatabase(getApplicationContext());
        // Get the writable database
        SQLiteDatabase db = handler.getWritableDatabase();
        //Get all notes from the database
        todoCursor = db.rawQuery("SELECT * FROM notes", null);

        TextView empty;
        empty = (TextView) findViewById(R.id.empty_list);

        if(todoCursor!=null && todoCursor.getCount()==0) {
            empty.setVisibility(View.VISIBLE);
        }
        else {
            empty.setVisibility(View.GONE);
        }

        // Create an instance of the NoteAdapter with our cursor
        adapter = new NoteAdapter(this, todoCursor, 0);

        // Set the NoteAdapter to the ListView (display all notes from DB)
        noteList.setAdapter(adapter);
        viewingCategory = "";
    }

    public  void loadNotesFromCategory(String category) {
        NoteTakingDatabase handler = new NoteTakingDatabase(getApplicationContext());

        SQLiteDatabase db = handler.getWritableDatabase();
        todoCursor = db.rawQuery("SELECT * FROM notes WHERE noteCategory='"+category+"'",  null);

        adapter = new NoteAdapter(this, todoCursor, 0);

        noteList.setAdapter(adapter);
        viewingCategory = category;
    }

    public void loadCategories() {
        CategoryDatabase handler = new CategoryDatabase(getApplicationContext());

        SQLiteDatabase db = handler.getWritableDatabase();
        todoCursor = db.rawQuery("SELECT * FROM categories", null);
        categories.clear();
        if  (todoCursor.getCount() != 0) {
            while (todoCursor.moveToNext()) {
                Log.d("CATS:", todoCursor.getString(1));
                categories.add(todoCursor.getString(1));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_by_category:
                sortbycategory();
                return true;
            case R.id.sort_by_title:
                sortbytitle();
                return true;
            case R.id.sort_by_date:
                sortbydate();
                return true;
            case R.id.add_note:
                loadCategories();
                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                startActivity(intent);
                return true;
            case R.id.delete:
                deleteItems();
                if (viewingCategory.equals(""))  {
                    loadNotesFromDatabase();
                } else {
                    loadNotesFromCategory(viewingCategory);
                }
                return true;
            case R.id.categories:
                categoryDialog();
                return true;
            case R.id.add_category:
                addCategoryDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addCategoryDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Add Category");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        b.setView(input);
        b.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!categories.contains(input.getText().toString())) {
                    storeCategory(input.getText().toString());
                    categories.add(input.getText().toString());
                }
            }
        });
        b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        b.show();
    }

    private void storeCategory(String category) {
        // Create a new instance of the NoteTakingDatabase
        CategoryDatabase handler = new CategoryDatabase(getApplicationContext());
        // Get the writable database
        SQLiteDatabase db = handler.getWritableDatabase();
        // Store the note in the database
        handler.storeCategory(db, category);
    }

    private void categoryDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Choose Category");
        String[] allCategories = new String[this.categories.size() + 1];
        for (int i = 0; i < this.categories.size(); ++i){
            allCategories[i] = this.categories.get(i);
        }
        allCategories[allCategories.length-1] = "All Notes";
        b.setItems(allCategories, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (which == MainActivity.categories.size()) {
                    dialog.dismiss();
                    loadNotesFromDatabase();
                } else {
                    dialog.dismiss();
                    loadNotesFromCategory(MainActivity.categories.get(which));
                }

            }

        });

        b.show();
    }

    public void sortbytitle(){
        // Create a new instance of the NoteTakingDatabase
        NoteTakingDatabase handler = new NoteTakingDatabase(getApplicationContext());
        // Get the writable database
        SQLiteDatabase db = handler.getWritableDatabase();
        //Get all notes from the database
        todoCursor = db.rawQuery("SELECT * FROM notes ORDER BY noteText", null);
        TextView empty;
        empty = (TextView) findViewById(R.id.empty_list);

        if(todoCursor!=null && todoCursor.getCount()==0) {
            empty.setVisibility(View.VISIBLE);
        }
        else {
            empty.setVisibility(View.GONE);
        }

        // Create an instance of the NoteAdapter with our cursor
        adapter = new NoteAdapter(this, todoCursor, 0);

        // Set the NoteAdapter to the ListView (display all notes from DB)
        noteList.setAdapter(adapter);
        viewingCategory = "";
    }

    public void sortbydate(){
        // Create a new instance of the NoteTakingDatabase
        NoteTakingDatabase handler = new NoteTakingDatabase(getApplicationContext());
        // Get the writable database
        SQLiteDatabase db = handler.getWritableDatabase();
        //Get all notes from the database
        todoCursor = db.rawQuery("SELECT * FROM notes ORDER BY noteDate DESC", null);
        TextView empty;
        empty = (TextView) findViewById(R.id.empty_list);

        if(todoCursor!=null && todoCursor.getCount()==0) {
            empty.setVisibility(View.VISIBLE);
        }
        else {
            empty.setVisibility(View.GONE);
        }

        // Create an instance of the NoteAdapter with our cursor
        adapter = new NoteAdapter(this, todoCursor, 0);

        // Set the NoteAdapter to the ListView (display all notes from DB)
        noteList.setAdapter(adapter);
        viewingCategory = "";
    }
    public void sortbycategory(){
        // Create a new instance of the NoteTakingDatabase
        NoteTakingDatabase handler = new NoteTakingDatabase(getApplicationContext());
        // Get the writable database
        SQLiteDatabase db = handler.getWritableDatabase();
        //Get all notes from the database
        todoCursor = db.rawQuery("SELECT * FROM notes ORDER BY noteCategory", null);
        TextView empty;
        empty = (TextView) findViewById(R.id.empty_list);

        if(todoCursor!=null && todoCursor.getCount()==0) {
            empty.setVisibility(View.VISIBLE);
        }
        else {
            empty.setVisibility(View.GONE);
        }

        // Create an instance of the NoteAdapter with our cursor
        adapter = new NoteAdapter(this, todoCursor, 0);

        // Set the NoteAdapter to the ListView (display all notes from DB)
        noteList.setAdapter(adapter);
        viewingCategory = "";
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
