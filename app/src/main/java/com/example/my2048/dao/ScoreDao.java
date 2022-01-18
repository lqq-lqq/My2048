package com.example.my2048.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.my2048.bean.Score;
import com.example.my2048.bean.Record;

import java.util.List;


//对两张表进行操作
@Dao
public interface ScoreDao {
    @Query("SELECT * FROM score") //获取最高分
    List<Score> getAllScore();
    @Query("SELECT * FROM record") //获取上次的游戏记录
    List<Record> getAllRecord();
    //插入更新的最高分
    @Insert
    void insertScore(Score score);
    //插入最新的一次的游戏记录
    @Insert
    void insertRecord(Record record);

    //清空score最高分数据表
    @Query("DELETE FROM score")
    void clearScore();

    //清空record游戏记录数据表
    @Query("DELETE FROM record")
    void clearRecord();
}
