package com.byted.camp.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.byted.camp.todolist.beans.Note;
import com.byted.camp.todolist.beans.State;
import com.byted.camp.todolist.db.TodoContract;
import com.byted.camp.todolist.db.TodoDbHelper;
import com.byted.camp.todolist.debug.DebugActivity;
import com.byted.camp.todolist.ui.NoteListAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD = 1002;

    private RecyclerView recyclerView;
    private NoteListAdapter notesAdapter;
    private TodoDbHelper mTodoDbHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTodoDbHelper = new TodoDbHelper(this);
        db = mTodoDbHelper.getWritableDatabase();
        //REFRESH
//        db.execSQL(TodoContract.SQL_DELETE_ENTRY);
//        db.execSQL(TodoContract.SQL_CREATE_LIST);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(MainActivity.this, NoteActivity.class),
                        REQUEST_CODE_ADD);
            }
        });

        recyclerView = findViewById(R.id.list_todo);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        notesAdapter = new NoteListAdapter(new NoteOperator() {
            @Override
            public void deleteNote(Note note) {
                MainActivity.this.deleteNote(note);
            }

            @Override
            public void updateNote(Note note) {
                MainActivity.this.updateNode(note);
            }
        });
        recyclerView.setAdapter(notesAdapter);

        notesAdapter.refresh(loadNotesFromDatabase());
    }

    @Override
    protected void onDestroy() {
        mTodoDbHelper.close();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_debug:
                startActivity(new Intent(this, DebugActivity.class));
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD
                && resultCode == Activity.RESULT_OK) {
            notesAdapter.refresh(loadNotesFromDatabase());
        }
    }

    private List<Note> loadNotesFromDatabase() {
        // TODO 从数据库中查询数据，并转换成 JavaBeans
        String OrderByDate  = TodoContract.TodoEntry.COLUMN_NAME_DATE +" DESC";
        String OrderByLevel = TodoContract.TodoEntry.COLUMN_NAME_LEVEL+" DESC";
        if(db==null) return Collections.emptyList();
        Cursor cursor = null;
        List Notes = null;
        try {
            cursor = db.query(
                    TodoContract.TodoEntry.TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    OrderByLevel+","+OrderByDate
            );
            Notes = new ArrayList<Note>();
            while(cursor.moveToNext()){
                Note mNote = new Note(cursor.getInt(cursor.getColumnIndexOrThrow(TodoContract.TodoEntry._ID)));
                mNote.setContent(cursor.getString(cursor.getColumnIndexOrThrow(TodoContract.TodoEntry.COLUMN_NAME_TEXT)));
                mNote.setState(State.from(cursor.getInt(cursor.getColumnIndexOrThrow(TodoContract.TodoEntry.COLUMN_NAME_DONE))));
                mNote.setPriority(cursor.getInt(cursor.getColumnIndexOrThrow(TodoContract.TodoEntry.COLUMN_NAME_LEVEL)));
                try{
                    String date = cursor.getString(cursor.getColumnIndexOrThrow(TodoContract.TodoEntry.COLUMN_NAME_DATE));
                    mNote.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).parse(date));
                }catch (Exception e){
                    e.printStackTrace();
                }
                Notes.add(mNote);
            }
        } finally {
            if(cursor!=null){
                cursor.close();
            }
        }

        return Notes;
    }

    private void deleteNote(Note note) {
        // TODO 删除数据
        String selection = TodoContract.TodoEntry._ID + " = ?";
        String[] selectionArgs = {""+note.id};
        db.delete(TodoContract.TodoEntry.TABLE_NAME,selection,selectionArgs);
        notesAdapter.refresh(loadNotesFromDatabase());
    }

    private void updateNode(Note note) {
        // 更新数据
        ContentValues values = new ContentValues();
        values.put(TodoContract.TodoEntry.COLUMN_NAME_DONE,note.getState().intValue);
        String selection = TodoContract.TodoEntry._ID + "= ?";
        String[] selectionArgs = {""+note.id};
        db.update(
                TodoContract.TodoEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );
        notesAdapter.refresh(loadNotesFromDatabase());
    }

}
