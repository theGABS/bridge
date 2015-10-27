package com.knotri.bridge;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Created by k on 20.10.15.
 */
public class Enemy {
    public float x,y, dx, dy;
    public Texture myTexture;
    public boolean down = false;
    public Enemy(){
        x = 100;
        y = 405;
    }

    public void update(float delta){
        dx = (float) (4.5 + Math.random()*1.5f) * 60 * delta;

        if(down){
            dx = 0;
            dy -= (0.3f + Math.random()*0.3f) * 60 * delta;
        }

        x += dx;
        y += dy;
    }

    public void draw(SpriteBatch batch){
        batch.draw(myTexture, x - myTexture.getWidth()/2, y);
    }



}
