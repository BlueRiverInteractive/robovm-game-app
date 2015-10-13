package com.robovm.robomission;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

/**
 * Takes a world and renders its state to the screen
 * and via audio output. Also manages all asset, from
 * UI graphics to sound effects.
 */
public class Renderer implements World.WorldCallback {
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final OrthographicCamera worldCamera;
    private final OrthographicCamera uiCamera;

    private final BitmapFont font;
    private final Texture background;
    private final TextureRegion ground;
    private final TextureRegion ceiling;
    private final TextureRegion obstacle;
    private final TextureRegion fuel;
    private final TextureRegion fuelBar;
    private final Color roboGreen = new Color(0x8BBF26FF);
    private final Animation roboUp;
    private final Animation roboDown;
    private final Animation roboDead;
    private final TextureRegion ready;
    private final TextureRegion gameOver;
    private final Music backgroundMusic;
    private final Sound explosion;
    private final Sound fuelPickedUp;
    private final Music thruster;

    // Used for generating an infinitely scrolling
    // world (ground and ceiling)
    private float groundOffsetX = 0;

    public Renderer() {
        // the SpriteBatch is used to render TextureRegions
        batch = new SpriteBatch();

        // the ShapeRenderer to render debug bounds and fuel gauge
        shapeRenderer = new ShapeRenderer();

        // The world camera is used to render objects within
        // the game world. We assume 800x480 world units to
        // be visible on the screen, irrespective of the
        // actual screen resolution
        worldCamera = new OrthographicCamera();
        worldCamera.setToOrtho(false, 800, 480);
        worldCamera.position.x = 400;

        // The UI camera is used to render UI elements
        // to the screen. We use a pixel perfect camera
        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiCamera.update();

        // finally we load all the assets we need
        // the fond used to display the score
        font = new BitmapFont(Gdx.files.internal("arial.fnt"));

        // the static background
        background = new Texture("background.png");
        background.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // the ground and ceiling
        ground = new TextureRegion(new Texture("ground.png"));
        ground.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        ceiling = new TextureRegion(ground);
        ceiling.flip(true, true);

        // the obstacle, and its upside down version
        obstacle = new TextureRegion(new Texture("rock.png"));
        obstacle.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // the fuel cell
        fuel = new TextureRegion(new Texture("fuel-pod.png"));
        fuel.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // the fuel bar
        fuelBar = new TextureRegion(new Texture("fuel-bar-background.png"));
        fuelBar.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // Robo's animations
        roboUp = loadAnimation("robo-up", 3, 0.07f);
        roboUp.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
        roboDown = loadAnimation("robo-down", 3, 0.1f);
        roboDown.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
        roboDead = loadAnimation("robo-dead", 3, 0.07f);
        roboDead.setPlayMode(Animation.PlayMode.LOOP);

        // The ready label
        ready = new TextureRegion(new Texture("ready.png"));
        ready.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // The game over label
        gameOver = new TextureRegion(new Texture("gameover.png"));
        gameOver.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // The background music, we immediately start playing it
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        backgroundMusic.setVolume(0.70f);
        backgroundMusic.setLooping(true);
        backgroundMusic.play();

        // The explosion sound when Robo hits an obstacle
        explosion = Gdx.audio.newSound(Gdx.files.internal("explode.wav"));

        // The pick up sound when Robo hits a fuel cell
        fuelPickedUp = Gdx.audio.newSound(Gdx.files.internal("fuel.wav"));

        // Thruster, we use a Music instance because sound effect looping
        // does not work on some Android devices
        thruster = Gdx.audio.newMusic(Gdx.files.internal("thruster.wav"));
    }

    /**
     * Loads an animation given a prefix and number of frames
     * @param prefix the prefix, e.g. robo-down
     * @param numFrames the number of frames
     * @param frameTime the duration of a frame
     * @return the animation
     */
    private Animation loadAnimation(String prefix, int numFrames, float frameTime) {
        Array<TextureRegion> frames = new Array<TextureRegion>();
        for(int i = 0; i < numFrames; i++) {
            Texture texture = new Texture(prefix + "-" + (i+1) + ".png");
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            frames.add(new TextureRegion(texture));
        }
        return new Animation(frameTime, frames);
    }

