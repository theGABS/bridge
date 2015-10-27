package com.knotri.bridge;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Created by k on 20.10.15.
 */
public class Player {
    public float x,y;
    public Texture myTexture;
    public Player(){
        x = 0;
        y = 405;
    }

    public void draw(SpriteBatch batch){
        batch.draw(myTexture, x, y);
    }

    public void update(float delta){
        x += 5 * 60 * delta;
    }
}
