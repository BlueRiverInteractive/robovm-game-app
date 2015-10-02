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
    public static final float GRAVITY = -15;
    public static final float FUEL_BURN_PER_SECOND = 25;
    public static final float ROBO_VELOCITY_X = 200;
    public static final float ROBO_START_Y = 240;
    public static final float ROBO_START_X = 50;
    public static final float ROBO_JUMP_IMPULSE = 280;
    public static final float ROBO_WIDTH = 100 / 2;
    public static final float ROBO_HEIGHT = 200 / 2;
    public static final float OBSTACLE_WIDTH = 180 / 2;
    public static final float OBSTACLE_HEIGHT = 219 / 2;
    public static final float OBSTACLE_DISTANCE = 250;
    public static final float FUEL_WIDTH = 100 / 2;
    public static final float FUEL_HEIGHT = 100 / 2;

    // Callback invoked to inform outside of events in the world
    private final WorldCallback callback;

    // The objects in the world
    private Robo robo;
    private final Array<Obstacle> obstacles = new Array<Obstacle>();
    private Fuel fuel;

    // The game state
    private WorldState state = WorldState.Ready;
    private final Vector2 gravity = new Vector2();
    private int score = 0;

    // Scratch rectangles used for collision detection
    // We keep them as instance fields to not generate
    // garbage every time we update the world!
    private final Rectangle rect1 = new Rectangle();
    private final Rectangle rect2 = new Rectangle();


    public World(WorldCallback callback) {
        this.callback = callback;
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

        // Setup a fuel instance
        fuel = new Fuel();
        fuel.getPosition().set(800 + OBSTACLE_DISTANCE / 2, MathUtils.random(40, 440));

        // Create initial obstacles
        obstacles.clear();
        for (int i = 0; i < 5; i++) {
            boolean isDown = MathUtils.randomBoolean();
            Obstacle o = new Obstacle();
            o.getPosition().set(800 + i * OBSTACLE_DISTANCE, MathUtils.random(0, 480 - OBSTACLE_HEIGHT));
            obstacles.add(o);
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

        // Check if the user tapped the screen. Depending on the
        // game state, perform an action
        if (Gdx.input.justTouched()) {
            // We are at the start screen, start playing!
            if (state == WorldState.Ready) {
                state = WorldState.Playing;
            }

            // We are in the game over state, start a new game!
            if (state == WorldState.GameOver) {
                state = WorldState.Ready;
                resetWorld();
            }
        }

        // If the user is holding down a finger on screen, add
        // upward velocity to Robo and decrease the fuel
        if(state == WorldState.Playing && Gdx.input.isTouched()) {
            if (state == state.Playing && robo.getFuel() > 0) {
                robo.getVelocity().set(ROBO_VELOCITY_X, ROBO_JUMP_IMPULSE);
                robo.removeFuel(FUEL_BURN_PER_SECOND * Gdx.graphics.getDeltaTime());
                callback.boosting();
            }
        } else {
            callback.boostingOff();
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
        Rectangle roboBounds = getRoboBounds();
        for (Obstacle o : obstacles) {

            // Check if the obstacles is outside the visible screen
            // area. If that is the case, reposition it at the
            // right side of the screen!
            if (robo.getPosition().x + 350 - o.getPosition().x > 400 + OBSTACLE_WIDTH) {
                boolean isDown = MathUtils.randomBoolean();
                reposition(o);
                o.setCounted(false);
                o.setRotation(MathUtils.random(360));
            }

            Rectangle obstacleBounds = getObstacleBounds(o);

            // Check if we collide with the current obstacle
            // and change to the game over state in that case
            if (roboBounds.overlaps(obstacleBounds)) {
                if (state != WorldState.GameOver) {
                    callback.hitObstacle();
                }
                state = WorldState.GameOver;
                robo.getVelocity().x = 0;
            }

            // If Robo passed the obstacle, increase the score
            if (o.getPosition().x < robo.getPosition().x && !o.isCounted()) {
                score++;
                o.setCounted(true);
            }

            o.setRotation(o.getRotation() + Gdx.graphics.getDeltaTime() * 20);
        }

        // Check if Robo hit a fuel cell
        if(roboBounds.overlaps(getFuelBounds(fuel))) {
            callback.hitFuel();
            robo.addFuel(100);
            reposition(fuel);
        }

        // Check if the fuel left the screen and reposition
        if(robo.getPosition().x + 350 - fuel.getPosition().x > 400 + FUEL_WIDTH) {
            reposition(fuel);
        }

        // Check if Robo has fuel left
        if(robo.getFuel() <= 0) {
            if(state != WorldState.GameOver) {
                callback.outOfFuel();
            }
            state = WorldState.GameOver;
        }

        // Check if Robo hit the ground or ceiling
        if (robo.getPosition().y < - ROBO_HEIGHT / 2 ||
                robo.getPosition().y + ROBO_HEIGHT / 2 > 480) {
            if (state != WorldState.GameOver) {
                callback.hitObstacle();
            }
            state = WorldState.GameOver;
            robo.getVelocity().x = 0;
        }
    }

    /**
     * Repositions an Obstacle to the right side of the screen
     * @param o the obstacle
     */
    private void reposition(Obstacle o) {
        o.getPosition().x += obstacles.size * OBSTACLE_DISTANCE;
        o.getPosition().y = MathUtils.random(OBSTACLE_HEIGHT, 480 - OBSTACLE_HEIGHT);
    }

    /**
     * Repositions the fuel to the right side of the screen
     * @param f the fuel
     */
    private void reposition(Fuel f) {
        f.getPosition().x += obstacles.size * OBSTACLE_DISTANCE;
        f.getPosition().y = MathUtils.random(OBSTACLE_HEIGHT, 480 - OBSTACLE_HEIGHT);
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

    /**
     * @return the current fuel cell
     */
    public Fuel getFuel() {
        return fuel;
    }

    /**
     * @return the bounds of Robo, always returns the same instance
     */
    public Rectangle getRoboBounds() {
        rect1.set(robo.getPosition().x + 10, robo.getPosition().y + 35, ROBO_WIDTH - 20, ROBO_HEIGHT - 50);
        return rect1;
    }

    /**
     * @param o the obstacle
     * @return the bounds of the obstacle, always returns the same instance
     */
    public Rectangle getObstacleBounds(Obstacle o) {
        rect2.set(o.getPosition().x + 15, o.getPosition().y + 20, OBSTACLE_WIDTH - 30, OBSTACLE_HEIGHT - 40);
        return rect2;
    }

    public Rectangle getFuelBounds(Fuel fuel) {
        rect2.set(fuel.getPosition().x, fuel.getPosition().y, FUEL_WIDTH, FUEL_HEIGHT);
        return rect2;
    }

    public interface WorldCallback {
        void hitObstacle();
        void hitFuel();
        void outOfFuel();
        void boosting();
        void boostingOff();
    }
}
