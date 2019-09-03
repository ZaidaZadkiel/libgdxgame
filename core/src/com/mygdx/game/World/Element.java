/*
 *
 *  * Created by ZaidaZadkiel on 7/13/19 11:56 AM
 *  * Copyright (c) 2019 . All rights reserved.
 *  * Last modified 7/13/19 11:56 AM
 *
 *  Element is the base class for anything that can be drawn in the screen besides the map itself
 */

package com.mygdx.game.World;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Rectangle;

public class Element extends Rectangle {
    //TODO: get rid of local Animation class and properly implement Gdx.Animation

    /*
    https://github.com/libgdx/libgdx/blob/a4805d6a017b80622d6bfdd3a791352257a3c539/tests/gdx-tests/src/com/badlogic/gdx/tests/SimpleAnimationTest.java

    apparently TextureRegions are to be used with Gdx.Animation
    * */
    public boolean   visible;
    public int       direction;
    public String    name;
    public Sprite    sprite;
    public Animation anim = new Animation();

    public Element(float x, float y, float w, float h, int framecount, Sprite sprite){
        super.x = x; super.y = y; super.width = w; super.height = h;
        anim.framecount  = framecount;
        anim.framewidth  = w;
        anim.frameheight = h;
        anim.frametime   = (framecount <= 1 ? 0.0f : anim.frametime); //if there are no frames, leave frametime = 0

        this.sprite     = sprite;
        anim.splitFrames(sprite);
        setFrame(0, 0);
    }

    public void setFrame(int frame, int index){
        anim.frame      = frame;
        anim.frameindex = index;
    }

    public ShaderProgram vshader;
    private ShaderProgram fshader;
    public boolean shaderEnabled = false;
    public void setShader(FileHandle vs, FileHandle fs){
        ShaderProgram.pedantic = false;
        vshader = new ShaderProgram(vs, fs);
        if(vshader.isCompiled() == false ) System.out.print(vshader.getLog() );
        shaderEnabled = true;
    }

    public ShaderProgram getShader(){
        return vshader;
    }


}
