package com.robovm.robomission;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * Represents an obstacle on screen. An obstacle has
 * a position and rectangular bounding box around it
 * which is used for collision detection with Robo.
 */
public class Obstacle {
    private final Vector2 position = new Vector2();
    private boolean counted;
    private float rotation;

    public Obstacle() {
        this.rotation = MathUtils.random(360);
    }

    /**
     * @return the position of the bottom left corner of the obstacle
     */
    public Vector2 getPosition() {
        return position;
    }

    /**
     * @return whether the obstacle has been passed by Robo and counted against the score
     */
    public boolean isCounted() {
        return counted;
    }

    /**
     * @param counted whether the obstacle has been passed by Robo and counted against the score
     */
    public void setCounted(boolean counted) {
        this.counted = counted;
    }

    /**
     * @return the rotation angle in degrees
     */
    public float getRotation() {
        return rotation;
    }

    /**
     * @param rotation the rotation angle in degrees
     */
    public void setRotation(float rotation) {
        this.rotation = rotation;
    }
}
