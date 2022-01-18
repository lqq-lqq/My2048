package com.example.my2048.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.my2048.bean.Record;
import com.example.my2048.bean.Score;
import com.example.my2048.dao.ScoreDao;

@Database(entities = { Score.class , Record.class}, version = 1,exportSchema = false)
public abstract class ScoreDataBase extends RoomDatabase {
    private static final String DB_NAME = "ScoreDataBase.db";
    private static volatile ScoreDataBase instance;
    static synchronized ScoreDataBase getInstance(Context context) {
        if (instance == null) {
            instance = create(context);
        }
        return instance;
    }
    private static ScoreDataBase create(final Context context) {
        return Room.databaseBuilder(
                context, ScoreDataBase.class, DB_NAME).build();
    }
    public abstract ScoreDao getScoreDao();
}
