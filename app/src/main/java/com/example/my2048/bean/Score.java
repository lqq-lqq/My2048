package com.example.my2048.bean;


import androidx.room.Entity;
import androidx.room.PrimaryKey;


//存储最高分（该表格只有一行记录）
@Entity(tableName = "score")
public class Score {
    @PrimaryKey(autoGenerate = true) //主键是否自动增长，默认为false
    private int id;
    private int score;  //历史最高分

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId(){
        return id;
    }
}
