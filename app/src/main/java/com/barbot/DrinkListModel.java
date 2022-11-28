package com.barbot;

import java.util.ArrayList;

public class DrinkListModel {

    Integer drinkImageResourceId;
    String drinkName;
    ArrayList<String> ingredients;

    public DrinkListModel(String drinkName, Integer drinkImageResourceId, ArrayList<String> ingredients) {
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

    public ArrayList<String> getIngredients() {
        return ingredients;
    }
}
