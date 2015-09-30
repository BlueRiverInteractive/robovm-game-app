package com.robovm.robomission;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/**
 * Represents an obstacle on screen. An obstacle has
 * a position and rectangular bounding box around it
 * which is used for collision detection with Robo.
 */
public class Obstacle {
    private final Vector2 position = new Vector2();
    private boolean counted;

    public Obstacle(float x, float y) {
        this.position.x = x;
        this.position.y = y;
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
}
