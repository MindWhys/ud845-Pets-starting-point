package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.pets.data.PetContract.PetsTable;



public class PetDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = PetDbHelper.class.getSimpleName();

    // If you change the database schema, you must increment the database version.

    public static final String DATABASE_NAME = "shelter.db";
    public static final int DATABASE_VERSION = 1;

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + PetsTable.TABLE_NAME;

    public PetDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {

        String SQL_CREATE_ENTRIES = "CREATE TABLE " + PetsTable.TABLE_NAME + " ("
                + PetsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PetsTable.COLUMN_PET_NAME + " TEXT NOT NULL, "
                + PetsTable.COLUMN_PET_BREED + " TEXT, "
                + PetsTable.COLUMN_PET_GENDER + " INTEGER NOT NULL, "
                + PetsTable.COLUMN_PET_WEIGHT + " INTEGER NOT NULL DEFAULT 0);";
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        // This command is for when you want to update the database to a new version,
        // e.g. containing a new column, after deleting the old one
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
