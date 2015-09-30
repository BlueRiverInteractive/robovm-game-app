package com.robovm.robomission;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/**
 * Represents Robo, our main protagonist. Robo
 * has a position and velocity. We also store
 * the amount of time Robo's been flying so far.
 * This is used for animation purposes.
 */
public class Robo {
    private final Vector2 position = new Vector2();
    private final Vector2 velocity = new Vector2();
    private float stateTime = 0;

    public Robo(float x, float y) {
        this.position.set(x, y);
    }

    /**
     * @return the position of the bottom left corner of Robo
     */
    public Vector2 getPosition() {
        return position;
    }

    /**
     * @return the velocity measured in world units per second
     */
    public Vector2 getVelocity() {
        return velocity;
    }

    /**
     * @return the number of seconds Robo has been flying
     */
    public float getStateTime() {
        return stateTime;
    }

    /**
     * Adds the delta time given in seconds to the total
     * state time Robo has been flying.
     * @param delta delta time in seconds
     */
    public void increaseStateTime(float delta) {
        this.stateTime += delta;
    }
}
