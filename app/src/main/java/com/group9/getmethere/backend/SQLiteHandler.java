package com.group9.getmethere.backend;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import java.util.HashMap;

/**
 * Created by JMorris on 23/03/15.
 */
public class SQLiteHandler extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "c1331799";
    private static final String TABLE_LOGIN = "login";
    private static final String KEY_EMAIL = "email";

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //create SQLite table
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_LOGIN + "("
                + KEY_EMAIL + " TEXT UNIQUE" + ")";

        db.execSQL(CREATE_LOGIN_TABLE);
    }

    //upgrade table
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Drop older table if it existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN);
        //Create tables
        onCreate(db);
    }

    public void addUser(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_EMAIL, email);


        db.insert(TABLE_LOGIN, null, values);   //Insert row into table
        db.close(); //Close database connection
    }

    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String query = "SELECT  * FROM " + TABLE_LOGIN;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToFirst();
        if (cursor.getCount() > 0) {

            user.put("email", cursor.getString(1));

        }
        cursor.close();
        db.close();


        return user;
    }

   /* public int getRowCount() {
        String query = "SELECT  * FROM " + TABLE_LOGIN;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        int rowCount = cursor.getCount();
        db.close();
        cursor.close();

        return rowCount;
    }*/

    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        //Delete all rows
        db.delete(TABLE_LOGIN, null, null);
        db.close();
    }
}
