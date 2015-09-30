package com.robovm.robomission;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * The World keeps track of all objects in the game. It
 * is also responsible for updating the objects, e.g.
 * move Robo based on his velocity as well as the
 * gravity applied to him.
 */
public class World {
    // Enum encoding the game state.
    public enum WorldState {
        Ready,
        Playing,
        GameOver
    }

    // A couple of constants used to move Robo
    // in a physically appealing way
    private static final float GRAVITY = -20;
    private static final float ROBO_VELOCITY_X = 200;
    private static final float ROBO_START_Y = 240;
    private static final float ROBO_START_X = 50;
    private static final float ROBO_JUMP_IMPULSE = 350;
    private static final float ROBO_WIDTH = 88 - 20;
    private static final float ROBO_HEIGHT = 73;
    private static final float OBSTACLE_WIDTH = 108;
    private static final float OBSTACLE_HEIGHT = 239;
    private static final float GROUND_HEIGHT = 71;

    // The objects in the world
    private Robo robo;
    private final Array<Obstacle> obstacles = new Array<Obstacle>();

    // The game state
    private WorldState state = WorldState.Ready;
    private final Vector2 gravity = new Vector2();
    private int score = 0;

    // Scratch rectangles used for collision detection
    // We keep them as instance fields to not generate
    // garbage every time we update the world!
    private final Rectangle rect1 = new Rectangle();
    private final Rectangle rect2 = new Rectangle();


    public World() {
        resetWorld();
    }

    /**
     * Resets the World to its initial state
     */
    private void resetWorld() {
        // Reset score
        score = 0;

        // Define gravity
        gravity.set(0, GRAVITY);

        // Setup Robo's starting position
        robo = new Robo(ROBO_START_X, ROBO_START_Y);

        // Create initial obstacles
        obstacles.clear();
        for (int i = 0; i < 5; i++) {
            boolean isDown = MathUtils.randomBoolean();
            obstacles.add(new Obstacle(700 + i * 200, isDown ? 480 - OBSTACLE_HEIGHT : 0));
        }
    }

    /**
     * Update the game world based on user input
     */
    public void update() {
        // calculate the number of seconds Robo has been flying so far.
        // This is used by the Renderer to pick the proper animation
        // frame for Robo.
        float deltaTime = Gdx.graphics.getDeltaTime();
        robo.increaseStateTime(deltaTime);

        // Check if the user touched the screen. Depending on the
        // game state, perform an action
        if (Gdx.input.justTouched()) {
            // We are at the start screen, start playing!
            if (state == WorldState.Ready) {
                state = WorldState.Playing;
            }
            // We are already playing, add upward velocity to Robo
            if (state == state.Playing) {
                robo.getVelocity().set(ROBO_VELOCITY_X, ROBO_JUMP_IMPULSE);
            }

            // We are in the game over state, start a new game!
            if (state == WorldState.GameOver) {
                state = WorldState.Ready;
                resetWorld();
            }
        }

        // If we are playing, apply gravity to Robo
        if (state != WorldState.Ready) {
            robo.getVelocity().add(gravity);
        }

        // integrate Robo's velocity over time
        robo.getPosition().mulAdd(robo.getVelocity(), deltaTime);


        // Check for collisions between Robo and the obstacles.
        // We put a rectangle around both Robo and an obstacle,
        // then check for overlap between the two rectangles.
        rect1.set(robo.getPosition().x + 20, robo.getPosition().y, ROBO_WIDTH, ROBO_HEIGHT);
        for (Obstacle o : obstacles) {

            // Check if the obstacles is outside the visible screen
            // area. If that is the case, reposition it at the
            // right side of the screen!
            if (robo.getPosition().x + 350 - o.getPosition().x > 400 + OBSTACLE_WIDTH) {
                boolean isDown = MathUtils.randomBoolean();
                o.getPosition().x += 5 * 200;
                o.getPosition().y = isDown ? 480 - OBSTACLE_HEIGHT : 0;
                o.setCounted(false);
            }

            rect2.set(o.getPosition().x + (OBSTACLE_WIDTH - 30) / 2 + 20, o.getPosition().y, 20, OBSTACLE_HEIGHT - 10);

            // Check if we collide with the current obstacle
            // and change to the game over state in that case
            if (rect1.overlaps(rect2)) {
                if (state != WorldState.GameOver) {
                    // explode.play();
                }
                state = WorldState.GameOver;
                robo.getVelocity().x = 0;
            }

            // If Robo passed the obstacle, increase the score
            if (o.getPosition().x < robo.getPosition().x && !o.isCounted()) {
                score++;
                o.setCounted(true);
            }
        }

        // Check if Robo hit the ground or ceiling
        if (robo.getPosition().y < GROUND_HEIGHT - 20 ||
                robo.getPosition().y + ROBO_HEIGHT > 480 - GROUND_HEIGHT + 20) {
            if (state != WorldState.GameOver) {
                // explode.play();
            }
            state = WorldState.GameOver;
            robo.getVelocity().x = 0;
        }
    }

    /**
     * @return Robo!
     */
    public Robo getRobo() {
        return robo;
    }

    /**
     * @return the obstacles
     */
    public Array<Obstacle> getObstacles() {
        return obstacles;
    }

    /**
     * @return the current game state
     */
    public WorldState getState() {
        return state;
    }

    /**
     * @return the current score, that is obstacles cleared by Robo
     */
    public int getScore() {
        return score;
    }
}
