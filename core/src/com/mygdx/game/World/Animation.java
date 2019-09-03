/*
 *
 *  * Created by ZaidaZadkiel on 7/13/19 1:41 PM
 *  * Copyright (c) 2019 . All rights reserved.
 *  * Last modified 7/13/19 1:41 PM
 *
 *  The simplest way to hold animation data
 *  each animation is a line of frames from x=0 .. x=frame.width*animation.length
 *  they are stacked on y: the idle animation is y = 0, the second animation is y + frame.height
 */

package com.mygdx.game.World;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Animation {
    public int        frameindex;
    public int        frame;
    public int        framecount;
    public float      framewidth;
    public float      frameheight;
    public float      frametime=0.051f;


    private Rectangle framepos = new Rectangle();
    private float     time;
    private int       animcount;
    private com.badlogic.gdx.graphics.g2d.Animation<TextureRegion> []
                      animations;

    public void setTime(float abs) {
        time = abs;
    }

    public void addTime(float delta){
        time += delta;
        if(false && time >= frametime){
            frame = frame + 1 % framecount;
            time -= frametime;
        }
    }
    public int anim_action = 0; // 0: idle, 1: starting walk, 2: walking loop, 3: stopping walk

    public TextureRegion getFrame(){
        switch(anim_action) {
            //return animations[frameindex].
            case 0:
                time = 0;
                return animations[frameindex].getKeyFrame(0, false);
            case 1:
                TextureRegion n = animations[frameindex].getKeyFrame(time, true);
                if(time >= (6 * frametime) ) {
                    time -= (5 * frametime);
                    anim_action = 2;
                }
                return n;
            case 2:
                return animations[frameindex].getKeyFrame(
                        (5 * frametime) + (time % (10 * frametime)),
                        true
                ); //return
            case 3:
                //TODO: sometimes this doesnt show the animation when stopping from walking (2)
                float nt = (15*frametime)+(time % (5*frametime));
                if(animations[frameindex].isAnimationFinished(nt)) anim_action = 0;
                return animations[frameindex].getKeyFrame(nt, false);
            default:
                return animations[frameindex].getKeyFrame(0, true);
        } // switch(anim_action)
    } // public getFrame()

    public void splitFrames(Sprite s){
        if(framecount == 0) {
            animations = new com.badlogic.gdx.graphics.g2d.Animation[1];
            animations[0] = new com.badlogic.gdx.graphics.g2d.Animation<TextureRegion>(
                    0,
                    new TextureRegion(s.getTexture())
                ); // new Animation<TextureRegion>
            return;
        }

        TextureRegion[][] frames = TextureRegion.split(
                s.getTexture(),
                s.getTexture().getWidth() / framecount,
                s.getTexture().getHeight()/4
        );
        //should be lines of frames
        animcount = frames.length;
        animations = new com.badlogic.gdx.graphics.g2d.Animation[animcount];
        int counter = 0;
        for(TextureRegion[] framegroup : frames){
            animations[counter++] = new com.badlogic.gdx.graphics.g2d.Animation<TextureRegion>(
                    frametime,
                    framegroup
            );// animations[i] = new Animation
            //System.out.println("Animation time: " + frametime + ", counter: " + counter);
        } // for each framegroup in frames

    }

    public Rectangle getRectangle(){
        framepos.x      = (frame * framewidth) % (framecount* framewidth);
        framepos.y      = frameindex * frameheight;
        framepos.width  = framewidth;
        framepos.height = frameheight;
        return framepos;
    }
}
