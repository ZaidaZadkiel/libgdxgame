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

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Animation {
    public int        framesetIndex;
    public int        framesetCount;
    public int        frame;
    public int        frameCount;
    public float      frameWidth;
    public float      frameHeight;
    public float      frameTime =0.25f;

    public int        anim_action = 0; // 0: idle, 1: starting walk, 2: walking loop, 3: stopping walk

    private Rectangle framepos = new Rectangle();
    private float     time;
    private int       animcount;
    private com.badlogic.gdx.graphics.g2d.Animation<TextureRegion> []
                      animations;
    TextureRegion[][] frames;


    public void  setTime(float abs) { time = abs; }
    public float getTime()          { return time; }

    public void addTime(float delta){
        if(anim_action==0) {
            frame = 0;
            return;
        }
        time = (time + delta);

        if(frameCount != 0 && time >= frameTime){
            frame = (frame + 1) ;//% framecount;
            time -= frameTime;
//            System.out.println("frametime is: " + frametime);
        }
    }

    public TextureRegion getFrame(){
        if(animations == null) return null;
//        System.out.println("anim_action: " + anim_action);
        switch(anim_action) {
            //return animations[frameindex].
            case 0:
                time = 0;
                frame=0;
                return animations[framesetIndex].getKeyFrame(0, false);
            case 1:
                TextureRegion n = frames[framesetIndex][frame];//.getKeyFrame(time, true);
//                if(time >= (3 * frametime) ) {
//                    time -= (1 * frametime);
//                    anim_action = 2;
//                }
                if(frame >= 2){
                    frame = 0;
                    anim_action=2;
                }
                return n;
            case 2:
                return frames[framesetIndex][2+(frame%(frameCount -4) )];//.getKeyFrame(time, true);
//                return animations[frameindex].getKeyFrame(
////                        (2 * frametime) + (time % (this.framecount-2 * frametime)),
////                        (2 * frametime)+(time % ((this.framecount-2) * frametime)),
//                        time % (framecount*frametime),
//                        true
//                ); //return
            case 3:
                //TODO: sometimes this doesnt show the animation when stopping from walking (2)
                if(frame== 1) anim_action=0;
                return frames[framesetIndex][(7+frame)% frameCount];//.getKeyFrame(time, true);
//                float nt = (15*frametime)+(time % (5*frametime));
//                if(animations[frameindex].isAnimationFinished(nt)) anim_action = 0;
//                return animations[frameindex].getKeyFrame(nt, false);
            default:
                return animations[framesetIndex].getKeyFrame(0, true);
        } // switch(anim_action)
    } // public getFrame()

    public void splitFrames(Sprite s){
        if(frameCount == 0) {
            animations = new com.badlogic.gdx.graphics.g2d.Animation[1];
            animations[0] = new com.badlogic.gdx.graphics.g2d.Animation<TextureRegion>(
                    0,
                    new TextureRegion(s.getTexture())
                ); // new Animation<TextureRegion>
            return;
        }

        frames = TextureRegion.split(
                s.getTexture(),
                s.getTexture().getWidth() / frameCount,
                s.getTexture().getHeight()/ framesetCount
        );
        //should be lines of frames
        animcount   = frames.length;
        animations  = new com.badlogic.gdx.graphics.g2d.Animation[animcount];
        int counter = 0;
        for(TextureRegion[] framegroup : frames){
            animations[counter++] = new com.badlogic.gdx.graphics.g2d.Animation<TextureRegion>(
                    frameTime,
                    framegroup
            );// animations[i] = new Animation
            //System.out.println("Animation time: " + frametime + ", counter: " + counter);
        } // for each framegroup in frames

    }

    public Rectangle getRectangle(){
        framepos.x      = (frame * frameWidth) % (frameCount * frameWidth);
        framepos.y      = framesetIndex * frameHeight;
        framepos.width  = frameWidth;
        framepos.height = frameHeight;
        return framepos;
    }
}
