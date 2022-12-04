package com.barbot.model;

import java.util.ArrayList;

public class DrinkListModel {

    Integer drinkImageResourceId;
    String drinkName;
    ArrayList<Ingredient> ingredients;

    public static class Ingredient {
        String name;
        Integer quantity;
        Integer bomb;

        public Ingredient(String name, Integer quantity, Integer bomb) {
            this.name = name;
            this.quantity = quantity;
            this.bomb = bomb;
        }

        public String getName() { return name; }

        public Integer getQuantity() { return quantity; }

        public Integer getBomb() { return bomb; }
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
