package com.game.team9.slidingpuzzle.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

@Database(version = 1, entities = User.class, exportSchema = false)
public abstract class HighScoreDatabase extends RoomDatabase {

    @Nullable
    private static HighScoreDatabase s_Instance;

    public abstract IUserDao userDao();

    public static void DestroyInstance()
    {
        s_Instance = null;
    }

    public static void Initialize(@NonNull Context context)
    {
        if(s_Instance == null)
        {
            s_Instance = Room.databaseBuilder(context, HighScoreDatabase.class,"user-database").allowMainThreadQueries().build();
        }
    }

    public static List<User> getTop()
    {
        if(s_Instance != null)
            return s_Instance.userDao().getTop();
        return new ArrayList<>();
    }

    public static int getScore(String name)
    {
        if(s_Instance != null)
        {
            return s_Instance.userDao().getScore(name);
        }
        return 0;
    }

    public static User findUser(String name)
    {
        if(s_Instance != null)
        {
            return s_Instance.userDao().findByName(name);
        }
        return null;
    }

    public static void updateUser(@NonNull User user)
    {

        if(s_Instance != null)
        {
            //s_Instance.beginTransaction();
            s_Instance.userDao().insertAll(user);
            s_Instance.userDao().Update(user.getName(), user.getScore());
          //  s_Instance.
           // s_Instance.endTransaction();
        }
    }

}
