/*
 *
 *  * Created by ZaidaZadkiel on 7/13/19 11:56 AM
 *  * Copyright (c) 2019 . All rights reserved.
 *  * Last modified 7/13/19 11:56 AM
 *
 *  Element is the base class for anything that can be drawn in the screen besides the map itself
 */

package com.mygdx.game.World;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

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
    public float     speed = 120; //pixels per second

    public Element(float x, float y, float w, float h, int framecount, Sprite sprite){
        super.x = x; super.y = y; super.width = w; super.height = h;
        anim.framecount  = framecount;
        anim.framewidth  = w;
        anim.frameheight = h;
        anim.frametime   = (framecount <= 1 ? 0.0f : anim.frametime); //if there are no frames, leave frametime = 0

        if(sprite == null) System.out.println("Sprite is null");
        this.sprite     = sprite;
        anim.splitFrames(sprite);
        setFrame(0, 0);
    }

    Vector3 bounds = new Vector3() ;
    public Vector3 getBounds(){
        bounds.set(this.x +32 , this.y +12, 32);
        return bounds;
    }

    public void update(float delta){
        anim.addTime(delta);
    }

    public void moveStop() {
        if(anim.anim_action == 1 || anim.anim_action == 2) {
            anim.anim_action = 3;
            anim.setTime(0);
        }
    }

    public boolean moveUpTry(Polygon bound){
        float newY = y + speed * Gdx.graphics.getDeltaTime();
        return bound.contains(x, newY ) ;
    }

    public void moveUp(){
        this.y += speed * Gdx.graphics.getDeltaTime();
        anim.frameindex = 1;
        if(anim.anim_action  == 0) {
            anim.anim_action = 1;
            anim.setTime(0);
        }
    }


    public void moveDown(){
        this.y -= speed * Gdx.graphics.getDeltaTime();
        anim.frameindex = 3;
        if(anim.anim_action == 0) {
            anim.anim_action = 1;
            anim.setTime(0);
        }
    }
    public void moveLeft(){
        x -= speed * Gdx.graphics.getDeltaTime();
        anim.frameindex = 0;
        if(anim.anim_action == 0) {
            anim.anim_action = 1;
            anim.setTime(0);
        }
    }

    public void moveRight(){
        this.x += speed * Gdx.graphics.getDeltaTime();
        anim.frameindex = 2;
        if(anim.anim_action  == 0) {
            anim.anim_action = 1;
            anim.setTime(0);
        }
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
