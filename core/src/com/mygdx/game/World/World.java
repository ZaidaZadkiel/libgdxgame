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
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.*;
import com.mygdx.game.Resources;

public class World {
    static  String    TAG = "World";
    private Stage     stage;
    private Resources resources;
    public  Element   player;
    /*
    have all the elements in the world
    1   player        Element
    n   NPC           Element
    n   map           Stage
    1   BGM           audio
    n   sound effects audio
    n   collision     polys
    n   actionable    polys

    controls how the different Elements can interact with the world

    every NPC must check for collision poly and each other's space
    there are some ploys which can activate something either
      by stepping on it (activate on collision) or
      by pressing action button (activate switch)
    any NPC produces sounds on their own or when walking

    */
    public World(Resources r) {
        this.resources = r;
    }


    public OrthographicCamera camera;
    public Vector3   targetpos    = new Vector3();
           Vector3   startpos     = new Vector3();
           Vector3   screenCamPos = new Vector3();
           float     smoothTime;
           float     camtime;
    public Rectangle moveBounds = new Rectangle();
    public void smoothCamera(float targetX, float targetY){
        if(camera == null) return;
        if(targetX==0 && targetY==0) return;

        smoothTime = 0.25f;
        camtime    = 0f;
        startpos .set(camera.position.x,camera.position.y,0);
        targetpos.set(targetX,          targetY,          0);

        moveBounds.set((camera.position.x)-((camera.viewportWidth /3f)*camera.zoom),
                       (camera.position.y)-((camera.viewportHeight/3.5f)*camera.zoom),
                       (camera.viewportWidth /1.5f)*camera.zoom,
                       (camera.viewportHeight/1.7f)*camera.zoom);

        if(moveBounds.contains(targetpos.x, targetpos.y) == true) camtime = 100; //disable movement
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
        Polygon[] boundaries = stage.boundaries;

        if(boundaries != null ){
            float adjustedX = e.getBounds().x + (newX * Gdx.graphics.getDeltaTime());
            float adjustedY = e.getBounds().y + (newY * Gdx.graphics.getDeltaTime());
            newPos.set(
                    adjustedX,
                    adjustedY
            );
            for(int i = 0; i != boundaries.length; i++)
                if(boundaries[i].contains(newPos) == true) {
                    collX = newPos.x;
                    collY = newPos.y;
                    collP = boundaries[i];
                    newX  = 0;
                    newY  = 0;
                    //System.out.println("newPos.x: " + newPos.x + ", newPos.y" + newPos.y);
                    //System.out.println("[i]: " + i + " newX: " + (e.x+newX) + ". newY: " + (e.y+newY));
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
//        moveBounds.set((camera.position.x)-((camera.viewportWidth /4f)*camera.zoom), (camera.position.y)-((camera.viewportHeight/4f)*camera.zoom), (camera.viewportWidth /2f)*camera.zoom, (camera.viewportHeight/2f)*camera.zoom);

        if(stage != null) stage.update(delta);
        if(camtime < smoothTime){
            camtime+=delta;
            float now = (smoothTime-camtime)/smoothTime;
            camera.position.x = Interpolation.linear.apply(targetpos.x, startpos.x, now);
            camera.position.y = Interpolation.linear.apply(targetpos.y, startpos.y, now);
//            Gdx.app.log(TAG,
//                    "\n f: " + now +
//                    "\n x: " + String.format("%2.2f", camera.position.x) +
//                    "\nsx: " + String.format("%2.2f", startpos.x) +
//                    "\ntx: " + String.format("%2.2f", targetpos.x)
//            );
        }
    }

    // changes stage and does transition
    //TODO: move all loading of resources here
    //TODO: make stage1..n as minimalistic as possible, keeping only direct game logic
    public void setStage(Stage newStage) {
        if(resources==null) Gdx.app.log(TAG, "resources is null");
        stage = newStage;
        System.out.println("Stage.file: " + newStage.getStageFilePath());
        Gdx.app.log("loading", "load result: " + resources.initStageFromXML(newStage));
    }

    //returns pointer to active stage
    public Stage getStage() {
        return stage;
    }

    // delegates to stage render function and renders gui on top
    public void present(SpriteBatch batch) { if(stage != null) stage.draw(batch); }
}
