package com.barbot;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DrinkModelDao {
    @Query(value = "SELECT * FROM drinkmodel")
    List<DrinkModel> getAll();

    @Query(value = "SELECT * FROM drinkmodel WHERE uid IN (:drinksIds)")
    List<DrinkModel> loadAllByIds(int[] drinksIds);

    @Query(value = "SELECT * FROM drinkmodel WHERE name LIKE :name LIMIT 1")
    DrinkModel findByName(String name);

    @Insert
    void insertAll(DrinkModel... drink);

    @Update
    void update(DrinkModel drink);

    @Delete
    void delete(DrinkModel drink);
}
