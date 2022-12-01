package com.barbot.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.barbot.model.DrinkModel;
import com.barbot.model.DrinkModelDao;

@Database(entities = {DrinkModel.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DrinkModelDao drinkDao();
}
