package com.barbot;

import java.util.ArrayList;

public class DrinkListModel {

    Integer drinkImageResourceId;
    String drinkName;
    ArrayList<String> ingredientsList;
//    Ingredient[] ingredients;

    public static class Ingredient {
        String name;
        Integer quantity;

        public Ingredient(String name, Integer quantity) {
            this.name = name;
            this.quantity = quantity;
        }

        public String getName() { return name; }

        public Integer getQuantity() { return quantity; }
    }

    public DrinkListModel(String drinkName, Integer drinkImageResourceId, ArrayList<String> ingredientsList) {
        this.drinkImageResourceId = drinkImageResourceId;
        this.drinkName = drinkName;
        this.ingredientsList = ingredientsList;
//        this.ingredients = ingredients;
    }

    public String getDrinkName() {
        return drinkName;
    }

    public Integer getDrinkImageResourceId() {
        return drinkImageResourceId;
    }

    public ArrayList<String> getIngredientsList() {
        return ingredientsList;
    }

//    public Ingredient[] getIngredients() { return ingredients; }
}
