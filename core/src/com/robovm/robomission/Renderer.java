package com.robovm.robomission;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Takes a world and renders its state to the screen
 * and via audio output. Also manages all asset, from
 * UI graphics to sound effects.
 */
public class Renderer {
    private final SpriteBatch batch;
    private final OrthographicCamera worldCamera;
    private final OrthographicCamera uiCamera;

    private final BitmapFont font;
    private final Texture background;
    private final TextureRegion ground;
    private final TextureRegion ceiling;
    private final TextureRegion obstacle;
    private final TextureRegion obstacleUpsideDown;
    private final Animation roboAnimation;
    private final TextureRegion ready;
    private final TextureRegion gameOver;
    private final Music backgroundMusic;
    private final Sound explosion;

    // Used for generating an infinitely scrolling
    // world (ground and ceiling)
    private float groundOffsetX = 0;

    public Renderer() {
        // the SpriteBatch is used to render TextureRegions
        batch = new SpriteBatch();

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

        // the ground and ceiling
        ground = new TextureRegion(new Texture("ground.png"));
        ceiling = new TextureRegion(ground);
        ceiling.flip(true, true);

        // the obstacle, and its upside down version
        obstacle = new TextureRegion(new Texture("rock.png"));
        obstacleUpsideDown = new TextureRegion(obstacle);
        obstacleUpsideDown.flip(false, true);

        // Robo's animation, composed of multiple frames
        Texture frame1 = new Texture("plane1.png");
        frame1.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        Texture frame2 = new Texture("plane2.png");
        Texture frame3 = new Texture("plane3.png");
        roboAnimation = new Animation(0.05f, new TextureRegion(frame1), new TextureRegion(frame2), new TextureRegion(frame3), new TextureRegion(frame2));
        roboAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // The ready label
        ready = new TextureRegion(new Texture("ready.png"));

        // The game over label
        gameOver = new TextureRegion(new Texture("gameover.png"));

        // The background music, we immediately start playing it
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.play();

        // The explosion sound when Robo hits an obstacle
        explosion = Gdx.audio.newSound(Gdx.files.internal("explode.wav"));
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
            batch.draw(o.getPosition().y > 200 ? obstacleUpsideDown : obstacle, o.getPosition().x, o.getPosition().y);
        }

        // Draw repeating ground and ceiling
        batch.draw(ground, groundOffsetX, 0);
        batch.draw(ground, groundOffsetX + ground.getRegionWidth(), 0);
        batch.draw(ceiling, groundOffsetX, 480 - ceiling.getRegionHeight());
        batch.draw(ceiling, groundOffsetX + ceiling.getRegionWidth(), 480 - ceiling.getRegionHeight());

        // Draw Robo's animation, based on the time he's been flying so far.
        batch.draw(roboAnimation.getKeyFrame(world.getRobo().getStateTime()), world.getRobo().getPosition().x, world.getRobo().getPosition().y);
        batch.end();

        // Draw the UI elements based on the world state
        // using the UI camera for pixel perfect rendering
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        if (world.getState() == World.WorldState.Ready) {
            groundOffsetX = 0;
            batch.draw(ready, Gdx.graphics.getWidth() / 2 - ready.getRegionWidth() / 2, Gdx.graphics.getHeight() / 2 - ready.getRegionHeight() / 2);
        }
        if (world.getState() == World.WorldState.GameOver) {
            batch.draw(gameOver, Gdx.graphics.getWidth() / 2 - gameOver.getRegionWidth() / 2, Gdx.graphics.getHeight() / 2 - gameOver.getRegionHeight() / 2);
        }
        if (world.getState() == World.WorldState.Playing || world.getState() == World.WorldState.GameOver) {
            font.draw(batch, "" + world.getScore(), Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() - 60);
        }
        batch.end();
    }
}
