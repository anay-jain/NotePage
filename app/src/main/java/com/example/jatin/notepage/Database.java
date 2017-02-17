package com.example.jatin.notepage;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jatin on 08/02/17.
 */

public class Database extends SQLiteOpenHelper{

    static final private String DATABASE_NAME = "notes.db";
    static final private int DATABASE_VERSION = 1;
    private static SQLiteDatabase database;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        init(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //TODO
    }

    public class DatabaseException extends Exception{

        String message = "Unknown Exception";

        DatabaseException(String message){
            this.message = message;
        }

        @Override
        public String toString(){
            return message;
        }
    }

    //initializing the database
    //register tables here
    public void init(SQLiteDatabase database){
        String sql;

        //creating Titles table
        /*
            id -> integer primary key
            title -> string
        */
        sql = "CREATE TABLE IF NOT EXISTS Titles (id integer primary key autoincrement, title varchar(25));";
        database.execSQL(sql);

        //creating Notes table
        /*
            id -> integer primary key
            title_id -> integer foreign key
            message -> string
        */
        sql = "CREATE TABLE IF NOT EXISTS Notes (id integer primary key autoincrement, title_id int, message text, completed int default 0);";
        database.execSQL(sql);

    }

    //method to add new title
    public void addTitle(String name) {
        SQLiteDatabase database = this.getWritableDatabase();

        //encoding incoming string for potential sql injections
        try {
            name = new String (name.getBytes(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //building query
        String sql = "INSERT INTO Titles(title) VALUES ('" + name.trim() + "');";
        //executing query
        database.execSQL(sql);

    }

    //method to add new note
    public void addNote(String name, int  title_id) throws DatabaseException{
        SQLiteDatabase database = this.getWritableDatabase();

        String sql;

        //encoding incoming string for potential sql injections
        try {
            name = new String (name.getBytes(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //checking if title_id is correct or not
        sql = "SELECT COUNT(*) FROM Titles WHERE id = " + title_id;
        Cursor resultSet = database.rawQuery(sql, null);
        resultSet.moveToFirst();
        if (resultSet.getInt(0) == 0) throw new DatabaseException("title_id can't be found !");
        else {
            sql = "INSERT INTO Notes(title_id, message) VALUES (" + title_id + ", '" + name + "');";
            database.execSQL(sql);
        }
    }

    //method to get all the titles with truncated latest message
    public List<Title> getTitles(){
        SQLiteDatabase database = this.getReadableDatabase();
        List<Title> list = new ArrayList<>();

        String sql = "select Titles.id, title, max(Notes.id), message From Titles Left Join Notes on Titles.id = Notes.title_id group by Titles.id order by Titles.id;";
        Cursor resultSet = database.rawQuery(sql, null);


        while(resultSet.moveToNext()){
            list.add(new Title(resultSet.getString(1), resultSet.getInt(0), resultSet.getString(3)));
            Log.e("jatin", "added " + resultSet.getString(1));
        }

        return list;
    }

    //method to get the titles based on query text for title name
    public List<Title> getTitles(String query){
        SQLiteDatabase database = this.getReadableDatabase();
        List<Title> list = new ArrayList<>();

        String sql = "select Titles.id, title, max(Notes.id), message From Titles Left Join Notes on Titles.id = Notes.title_id group by Titles.id having title like ? || '%' order by Titles.id;";
        Cursor resultSet = database.rawQuery(sql, new String[] {query});

        while (resultSet.moveToNext()){
            list.add(new Title(resultSet.getString(1), resultSet.getInt(0), resultSet.getString(3)));
        }

        return list;
    }

    //method to get single title
    public Title getTitle(int id){
        Title title;

        SQLiteDatabase database = this.getReadableDatabase();
        String sql  = "select Titles.id, title, message from Titles Left Join Notes on Titles.id = Notes.title_id group by Titles.id having Titles.id = " + id + " order by Titles.id";
        Cursor resultSet = database.rawQuery(sql, null);
        resultSet.moveToFirst();
        title = new Title(resultSet.getString(1), resultSet.getInt(0), resultSet.getString(2));
        return title;
    }

    //method to chenge title
    public void editTitle(int id, String title){
        SQLiteDatabase database = this.getWritableDatabase();
        String sql = "update Titles set title = ? where id = " + id;
        database.rawQuery(sql, new String[]{title});
    }

    //method to get all the notes based on title id
    public List<Note> getNotes(int title_id){
        SQLiteDatabase database = this.getReadableDatabase();
        List<Note> list = new ArrayList<>();

        String sql = "SELECT id, message, completed FROM Notes WHERE title_id = " + title_id;
        Cursor resultSet = database.rawQuery(sql, null);

        while (resultSet.moveToNext()){
            list.add(new Note(resultSet.getString(1), resultSet.getInt(0), resultSet.getInt(2)));
            Log.e("jatin", "added note" + resultSet.getString(1));
        }

        return list;
    }

    //method to get completed notes
    public List<Note> getUnCompletedNotes(int title_id){
        SQLiteDatabase database = this.getReadableDatabase();
        List<Note> list = new ArrayList<>();

        String sql = "SELECT id, message, completed FROM Notes WHERE completed=0 and title_id = " + title_id;
        Cursor resultSet = database.rawQuery(sql, null);

        while (resultSet.moveToNext()){
            list.add(new Note(resultSet.getString(1), resultSet.getInt(0), resultSet.getInt(2)));
            Log.e("jatin", "added note" + resultSet.getString(1));
        }

        return list;
    }

    //method to get a single not
    public Note getNote(int id){
        Note note;

        SQLiteDatabase database = this.getReadableDatabase();
        String sql = "select message, id, completed from Notes where id = " + id;
        Cursor resultSet = database.rawQuery(sql, null);
        resultSet.moveToFirst();
        note = new Note(resultSet.getString(0),resultSet.getInt(1), resultSet.getInt(2));

        return note;
    }

    public List<Note> getNote(String query, int title_id){
        SQLiteDatabase database = this.getReadableDatabase();
        List<Note> list = new ArrayList<>();

        String sql = "select message, id, completed from Notes where title_id = ? and message like ? || '%';";
        Cursor resultSet = database.rawQuery(sql, new String[] {""+title_id, query});

        while (resultSet.moveToNext()){
            list.add(new Note(resultSet.getString(0), resultSet.getInt(1), resultSet.getInt(2)));
        }

        return list;
    }

    public void setNoteComplete(int id, boolean complete){
        SQLiteDatabase database = this.getWritableDatabase();
        String sql = "Update Notes set completed = " + (complete?1:0) + " where id = " + id;
        database.execSQL(sql);
        Log.e("jatin", "changed to "+complete);
    }

    public boolean delTitle(int id) {

        SQLiteDatabase database = this.getWritableDatabase();
        String sql;
        try{
            sql = "DELETE FROM Titles WHERE id = " + id;
            database.execSQL(sql);
            sql = "DELETE FROM Notes WHERE title_id = " + id;
            database.execSQL(sql);
            return true;
        } catch (Exception e) {
            return false;

        }
    }


    public boolean delNote(int id){
        SQLiteDatabase database = this.getWritableDatabase();
        String sql;
        try{
            sql = "DELETE FROM Notes WHERE id = " + id;
            database.execSQL(sql);
            return true;
        } catch (Exception e){
            return false;
        }
    }

}
