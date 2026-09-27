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
        addRecipe(db, "Scrambled Eggs on Toast",
                "1. Whisk eggs with a splash of milk.\n2. Melt butter in a pan, cook eggs on low heat, stirring gently.\n3. Toast the bread and serve eggs on top with salt and pepper.",
                new Object[][]{{"egg", 2, "unit"}, {"bread", 2, "unit"}, {"butter", 10, "g"}, {"milk", 20, "ml"}});

        addRecipe(db, "Tomato Pasta",
                "1. Boil pasta until al dente.\n2. Saute garlic in oil, add chopped tomatoes and simmer 10 min.\n3. Toss pasta through the sauce and season.",
                new Object[][]{{"pasta", 200, "g"}, {"tomato", 3, "unit"}, {"garlic clove", 2, "unit"}, {"olive oil", 15, "ml"}});

        addRecipe(db, "Chicken Stir Fry",
                "1. Slice chicken and vegetables.\n2. Stir-fry chicken until cooked through.\n3. Add vegetables and soy sauce, cook 5 more minutes.",
                new Object[][]{{"chicken breast", 300, "g"}, {"bell pepper", 1, "unit"}, {"onion", 1, "unit"}, {"soy sauce", 30, "ml"}});

        addRecipe(db, "Vegetable Omelette",
                "1. Whisk eggs.\n2. Saute diced vegetables until soft.\n3. Pour eggs over vegetables and cook until set, fold and serve.",
                new Object[][]{{"egg", 3, "unit"}, {"onion", 1, "unit"}, {"bell pepper", 1, "unit"}, {"cheese", 30, "g"}});

        addRecipe(db, "Rice and Beans",
                "1. Cook rice according to packet instructions.\n2. Heat beans with onion and garlic.\n3. Serve beans over rice.",
                new Object[][]{{"rice", 200, "g"}, {"beans", 250, "g"}, {"onion", 1, "unit"}, {"garlic clove", 1, "unit"}});

        addRecipe(db, "Grilled Cheese Sandwich",
                "1. Butter one side of each bread slice.\n2. Place cheese between slices, butter-side out.\n3. Grill in a pan until golden on both sides.",
                new Object[][]{{"bread", 2, "unit"}, {"cheese", 50, "g"}, {"butter", 10, "g"}});

        addRecipe(db, "Vegetable Soup",
                "1. Saute onion, carrot and celery in a pot.\n2. Add stock and simmer 20 minutes.\n3. Season and serve hot.",
                new Object[][]{{"onion", 1, "unit"}, {"carrot", 2, "unit"}, {"celery", 2, "unit"}, {"vegetable stock", 500, "ml"}});

        addRecipe(db, "Fried Rice",
                "1. Scramble eggs in a hot pan and set aside.\n2. Fry cold rice with soy sauce and vegetables.\n3. Stir the eggs back through and serve.",
                new Object[][]{{"rice", 300, "g"}, {"egg", 2, "unit"}, {"soy sauce", 20, "ml"}, {"carrot", 1, "unit"}});

        addRecipe(db, "Tuna Salad",
                "1. Drain tuna and flake into a bowl.\n2. Mix with mayonnaise, sweetcorn and diced onion.\n3. Serve on its own or with bread.",
                new Object[][]{{"tuna", 1, "unit"}, {"mayonnaise", 30, "g"}, {"sweetcorn", 50, "g"}, {"onion", 1, "unit"}});

        addRecipe(db, "Pancakes",
                "1. Whisk flour, egg and milk into a smooth batter.\n2. Melt a little butter in a pan.\n3. Cook spoonfuls of batter until bubbles form, flip and finish cooking.",
                new Object[][]{{"flour", 150, "g"}, {"egg", 1, "unit"}, {"milk", 200, "ml"}, {"butter", 15, "g"}});

        addRecipe(db, "Garlic Butter Mushrooms",
                "1. Melt butter in a hot pan.\n2. Add sliced mushrooms and cook until golden.\n3. Stir in chopped garlic, cook 1 more minute and season.",
                new Object[][]{{"mushroom", 250, "g"}, {"butter", 30, "g"}, {"garlic clove", 2, "unit"}});

        addRecipe(db, "Baked Potato with Cheese",
                "1. Pierce potato and bake until soft, about 45 minutes.\n2. Cut open and fluff the inside with a fork.\n3. Top with butter and grated cheese.",
                new Object[][]{{"potato", 1, "unit"}, {"cheese", 40, "g"}, {"butter", 10, "g"}});

        addRecipe(db, "Chicken Noodle Soup",
                "1. Simmer chicken in stock until cooked, then shred.\n2. Add noodles and carrot, cook until noodles are tender.\n3. Return chicken to the pot and season.",
                new Object[][]{{"chicken breast", 200, "g"}, {"noodles", 100, "g"}, {"carrot", 1, "unit"}, {"vegetable stock", 600, "ml"}});

        addRecipe(db, "Caprese Salad",
                "1. Slice tomato and mozzarella.\n2. Arrange alternately on a plate with basil leaves.\n3. Drizzle with olive oil and season.",
                new Object[][]{{"tomato", 2, "unit"}, {"mozzarella", 125, "g"}, {"basil", 10, "g"}, {"olive oil", 15, "ml"}});
    }
    private void addRecipe(SQLiteDatabase db, String name, String steps, Object[][] ingredients){
            ContentValues rv = new ContentValues();
            rv.put(COL_R_NAME, name);
            rv.put(COL_R_STEPS, steps);
            long recipeId = db.insert(TABLE_RECIPES, null, rv);

            for (Object[] ing : ingredients) {
                ContentValues iv = new ContentValues();
                iv.put(COL_RI_RECIPE_ID, recipeId);
                iv.put(COL_RI_NAME, (String) ing[0]);
                iv.put(COL_RI_QTY, ((Number) ing[1]).doubleValue());
                iv.put(COL_RI_UNIT, (String) ing[2]);
                db.insert(TABLE_RECIPE_INGREDIENTS, null, iv);
            }
    }
}