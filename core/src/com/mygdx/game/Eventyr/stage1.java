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
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
    Element drop ;
    Element player;
    Element layer1;
    Element layer2;
    Element npc1, npc2, npc3, npc4, npc5, npc6;


    FileHandle vertexshader;
    FileHandle fragmentshader;

    private Sound     dropSound;
    private Resources resources;

    private Texture texture, texture2;

    public stage1(Resources resources) {
        this.resources = resources;
        if(resources.ready == false){
            System.out.println("ERROR! resources not ready");
        }

        drop   = new Element(0,               0, 64, 64, 1, resources.getDropImage());
        player = new Element(800/2-32, 400/2-32, 64, 64,20, resources.getSpritesImage());
        layer1 = new Element(0,               0,resources.getBackgroundImage().getWidth(),resources.getBackgroundImage().getHeight(), 1, resources.getBackgroundImage());
        layer2 = new Element(0,               0,resources.getBackgroundImage2().getWidth(),resources.getBackgroundImage2().getHeight(), 1, resources.getBackgroundImage2());
        npc1   = new Element( 100, 100, 64, 64, 20, resources.getSpritesImage());
        npc2   = new Element( 200, 200, 64, 64, 20, resources.getSpritesImage());
        npc3   = new Element( 300, 300, 64, 64, 20, resources.getSpritesImage());
        npc4   = new Element( 100, 100, 64, 64, 20, resources.getSpritesImage());
        npc5   = new Element( 200, 200, 64, 64, 20, resources.getSpritesImage());
        npc6   = new Element( 300, 300, 64, 64, 20, resources.getSpritesImage());

        texture  = new Texture(Gdx.files.internal("data/water.png"));
        texture2 = new Texture(Gdx.files.internal("data/waterdisplacement.png"));
        com.badlogic.gdx.graphics.g2d.Sprite x;
        // keep local reference copy
        dropSound       = resources.getDropSound();

        vertexshader   = resources.getshader(1);
        fragmentshader = resources.getshader(3);

        if(vertexshader == null || fragmentshader == null) System.out.println("Error leyendo vertex o fragment");

        //layer1.setShader(vertexshader, fragmentshader);
        layer1.setShader(vertexshader, fragmentshader);


        stageElements = new Element[] {
            player,
            npc1, npc2, npc3,
            npc4, npc5, npc6,
            layer2
        };

        // start the playback of the background music immediately
        resources.getRainMusic().setLooping(true);
        resources.getRainMusic().play();
    }

    @Override
    public void draw(SpriteBatch batch){
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.setShader(layer1.getShader());
        texture.bind(2);
        texture2.bind(3);
        layer1.getShader().setUniformi("u_texture1", 2);
        layer1.getShader().setUniformi("u_texture2", 3);
        batch.draw(layer1.sprite.getTexture(),layer1.x,layer1.y,layer1.width,layer1.height);
        batch.end();
        batch.setShader(null);

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

    //@Override
    public void draw2(SpriteBatch batch) {


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
    } // void update


    @Override
    public Element[] getElements() {
        return stageElements;
    }


}
