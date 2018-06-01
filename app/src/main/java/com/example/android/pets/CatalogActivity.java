/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.ListView;

import com.example.android.pets.data.PetContract;
import com.example.android.pets.data.PetContract.PetsTable;

//Old code
//import com.example.android.pets.data.PetDbHelper;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    // Old code
//    // Database helper that will provide us access to the database
//    private PetDbHelper mDbHelper;

    private static final int PET_LOADER = 0;

    PetCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the pet data
        ListView petListView = findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);

        // Old code
//        // To access our database, we instantiate our subclass of SQLiteOpenHelper
//        // and pass the context, which is the current activity.
//        mDbHelper = new PetDbHelper(this);
//
//        displayDatabaseInfo();

        // Setup an Adapter to create a list item for each row of pet data in the Cursor.
        // There is no pet data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new PetCursorAdapter(this, null);
        petListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific pet that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link PetEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.pets/pets/2"
                // if the pet with ID 2 was clicked on.
                Uri currentPetUri = ContentUris.withAppendedId(PetsTable.CONTENT_URI, id);

                // Set rhe URI on the data field of the intent
                intent.setData(currentPetUri);

                // Launch the {@link EditorActivity} to display the data for the current pet.
                startActivity(intent);

            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(PET_LOADER, null, this);
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        displayDatabaseInfo();
//    }

        /**
         * Temporary helper method to display information in the onscreen TextView about the state of
         * the pets database.
         */
//        private void displayDatabaseInfo() {
//
////            // Create and/or open a database to read from it
////            SQLiteDatabase db = mDbHelper.getReadableDatabase();
//
//            String[] projection = {
//                    PetsTable._ID,
//                    PetsTable.COLUMN_PET_NAME,
//                    PetsTable.COLUMN_PET_BREED,
//                    PetsTable.COLUMN_PET_GENDER,
//                    PetsTable.COLUMN_PET_WEIGHT };
//
////            Cursor cursor = db.query(
////                    PetsTable.TABLE_NAME,    // The table to query
////                    projection,              // The columns to return
////                    null,            // The columns for the WHERE clause
////                    null,         // The values for the WHERE clause
////                    null,            // Don't group the rows
////                    null,             // Don't filter by row groups
////                    null);           // The sort order
//
//            // Perform a query on the provider using the ContentResolver.
//            // Use the {@link PetsTable.CONTENT_URI} to access the pet data.
//            Cursor cursor = getContentResolver().query(
//                    PetsTable.CONTENT_URI,      // The content URI of the words table
//                    projection,                 // The columns to return for each row
//                    null,               // Selection criteria
//                    null,            // Selection criteria
//                    null);             // The sort order for the returned rows
//
////            TextView displayView = findViewById(R.id.text_view_pet);
//            // Find the ListView which will be populates with the pet data
//            ListView petListView = findViewById(R.id.list);
//
////            try {
////                // Create a header in the Text View that looks like this:
////                //
////                // The pets table contains <number of rows in Cursor> pets.
////                // _id - name - breed - gender - weight
////                //
////                // In the while loop below, iterate through the rows of the cursor and display
////                // the information from each column in this order.
////                displayView.setText("The pets table contains " + cursor.getCount() + " pets.\n\n");
////                displayView.append(PetsTable._ID + " - "
////                                    + PetsTable.COLUMN_PET_NAME + " - "
////                                    + PetsTable.COLUMN_PET_BREED + " - "
////                                    + PetsTable.COLUMN_PET_GENDER + " - "
////                                    + PetsTable.COLUMN_PET_WEIGHT + " - " + "\n");
//
//            // Setup an Adapter to create a list item for each row of pet data in the Cursor.
//            PetCursorAdapter adapter = new PetCursorAdapter(this, cursor);
//
////                // Figure out the index of each column
////                int idColumnIndex = cursor.getColumnIndex(PetsTable._ID);
////                int nameColumnIndex = cursor.getColumnIndex(PetsTable.COLUMN_PET_NAME);
////                int breedColumnIndex = cursor.getColumnIndex(PetsTable.COLUMN_PET_BREED);
////                int genderColumnIndex = cursor.getColumnIndex(PetsTable.COLUMN_PET_GENDER);
////                int weightColumnIndex = cursor.getColumnIndex(PetsTable.COLUMN_PET_WEIGHT);
////
////                // Iterate through all the returned rows in the cursor
////                while (cursor.moveToNext()) {
////                    // Use that index to extract the String or Int value of the word
////                    // at the current row on the cursor is on.
////                    int currentID = cursor.getInt(idColumnIndex);
////                    String currentName = cursor.getString(nameColumnIndex);
////                    String currentBreed = cursor.getString(breedColumnIndex);
////                    int currentGender = cursor.getInt(genderColumnIndex);
////                    int currentWeight = cursor.getInt(weightColumnIndex);
////                    // Display the values from each column of the current row in the cursor in the TextView
////                    displayView.append(("\n" + currentID + " - "
////                                            + currentName + " - "
////                                            + currentBreed + " - "
////                                            + currentGender + " - "
////                                            + currentWeight));
////                }
////                // Old code - Display the number of rows in the Cursor (which reflects the number of rows in the
////                // pets table in the database).
//////                TextView displayView = (TextView) findViewById(R.id.text_view_pet);
//////                displayView.setText("Number of rows in pets database table: " + cursor.getCount());
////            } finally {
////                // Always close the cursor when you're done reading from it. This releases all its
////                // resources and makes it invalid.
////                cursor.close();
////            }
//            petListView.setAdapter(adapter);
//        }

    /**
     * Helper method to insert hardcoded pet data into the database.  For debugging purposes only.
     */
    private void insertPet() {
        //Old Code
//            // Gets the data repository in write mode
//            SQLiteDatabase db = mDbHelper.getWritableDatabase();
//            // Create a new map of values, where column names are the keys

        // Create a ContentValues object where column names are the keys,
        // and Toto's pet attributes are the values.
            ContentValues values = new ContentValues();
            values.put(PetsTable.COLUMN_PET_NAME, "Toto");
            values.put(PetsTable.COLUMN_PET_BREED, "Terrier");
            values.put(PetsTable.COLUMN_PET_GENDER, PetsTable.GENDER_MALE);
            values.put(PetsTable.COLUMN_PET_WEIGHT, 7);

            // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the {@link PetEntry#CONTENT_URI} to indicate that we want to insert
        // into the pets database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.
        Uri newUri = getContentResolver().insert(PetsTable.CONTENT_URI, values);

            //Old Code
//            // Insert the new row, returning the primary key value of the new row
//            long newRowId = db.insert(PetsTable.TABLE_NAME,null, values);
//            Log.v("CatalogActivity", "New row ID " + newRowId);
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertPet();
//                displayDatabaseInfo();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies the columns form the table we care about.
        String[] projection = {
                PetsTable._ID,
                PetsTable.COLUMN_PET_NAME,
                PetsTable.COLUMN_PET_BREED };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,     // Parent activity context
                PetsTable.CONTENT_URI,           // Provider content URI to query
                projection,                      // Columns to include in the resulting Cursor
                null,                    // No selection clause
                null,                 // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link PetCursorAdapter} with this new cursor containing updated pet data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
