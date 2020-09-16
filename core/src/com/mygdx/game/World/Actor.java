package com.mygdx.game.World;

import com.badlogic.gdx.graphics.g2d.Sprite;

/*
* file definition
* actor name
* sprite name
* mood list
* dialogs
* complex scripts
*
* "Guy Name"\n  -- String, sets the Display Name
* "guyname/"\n  -- String, sets the directory to search mood files
* "mood1, mood2, mood3(...)"
* */

public class Actor extends Element {
    public float x,y, nextx, nexty;

    public Actor(float x, float y, float w, float h, int framecount, Sprite sprite, String name) {
        super(x, y, w, h, framecount, sprite, name);
    }

    public Actor(Sprite s, String name){
        super(0,0, 64, 64, 7, s, name);
        return;
    }

    public Actor loadFrom(String file){
        return null;
    }

    int mood = 0;
    int setMood(String moodName){
        return 0;
    }
    int addMood(String moodName, String moodString){
        return 0;
    }

    public int moveXY(int x, int y){
        nextx = x;
        nexty = y;
        return 0;
    }

    public int update(){
        // do pathfinding or whatever in here
        if(x < nextx) x++;
        if(x > nextx) x--;
        if(y < nexty) y++;
        if(y > nexty) y--;
        return 0;
    }
}
