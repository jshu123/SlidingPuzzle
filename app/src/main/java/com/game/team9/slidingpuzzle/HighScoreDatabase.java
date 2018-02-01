package com.game.team9.slidingpuzzle;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.Update;
import android.content.Context;

import java.util.List;

public abstract class HighScoreDatabase extends RoomDatabase {

    private static HighScoreDatabase s_Instance;
    public abstract IUserDao userDao();

    public static void DestroyInstance()
    {
        s_Instance = null;
    }

    public static HighScoreDatabase getDatabase(Context context)
    {
        if(s_Instance == null)
        {
            s_Instance = Room.databaseBuilder(context, HighScoreDatabase.class,"user-database").allowMainThreadQueries().build();
        }
        return s_Instance;
    }
public static List<User> getTop(final HighScoreDatabase db)
{
    return db.userDao().getTop();
}
    public static void addUser(final HighScoreDatabase db, User user) {
        db.userDao().insertAll(user);
    }
    public static void updateUser(final HighScoreDatabase db, User user)
    {
        db.userDao().updateUsers(user);
    }

}
