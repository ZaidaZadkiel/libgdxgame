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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.mygdx.game.Resources;
import com.mygdx.game.World.Element;
import com.mygdx.game.World.Stage;

import java.util.Iterator;

public class stage1 extends Stage {
    Element player;
    //Element layer1;
    //Element layer2, layer3;
    Element npc1, npc2, npc3, npc4, npc5, npc6;


    FileHandle vertexshader;
    FileHandle fragmentshader;

    ShaderProgram shader;

    private Sound     dropSound;
    private Resources resources;

    private Texture texture, texture2;
    private Element scene[];
    private Element scenebg;

    public stage1(Resources resources) {
        this.resources = resources;
        if(resources.ready == false){
            System.out.println("ERROR! resources not ready");
        }

        Sprite s = resources.getSpritesImage();
        player = new Element(800/2-32, 400/2-32, 64, 64,19, s);


        /* scene images */
        scene = resources.loadxml("data/scene1/stack.xml");
        scenebg = scene[7];
        scene[7] = player;


        // characters images
//          s = resources.getBackgroundImage();
//        layer1 = new Element(0, 0,s.getWidth(),s.getHeight(), 1, s);
//        s = resources.getBackgroundImage2();
//        layer2 = new Element(0, 550,s.getWidth(),s.getHeight(), 1, s);
//        s = resources.getBackgroundImage3();
//        layer3 = new Element(10,116,s.getWidth(),s.getHeight(), 1, s);
        s =  resources.getSpritesImage();
        npc1   = new Element( 100, 100, 64, 64, 20, s);
        npc2   = new Element( 200, 200, 64, 64, 20, s);
        npc3   = new Element( 300, 300, 64, 64, 20, s);
        npc4   = new Element( 100, 100, 64, 64, 20, s);
        npc5   = new Element( 200, 200, 64, 64, 20, s);
        npc6   = new Element( 300, 300, 64, 64, 20, s);

        texture  = new Texture(Gdx.files.internal("data/water.png"));
        texture2 = new Texture(Gdx.files.internal("data/waterdisplacement.png"));

        // keep local reference copy
        dropSound       = resources.getDropSound();

        vertexshader   = resources.getshader(1);
        fragmentshader = resources.getshader(3);

        if(vertexshader == null || fragmentshader == null) System.out.println("Error leyendo vertex o fragment");

        //layer1.setShader(vertexshader, fragmentshader);
        //layer1.setShader(vertexshader, fragmentshader);

        ShaderProgram.pedantic = false;
        shader = new ShaderProgram(vertexshader, fragmentshader);
        if(shader.isCompiled() == false ) System.out.print(shader.getLog() );

        stageElements = new Element[] {
            npc1, npc2, npc3,
            npc4, npc5, npc6
        };

        // start the playback of the background music immediately
        resources.getRainMusic().setLooping(true);
        resources.getRainMusic().play();
    }

    private Element[] sort(){
        if(false){
            for(Element e : scene)  System.out.println("scene.y: " + (e != null ? e.getY() : "null"));
        }
/*
*     private static void sort(int nos[], int n) {
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
        for( int i = 0; i != scene.length; i++){
            int j = i;
            Element b = scene[i];
            while((j>0) && scene[j-1].getY() < b.getY()){
               scene[j] = scene[j-1];
               j--;
            } // while
            scene[j] = b;
        } // for

        if(false){
            for(Element e : scene)  System.out.println("scene.y: " + (e != null ? e.getY() : "null"));
        }
        return scene;
    } // sort()

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
        //ShaderProgram s = layer1.getShader();
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

        Element[] elements = getElements();

        if(elements == null) {
            System.out.println("elements is empty");
            return;
        }
        batch.begin();

        sort();
        for(Element e : scene) {
            //Sprite s = e.sprite;
            batch.draw(e.anim.getFrame(), e.getX(), e.getY());
        }
        //for(int i = 6; i>=0; i--){
         //   if(player.getY() > scene[i].getY()) {
         //       batch.draw(scene[i], scene[i].getX(), scene[i].getY());
          //  } else {
         //       batch.draw(player.anim.getFrame(), player.getX()+(i*5), player.getY());
         //   }
            //batch.draw(player.anim.getFrame(), scene[i].getX(), scene[i].getY());
        //}



        for(Element e : elements){
            if(e == null) continue;

            Rectangle r = e.anim.getRectangle();
            //e.prite.draw(batch);
            //batch.draw(e.anim.getFrame(), e.x, e.y);
            //batch.draw(e.sprite, e.x, e.y, (int)r.x, (int)r.y, (int)r.width, (int)r.height);
        }
        batch.end();
    }

    //@Override
    public void draw2(SpriteBatch batch) {

/*
        if(layer1.shaderEnabled == true){
            batch.begin();
            //texture.bind(1);
            //Matrix4 m = batch.getProjectionMatrix();


            layer1.vshader.begin();
            //texture.bind(1);
            layer1.vshader.setUniformMatrix("u_worldView", batch.getProjectionMatrix());
            //layer1.vshader.setUniformMatrix("u_worldView", batch.getTransformMatrix());
            layer1.vshader.end();
            layer1.sprite.draw(batch);
            //batch.begin();
            //batch.getShader().setUniformMatrix("u_worldView",  m);
            //batch.getShader().setUniformi("u_texture", 1);
            //batch.draw(layer1.anim.getFrame(), layer1.x, layer1.y);
            batch.end();

            //batch.setShader(null);
        }
*/
        Element[] elements = getElements();

        if(elements == null) {
            System.out.println("elements is empty");
            return;
        }
        batch.begin();
        for(Element e : elements){
            if(e == null) continue;

            Rectangle r = e.anim.getRectangle();
            //e.prite.draw(batch);
            batch.draw(e.anim.getFrame(), e.x, e.y);
            //batch.draw(e.sprite, e.x, e.y, (int)r.x, (int)r.y, (int)r.width, (int)r.height);
        }
        batch.end();
    }

    @Override
    public Element getPlayer() {
        //System.out.println("flag player is null?" + (this.player == null ? "null" : "not null"));
        return this.player;
    }

    float time;
    private void updatenpc(Element npc, float delta){
        if(time > 1){
            time = 0;
            npc.direction = MathUtils.random(3);
        }
        time += delta;
        npc.anim.frameindex = npc.direction;
        switch(npc.direction){
            case 0:
                npc.y = npc.y - (80 * delta);
                if(npc.y < 1) npc.direction = 2;
                break;
            case 1:
                npc.x = npc.x - (80 * delta);
                if(npc.x < 1) npc.direction = 3;
                break;
            case 2:
                npc.y = npc.y + (80 * delta);
                if(npc.y > 1500) npc.direction = 0;
                break;
            case 3:
                npc.x = npc.x + (80 * delta);
                if(npc.x > 1500) npc.direction = 1;
                break;
        }
    }

    @Override
    public void update(float delta){
        updatenpc(npc1, delta);
        updatenpc(npc2, delta);
        updatenpc(npc3, delta);
        updatenpc(npc4, delta);
        updatenpc(npc5, delta);
        updatenpc(npc6, delta);
        for(Element el : stageElements) {
            el.anim.addTime(delta);
        }
        player.anim.addTime(delta);
    } // void update


    @Override
    public Element[] getElements() {
        return stageElements;
    }


}
