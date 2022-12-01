package com.barbot;

import java.util.ArrayList;

public class DrinkListModel {

    Integer drinkImageResourceId;
    String drinkName;
    ArrayList<Ingredient> ingredients;

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

    public DrinkListModel(String drinkName, Integer drinkImageResourceId, ArrayList<Ingredient> ingredients) {
        this.drinkImageResourceId = drinkImageResourceId;
        this.drinkName = drinkName;
        this.ingredients = ingredients;
    }

    public String getDrinkName() {
        return drinkName;
    }

    public Integer getDrinkImageResourceId() {
        return drinkImageResourceId;
    }

    public ArrayList<Ingredient> getIngredients() {
        return ingredients;
    }
}
