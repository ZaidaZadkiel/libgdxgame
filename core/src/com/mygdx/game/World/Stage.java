/*
 *
 *  * Created by ZaidaZadkiel on 7/13/19 11:54 AM
 *  * Copyright (c) 2019 . All rights reserved.
 *  * Last modified 7/13/19 11:54 AM
 *
 *  Stage defines the map image, position of items and everything related
 *  like a teather scenario
 */

package com.mygdx.game.World;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.Resources;

public class Stage {
    static String TAG = "Stage";

    public  boolean        ready = false;
            Element        scenebg;
    public  Element[]      allElements;
    public  Polygon[]      allPolys;


    public  Polygon[]      boundaries; //never change
    public  Polygon[]      triggers;   // "
    public  Polygon[]      cases;      // "
            Element        player;     //shouldnt change, might
            Array<Element> actors;     //can change often
            Array<Element> props;      //never changes
    public Vector2         spawn;      //start position for this stage

    public Stage(){}
    public Stage(Resources resources) {}

    // returns a string with the path for the config file for this stage
    public String getStageFilePath(){return "unset";}

    public void      setReady(){
        Gdx.app.log(TAG, "setReady:\n" + getStageFilePath() +
                        "\nactors: "+(getActors()==null ? "is null" : getActors().size)+
                        "\nprops:  "+(getProps() ==null ? "is null" : getProps().size)+
                        "\nplayer: "+(getPlayer()==null ? "is null" : getPlayer().toString())
                    );

        if(spawn==null) spawn = new Vector2(100, 100);

        scenebg = getPropByName("scenebg");

        int allElemsSize = 0;
        if(getProps()  != null) allElemsSize += getProps().size;
        if(getActors() != null) allElemsSize += getActors().size;

        allElements = new Element[allElemsSize+1]; // +1 == player
        int i = 0;
//        System.arraycopy(getActors(), 0,allElements, 0 , getActors().size);
        if(getActors() != null)
            for(Element e : getActors()) {
                allElements[i] = e;
                i++;
            }
        if(getProps() != null)
            for(Element e : getProps()) {
                if(e != scenebg){
                    allElements[i] = e;
                    i++;
                }
            }
        allElements[i] = player;

        int allPolysSize = 0; //
        if(boundaries != null) allPolysSize += boundaries.length;
        if(triggers   != null) allPolysSize += triggers.length;
        if(cases      != null) allPolysSize += cases.length;
        i = 0;
        allPolys = new Polygon[allPolysSize];
        if(cases != null)
            for(Polygon p : cases) {
                if(p != null) {
                    allPolys[i] = p;
                    i++;
                }
            }

        if(triggers != null)
            for(Polygon p : triggers) {
                if(p != null) {
                    allPolys[i] = p;
                    i++;
                }
            }
        if(boundaries != null)
            for(Polygon p : boundaries) {
                if(p != null) {
                    allPolys[i] = p;
                    i++;
                }
            }
        Gdx.app.log(TAG, "got " + i + " polys total");
        Gdx.app.log(TAG, "set ready");
        ready = true;
    }

    //loops through all elements in the stage and updates their state
    public void      update(float delta){
        if(allElements !=null) for(Element e : allElements) if(e !=null) e.update(delta);
        sort();
    }

    //reads all elements in the stage and draws in world space
    public void      draw(SpriteBatch batch){
        batch.begin();

        if(scenebg!= null) scenebg.draw(batch);
        if(allElements != null) for(Element el : allElements) if(el != null) el.draw(batch);

        batch.end();
    }

    private Element[] sort(){
/* sorting original sauce:
     private static void sort(int nos[], int n) {
     for (int i = 1; i < n; i++){
          int j = i;
          int B = nos[i];
          while ((j > 0) && (nos[j-1] > B)){
            nos[j] = nos[j-1];
            j--;
          }
          nos[j] = B;
        }
    }
* */
        if(allElements == null) return null;

        for(int i = 0; i != allElements.length; i++){
            int j = i;
            Element b = allElements[i];
            if(b != null){
                while((j>0) && allElements[j-1].getY() < b.getY()){
                    allElements[j] = allElements[j-1];
                    j--;
                } // while
                allElements[j] = b;
            }
        } // for

        if(false){
            for(Element e : allElements)  System.out.println("everything.y: " + (e != null ? e.getY() : "null"));
        }

        return allElements;
    } // sort()


    //pointer to player element
    public Element        getPlayer()                        { return player;}
    public void           setPlayer(Element player)          { this.player = player;
                                                               if(spawn != null) {player.x = spawn.x; player.y = spawn.y;}
                                                               if(this.getActors()!= null) addActor(player); }

    //pointer to all npc and activable things
    public Array<Element> getActors()                        { return actors; }
    public void           setActors(Array<Element> elements) { actors = elements; }
    public void           addActor (Element element)         { if(actors == null) actors = new Array<Element>();
                                                               actors.add(element);
                                                               if(ready)  setReady(); }
    public Element        getActorByName(String name)        { if(getActors() != null && getActors().size > 0){
                                                                 for(Element item : getProps())
                                                                     if(    item      != null
                                                                         && item.name != null
                                                                         && item.name.equals(name)
                                                                     ) return item;
                                                               }
                                                               return null;}

    //pointer to static decorations
    public Array<Element> getProps()                        { return props; }
    public void           setProps(Array<Element> elements) { props = elements; }
    public void           addProp (Element prop)            { if(props == null) props = new Array<Element>();
                                                              props.insert(0, prop);
                                                              if(ready) setReady();}
    public Element        getPropByName(String name)        { if(getProps() != null && getProps().size > 0){
                                                                for(Element item : getProps())
                                                                    if(    item      != null
                                                                        && item.name != null
                                                                        && item.name.equals(name)
                                                                    ) return item;
                                                              }
                                                              return null;}

    // setters only, they should never change
    public void           setBoundaries(Polygon[] boundaries) { this.boundaries = boundaries; }
    public void           setTriggers  (Polygon[] triggers, String[] script)   { this.triggers   = triggers; }
    public void           setCases     (Polygon[] cases, String[] script)      { this.cases      = cases; }

}
