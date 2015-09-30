package com.robovm.robomission;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

public class RoboMission extends ApplicationAdapter {
    private World world;
    private Renderer renderer;

    @Override
    public void create() {
        // create our initial world
        world = new World();

        // create our initial renderer
        renderer = new Renderer();
    }

    @Override
    public void render() {
        // clear the screen
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update the world based on user input
        world.update();

        // Render the world and UI
        renderer.render(world);
    }
}