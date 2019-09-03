package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.Resources;
import com.mygdx.game.World.Element;
import com.mygdx.game.World.World;

import java.util.Iterator;

public class GameScreen extends DefaultScreen {

    private static final int DIR_STOP  = 0;
    private static final int DIR_LEFT  = 1;
    private static final int DIR_UP    = 2;
    private static final int DIR_RIGHT = 3;
    private static final int DIR_DOWN  = 4;

    private static final int VIRTUAL_WIDTH = 800;
    private static final int VIRTUAL_HEIGHT = 400;
    private static final float ASPECT_RATIO =
        (float)VIRTUAL_WIDTH/(float)VIRTUAL_HEIGHT;

    private Resources   r;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private int direction = 0;
    private World world;
    private Element player;
    private int screenspeed = 120;

    public GameScreen(MyGdxGame game) {
        super(game);

        r = new Resources();

        world = new World();
        world.setStage( r.getStage() );
        world.start();
        player = world.getStage().getPlayer();
        if(player == null) System.out.println("player is left null");

        //android 1.7
        // pc  0.6
        float dpi = 160 * Gdx.graphics.getDensity();
        System.out.println ("DPI: " + Gdx.graphics.getDensity());
        // setup the camera
        camera = new OrthographicCamera();
        camera.setToOrtho(
        false,
              VIRTUAL_WIDTH,
                VIRTUAL_HEIGHT
            );
        batch = new SpriteBatch();
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void input() {
        // process user input
        direction = DIR_STOP;

        // TODO: change direction for flags to do diagonal movement
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT))  direction = DIR_LEFT;
        if(Gdx.input.isKeyPressed(Input.Keys.UP))    direction = DIR_UP;
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) direction = DIR_RIGHT;
        if(Gdx.input.isKeyPressed(Input.Keys.DOWN))  direction = DIR_DOWN;

        if(Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);

            float x = (player.x+32) - touchPos.x ;
            float y = (player.y+32) - touchPos.y ;
            float slope = y / x;

            // if cursor distance is bigger than threshold 32
            if(Math.abs(x) > 32 || Math.abs(y) > 32) {
                if (Math.abs(slope) < 1 ) {
                    if (Math.signum(x) > 0) { // to the left
                        direction = DIR_LEFT;
                    } else if (Math.signum(x) < 0) {
                        direction = DIR_RIGHT; //right)
                    } // if math.signum
                } else { // abs(slope) > 1
                    if (Math.signum(y) < 0) { // up
                        direction = DIR_UP;
                    } else if (Math.signum(y) > 0) {
                        direction = DIR_DOWN; // down)
                    } // if math.signum
                }
            } // if(mouse distance to sprite pos > 32

        }

        int act = player.anim.anim_action;
        switch (direction) {
            case DIR_STOP: {
                if (act == 2 || act == 1) {
                    player.anim.anim_action = 3;
                    player.anim.setTime(0);
                }
                break;
            }
            case DIR_LEFT: {
                player.x -= screenspeed * Gdx.graphics.getDeltaTime();
                player.anim.frameindex = 0;
                if(act == 0) {
                    player.anim.anim_action = 1;
                    player.anim.setTime(0);
                }
                break;
            }

            case DIR_RIGHT: {
                player.x += screenspeed * Gdx.graphics.getDeltaTime();
                player.anim.frameindex = 2;
                if(act == 0) {
                    player.anim.anim_action = 1;
                    player.anim.setTime(0);
                }
                break;
            }
            case DIR_UP: {
                player.y += screenspeed * Gdx.graphics.getDeltaTime();
                player.anim.frameindex = 1;
                if(act == 0) {
                    player.anim.anim_action = 1;
                    player.anim.setTime(0);
                }
                break;
            }
            case DIR_DOWN: {
                player.y -= screenspeed * Gdx.graphics.getDeltaTime();
                player.anim.frameindex = 3;
                if(act == 0) {
                    player.anim.anim_action = 1;
                    player.anim.setTime(0);
                }
                break;
            }
        } //switch (direction)


        camera.position.x = player.x;
        camera.position.y = player.y;
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        this.input();

        //camera.translate(0,0.1f,0);
        // tell the camera to update its matrices.
        camera.update();
        world.update(delta);

        // tell the SpriteBatch to render in the
        // coordinate system specified by the camera.
        batch.setProjectionMatrix(camera.combined);


        world.present(batch);


    }

    @Override
    public void dispose() {
        r.dispose();
        batch.dispose();
        super.dispose();
    }
}
