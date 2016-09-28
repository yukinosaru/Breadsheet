package com.dkcy.breadsheet;

import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.*;
import java.io.*;
import java.util.ArrayList;

import android.content.Context;

/**
 * Copied from tutorial on accessing own SQLLite database
 * http://blog.reigndesign.com/blog/using-your-own-sqlite-database-in-android-applications/
 */

public class DataBaseHelper extends SQLiteOpenHelper {

    //The Android's default system path of your application database.

    private SQLiteDatabase myDataBase;

    private final Context myContext;

    private static String DB_PATH = "/data/data/com.dkcy.breadsheet/databases/";

    private static String DB_NAME = "breadsheet";


    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context
     */
    public DataBaseHelper(Context context) {

        super(context, DB_NAME, null, 1);
        this.myContext = context;
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() throws IOException{

        boolean dbExist = checkDataBase();

        if(dbExist){
            //do nothing - database already exist
        }else{

            //By calling this method an empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();

            try {

                copyDataBase();

            } catch (IOException e) {

                throw new Error("Error copying database");

            }
        }

    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){

        SQLiteDatabase checkDB = null;

        try{
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        }catch(SQLiteException e){

            //database does't exist yet.

        }

        if(checkDB != null){

            checkDB.close();

        }

        return checkDB != null ? true : false;
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException{

        //Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    public void openDataBase() throws SQLiteException{

        //Open the database
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

    }

    @Override
    public synchronized void close() {

        if(myDataBase != null)
            myDataBase.close();

        super.close();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // My own public helper methods to access and get content from the database.
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

    // Get dough names (for use in selector)
    public String[] getDoughNames() {
        ArrayList<String> doughNamesList = new ArrayList<String>();
        if(myDataBase.isOpen()){
            String query = "SELECT " + KEY_BREADS_NAME + " FROM " + TABLE_BREADS + ";";
            Cursor cursor = myDataBase.rawQuery(query,null);
            if (cursor!=null) {
                cursor.moveToFirst();
                while(cursor.isAfterLast() == false){
                    // get string and put into doughNames array;
                    String name = cursor.getString(0);
                    doughNamesList.add(name);
                    cursor.moveToNext();
                }
                cursor.close();
            }
        }
        String[] doughNames = doughNamesList.toArray(new String[doughNamesList.size()]);
        return doughNames;
    }

    // Getting single recipe
    public String[][] getRecipe(String breadName) {
        ArrayList<String> ingredientsList = new ArrayList<String>();
        ArrayList<String> proportionsList = new ArrayList<String>();
        String name = "";
        int breadid = 1;

        if(myDataBase.isOpen()) {
            String query = "SELECT " + KEY_BREADS_ID + " FROM " + TABLE_BREADS + " WHERE " + KEY_BREADS_NAME + " = '" + breadName + "';";
            Cursor cursor = myDataBase.rawQuery(query, null);
            if (cursor != null) {
                cursor.moveToFirst();
                //name = cursor.getString(0);
                breadid = cursor.getInt(0);
                cursor.close();
                // ings[1][1] = name;
            }

            query = "SELECT " + TABLE_INGREDIENTS + "." + KEY_INGREDIENTS_NAME + ", " + TABLE_RECIPES + "." + KEY_PROPORTION +
                    " FROM " + TABLE_RECIPES +
                    " JOIN " + TABLE_INGREDIENTS +
                    " ON " + TABLE_RECIPES + "." + KEY_INGREDIENTID + " = " + TABLE_INGREDIENTS + "." + KEY_INGREDIENTS_ID +
                    " WHERE " + TABLE_RECIPES + "." + KEY_BREADID + " = " + breadid + ";";

            cursor = myDataBase.rawQuery(query,null);
            if (cursor!=null){
                cursor.moveToFirst();
                while(cursor.isAfterLast() == false){
                    ingredientsList.add(cursor.getString(0));
                    proportionsList.add(cursor.getString(1));
                    cursor.moveToNext();
                }
                ingredientsList.add(0,"Flour");
                proportionsList.add(0,"100");
            }
        }

        String[][] ingredients = new String[ingredientsList.size()][2];
        for (int i = 0; i < ingredientsList.size(); i++){
            ingredients[i][0] = ingredientsList.get(i);
            ingredients[i][1] = proportionsList.get(i);
        }

        //String[][] ingredients = {{ingredient,"72%"}};
        return ingredients;

        /* Cursor cursor = db.rawQuery("SELECT " + TABLE_INGREDIENTS + "." + KEY_INGREDIENTS_NAME + ", " + TABLE_RECIPES + "." + KEY_PROPORTION +
                " FROM " + TABLE_RECIPES +
                " JOIN " + TABLE_BREADS +
                " ON " + TABLE_RECIPES + "." + KEY_INGREDIENTID + " = " + TABLE_INGREDIENTS + "." + KEY_INGREDIENTS_ID +
                " WHERE " + TABLE_RECIPES + "." + KEY_BREADID + " = " + breadid + ";", null);
        if (cursor2 != null) {
            cursor2.moveToFirst();
            int i = 0;
            while (cursor2.isAfterLast() != false) {
                ingredients[i][0] = cursor2.getString(0);
                ingredients[i][1] = cursor2.getString(1);
                i++;
                cursor2.moveToNext();
            }
        } */

        // return recipe
        // Recipe recipe = new Recipe (id, name, ingredients);
        // return recipe;
    }
}