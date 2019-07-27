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

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;

public class World {
    private Stage stage;

    public World() {

    }


    /* start up all objects to their default positions
    *  delegate to stage to setup everything */
    public void start() {

    }

    public void update(float delta){
        if(stage != null) stage.update(delta);
    }

    public void setStage(Stage newStage) {
        stage = newStage;
    }

    public Stage getStage() {
        return stage;
    }

    public void present(SpriteBatch batch) { stage.draw(batch); }
}
