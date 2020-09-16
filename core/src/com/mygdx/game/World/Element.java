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
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Element extends Rectangle {
    //TODO: get rid of local Animation class and properly implement Gdx.Animation

    static final String TAG = "Element";
    /*
    https://github.com/libgdx/libgdx/blob/a4805d6a017b80622d6bfdd3a791352257a3c539/tests/gdx-tests/src/com/badlogic/gdx/tests/SimpleAnimationTest.java

    apparently TextureRegions are to be used with Gdx.Animation
    * */
    public boolean   visible;
    public int       direction;
    public Sprite    sprite;
    public String    name   = "unset";
    public Animation anim   = new Animation();
    public float     speed  = 120; //pixels per second
    public String    script = "nothing";

    public Element(){};

    public Element(float x, float y, String anifile){
        this(x, y, anifile, null);
    }

    public Element(float x, float y, String anifile, String script){
        //load ani file
        String s = Gdx.files.internal("ani/"+anifile).readString();
        Gdx.app.log(TAG, "string: " + s);
        //file cant be read, return empty TODO: starter Element
        if(s.length() == 0) return;

        String[] pieces = s.split("\\s+");
//        int i = 0;
//        for(String pz : pieces){
//            Gdx.app.log(TAG, i++ + ":"+ pz);
//        }
        this.x = x; this.y = y;
        name    = pieces[0];
        if(script==null) this.script  = pieces[1];
        sprite  = new Sprite(new Texture(Gdx.files.internal(pieces[2])));
        speed   = Float.parseFloat(pieces[3]);
        anim.frameCount    = Integer.parseInt(pieces[4]);
        anim.framesetCount = Integer.parseInt(pieces[5]);
        anim.frameTime     = (anim.frameCount == 0 ? 0 : Float.parseFloat(pieces[6]));
        anim.frameWidth    = sprite.getWidth()  / anim.frameCount;
        anim.frameHeight   = sprite.getHeight() / anim.framesetCount;
        width  = anim.frameWidth;
        height = anim.frameHeight;
        anim.splitFrames(sprite);

        setFrame(0,0);
    };

    public Element(float x, float y, float w, float h, int framecount, Sprite sprite, String name){
        super.x = x; super.y = y; super.width = w; super.height = h;
        anim.frameCount    = framecount;
        anim.framesetCount = 4;
        anim.frameWidth    = w;
        anim.frameHeight   = h;
        anim.frameTime     = (framecount <= 1 ? 0.0f : anim.frameTime); //if there are no frames, leave frametime = 0
        if(sprite == null) System.out.println("Sprite is null");
        this.sprite        = sprite;
        anim.splitFrames(sprite);
        setFrame(0, 0);

        this.name = name;
        Gdx.app.log("Element", "Created at " + x + ", " + y);
    }

    public void setPos(Vector2 pos){
        this.x = pos.x-getBounds().x;
        this.y = pos.y-getBounds().y;
    }

    Vector3 bounds;
    public Vector3 getBounds(){
        if(bounds == null) bounds = new Vector3(32,12, 32);
        return bounds;
    }

    public void update(float delta){
        anim.addTime(delta);
    }

    public void moveStop() {
        if(anim.anim_action == 1 || anim.anim_action == 2) {
            anim.frame=0;
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
        anim.framesetIndex = 2;
        if(anim.anim_action  == 0) {
            anim.anim_action = 1;
            anim.setTime(0);
        }
    }


    public void moveDown(){
        this.y -= speed * Gdx.graphics.getDeltaTime();
        anim.framesetIndex = 0;
        if(anim.anim_action == 0) {
            anim.anim_action = 1;
            anim.setTime(0);
        }
    }
    public void moveLeft(){
        x -= speed * Gdx.graphics.getDeltaTime();
        anim.framesetIndex = 3;
        if(anim.anim_action == 0) {
            anim.anim_action = 1;
            anim.setTime(0);
        }
    }

    public void moveRight(){
        this.x += speed * Gdx.graphics.getDeltaTime();
        anim.framesetIndex = 1;
        if(anim.anim_action  == 0) {
            anim.anim_action = 1;
            anim.setTime(0);
        }
    }
    public void setFrame(int frame, int index){
        anim.frame      = frame;
        anim.framesetIndex = index;
    }

    public ShaderProgram  vshader;
    private ShaderProgram fshader;
    public boolean shaderEnabled = false;

    public void setShader(FileHandle vs, FileHandle fs){
        ShaderProgram.pedantic = false;
        vshader = new ShaderProgram(vs, fs);
        if(vshader.isCompiled() == false ) System.out.print(vshader.getLog() );
        shaderEnabled = true;
    }

    public void draw(SpriteBatch batch){
        TextureRegion tr = anim.getFrame();
        if(tr != null) batch.draw(tr, x, y);
    }

    public ShaderProgram getShader(){
        return vshader;
    }

    @Override
    public String toString() {
        return name + ": " + super.toString();
    }
}
