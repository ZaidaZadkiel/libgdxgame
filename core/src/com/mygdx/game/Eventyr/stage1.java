/*
 *
 *  * Created by ZaidaZadkiel on 7/13/19 12:16 PM
 *  * Copyright (c) 2019 . All rights reserved.
 *  * Last modified 7/13/19 12:16 PM
 */

package com.mygdx.game.Eventyr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.mygdx.game.Resources;
import com.mygdx.game.World.Element;
import com.mygdx.game.World.Stage;

import java.util.Arrays;
import java.util.Iterator;

public class stage1 extends Stage {

    public String getFile(){return "data/scene1/stack.xml";}

    FileHandle vertexshader;
    FileHandle fragmentshader;

    ShaderProgram shader;

    private Sound     dropSound;
    private Resources resources;

    private Texture texture, texture2;

    private Element props[]; //holds only props
    private Element actors[]; //holds only npcs
    private Element everything[];
    private Element scenebg; //fixed background most

    private Polygon boundaries;
    Element player;
    Element npc1, npc2, npc3, npc4, npc5, npc6;

    public stage1(Resources resources) {
        this.resources = resources;
        if(resources.ready == false){
            System.out.println("ERROR! resources not ready");
        }

//        Sprite s = resources.getSpritesImage();
//        player = new Element(800/2-32, 400/2-32, 64, 64,19, s);

        /* scene images */
        //scene = resources.loadxml(getFile());
        //TODO: hax, echanges the background for the player in order to add it to the sorting list

        //scene[7] = player;

        //s =  resources.getSpritesImage();
        //npc1   = new Element( 100, 100, 64, 64, 19, s);
        //npc2   = new Element( 200, 200, 64, 64, 19, s);
        //npc3   = new Element( 300, 300, 64, 64, 19, s);
        //npc4   = new Element( 100, 100, 64, 64, 19, s);
        //npc5   = new Element( 200, 200, 64, 64, 19, s);
        //npc6   = new Element( 300, 300, 64, 64, 19, s);

        texture  = new Texture(Gdx.files.internal("data/water.png"));
        texture2 = new Texture(Gdx.files.internal("data/waterdisplacement.png"));

        // keep local reference copy
        dropSound       = resources.getDropSound();

        vertexshader   = resources.getshader(1);
        fragmentshader = resources.getshader(3);

        if(vertexshader == null || fragmentshader == null) System.out.println("Error leyendo vertex o fragment");

        ShaderProgram.pedantic = false;
        shader = new ShaderProgram(vertexshader, fragmentshader);
        if(shader.isCompiled() == false ) System.out.print(shader.getLog() );

        //actors = new Element[] {
        //    npc1, npc2, npc3,
        //    npc4, npc5, npc6
        //};

        // start the playback of the background music immediately
        resources.getRainMusic().setLooping(true);
        resources.getRainMusic().play();

    }

    @Override
    public void setReady(){
        //TODO: hax: leaves props[0] as the image fixed on the bottom of everything
        scenebg = props[7];
        props[7] = player;
        //creates a new array with both props and actors in it

        everything = Arrays.copyOfRange(props, 0,props.length + actors.length ); //-1 because we remove scenebg
        //doc: System.arraycopy(second, 0, result, first.length, second.length);
        System.arraycopy(actors,0, everything, props.length, actors.length);
        //everything[everything.length-1] = player;
    }

    private Element[] sort(){
        if(false){
            for(Element e : everything)  System.out.println("everything.y: " + (e != null ? e.getY() : "null"));
        }


/* sorting original sauce:
     private static void sort(int nos[], int n) {
     for (int i = 1; i < n; i++){
          int j = i;
          int B = nos[i];
          while ((j > 0) && (nos[j-1] > B)){
            nos[j] = nos[j-1];
            j--;
          }
          nos[j] = B;
        }
    }
* */
        for( int i = 0; i != everything.length; i++){
            int j = i;
            Element b = everything[i];
            while((j>0) && everything[j-1].getY() < b.getY()){
                everything[j] = everything[j-1];
               j--;
            } // while
            everything[j] = b;
        } // for

        if(false){
            for(Element e : everything)  System.out.println("everything.y: " + (e != null ? e.getY() : "null"));
        }

        return everything;
    } // sort()

    float time;
    @Override
    public void draw(SpriteBatch batch){
        float dt = Gdx.graphics.getDeltaTime();
        time += dt;
        float angle = time * (2 * MathUtils.PI);
        if (angle > (2 * MathUtils.PI))
            angle -= (2 * MathUtils.PI);

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.setShader(shader);

        shader.setUniformf("timedelta", -angle);

        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE2);
        texture2.bind(2);
        shader.setUniformi("u_noise", 2);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE1);
        texture.bind(1);
        shader.setUniformi("u_water", 1);

        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
        scenebg.sprite.getTexture().bind(0);

        Sprite sprscene = scenebg.sprite;
        batch.draw(
                sprscene.getTexture(),
                sprscene.getX(),
                sprscene.getY(),
                sprscene.getWidth(),
                sprscene.getHeight()
        );
        batch.end();
        batch.setShader(null);

        batch.begin();

        sort();

        for(Element e : everything) {
            //Sprite s = e.sprite;
            batch.draw(e.anim.getFrame(), e.getX(), e.getY());
        }

        //batch.draw(player.anim.getFrame(), player.getX(), player.getY());

        batch.end();
    }

    //@Override
    public void draw2(SpriteBatch batch) {

        Element[] elements = everything;//getElements();

        if(elements == null) {
            System.out.println("elements is empty");
            return;
        }
        batch.begin();
        for(Element e : elements){
            if(e == null) continue;

            Rectangle r = e.anim.getRectangle();
            batch.draw(e.anim.getFrame(), e.x, e.y);
        }
        batch.end();
    }

    @Override
    public void update(float delta){
        for(Element el : everything) {
            el.update(delta);
        }
        //player.anim.addTime(delta);
    } // void update


    @Override
    public Element[] getActors() {
        return actors;
    }

    @Override
    public void setActors(Element[] elements) {
        actors = elements;
    }

    @Override
    public Element[] getProps() {
        return props;
    }

    @Override
    public void setProps(Element[] elements) {
        props = elements;
    }

    @Override
    public Element getPlayer() {
        return this.player;
    }

    @Override
    public void setPlayer(Element player) {
        this.player = player;
        System.out.println("Player is set");
    }
}
