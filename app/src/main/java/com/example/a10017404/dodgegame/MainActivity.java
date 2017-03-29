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
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    //Code from this program has been used from "Beginning Android Games" by Mario Zechner
    //Review SurfaceView, Canvas, continue

    GameSurface gameSurface;
    int objx=100;
    ArrayList<Obstacle> obstacles;
    int hits=0;
    int runs=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameSurface = new GameSurface(this);
        setContentView(gameSurface);
        obstacles = new ArrayList<>();
        obstacles.add(new Obstacle());
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                obstacles.add(new Obstacle());
            }
        },5000);
        SensorManager manager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        Sensor mysensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        manager.registerListener(this,mysensor,SensorManager.SENSOR_DELAY_FASTEST);
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
        Bitmap myImage;
        Paint paintProperty;

        int screenWidth;
        int screenHeight;

        public GameSurface(Context context) {
            super(context);

            holder=getHolder();

            myImage = BitmapFactory.decodeResource(getResources(),R.drawable.spaceship);

            Display screenDisplay = getWindowManager().getDefaultDisplay();
            Point sizeOfScreen = new Point();
            screenDisplay.getSize(sizeOfScreen);
            screenWidth=sizeOfScreen.x;
            screenHeight=sizeOfScreen.y;

            paintProperty= new Paint();
            paintProperty.setTextSize(100);

        }

        @Override
        public void run() {
            while (running){

                if (holder.getSurface().isValid() == false)
                    continue;

                final Canvas canvas= holder.lockCanvas();
                canvas.drawRGB(255,0,0);
                canvas.drawBitmap(myImage,objx,1300,null);
                final Paint myPaint = new Paint();
                myPaint.setColor(Color.rgb(0, 255, 0));
                myPaint.setStrokeWidth(10);
                Paint pointsPaint = new Paint();
                pointsPaint.setTextSize(200);
                canvas.drawText(String.valueOf(runs-hits),800,500,pointsPaint);
                if (obstacles.size()>0) {
                    for (int index = 0; index < obstacles.size(); index++) {
                        obstacles.get(index).setY(obstacles.get(index).getY()+10);
                        Rect obst = new Rect(obstacles.get(index).getX(), obstacles.get(index).getY(), obstacles.get(index).getX() + 200, obstacles.get(index).getY() + 300);
                        canvas.drawRect(obst, myPaint);
                        Rect hitbox = new Rect(objx, 1300, objx + myImage.getWidth(), 1300 + myImage.getHeight());
                        if (!obstacles.get(index).isHit()){
                           obstacles.get(index).setHit(obst.intersect(hitbox));
                        }
                        if (obstacles.get(index).getY()>screenHeight){
                            obstacles.get(index).setY(0);
                            obstacles.get(index).setX();
                            if (obstacles.get(index).isHit()) {
                                hits++;
                            }
                            obstacles.get(index).setHit(false);
                            runs++;
                        }

                    }
                }
                holder.unlockCanvasAndPost(canvas);
            }
        }

        public void resume(){
            running=true;
            gameThread=new Thread(this);
            gameThread.start();
        }

        public void pause() {
            running = false;
            while (true) {
                try {
                    gameThread.join();
                } catch (InterruptedException e) {
                }
            }
        }


    }//GameSurface
}//Activity
