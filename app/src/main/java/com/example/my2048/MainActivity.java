package com.example.my2048;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button start=findViewById(R.id.start);
        Button continue1=findViewById(R.id.continue1);
        //开始游戏，指重新开始新的游戏
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,GameActivity.class);
                intent.putExtra("recover","false");
                startActivity(intent);
            }
        });
        //继续游戏，恢复上次的游戏记录
        continue1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,GameActivity.class);
                intent.putExtra("recover","true"); //表示需要恢复数据
                startActivity(intent);
            }
        });
    }
}