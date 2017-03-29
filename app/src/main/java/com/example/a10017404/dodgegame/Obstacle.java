package com.example.a10017404.dodgegame;

/**
 * Created by 10017404 on 3/27/2017.
 */

public class Obstacle {
    int x = (int)(Math.random()*1250);
    int y = 0;
    boolean hit;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(){
        x = (int)(Math.random()*1250);
    }

    public void setY(int newy){
        y=newy;
    }

    public boolean isHit(){
        return hit;
    }

    public void setHit(boolean bool){
        hit=bool;
    }
}


