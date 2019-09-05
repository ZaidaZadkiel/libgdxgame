/*
 *
 *  * Created by ZaidaZadkiel on 7/13/19 11:54 AM
 *  * Copyright (c) 2019 . All rights reserved.
 *  * Last modified 7/13/19 11:54 AM
 *
 *  Stage defines the map image, position of items and everything related
 *  like a teather scenario
 */

package com.mygdx.game.World;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Resources;

public abstract class Stage {

    public Stage(){}

    public Stage(Resources resources) {}

    // returns a string with the path for the config file for this stage
    abstract public String getFile();

    abstract public void setReady();

    //loops through all elements in the stage and updates their state
    abstract public void update(float delta);

    //reads all elements in the stage and draws in world space
    abstract public void draw(SpriteBatch batch);

    //pointer to player element
    abstract public Element getPlayer();
    abstract public void    setPlayer(Element player);

    //pointer to all npc and activable things
    abstract public Element[] getActors();
    abstract public void      setActors(Element[] elements);

    //pointer to static decorations
    abstract public Element[] getProps();
    abstract public void      setProps(Element[] elements);

    //abstract public void prepareStage(Element[] actors, Element[] props);
}
