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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.mygdx.game.Eventyr.stage1;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.Resources;
import com.mygdx.game.World.Element;
import com.mygdx.game.World.World;

import java.util.Iterator;

/*
* GameScreen deals with direct gameplay
* Handles game input and settin up and cleaning on the render loop
* most likely sets up sound system stuff
* */

public class GameScreen extends DefaultScreen {

    private static final int DIR_STOP  = 0;
    private static final int DIR_LEFT  = 1;
    private static final int DIR_UP    = 2;
    private static final int DIR_RIGHT = 4;
    private static final int DIR_DOWN  = 8;

    private int movement_flags = 0;

    private static final int VIRTUAL_WIDTH = 800;
    private static final int VIRTUAL_HEIGHT = 400;
    private static final float ASPECT_RATIO =
        (float)VIRTUAL_WIDTH/(float)VIRTUAL_HEIGHT;


    //libGdx
    private SpriteBatch        batch;
    private OrthographicCamera camera;
    private OrthographicCamera overlayCamera;
    private ShapeRenderer      sr;

    //game system
    private World     world;
    private Element   player;
    private Resources r;

    boolean renderActors = false;

    public GameScreen(MyGdxGame game) {
        super(game);

        r = game.resources;

        world = new World(r);
        world.setStage( r.getStage() );
        player = world.getStage().getPlayer();

        if(player == null) System.out.println("player is left null");

        //android 1.7
        // pc  0.6
        float dpi = 160 * Gdx.graphics.getDensity();
        System.out.println ("DPI: " + Gdx.graphics.getDensity());
        // setup the cameras
        camera = new OrthographicCamera();
        camera.setToOrtho(
        false,
              VIRTUAL_WIDTH,
                VIRTUAL_HEIGHT
            );
        overlayCamera = new OrthographicCamera();
        overlayCamera.setToOrtho(
            false,
            VIRTUAL_WIDTH,
            VIRTUAL_HEIGHT
        );

        batch = new SpriteBatch();
        sr    = new ShapeRenderer();

        renderActors = (    world.getStage().getActors() != null
                        &&  world.getStage().getActors().size>0 );
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void input() {
        // process user input
        movement_flags = DIR_STOP;

        // TODO: change direction for flags to do diagonal movement
        /*
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT))  direction = DIR_LEFT;
        if(Gdx.input.isKeyPressed(Input.Keys.UP))    direction = DIR_UP;
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) direction = DIR_RIGHT;
        if(Gdx.input.isKeyPressed(Input.Keys.DOWN))  direction = DIR_DOWN;
        *

        if(Gdx.input.isKeyPressed(Input.Keys.LEFT))  movement_flags |= DIR_LEFT;
        if(Gdx.input.isKeyPressed(Input.Keys.UP))    movement_flags |= DIR_UP;
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) movement_flags |= DIR_RIGHT;
        if(Gdx.input.isKeyPressed(Input.Keys.DOWN))  movement_flags |= DIR_DOWN;

        if((movement_flags & DIR_LEFT)  != 0) player.moveLeft();
        if((movement_flags & DIR_UP)    != 0) player.moveUp();
        if((movement_flags & DIR_DOWN)  != 0) player.moveDown();
        if((movement_flags & DIR_RIGHT) != 0) player.moveRight();
        if(movement_flags == 0)               player.moveStop();
        */

        float newX = 0;
        float newY = 0;
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT))  newX = -player.speed;
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) newX = player.speed;
        if(Gdx.input.isKeyPressed(Input.Keys.UP))    newY = player.speed;
        if(Gdx.input.isKeyPressed(Input.Keys.DOWN))  newY = -player.speed;
        if(Gdx.input.isKeyPressed(Input.Keys.SPACE)) movement_flags |= 1;

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
                        newX = -player.speed; //player.moveLeft(); //direction = DIR_LEFT;
                    } else if (Math.signum(x) < 0) {
                        newX = player.speed; //player.moveRight(); //direction = DIR_RIGHT; //right)
                    } // if math.signum
                } else { // abs(slope) > 1
                    if (Math.signum(y) < 0) { // up
                        newY = player.speed; //player.moveUp(); //direction = DIR_UP;
                    } else if (Math.signum(y) > 0) {
                        newY = -player.speed;//player.moveDown(); //direction = DIR_DOWN; // down)
                    } // if math.signum
                }
            } // if(mouse distance to sprite pos > 32

        }

        world.moveElement(player, newX, newY);

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

        Gdx.gl.glClear( Gdx.gl20.GL_COLOR_BUFFER_BIT | Gdx.gl20.GL_DEPTH_BUFFER_BIT );

        // tell the SpriteBatch to render in the
        // coordinate system specified by the camera.
        batch.setProjectionMatrix(camera.combined);
        world.present(batch);

        batch.setProjectionMatrix(overlayCamera.combined);
        batch.begin();
        r.getFont().draw(batch,"frame: " + player.anim.frame,10,100);
        batch.end();

        sr.setProjectionMatrix(camera.combined);
        sr.begin(ShapeRenderer.ShapeType.Line);

/*
        for(Element el : world.getStage().getProps()) {
            sr.rectLine(
                    el.x,
                    el.y,
                    world.getStage().getPlayer().x,
                    world.getStage().getPlayer().y,
                    1);
        }*/

        if(renderActors==true){
            for(Element el : world.getStage().getActors() ) {
                sr.rect(el.x, el.y, el.width, el.height);
            }
        }

        Element el;
        el = world.getStage().getPlayer();

        //z is the radius length
        //sr.circle(el.getBounds().x, el.getBounds().y, el.getBounds().z);

        //if(world.collP != null){
            sr.circle(world.collX, world.collY, 10);
           // sr.line(world.collX, world.collY, world.collP.getX(), world.collP.getY());
        //}

        //for(Polygon p : world.boundaries){
        //    sr.polygon(p.getVertices());

            /*
            float[] points = p.getVertices();
            float x = points[0];
            float y = points[1];
            for(int i = 2; i != points.length; i=i+2){
                sr.rectLine(
                    points[i],
                    points[i+1],
                    x,
                    y,
                        1
                );
                x = points[i];
                y = points[i+1];
            }
            sr.rectLine(
                    points[0],
                    points[1],
                    points[points.length-2],
                    points[points.length-1],
                    1
            );
*/
        //}
        sr.end();

    }

    @Override
    public void dispose() {
        r.dispose();
        batch.dispose();
        super.dispose();
    }
}
