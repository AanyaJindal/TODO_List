package com.example.aanya.todolist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.aanya.todolist.db.TaskTable;

/**
 * Created by aanya on 7/13/2016.
 */
public class MyDbOpener extends SQLiteOpenHelper {

    public static final String DB_NAME = "mydatabase";
    public static final int DB_VER = 1;

    private static MyDbOpener myDbOpener = null;

    public static SQLiteDatabase openReadableDatabase(Context c){
        if(myDbOpener == null){
            myDbOpener = new MyDbOpener(c);
        }
        return myDbOpener.getReadableDatabase();
    }

    public static SQLiteDatabase openWritableDatabase(Context c){
        if(myDbOpener == null){
            myDbOpener = new MyDbOpener(c);
        }
        return myDbOpener.getWritableDatabase();
    }

    public MyDbOpener(Context context) {
        super(context, DB_NAME, null, DB_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(TaskTable.TABLE_CREATE_CMD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
