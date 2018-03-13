package com.game.team9.slidingpuzzle.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface IUserDao {
    @Query("SELECT * FROM users Order By score Limit 5")
    List<User> getTop();

    @Query("SELECT * FROM users WHERE name LIKE :first LIMIT 1")
    User findByName(String first);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(User... users);

    @Query("UPDATE users SET score = :score WHERE name LIKE :name AND score < :score")
    void Update(String name, int score);

    @Query("SELECT score FROM users WHERE name LIKE :first LIMIT 1")
    int getScore(String first);

    @Query("SELECT * from users")
    List<User> getAll();

    @Query("SELECT COUNT(*) from users")
    int getTotal();

    @Query("DELETE FROM users")
    void clearAll();
}
