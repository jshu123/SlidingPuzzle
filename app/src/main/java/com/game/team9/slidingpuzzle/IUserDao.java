package com.game.team9.slidingpuzzle;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface IUserDao {
    @Query("SELECT * FROM user Order By score Limit 5")
    List<User> getTop();


    @Query("SELECT * FROM user WHERE name LIKE :first LIMIT 1")
    User findByName(String first);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(User... users);

    @Update
    public void updateUsers(User... users);

    @Query("SELECT score FROM user WHERE name LIKE :first LIMIT 1")
    int getScore(String first);

    @Delete
    void delete(User user);
}
