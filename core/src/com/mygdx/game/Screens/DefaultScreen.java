

/*
 *
 *  * Created by ZaidaZadkiel on 7/13/19 9:53 AM
 *  * Copyright (c) 2019 . All rights reserved.
 *  * Last modified 7/13/19 9:49 AM
 *
 *  Default Screen empty class
 *  Screens should handle input on their own as the input requirements in each screen may differ
 *
 */

package com.mygdx.game.Screens;

import com.badlogic.gdx.Screen;
import com.mygdx.game.MyGdxGame;

public class DefaultScreen implements Screen {
    public MyGdxGame game;


    public DefaultScreen(MyGdxGame game) {
        this.game = game;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {

    }

    /* polls input and does what it must within the screen */
    public void input(){

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
