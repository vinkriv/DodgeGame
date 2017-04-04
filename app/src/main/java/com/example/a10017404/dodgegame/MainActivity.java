package com.example.a10017404.dodgegame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    //Code from this program has been used from "Beginning Android Games" by Mario Zechner
    //Review SurfaceView, Canvas, continue

    GameSurface gameSurface;
    int objx=100;
    int obstspeed=10;
    ArrayList<Obstacle> obstacles;
    int hits=0;
    int runs=0;
    boolean fast=false;
    SoundPool soundPool;
    int chimesID,chordID,dingID;
    MediaPlayer player;
    boolean isHit;
    int hyperdriveId;
    int collisionID;
    float starttime;
    SystemClock clock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameSurface = new GameSurface(this);
        starttime = clock.currentThreadTimeMillis();
        setContentView(gameSurface);
        obstacles = new ArrayList<>();
        obstacles.add(new Obstacle());
        player = MediaPlayer.create(this,R.raw.rickroll);
        player.setLooping(true);
        player.setVolume((float)0.25,(float)0.25);
        player.start();
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                obstacles.add(new Obstacle());
            }
        }, 5000);
        SensorManager manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor mysensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        manager.registerListener(this, mysensor, SensorManager.SENSOR_DELAY_FASTEST);
        SoundPool.Builder builder = new SoundPool.Builder();
        builder.setMaxStreams(2);
        soundPool = builder.build();
        hyperdriveId = soundPool.load(MainActivity.this, R.raw.hyperdrive, 1);
    }

    @Override
    protected void onPause(){
        super.onPause();
        gameSurface.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        gameSurface.resume();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        int newx=objx-=Math.round(sensorEvent.values[0]);
        if (newx<=15){
            newx=16;
        }
        if (newx>=1250){
            newx=1249;
        }
        objx=newx;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    //----------------------------GameSurface Below This Line--------------------------
    public class GameSurface extends SurfaceView implements Runnable {

        Thread gameThread;
        SurfaceHolder holder;
        volatile boolean running = false;
        Bitmap spaceship;
        Bitmap firespaceship;

        Paint paintProperty;

        int screenWidth;
        int screenHeight;

        public GameSurface(Context context) {
            super(context);

            holder=getHolder();
            spaceship = BitmapFactory.decodeResource(getResources(),R.drawable.spaceship);
            firespaceship = BitmapFactory.decodeResource(getResources(),R.drawable.firespaceship);

            Display screenDisplay = getWindowManager().getDefaultDisplay();
            Point sizeOfScreen = new Point();
            screenDisplay.getSize(sizeOfScreen);
            screenWidth=sizeOfScreen.x;
            screenHeight=sizeOfScreen.y;

            paintProperty= new Paint();
            paintProperty.setTextSize(100);

            this.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    soundPool.play(hyperdriveId,1,1,1,0,1);
                    if (fast) {
                        obstspeed = 10;
                        fast=false;
                    } else {
                        obstspeed = 30;
                        fast=true;
                    }
                }
            });

        }

        @Override
        public void run() {
            while (running){
                if (10000-clock.currentThreadTimeMillis()-starttime<=0){
                    running=false;
                }
                if (!holder.getSurface().isValid())
                    continue;
                final Canvas canvas= holder.lockCanvas();
                canvas.drawRGB(255,0,0);
                //canvas.drawBitmap(spaceship,objx,1300,null);
                final Paint obstPaint = new Paint();
                obstPaint.setColor(Color.rgb(0, 255, 0));
                obstPaint.setStrokeWidth(10);
                final Paint pointsPaint = new Paint();
                pointsPaint.setTextSize(100);
                canvas.drawText(String.valueOf(runs-hits),1000,200,pointsPaint);
                canvas.drawText(String.valueOf((10000-clock.currentThreadTimeMillis()-starttime)/1000),100,200,pointsPaint);
                if (obstacles.size()>0) {
                    for (int index = 0; index < obstacles.size(); index++) {
                        obstacles.get(index).setY(obstacles.get(index).getY()+obstspeed);
                        Rect obst = new Rect(obstacles.get(index).getX(), obstacles.get(index).getY(), obstacles.get(index).getX() + 200, obstacles.get(index).getY() + 300);
                        canvas.drawRect(obst, obstPaint);
                        Rect hitbox = new Rect(objx, 1300, objx + spaceship.getWidth(), 1300 + spaceship.getHeight());
                        if (isHit){
                            canvas.drawBitmap(firespaceship,objx,1300,null);
                        }
                        else {
                            canvas.drawBitmap(spaceship, objx, 1300, null);
                        }
                        if (!obstacles.get(index).isHit()){
                           obstacles.get(index).setHit(obst.intersect(hitbox));
                        }
                        if (obst.intersect(hitbox)){
                            soundPool.play(collisionID,100,100,1,0,1);
                        }
                        if (!isHit){
                            isHit=obst.intersect(hitbox);
                        }
                        if (obstacles.get(index).getY()>screenHeight){
                            obstacles.get(index).setY(0);
                            obstacles.get(index).setX();
                            if (obstacles.get(index).isHit()) {
                                hits++;
                            }
                            obstacles.get(index).setHit(false);
                            isHit=false;
                            runs++;
                        }
                    }
                }
                if (!running){
                    canvas.drawRGB(255,0,0);
                    canvas.drawText("GAME OVER",400,200,pointsPaint);
                    canvas.drawText("Score: "+String.valueOf(runs-hits),500,500,pointsPaint);
                }
                holder.unlockCanvasAndPost(canvas);
            }
        }

        public void resume(){
            running=true;
            gameThread=new Thread(this);
            gameThread.start();
            player.start();
        }

        public void pause() {
            running = false;
            player.pause();
            while (true) {
                try {
                    gameThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }


    }//GameSurface
}//Activity
