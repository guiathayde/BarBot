package com.barbot;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class DrinkModel {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "quantity")
    public Integer quantity;

    public DrinkModel(Integer uid, String name, Integer quantity) {
        if (uid != null)
            this.uid = uid;
        this.name = name;
        this.quantity = quantity;
    }

    public String getName() { return name; }
    public Integer getQuantity() { return quantity; }
}
