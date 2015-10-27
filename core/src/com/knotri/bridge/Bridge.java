package com.knotri.bridge;

/**
 * Created by k on 20.10.15.
 */
public class Bridge {
    public float width;
    public float x;
    public float y;
    public boolean haveBomb = false;
    public float timeToBoom = 1000;
    public boolean broken = false;
    public Bridge(){
        y = 357;
    }

    public void mining(){
        haveBomb = true;
        timeToBoom = 1000;
    }

    public void update(float dt){
        if(haveBomb) {
            timeToBoom -= dt;
        }
        if(timeToBoom < 0){
            y -= 2;
            if(broken == false){
                broken = true;
                ParticleManage.activeEffect("rockBoom", x + 45 + width/2, y + 20);
            }
            haveBomb = false;
        }
    }
}