    /**
     * Renders the game world and the overlayed UI based on the
     * {@link World}.
     */
    public void render(World world) {

        // Update the camera based on Robo's position
        worldCamera.position.x = world.getRobo().getPosition().x + 350;

        // If the camera has moved far enough for the ground tiles to
        // disappear off-screen, updated the groundOffset
        if (worldCamera.position.x - groundOffsetX > ground.getRegionWidth() + 400) {
            groundOffsetX += ground.getRegionWidth();
        }

        // Update thew orld camera matrices and set them on the batch
        worldCamera.update();
        batch.setProjectionMatrix(worldCamera.combined);

        // draw the background
        batch.begin();
        batch.draw(background, worldCamera.position.x - background.getWidth() / 2, 0);

        // Draw the obstacles
        for (Obstacle o : world.getObstacles()) {
            batch.draw(obstacle, o.getPosition().x, o.getPosition().y, World.OBSTACLE_WIDTH / 2, World.OBSTACLE_HEIGHT / 2, World.OBSTACLE_WIDTH, World.OBSTACLE_HEIGHT, 1, 1, o.getRotation());
        }

        // Draw the fuel cell
        batch.draw(fuel, world.getFuel().getPosition().x, world.getFuel().getPosition().y, World.FUEL_WIDTH, World.FUEL_HEIGHT);

        // Draw repeating ground and ceiling
        batch.draw(ground, groundOffsetX, 0);
        batch.draw(ground, groundOffsetX + ground.getRegionWidth(), 0);
        batch.draw(ceiling, groundOffsetX, 480 - ceiling.getRegionHeight());
        batch.draw(ceiling, groundOffsetX + ceiling.getRegionWidth(), 480 - ceiling.getRegionHeight());

        // Draw Robo's animation, based on the time he's been flying so far.
        Animation anim = null;
        if(world.getState() == World.WorldState.Ready) {
            anim = roboDown;
        } else if (world.getState() == World.WorldState.GameOver) {
            anim = roboDead;
        } else if (world.getState() == World.WorldState.Playing) {
            if(world.getRobo().getFuel() == 0) {
                anim = roboDead;
            } else if(world.getRobo().getVelocity().y > 0) {
                anim = roboUp;
            } else {
                anim = roboDown;
            }
        }
        batch.draw(anim.getKeyFrame(world.getRobo().getStateTime()), world.getRobo().getPosition().x, world.getRobo().getPosition().y, World.ROBO_WIDTH, World.ROBO_HEIGHT);
        batch.end();

        // Draw the UI elements based on the world state
        // using the UI camera for pixel perfect rendering
        batch.setProjectionMatrix(uiCamera.combined);
        if (world.getState() == World.WorldState.Ready) {
            groundOffsetX = 0;
            batch.begin();
            batch.draw(ready, Gdx.graphics.getWidth() / 2 - ready.getRegionWidth() / 2, Gdx.graphics.getHeight() / 2 - ready.getRegionHeight() / 2);
            batch.end();
        }
        if (world.getState() == World.WorldState.GameOver) {
            batch.begin();
            batch.draw(gameOver, Gdx.graphics.getWidth() / 2 - gameOver.getRegionWidth() / 2, Gdx.graphics.getHeight() / 2 - gameOver.getRegionHeight() / 2);
            batch.end();
        }
        if (world.getState() == World.WorldState.Playing || world.getState() == World.WorldState.GameOver) {
            batch.begin();
            font.draw(batch, "" + world.getScore(), Gdx.graphics.getWidth() / 2 - 20, Gdx.graphics.getHeight() - 60);
            batch.draw(fuelBar, Gdx.graphics.getWidth() - 10 - fuelBar.getRegionWidth(), Gdx.graphics.getHeight() / 2 - fuelBar.getRegionHeight() / 2);
            batch.end();
            shapeRenderer.setProjectionMatrix(uiCamera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(roboGreen);
            shapeRenderer.rect(Gdx.graphics.getWidth() - 10 - fuelBar.getRegionWidth() + 6, Gdx.graphics.getHeight() / 2 - fuelBar.getRegionHeight() / 2 + 18, 19, world.getRobo().getFuel() / 100f * 309);
            shapeRenderer.end();
        }

        // renderDebug(world);
    }

    /**
     * Renderes the bounds of all objects for debugging
     * @param world
     */
    private void renderDebug(World world) {
        Rectangle roboBounds = world.getRoboBounds();
        shapeRenderer.setProjectionMatrix(worldCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.rect(roboBounds.x, roboBounds.y, roboBounds.width, roboBounds.height);
        for(Obstacle o: world.getObstacles()) {
            Rectangle obstacleBounds = world.getObstacleBounds(o);
            shapeRenderer.rect(obstacleBounds.x, obstacleBounds.y, obstacleBounds.width, obstacleBounds.height);
        }
        shapeRenderer.end();
    }

    @Override
    public void hitObstacle() {
        explosion.play();
    }

    @Override
    public void hitFuel() {
        fuelPickedUp.play();
    }

    @Override
    public void outOfFuel() {
        explosion.play();
    }

    @Override
    public void boosting() {
        if(!thruster.isPlaying()) {
            thruster.play();
        }
    }

    @Override
    public void boostingOff() {
        thruster.stop();
    }

    public void resize(int width, int height) {
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiCamera.update();
    }
}
