package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.game.Eventyr.stage1;
import com.mygdx.game.World.Element;
import com.mygdx.game.World.Stage;

public class Resources {
    private Sprite  dropImage;
    private Sprite  spritesImage;
    private Sprite  backgroundImage;
    private Sprite  backgroundImage2;
    private Sound   dropSound;
    private Music   rainMusic;


    public boolean ready = false;

    private Sprite loadTex(String path){
        return new Sprite(new Texture(Gdx.files.internal(path)));
    }

    public FileHandle getshader(int which){
        FileHandle n = null;
        switch(which){
            case 1:
                n= Gdx.files.internal("glsl/vertexshader.glsl");
                break;
            case 2:
                n = Gdx.files.internal("glsl/fragmentshader1.glsl");
                break;
            case 3:
                n = Gdx.files.internal("glsl/fragmentshader2.glsl");
                break;
        }
        return n;
    }

    public Resources() {
        // load the images for the droplet and the bucket, 64x64 pixels each
        dropImage        = loadTex("droplet.png");
        spritesImage     = loadTex("anim.png");
        backgroundImage  = loadTex("DragonDormido.png");
        backgroundImage2 = loadTex("DragonDormido2.png");

        // load the drop sound effect and the rain background "music"
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("level1.ogg"));
        ready = true;
    }

    public void dispose(){
        // dispose of all the native resources
        dropImage.getTexture().dispose();
        spritesImage.getTexture().dispose();
        dropSound.dispose();
        rainMusic.dispose();
        backgroundImage.getTexture().dispose();
        backgroundImage2.getTexture().dispose();
    }



    public Sprite getDropImage() {
        return dropImage;
    }

    public Sprite  getSpritesImage() {
        return spritesImage;
    }

    public Sprite  getBackgroundImage() {
        return backgroundImage;
    }
    public Sprite  getBackgroundImage2() {
        return backgroundImage2;
    }

    public Sound getDropSound() {
        return dropSound;
    }

    public Music getRainMusic() {
        return rainMusic;
    }

    public Element[] getElements() {
        return null;
    }

    /* this thing should do magic to return the correct stage */
    public Stage getStage(){
        Stage s = new stage1(this);
        return s;
    }
}
