/*
 *
 *  * Created by ZaidaZadkiel on 7/13/19 12:16 PM
 *  * Copyright (c) 2019 . All rights reserved.
 *  * Last modified 7/13/19 12:16 PM
 */

package com.mygdx.game.Eventyr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
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

    private Sound     dropSound;
    private Resources resources;

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

        com.badlogic.gdx.graphics.g2d.Sprite x;
        // keep local reference copy
        dropSound       = resources.getDropSound();

        stageElements = new Element[] {
            layer1, player,
            npc1, npc2, npc3,
            npc4, npc5, npc6,
            layer2
        };

        // start the playback of the background music immediately
        resources.getRainMusic().setLooping(true);
        resources.getRainMusic().play();
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
