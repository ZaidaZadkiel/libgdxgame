package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.mygdx.game.Screens.GameScreen;

public class MyGdxGame extends Game {

    @Override
    public void create() {
        setScreen(new GameScreen(this));
    }

    @Override
    public void dispose() {
    }
}