/*
 *
 *  * Created by ZaidaZadkiel on 7/13/19 11:49 AM
 *  * Copyright (c) 2019 . All rights reserved.
 *  * Last modified 7/13/19 11:49 AM
 *
 *  Scene is the base class through all different game-screens are to be extended
 *  Scene deals with the basic composition of elements within the world simulation
 *  iterates through all objects (within a game-screen) and updates their status
 *  deals with boundary checks, scenery animation, npc movement, map special parts, etc
 *  gets the final position to be rendered, culling nonvisibles
 *
 */

package com.mygdx.game.World;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.Resources;

public class World {
    private Stage     stage;
    private Resources resources;
    private Element   player;
    public Polygon[] boundaries;

    /*
    load and hold sounds with some kind of index
    load and hold textures
    check collisions

    */
    public World() {

    }

    public float collX;
    public float collY;
    public Polygon collP;
    Vector2 newPos = new Vector2();
    public void moveElement(Element e, float newX, float newY){
        if(newX == 0 && newY == 0) {
            e.moveStop();
            return; //do nothing if there's no movement
        }

        if(boundaries != null ){
            float adjustedX = e.getBounds().x + (newX * Gdx.graphics.getDeltaTime());
            float adjustedY = e.getBounds().y + (newY * Gdx.graphics.getDeltaTime());
            newPos.set(
                    adjustedX,
                    adjustedY
            );
            for(int i = 0; i != boundaries.length; i++)
                if(boundaries[i].contains(newPos) ==true) {
                    collX = newPos.x;
                    collY = newPos.y;
                    collP = boundaries[i];
                    //System.out.println("newPos.x: " + newPos.x + ", newPos.y" + newPos.y);
                    //System.out.println("[i]: " + i + " newX: " + (e.x+newX) + ". newY: " + (e.y+newY));
                    newX = 0;
                    newY = 0;
                    break;
                }
        }
        if(newX < 0) e.moveLeft();
        if(newX > 0) e.moveRight();
        if(newY > 0) e.moveUp();
        if(newY < 0) e.moveDown();
        if(newX == 0 && newY == 0) e.moveStop();
    }

    float time;
    private void updatenpc(Element npc, float delta){
        if(time > 1){
            time = 0;
            npc.direction = MathUtils.random(3);
        }
        time += delta;
        float newX = 0;
        float newY = 0;
        switch(npc.direction){
            case 0:
                newX = npc.speed;
                break;
            case 1:
                newX = -npc.speed;
                break;
            case 2:
                newY = npc.speed;
                break;
            case 3:
                newX = -npc.speed;
                break;
        }
        moveElement(npc, newX, newY);
    }

    /* start up all objects to their default positions
    *  delegate to stage to setup everything */
    public void start(Resources resources) {
        this.resources = resources;
    }

    // delegates update to stage and updates gui and global things
    public void update(float delta){
        if(stage != null) stage.update(delta);
        for(Element actor : getStage().getActors())
            updatenpc(actor, delta);
    }

    // changes stage and does transition
    //TODO: move all loading of resources here
    //TODO: make stage1..n as minimalistic as possible, keeping only direct game logic
    public void setStage(Stage newStage) {
        stage = newStage;

        player = new Element(
                1024/2-32, 100,
                64, 64,19,
                resources.getSpritesImage()
        ); //player

        //newStage.setPlayer(player);
        System.out.println("Stage.file: " + newStage.getFile());

        resources.openXML(newStage.getFile());

        Element[] npcs = new Element[5];
        npcs[0] = new Element( 500, 150, 64, 64, 19, player.sprite);
        npcs[1] = new Element( 500, 150, 64, 64, 19, player.sprite);
        npcs[2] = new Element( 500, 150, 64, 64, 19, player.sprite);
        npcs[3] = new Element( 500, 150, 64, 64, 19, player.sprite);
        npcs[4] = new Element( 500, 150, 64, 64, 19, player.sprite);

        newStage.setPlayer(player);
        newStage.setActors(npcs);
        newStage.setProps (resources.readProps());

        boundaries = resources.readBoundaries();

        newStage.setReady(); //everything should be ready to run
    }

    //returns pointer to active stage
    public Stage getStage() {
        return stage;
    }

    // delegates to stage render function and renders gui on top
    public void present(SpriteBatch batch) { stage.draw(batch); }
}
