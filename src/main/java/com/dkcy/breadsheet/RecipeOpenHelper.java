package com.dkcy.breadsheet;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.*;

/**
 * Created by danielyeo on 27/09/2016.
 */

// Constructor, calls superclass constructor
public class RecipeOpenHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "/assets/breadsheet.db";

    RecipeOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    public void openDatabase() {
        super.getReadableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // All Static variables

    // Contacts table name
    private static final String TABLE_RECIPES = "recipes";
    private static final String TABLE_INGREDIENTS = "ingredients";
    private static final String TABLE_BREADS = "breads";

    // Recipes Table Columns names
    private static final String KEY_BREADID = "bread_id";
    private static final String KEY_INGREDIENTID = "ingredient_id";
    private static final String KEY_PROPORTION = "proportion";

    private static final String KEY_INGREDIENTS_ID = "_id";
    private static final String KEY_INGREDIENTS_NAME = "name";
    private static final String KEY_BREADS_ID = "_id";
    private static final String KEY_BREADS_NAME = "name";

    // Method to get recipe - returns string array of ingreidents and absolute amounts
    public String[][] getRecipe(int id) {
        SQLiteDatabase db = super.getReadableDatabase();
        String[][] ingredients = {{"", ""}};
        String name = "";

        // Returns a Cursor interface
        String query = "SELECT " + KEY_BREADS_NAME + " FROM " + TABLE_BREADS + " WHERE " + KEY_BREADS_ID + " = " + id + ";";
        Cursor cursor = db.rawQuery(query, null);

        // Retrieves the name
        if (cursor != null) {
            cursor.moveToFirst();
            name = cursor.getString(0);
        }
        cursor.close();

        query = "SELECT " + TABLE_INGREDIENTS + "." + KEY_INGREDIENTS_NAME + ", " + TABLE_RECIPES + "." + KEY_PROPORTION +
                " FROM " + TABLE_RECIPES +
                " JOIN " + TABLE_BREADS +
                " ON " + TABLE_RECIPES + "." + KEY_INGREDIENTID + " = " + TABLE_INGREDIENTS + "." + KEY_INGREDIENTS_ID +
                " WHERE " + TABLE_RECIPES + "." + KEY_BREADID + " = " + id + ";";
        cursor = db.rawQuery(query, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int i = 0;
            while (cursor.isAfterLast() != false) {
                ingredients[i][0] = cursor.getString(0);
                ingredients[i][1] = cursor.getString(1);
                i++;
                cursor.moveToNext();
            }
        }
        cursor.close();

        return ingredients;
    }
// Close database
// Get recipe
}