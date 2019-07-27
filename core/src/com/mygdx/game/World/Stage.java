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
    public Element[] stageElements;

    public Stage(){}

    public Stage(Resources resources) {}

    abstract public void update(float delta);

    abstract public void draw(SpriteBatch batch);

    abstract public Element getPlayer();

    abstract public Element[] getElements();


}
