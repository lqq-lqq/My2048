package com.example.my2048;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.my2048.bean.Record;
import com.example.my2048.bean.Score;
import com.example.my2048.database.ScoreDataBase;
import com.example.my2048.fragment.BlockFragment;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private static int line=4;
    private static int column=4;
    private TextView[][] block;  //显示数字的小块
    private ScoreDataBase scoreDB;
    private String dataBaseName = "ScoreDatabase";
    private int[][] number;  //当前的16个方块的情况
    private int[][] past_number;   //为了实现“撤回”，每次发生变化都存储上一次的情况
    //滑动界面的捕捉，实现相对应的响应事件，产生相同方块结合
    private GestureDetector gestureDetector;
    private int verticalMinistance = 200;            //水平最小识别距离
    private int minVelocity = 5;            //最小识别速度
    private int score;
    private int topScore;
    private BlockFragment blockFragment;   //存放方块的fragment类
    private TextView scoreView;   //显示当前分数的textView
    private TextView topScoreTextView; //显示最高分数的textView
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Intent intent=getIntent();
        String recover=intent.getStringExtra("recover");
        block=new TextView[line][column];
        scoreView=findViewById(R.id.score);
        number=new int[line][column];  //初始化全为0
        past_number=new int[line][column];
        cloneNumber();//注意！！！Java数组拷贝不能直接等号
        blockFragment=(BlockFragment) getSupportFragmentManager().findFragmentById(R.id.blockFragment);
        initBlock();
        initBlockColor();
        initDatabase();
        //下面是从数据库中取数据
        List<Score> scoreList=scoreDB.getScoreDao().getAllScore();
        if(scoreList.size()==0){
            topScore=0;
        }
        else{
            topScore=scoreList.get(0).getScore();
        }
        topScoreTextView=findViewById(R.id.topScoreTextView);
        topScoreTextView.setText(String.valueOf(topScore));
        score=0;
        //开始游戏or继续游戏

        if(recover.equals("true")){ //需要恢复数据
            List<Record> record=scoreDB.getScoreDao().getAllRecord(); //得到游戏历史记录
            if(record.size()==0){
                Toast t = Toast.makeText(this,
                        "您当前没有游戏记录，已自动为您开启新的游戏", Toast.LENGTH_SHORT);
                t.show();
            }
            else{
                //历史记录以String的形式存储子啊数据库中，每个数字之间以空格间隔
                String numberStr=record.get(0).getNumberStr();
                String[] number_list=numberStr.split(" ");
                for(int i=0;i<line;i++){
                    for(int j=0;j<column;j++){
                        number[i][j]=Integer.parseInt(number_list[i*column+j]);
                    }
                }
            }
        }
        //不需要恢复数据
        else{
            //初始化，需要随机生成两个数字块
            generateOneRandom();
            generateOneRandom();
        }
        showBlock();  //将初始化的block显示

        //检测用户的手势，并且做出相应的响应
        gestureDetector=new GestureDetector(this, new GestureDetector.OnGestureListener() {
            //用户上下左右滑动
            @Override
            public boolean onFling(MotionEvent motionEvent,
                                   MotionEvent motionEvent1,
                                   float velocityX, float velocityY) {
                if (motionEvent.getX() - motionEvent1.getX() > verticalMinistance
                        && Math.abs(velocityX) > minVelocity) {
                    goLeft();
                    showBlock();
                    System.out.println("left");
                } else if (motionEvent1.getX() - motionEvent.getX() > verticalMinistance
                        && Math.abs(velocityX) > minVelocity) {
                    goRight();
                    showBlock();
                    System.out.println("right");
                } else if (motionEvent.getY() - motionEvent1.getY() > verticalMinistance
                        && Math.abs(velocityY) > minVelocity) {
                    goUp();
                    showBlock();
                    System.out.println("up");
                } else if (motionEvent1.getY() - motionEvent.getY() > verticalMinistance
                        && Math.abs(velocityY) > minVelocity) {
                    goDown();
                    showBlock();
                    System.out.println("down");
                }
                return false;
            }
            @Override
            public boolean onDown(MotionEvent motionEvent) {
                return false;
            }
            @Override
            public void onShowPress(MotionEvent motionEvent) {
            }
            @Override
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                return false;
            }
            @Override
            public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                return false;
            }
            @Override
            public void onLongPress(MotionEvent motionEvent) {

            }
        });
        //撤回
        Button withdraw=findViewById(R.id.withdraw);
        withdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                clonePastNumber();
                showBlock();
            }
        });
        //菜单
        Button menu=findViewById(R.id.menu);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(GameActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

    }

    //退出程序需要进行信息的存储
    @Override
    protected void onStop() {
        super.onStop();
        Score newScore=new Score();
        newScore.setScore(topScore);   //更新最高分
        scoreDB.getScoreDao().clearScore();
        scoreDB.getScoreDao().insertScore(newScore);

        Record newRecord=new Record();
        String numberStr="";   //存放游戏记录的字符串形式，方块数字之间通过空格隔开
        for(int i=0;i<number.length;i++){
            for(int j=0;j<number[i].length;j++){
                numberStr=numberStr+String.valueOf(number[i][j])+" ";
            }
        }
        System.out.println("numberStr111 "+numberStr);
        newRecord.setNumberStr(numberStr);
        scoreDB.getScoreDao().clearRecord();
        scoreDB.getScoreDao().insertRecord(newRecord);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev); // 让GestureDetector响应触碰事件
        super.dispatchTouchEvent(ev); // 让Activity响应触碰事件
        return false;
    }
    //将TextView对应到block中，方便操作
    private void initBlock(){
        for(int i=0;i<line;i++){
            for(int j=0;j<column;j++){
                number[i][j]=0;
            }
        }
        TextView block_temp=blockFragment.getView().findViewById(R.id.block00);
        block[0][0]=block_temp;
        block_temp=blockFragment.getView().findViewById(R.id.block01);
        block[0][1]=block_temp;
        block_temp=blockFragment.getView().findViewById(R.id.block02);
        block[0][2]=block_temp;
        block_temp=blockFragment.getView().findViewById(R.id.block03);
        block[0][3]=block_temp;

        block_temp=blockFragment.getView().findViewById(R.id.block10);
        block[1][0]=block_temp;
        block_temp=blockFragment.getView().findViewById(R.id.block11);
        block[1][1]=block_temp;
        block_temp=blockFragment.getView().findViewById(R.id.block12);
        block[1][2]=block_temp;
        block_temp=blockFragment.getView().findViewById(R.id.block13);
        block[1][3]=block_temp;


        block_temp=blockFragment.getView().findViewById(R.id.block20);
        block[2][0]=block_temp;
        block_temp=blockFragment.getView().findViewById(R.id.block21);
        block[2][1]=block_temp;
        block_temp=blockFragment.getView().findViewById(R.id.block22);
        block[2][2]=block_temp;
        block_temp=blockFragment.getView().findViewById(R.id.block23);
        block[2][3]=block_temp;

        block_temp=blockFragment.getView().findViewById(R.id.block30);
        block[3][0]=block_temp;
        block_temp=blockFragment.getView().findViewById(R.id.block31);
        block[3][1]=block_temp;
        block_temp=blockFragment.getView().findViewById(R.id.block32);
        block[3][2]=block_temp;
        block_temp=blockFragment.getView().findViewById(R.id.block33);
        block[3][3]=block_temp;
    }

    //初始化方块的颜色和字的颜色
    @SuppressLint("ResourceAsColor")
    private void initBlockColor(){
        for(int i=0;i<line;i++){
            for(int j=0;j<column;j++){
                block[i][j].setBackgroundColor(getResources().getColor(R.color.background2));
                block[i][j].setTextColor(Color.parseColor("#ffffff"));
            }
        }
    }
    //更新方块颜色
    private void flushBlockColor(){
        //System.out.println("更新方块颜色");
        for(int i=0;i<line;i++){
            for(int j=0;j<column;j++){
                System.out.println("i="+i+" j="+j+" number="+number[i][j]);
                if(number[i][j]==0){
                    block[i][j].setBackgroundResource(R.drawable.round_corner0);
                }
                else if(number[i][j]==2){
                    block[i][j].setBackgroundResource(R.drawable.round_corner2);
                }
                else if(number[i][j]==4){
                    block[i][j].setBackgroundResource(R.drawable.round_corner4);
                }
                else if(number[i][j]==8){
                    block[i][j].setBackgroundResource(R.drawable.round_corner8);
                }
                else if(number[i][j]==16){
                    block[i][j].setBackgroundResource(R.drawable.round_corner16);
                }
                else if(number[i][j]==32){
                    block[i][j].setBackgroundResource(R.drawable.round_corner32);
                }
                else if(number[i][j]==64){
                    block[i][j].setBackgroundResource(R.drawable.round_corner64);
                }
                else if(number[i][j]==128){
                    block[i][j].setBackgroundResource(R.drawable.round_corner128);
                }
                else if(number[i][j]==256){
                    block[i][j].setBackgroundResource(R.drawable.round_corner256);
                }
                else if(number[i][j]==512){
                    block[i][j].setBackgroundResource(R.drawable.round_corner512);
                }
                else if(number[i][j]==1024){
                    block[i][j].setBackgroundResource(R.drawable.round_corner1024);
                }
                else if(number[i][j]==2048){
                    block[i][j].setBackgroundResource(R.drawable.round_corner2048);
                }
                else if(number[i][j]==4096){
                    block[i][j].setBackgroundResource(R.drawable.round_corner4096);
                }
                else if(number[i][j]==8192){
                    block[i][j].setBackgroundResource(R.drawable.round_corner8192);
                }


            }
        }
    }

    //初始化数据库
    private void initDatabase(){
        scoreDB = Room.databaseBuilder(this, ScoreDataBase.class, dataBaseName)
                .allowMainThreadQueries()
                .build();
    }

    //用户每次滑动，都会随机生成一个位置为2或4，兼职”判断是否刷新纪录“，兼职”更新最高分“
    private void generateOneRandom(){
        //1.更新最高分
        if(score>topScore){
            topScore=score;
            topScoreTextView.setText(String.valueOf(score));
        }
        //2.0判断游戏能否继续
        boolean flag=canContinue();
        if(flag==false){   //表示游戏无法继续了
            showDialog();
            return;
        }
        //2.1判断是否还能插入2、4
        boolean canInsert=false;
        for(int m=0;m<number.length;m++){
            for(int n=0;n<number[m].length;n++){
                if(number[m][n]==0){
                    canInsert=true;
                    break;
                }
            }
            if(canInsert){
                break;
            }
        }
        if(canInsert==false){
            return;
        }
        //3.能继续插入的情况下开始插入
        Random random= new Random();
        //随机生成产生2或4的格子位置
        int i= random.nextInt(line*column);
        //计算行列
        int x=i/column;
        int y=i-x*column;
        while(number[x][y]!=0){
            i= random.nextInt(line*column);
            x=i/column;
            y=i-x*column;
        }
        Float f=random.nextFloat();
        int addNumber;  //随机生成的块的数字，2的概率比4大
        if(f>0.8){
            addNumber=4;
        }
        else{
            addNumber=2;
        }
        number[x][y]=addNumber;
    }
    //对整个方块内的数字进行判断，是否还可以合成
    private boolean canContinue(){
        for(int m=0;m<number.length;m++){
            for(int n=0;n<number[m].length;n++){
                if(number[m][n]==0){
                    return true;
                }
                if(m==0 && n==0){  //左上角  （下右）
                    if(number[m][n]==number[m][n+1] || number[m][n]==number[m+1][n]){
                        return true;
                    }
                }
                else if(m==line-1 && n==0){  //左下角 （上右）
                    if(number[m][n]==number[m-1][n] || number[m][n]==number[m][n+1]){
                        return true;
                    }
                }
                else if(m==0 && n==column-1){  //右上角 （下左）
                    if(number[m][n]==number[m+1][n] || number[m][n]==number[m][n-1]){
                        return true;
                    }
                }
                else if(m==line-1 && n==column-1){ //右下角 （上左）
                    if(number[m][n]==number[m-1][n] || number[m][n]==number[m][n-1]){
                        return true;
                    }
                }
                else if(n==0){  //最左列，（上下右）
                    if(number[m][n]==number[m-1][n] || number[m][n]==number[m+1][n] || number[m][n]==number[m][n+1]){
                        return true;
                    }
                }
                else if(m==0){  //最上列 （下左右）
                    if(number[m][n]==number[m+1][n] || number[m][n]==number[m][n-1] || number[m][n]==number[m][n+1]){
                        return true;
                    }
                }
                else if(m==line-1){ //最下列 （上左右）
                    if(number[m][n]==number[m-1][n] || number[m][n]==number[m][n-1] || number[m][n]==number[m][n+1]){
                        return true;
                    }
                }
                else if(n==column-1){ //最右列 （上下左）
                    if(number[m][n]==number[m-1][n] || number[m][n]==number[m+1][n] || number[m][n]==number[m][n-1]){
                        return true;
                    }
                }
                else{ //中间的 （上下左右）
                    if(number[m][n]==number[m-1][n] || number[m][n]==number[m+1][n] || number[m][n]==number[m][n-1]
                            || number[m][n]==number[m][n+1]){
                        return true;
                    }
                }
            }
        }
        return false;
    }
    //重新开始游戏
    private void reStart(){
        for(int m=0;m<number.length;m++){
            for(int n=0;n<number[m].length;n++){
                number[m][n]=0;
                past_number[m][n]=0;
            }
        }
        score=0;
        scoreView.setText("0");
        generateOneRandom();
        showBlock();
    }

    private void showDialog(){
        View view = LayoutInflater.from(this).inflate(R.layout.my_dialog_layout,null,false);
        final androidx.appcompat.app.AlertDialog my_dialog= new AlertDialog.Builder(this).setView(view).create();
        TextView message=view.findViewById(R.id.message);
        Button decide=view.findViewById(R.id.decide);
        message.setText("你此次得分为: "+score);
        decide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reStart(); //重新开始游戏
                my_dialog.dismiss();
            }
        });
        my_dialog.show();
    }

    //将变换后的number序列，通过block显示在界面上
    private void showBlock(){
        for(int i=0;i<line;i++){
            for(int j=0;j<column;j++){
                //System.out.println("i="+i+" j="+j+" "+number[i][j]);
                if(number[i][j]==0){
                    block[i][j].setText("");
                }
                else{
                    block[i][j].setText(String.valueOf(number[i][j]));
                }
            }
        }
        flushBlockColor();
        scoreView.setText(String.valueOf(score));
    }
    //将number中的值复制给past_number
    private void cloneNumber(){
        for(int i=0;i<number.length;i++){
            for(int j=0;j<number[i].length;j++){
                past_number[i][j]=number[i][j];
            }
        }
    }
    //将past_number中的值复制给number，”撤回“
    private void clonePastNumber(){
        for(int i=0;i<number.length;i++){
            for(int j=0;j<number[i].length;j++){
                number[i][j]=past_number[i][j];
            }
        }
    }

    //往左滑的逻辑
    private void goLeft(){
        //将存储上一步记录的past_number更新为number，为了实现“撤回”
        cloneNumber();
        int[] temp;  //存储一行中的非零数字
        for(int i=0;i<line;i++){
            int index=0;
            temp=new int[column];
            for(int j=0;j<column;j++){
                temp[j]=0;
            }
            //将一行的不为0的数据都集合起来
            for(int j=0;j<column;j++){
                if(number[i][j]!=0){
                    temp[index++]=number[i][j];
                }
            }
            //开始合并相同的数字（方块）
            for(int j=0;j<column-1;j++){
                if(temp[j+1]==0){
                    break;
                }
                else{
                    if(temp[j]==temp[j+1]){
                        temp[j]*=2;
                        score+=temp[j];
                        //合并后，将后面的数字往前一步
                        for(int m=j+1;m<column-1;m++){
                            temp[m]=temp[m+1];
                        }
                        temp[column-1]=0;
                        break;
                    }
                }
            }
            for(int j=0;j<column;j++){
                number[i][j]=temp[j];  //将合并后的序列重新赋值给总序列
            }
        }
        generateOneRandom();  //随机生成两个2或4
    }
    private void goRight(){
        cloneNumber();
        int[] temp;
        for(int i=0;i<line;i++){
            int index=column-1;
            temp=new int[column];
            for(int j=0;j<column;j++){
                temp[j]=0;
            }
            for(int j=column-1;j>=0;j--){
                if(number[i][j]!=0){  //将一行的不为0的数据都集合起来
                    //System.out.println("i="+i+"j="+j+"  "+number[i][j]);
                    temp[index--]=number[i][j];
                }
            }
            //System.out.println("temp1 "+temp[0]+" "+temp[1]+" "+temp[2]+" "+temp[3]);
            for(int j=column-1;j>0;j--){
                if(temp[j-1]==0){
                    break;
                }
                else{
                    if(temp[j]==temp[j-1]){
                        temp[j]*=2;
                        score+=temp[j];
                        for(int m=j-1;m>0;m--){
                            temp[m]=temp[m-1];
                        }
                        temp[0]=0;
                        break;
                    }
                }
            }
            for(int j=0;j<column;j++){
                number[i][j]=temp[j];
            }
            //System.out.println("temp2 "+temp[0]+" "+temp[1]+" "+temp[2]+" "+temp[3]);
        }
        generateOneRandom();
    }
    private void goUp(){
        cloneNumber();
        int[] temp;
        for(int j=0;j<column;j++){
            int index=0;
            temp=new int[line];
            for(int i=0;i<line;i++){
                temp[j]=0;
            }
            for(int i=0;i<line;i++){
                if(number[i][j]!=0){  //将一行的不为0的数据都集合起来
                    //System.out.println("i="+i+"j="+j+"  "+number[i][j]);
                    temp[index++]=number[i][j];
                }
            }
            //System.out.println("temp1 "+temp[0]+" "+temp[1]+" "+temp[2]+" "+temp[3]);
            for(int i=0;i<line-1;i++){
                if(temp[i+1]==0){
                    break;
                }
                else{
                    if(temp[i]==temp[i+1]){
                        temp[i]*=2;
                        score+=temp[i];
                        for(int m=i+1;m<line-1;m++){
                            temp[m]=temp[m+1];
                        }
                        temp[line-1]=0;
                        break;
                    }
                }
            }
            for(int i=0;i<line;i++){
                number[i][j]=temp[i];
            }

            //System.out.println("temp2 "+temp[0]+" "+temp[1]+" "+temp[2]+" "+temp[3]);
        }
        generateOneRandom();
    }
    private void goDown(){
        cloneNumber();
        int[] temp;
        for(int j=0;j<column;j++){
            int index=line-1;
            temp=new int[line];
            for(int i=0;i<line;i++){
                temp[j]=0;
            }
            for(int i=line-1;i>=0;i--){
                if(number[i][j]!=0){  //将一行的不为0的数据都集合起来
                    //System.out.println("i="+i+"j="+j+"  "+number[i][j]);
                    temp[index--]=number[i][j];
                }
            }
            //System.out.println("temp1 "+temp[0]+" "+temp[1]+" "+temp[2]+" "+temp[3]);
            for(int i=line-1;i>0;i--){
                if(temp[i-1]==0){
                    break;
                }
                else{
                    if(temp[i]==temp[i-1]){
                        temp[i]*=2;
                        score+=temp[i];
                        for(int m=i-1;m>0;m--){
                            temp[m]=temp[m-1];
                        }
                        temp[0]=0;
                        break;
                    }
                }
            }
            for(int i=0;i<line;i++){
                number[i][j]=temp[i];
            }
            //System.out.println("temp2 "+temp[0]+" "+temp[1]+" "+temp[2]+" "+temp[3]);
        }
        generateOneRandom();
    }
}