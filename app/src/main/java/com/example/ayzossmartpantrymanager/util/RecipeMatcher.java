package com.example.ayzossmartpantrymanager.util;

import com.example.ayzossmartpantrymanager.model.PantryItem;
import com.example.ayzossmartpantrymanager.model.Recipe;
import com.example.ayzossmartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecipeMatcher {

    public static List<Recipe> getSuggestedRecipes(List<Recipe> allRecipes, List<PantryItem> pantry) {
        List<Recipe> result = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            if (getMissingIngredients(recipe, pantry).isEmpty()) {
                result.add(recipe);
            }
        }
        return result;
    }

    public static List<Recipe> getAlmostThereRecipes(List<Recipe> allRecipes, List<PantryItem> pantry) {
        List<Recipe> result = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            if (getMissingIngredients(recipe, pantry).size() == 1) {
                result.add(recipe);
            }
        }
        return result;
    }

    public static List<String> getMissingIngredients(Recipe recipe, List<PantryItem> pantry) {
        List<String> missing = new ArrayList<>();
        for (RecipeIngredient required : recipe.getIngredients()) {
            if (!isSatisfied(required, pantry)) {
                missing.add(required.getName());
            }
        }
        return missing;
    }

    private static boolean isSatisfied(RecipeIngredient required, List<PantryItem> pantry) {
        String normalizedRequiredName = normalizeName(required.getName());
        for (PantryItem item : pantry) {
            if (normalizeName(item.getName()).equals(normalizedRequiredName)) {
                return hasEnoughQuantity(required, item);
            }
        }
        return false;
    }

    private static boolean hasEnoughQuantity(RecipeIngredient required, PantryItem owned) {
        String requiredUnit = normalizeUnit(required.getUnit());
        String ownedUnit = normalizeUnit(owned.getUnit());

        if (requiredUnit.isEmpty() || ownedUnit.isEmpty()) {
            return true;
        }

        Double requiredInBase = toBaseUnits(required.getQuantity(), requiredUnit);
        Double ownedInBase = toBaseUnits(owned.getQuantity(), ownedUnit);

        if (requiredInBase == null || ownedInBase == null || !sameUnitFamily(requiredUnit, ownedUnit)) {
            return true;
        }

        return ownedInBase >= requiredInBase;
    }

    public static String normalizeName(String rawName) {
        if (rawName == null) return "";
        String n = rawName.trim().toLowerCase(Locale.ROOT);
        n = n.replaceAll("\\s+", " ");
        if (n.endsWith("es") && n.length() > 4) {
            n = n.substring(0, n.length() - 2);
        } else if (n.endsWith("s") && n.length() > 3) {
            n = n.substring(0, n.length() - 1);
        }
        return n;
    }

    private static String normalizeUnit(String rawUnit) {
        if (rawUnit == null) return "";
        String u = rawUnit.trim().toLowerCase(Locale.ROOT);
        switch (u) {
            case "g":
            case "gram":
            case "grams":
                return "g";
            case "kg":
            case "kilogram":
            case "kilograms":
                return "kg";
            case "ml":
            case "milliliter":
            case "milliliters":
            case "millilitre":
            case "millilitres":
                return "ml";
            case "l":
            case "liter":
            case "liters":
            case "litre":
            case "litres":
                return "l";
            case "tsp":
            case "teaspoon":
            case "teaspoons":
                return "tsp";
            case "tbsp":
            case "tablespoon":
            case "tablespoons":
                return "tbsp";
            case "cup":
            case "cups":
                return "cup";
            case "unit":
            case "units":
            case "piece":
            case "pieces":
            case "":
                return "unit";
            default:
                return u;
        }
    }

    private static boolean sameUnitFamily(String a, String b) {
        boolean aWeight = a.equals("g") || a.equals("kg");
        boolean bWeight = b.equals("g") || b.equals("kg");
        boolean aVolume = a.equals("ml") || a.equals("l") || a.equals("tsp") || a.equals("tbsp") || a.equals("cup");
        boolean bVolume = b.equals("ml") || b.equals("l") || b.equals("tsp") || b.equals("tbsp") || b.equals("cup");
        boolean aCount = a.equals("unit");
        boolean bCount = b.equals("unit");
        return (aWeight && bWeight) || (aVolume && bVolume) || (aCount && bCount);
    }

    private static Double toBaseUnits(double qty, String unit) {
        switch (unit) {
            case "g":
                return qty;
            case "kg":
                return qty * 1000.0;
            case "ml":
                return qty;
            case "l":
                return qty * 1000.0;
            case "tsp":
                return qty * 5.0;
            case "tbsp":
                return qty * 15.0;
            case "cup":
                return qty * 240.0;
            case "unit":
                return qty;
            default:
                return null;
        }
    }
}

