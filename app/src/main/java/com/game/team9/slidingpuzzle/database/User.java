package com.game.team9.slidingpuzzle.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;

/**
 * Created on: 1/31/18
 *     Author: David Hiatt - dhiatt89@gmail.com
 */

@Entity(primaryKeys = "name", tableName = "users")
public class User
{
    @ColumnInfo(name = "name")
    @NonNull
    private String name;

    @ColumnInfo(name = "score")
    private int score;


    public void setName(@NonNull String n){name = n;}
    @NonNull
    public String getName(){return name;}
    public void setScore(int s){score = s;}
    public int getScore(){return score;}
}
