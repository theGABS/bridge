package com.knotri.bridge;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;

/**
 * Created by k on 26.10.15.
 */
public class ParticleManage {
    static Array<ParticleEffectPool.PooledEffect> effects = new Array();

    static HashMap<String, ParticleEffect> particleEffects = new HashMap<String , ParticleEffect>();
    static HashMap<String, ParticleEffectPool> particleEffectsPool = new HashMap<String, ParticleEffectPool>();


    public static void addParticle(String name, ParticleEffect particleEffect){
        particleEffects.put(name, particleEffect);
        particleEffectsPool.put(name, new ParticleEffectPool(particleEffect,1,2));
    }

    public static void activeEffect(String name, float x, float y){
        ParticleEffectPool.PooledEffect effect = particleEffectsPool.get(name).obtain();
        effect.setPosition(x, y);
        effects.add(effect);
    }

    public static void draw(SpriteBatch batch, float delta){
        for (int i = effects.size - 1; i >= 0; i--) {
            ParticleEffectPool.PooledEffect effect = effects.get(i);
            effect.draw(batch, delta);
            if (effect.isComplete()) {
                effect.free();
                effects.removeIndex(i);
            }
        }
    }
}
