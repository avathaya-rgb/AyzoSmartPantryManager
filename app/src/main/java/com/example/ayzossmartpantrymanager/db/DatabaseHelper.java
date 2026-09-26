package com.example.ayzossmartpantrymanager.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.ayzossmartpantrymanager.model.PantryItem;
import com.example.ayzossmartpantrymanager.model.Recipe;
import com.example.ayzossmartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_PANTRY = "pantry_items";
    public static final String COL_P_ID = "_id";
    public static final String COL_P_NAME = "name";
    public static final String COL_P_QTY = "quantity";
    public static final String COL_P_UNIT = "unit";
    public static final String COL_P_EXPIRY = "expiry_date";

    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_R_ID = "_id";
    public static final String COL_R_NAME = "name";
    public static final String COL_R_STEPS = "steps";

    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String COL_RI_ID = "_id";
    public static final String COL_RI_RECIPE_ID = "recipe_id";
    public static final String COL_RI_NAME = "name";
    public static final String COL_RI_QTY = "quantity";
    public static final String COL_RI_UNIT = "unit";

    public static final String TABLE_SETTINGS = "settings";
    public static final String COL_S_KEY = "key";
    public static final String COL_S_VALUE = "value";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " (" +
                COL_P_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_P_NAME + " TEXT NOT NULL, " +
                COL_P_QTY + " REAL NOT NULL, " +
                COL_P_UNIT + " TEXT, " +
                COL_P_EXPIRY + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " (" +
                COL_R_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_R_NAME + " TEXT NOT NULL, " +
                COL_R_STEPS + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                COL_RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RI_RECIPE_ID + " INTEGER NOT NULL, " +
                COL_RI_NAME + " TEXT NOT NULL, " +
                COL_RI_QTY + " REAL NOT NULL, " +
                COL_RI_UNIT + " TEXT, " +
                "FOREIGN KEY(" + COL_RI_RECIPE_ID + ") REFERENCES " + TABLE_RECIPES + "(" + COL_R_ID + "))");

        db.execSQL("CREATE TABLE " + TABLE_SETTINGS + " (" +
                COL_S_KEY + "TEXT PRIMARY KEY, " +
                COL_S_VALUE + " TEXT)");

        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
        onCreate(db);
    }

    public long addPantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        return db.insert(TABLE_PANTRY, null, pantryToValues(item));
    }

    public int updatePantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        return db.update(TABLE_PANTRY, pantryToValues(item), COL_P_ID + "=?", new String[]{String.valueOf(item.getId())});
    }

    public void deletePantryItem(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_PANTRY, COL_P_ID + "=?", new String[]{String.valueOf(id)});
    }

    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_PANTRY, null, null, null, null, null, COL_P_NAME + " ASC");
        while (c.moveToNext()) {
            items.add(cursorToPantryItem(c));
        }
        c.close();
        return items;
    }

    public PantryItem getPantryItem(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_PANTRY, null, COL_P_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        PantryItem item = null;
        if (c.moveToFirst()) {
            item = cursorToPantryItem(c);
        }
        c.close();
        return item;
    }

    private ContentValues pantryToValues(PantryItem item) {
        ContentValues cv = new ContentValues();
        cv.put(COL_P_NAME, item.getName());
        cv.put(COL_P_QTY, item.getQuantity());
        cv.put(COL_P_UNIT, item.getUnit());
        cv.put(COL_P_EXPIRY, item.getExpiryDate());
        return cv;
    }

    private PantryItem cursorToPantryItem(Cursor c) {
        return new PantryItem(
                c.getLong(c.getColumnIndexOrThrow(COL_P_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_P_NAME)),
                c.getDouble(c.getColumnIndexOrThrow(COL_P_QTY)),
                c.getString(c.getColumnIndexOrThrow(COL_P_UNIT)),
                c.getString(c.getColumnIndexOrThrow(COL_P_EXPIRY))
        );
    }

    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_RECIPES, null, null, null, null, null, COL_R_NAME + " ASC");
        while (c.moveToNext()) {
            Recipe r = new Recipe(
                    c.getLong(c.getColumnIndexOrThrow(COL_R_ID)),
                    c.getString(c.getColumnIndexOrThrow(COL_R_NAME)),
                    c.getString(c.getColumnIndexOrThrow(COL_R_STEPS))
            );
            r.setIngredients(getIngredientsForRecipe(db, r.getId()));
            recipes.add(r);
        }
        c.close();
        return recipes;
    }

    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_RECIPES, null, COL_R_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        Recipe r = null;
        if (c.moveToFirst()) {
            r = new Recipe(
                    c.getLong(c.getColumnIndexOrThrow(COL_R_ID)),
                    c.getString(c.getColumnIndexOrThrow(COL_R_NAME)),
                    c.getString(c.getColumnIndexOrThrow(COL_R_STEPS))
            );
            r.setIngredients(getIngredientsForRecipe(db, r.getId()));
        }
        c.close();
        return r;
    }

    private List<RecipeIngredient> getIngredientsForRecipe(SQLiteDatabase db, long recipeId) {
        List<RecipeIngredient> list = new ArrayList<>();
        Cursor c = db.query(TABLE_RECIPE_INGREDIENTS, null, COL_RI_RECIPE_ID + "=?",
                new String[]{String.valueOf(recipeId)}, null, null, COL_RI_NAME + " ASC");
        while (c.moveToNext()) {
            list.add(new RecipeIngredient(
                    c.getLong(c.getColumnIndexOrThrow(COL_RI_ID)),
                    c.getLong(c.getColumnIndexOrThrow(COL_RI_RECIPE_ID)),
                    c.getString(c.getColumnIndexOrThrow(COL_RI_NAME)),
                    c.getDouble(c.getColumnIndexOrThrow(COL_RI_QTY)),
                    c.getString(c.getColumnIndexOrThrow(COL_RI_UNIT))
            ));
        }
        c.close();
        return list;
    }

    public void setSetting(String key, String value) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_S_KEY, key);
        cv.put(COL_S_VALUE, value);
        db.insertWithOnConflict(TABLE_SETTINGS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public String getSetting(String key, String defaultValue) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_SETTINGS, new String[]{COL_S_VALUE}, COL_S_KEY + "=?",
                new String[]{key}, null, null, null);
        String result = defaultValue;
        if (c.moveToFirst()) {
            result = c.getString(0);
        }
        c.close();
        return result;
    }

    private void seedRecipes(SQLiteDatabase db) {
        // Recipe data goes here - added in the next step
    }
}
