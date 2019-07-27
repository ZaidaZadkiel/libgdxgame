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

    private static final int DIR_LEFT  = 1;
    private static final int DIR_UP    = 2;
    private static final int DIR_RIGHT = 3;
    private static final int DIR_DOWN  = 4;

    private Resources   r;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private int direction = 0;
    private World world;
    private Element player;

    public GameScreen(MyGdxGame game) {
        super(game);

        r = new Resources();

        world = new World();
        world.setStage( r.getStage() );
        world.start();
        player = world.getStage().getPlayer();
        if(player == null) System.out.println("player is left null");

        // setup the camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        batch = new SpriteBatch();
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void input() {
        // process user input
        direction = 0;

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

        switch (direction) {
            case DIR_LEFT: {
                player.x -= 100 * Gdx.graphics.getDeltaTime();
                player.anim.frameindex = 1;
                //spritesheet.setRegion(currframe * 64, 64 , 64, 64);
                break;
            }

            case DIR_RIGHT: {
                player.x += 100 * Gdx.graphics.getDeltaTime();
                player.anim.frameindex = 3;
                //spritesheet.setRegion(currframe * 64, 64*3, 64, 64);
                break;
            }

            case DIR_UP: {
                player.y += 100 * Gdx.graphics.getDeltaTime();
                player.anim.frameindex = 2;
                //spritesheet.setRegion(currframe * 64, 64 * 2, 64, 64);
                break;
            }
            case DIR_DOWN: {
                player.y -= 100 * Gdx.graphics.getDeltaTime();
                player.anim.frameindex = 0;
                //spritesheet.setRegion(currframe * 64, 0, 64, 64);
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

        // clear the screen with a dark blue color. The
        // arguments to glClearColor are the red, green
        // blue and alpha component in the range [0,1]
        // of the color to be used to clear the screen.
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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
