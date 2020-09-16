package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.mygdx.game.Screens.AniEditorScreen;
import com.mygdx.game.Screens.EditorScreen;
import com.mygdx.game.Screens.GameScreen;

public class MyGdxGame extends Game {

    public Resources resources;
    public Configuration configuration;

    public MyGdxGame() {
    }

    @Override
    public void create() {
        configuration = new Configuration();
        this.resources = new Resources();
//        setScreen(new GameScreen(this));
//        setScreen(new EditorScreen(this));
        setScreen(new AniEditorScreen(this));
    }

    Screen oldScreen;
    public void changeScreen(Screen newScreen){
        oldScreen = getScreen();
        setScreen(newScreen);
    }

    @Override
    public void dispose() {
    }
}