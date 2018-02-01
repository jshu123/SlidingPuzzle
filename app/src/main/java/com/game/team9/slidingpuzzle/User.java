package com.game.team9.slidingpuzzle;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;

/**
 * Created by x on 1/31/18.
 */

@Entity(primaryKeys = "name", tableName = "users")
public class User
{
    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "score")
    private int score;


    public void setName(String n){name = n;}
    public String getName(){return name;}
    public void setScore(int s){score = s;}
    public int getScore(){return score;}
}
