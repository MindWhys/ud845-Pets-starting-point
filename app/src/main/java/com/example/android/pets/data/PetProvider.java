package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.pets.R;

/**
 * {@link ContentProvider} for Pets app.
 */

public class PetProvider extends ContentProvider {

    /** URI matcher code for the content URI for the pets table */
    private static final int PETS = 100;

    /** URI matcher code for the content URI for a single pet in the pets table */
    private static final int PET_ID = 101;

    /**
     * UriMAtcher object to match a content URI to a corresponding code.
     * The input passed into the contructor represents the code to return for the root URI.
     * It;s common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer.  This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns tha the provider
        // should recognise.  All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY,PetContract.PATH_PETS + "/#", PET_ID);
    }

    /** Tag for the log messages */
    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    /** Database helper object */
    private PetDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new PetDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch(match) {
            case PETS:
                // For the PETS code, query the pets table directly with the given
                // projection, selection, selectionArgs and sort order.  The sursor
                // could contain multiple rows of the pets table.
                cursor = database.query(PetContract.PetsTable.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI.
                // For an example URI as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?".  Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = PetContract.PetsTable._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(PetContract.PetsTable.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI: " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was create for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        // Return the cursor
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch(match) {
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a pet into the database with the given content values.  Return the new content URI
     * fort hat specific row in the database.
     *
     */
    private Uri insertPet(Uri uri, ContentValues values) {

        // Check that the name is not null
        String name = values.getAsString(PetContract.PetsTable.COLUMN_PET_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Pet requires a name");
        }

        // Check that gender is worth 0, 1, or 2
        Integer gender = values.getAsInteger(PetContract.PetsTable.COLUMN_PET_GENDER);
        if (gender == null || !PetContract.PetsTable.isValidGender(gender)) {
            throw new IllegalArgumentException("Pet requires valid gender");
        }

        // If the weight is provided, check that it's greater than or equal to 0kg
        Integer weight = values.getAsInteger(PetContract.PetsTable.COLUMN_PET_WEIGHT);
        if (weight != null && weight < 0) {
            throw new IllegalArgumentException("Pet requires valid weight");
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new pet with the given values
        long id = database.insert(PetContract.PetsTable.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed.  Log and error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it.
        return ContentUris.withAppendedId(uri, id);

    }

    /**
     * Updates the data at the given selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update.  Selection will be the "_id=?" and
                // selection arguments will be a String array containing the actual ID.
                selection = PetContract.PetsTable._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri)) };
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update pets in the database with the given content values.  Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 0r more poets).
     * Return the number of rows that were successfully updated.
     */
    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {



        // If the {@link PetsTable#COLUMN_PET_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(PetContract.PetsTable.COLUMN_PET_NAME)) {
            String name = values.getAsString(PetContract.PetsTable.COLUMN_PET_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }

        // If the {@link PetsTable#COLUMN_PET_GENDER} key is present,
        // check that the gender value is valid.
        if (values.containsKey(PetContract.PetsTable.COLUMN_PET_GENDER)) {
            Integer gender = values.getAsInteger(PetContract.PetsTable.COLUMN_PET_GENDER);
            if (gender == null || !PetContract.PetsTable.isValidGender(gender)) {
                throw new IllegalArgumentException("Pet requires valid gender");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_WEIGHT} key is present,
        // check that the weight value is valid.
        if (values.containsKey(PetContract.PetsTable.COLUMN_PET_WEIGHT)) {
            // Check that the weight is greater than or equal to 0kg
            Integer weight = values.getAsInteger(PetContract.PetsTable.COLUMN_PET_WEIGHT);
            if (weight != null && weight < 0) {
                throw new IllegalArgumentException("Pet requires valid weight");
            }
        }

        // No need to check the breed, any value is valid (including null).

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get the writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

//        // Returns the number of database rows affected by the update statement
//        return database.update(PetContract.PetsTable.TABLE_NAME, values, selection, selectionArgs);

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(PetContract.PetsTable.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows are updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated !=0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                // Delete all rows that match the selection and selection args
//                return database.delete(PetContract.PetsTable.TABLE_NAME, selection, selectionArgs);
                rowsDeleted = database.delete(PetContract.PetsTable.TABLE_NAME, selection, selectionArgs);
                break;
            case PET_ID:
                // Delete a single row given by the ID in the URI
                    selection = PetContract.PetsTable._ID + "=?";
                    selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri))};
//                    return database.delete(PetContract.PetsTable.TABLE_NAME, selection, selectionArgs);
                rowsDeleted = database.delete(PetContract.PetsTable.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
    final int match = sUriMatcher.match(uri);
    switch (match) {
        case PETS:
            return PetContract.PetsTable.CONTENT_LIST_TYPE;
        case PET_ID:
            return PetContract.PetsTable.CONTENT_ITEM_TYPE;
        default:
            throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
    }
    }

}
