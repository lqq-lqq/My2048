package com.example.my2048.bean;

import androidx.room.Entity;
import androidx.room.PrimaryKey;


//存储上一次游戏的记录（该表格只有一行记录）
@Entity(tableName = "record")
public class Record {
    @PrimaryKey(autoGenerate = true) //主键是否自动增长，默认为false
    private int id;
    private String numberStr; //4*4个游戏记录的String形式，数字之间用空格隔开

    public void setNumberStr(String numberStr) {
        this.numberStr = numberStr;
    }

    public String getNumberStr() {
        return numberStr;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId(){
        return id;
    }
}
