package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.mygdx.game.Eventyr.stage1;
import com.mygdx.game.World.Element;
import com.mygdx.game.World.Stage;

import java.io.File;
import java.util.StringTokenizer;

public class Resources {
    static String TAG = "Resources";

    private Sprite     spritesImage;
    private Sound      dropSound;
    private Music      rainMusic;
    private BitmapFont fontImage;

    private Sprite loadTex(String path){
//        System.out.println("loadTex: " + path);
        FileHandle f = Gdx.files.internal(path);
        if(f == null) Gdx.app.log(TAG+":loadTex", "file is null: " + path);
        return new Sprite(new Texture(f));
    }

    public Element loadani(String path){
        return new Element(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, path);
    }

    public Stage loadStage(String s){
        return null;
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
        //TODO: create assetmanager and stuff

        // load the images for the droplet and the bucket, 64x64 pixels each
        spritesImage = loadTex("anim.png");

        // load the drop sound effect and the rain background "music"
        dropSound    = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        rainMusic    = Gdx.audio.newMusic(Gdx.files.internal("level1.ogg"));

        fontImage = new BitmapFont( );
    }

    public void dispose(){
        // dispose of all the native resources
        spritesImage.getTexture().dispose();
        dropSound.dispose();
        rainMusic.dispose();
    }

    public String initStageFromXML(Stage stage){
        if(stage.getStageFilePath() == "unset") return "failed";

        this.openXML(stage.getStageFilePath());

        stage.setPlayer    (null);
        stage.setProps     (readProps());
        stage.setActors    (readActors());
        readBoundaries(stage);
//        stage.setBoundaries(readBoundaries());
//        stage.setTriggers  (readTriggers());
//        stage.setCases     (readCases());
        Gdx.app.log(TAG, "trying to read spawn point");
        XmlReader.Element pos = xmle.getChildByName("spawnpoint");
        if(pos != null){
            stage.spawn = new Vector2(
                        pos.getFloat("x"),
                        pos.getFloat("y") );

        }
        if(stage.spawn == null) Gdx.app.log(TAG, "spawnpoint read fail");
        stage.setPlayer(loadPlayer(stage.spawn.x, stage.spawn.y));

        stage.setReady();

        return "success";
    }

    // opens xml for reading, if path is null it will crash (desired result)
    private FileHandle        xmlfile;
    private XmlReader         xml;
    public  XmlReader.Element xmle;

    public  XmlReader.Element openXML(String path){
        System.out.println("Reading xml from " + path);
        xmlfile = Gdx.files.internal(path);
        xml     = new XmlReader();
        xmle    = xml.parse(xmlfile);
        return xmle;
    }


    public Array<Element> readActors() {
        if(xmle == null || xmle.hasChild("cast") == false) return null;
        Array<Element> actors = new Array<Element>();
        Array<XmlReader.Element> cast = xmle.getChildByName("cast").getChildrenByName("actor");
/*                    piece.element("cast");
                    for(Element actor : world.getStage().getActors()){
                        if(actor != null)
                            piece.element("actor")
                                    .attribute("name",      actor.name)
                                    .attribute("x",         actor.x)
                                    .attribute("y",         actor.y)
                                    .attribute("script",    actor.script)

* */
        for(XmlReader.Element actordata : cast){
            String name   = actordata.getAttribute("name");
            String script = actordata.getAttribute("script");
            float  x      = Float.parseFloat(actordata.getAttribute("x"));
            float  y      = Float.parseFloat(actordata.getAttribute("y"));
            Gdx.app.log(TAG, "Added actor " + name +" at "+ x +","+ y);
            actors.add(new Element(x, y, name, script));
        }
        Gdx.app.log(TAG, "Read " + actors.size);
        return actors;
    }

    public Array<Element> readProps() {
        if(xmle == null || xmle.hasChild("stack") == false) return null;
        Array<XmlReader.Element> layer = xmle.getChildByName("stack").getChildrenByName("layer");

        Array<Element> xmlelements     = new Array<Element>();
        for(XmlReader.Element level_element : layer){
            // TODO_ Figure if name is needed for something
            String name       = level_element.getAttribute("name");
            String opacity    = level_element.getAttribute("opacity"); //unused
            String src        = level_element.getAttribute("src");
            String visibility = level_element.getAttribute("visibility"); //unused
            float x           = level_element.getFloat("x");
            float y           = level_element.getFloat("y");

            Sprite s = loadTex(src);
            s.setPosition(x, y);
            //TODO: make something to visibilty allowing different ways to display
            Element element = new Element(x, y, s.getWidth(), s.getHeight(), 0, s, name);
            xmlelements.add(element);
        }
        return xmlelements;
    } // Element[] readProps()

    public Polygon[] readCases(){
        if(xmle == null || xmle.hasChild("boundary") == false){
            float v[] = new float[6];
            v[0] = 0;
            v[1] = 0;
            v[2] = 100;
            v[3] = 100;
            v[4] = 50;
            v[5] = 100;
            Polygon[] p = new Polygon[1];
            p[0] = new Polygon(v);
            return p;
        }

        Array<XmlReader.Element> layer = xmle.getChildByName("boundary").getChildrenByName("area");

        Polygon[] poly = new Polygon[layer.size];;
        int c = 0;

        for(XmlReader.Element level_element : layer){
            String area= level_element.getAttribute("coords");
            //System.out.println(area);
            StringTokenizer st = new StringTokenizer(area, ",");
            int p = 0;
            float [] points = new float[st.countTokens()];
            while (st.hasMoreTokens()) {
                points[p] = Float.parseFloat(st.nextToken());
                //System.out.println("p[" + p + "]: " + points[p]);
                p++;
            }
            poly[c] = new Polygon(points);
            c++;
        }

        return poly;
    }

    public Polygon[] readTriggers(){
        if(xmle == null || xmle.hasChild("boundary") == false){
            float v[] = new float[6];
            v[0] = 0;
            v[1] = 0;
            v[2] = 100;
            v[3] = 100;
            v[4] = 50;
            v[5] = 100;
            Polygon[] p = new Polygon[1];
            p[0] = new Polygon(v);
            return p;
        }

        Array<XmlReader.Element> layer = xmle.getChildByName("boundary").getChildrenByName("area");

        Polygon[] poly = new Polygon[layer.size];;
        int c = 0;

        for(XmlReader.Element level_element : layer){
            String area= level_element.getAttribute("coords");
            //System.out.println(area);
            StringTokenizer st = new StringTokenizer(area, ",");
            int p = 0;
            float [] points = new float[st.countTokens()];
            while (st.hasMoreTokens()) {
                points[p] = Float.parseFloat(st.nextToken());
                //System.out.println("p[" + p + "]: " + points[p]);
                p++;
            }
            poly[c] = new Polygon(points);
            c++;
        }

        return poly;
    }

    public Polygon[] readBoundaries(Stage stage){
        if(xmle == null || xmle.hasChild("boundary") == false){
            float v[] = new float[6];
            v[0] = 0;
            v[1] = 0;
            v[2] = 100;
            v[3] = 100;
            v[4] = 50;
            v[5] = 100;
            Polygon[] p = new Polygon[1];
            p[0] = new Polygon(v);
            return p;
        }

        Array<XmlReader.Element> layer = xmle.getChildByName("boundary").getChildrenByName("area");

//        Polygon[] poly = new Polygon[layer.size];;
//        int c = 0;
//
//        for(XmlReader.Element level_element : layer){
//            String area= level_element.getAttribute("coords");
//            //System.out.println(area);
//            StringTokenizer st = new StringTokenizer(area, ",");
//            int p = 0;
//            float [] points = new float[st.countTokens()];
//            while (st.hasMoreTokens()) {
//                points[p] = Float.parseFloat(st.nextToken());
//                //System.out.println("p[" + p + "]: " + points[p]);
//                p++;
//            }
//            poly[c] = new Polygon(points);
//            c++;
//        }

        Array<Polygon> bounds = new Array<Polygon>();
        Array<Polygon> trigs  = new Array<Polygon>();
        Array<Polygon> cases  = new Array<Polygon>();

        Array<String>  trigscripts = new Array<String>();
        Array<String>  casesscripts = new Array<String>();

        for(XmlReader.Element level_element : layer){
            String area   = level_element.getAttribute("coords");
            String shape  = level_element.getAttribute("shape");
            String script = level_element.hasAttribute("script") ? level_element.getAttribute("script") : "nothing";

            StringTokenizer st = new StringTokenizer(area, ",");
            int p = 0;
            float [] points = new float[st.countTokens()];
            while (st.hasMoreTokens()) {
                points[p] = Float.parseFloat(st.nextToken());
                //System.out.println("p[" + p + "]: " + points[p]);
                p++;
            }
            if("bound"  .equals(shape))  bounds.add(new Polygon(points));
            if("trigger".equals(shape)) {trigs .add(new Polygon(points)); trigscripts.add(script);}
            if("case"   .equals(shape)) {cases .add(new Polygon(points)); casesscripts.add(script); }
        }

        Gdx.app.log("readBounds", "boundaries got: " + bounds.size);
        if(bounds.isEmpty() == false){
            Polygon[] b = new Polygon[bounds.size];
            for(int i = 0; i!= bounds.size; i++){
                b[i] = bounds.get(i);
                Gdx.app.log("", "c: " + b[i].getVertices().length);
            }
            stage.setBoundaries(b);
        }

        Gdx.app.log("readBounds", "triggers got: " + trigs.size);
        if(trigs.isEmpty() == false){
            Polygon[] t = new Polygon[trigs.size];
            String [] s = new String[trigscripts.size];

            for(int i = 0; i!= trigs.size; i++){
                t[i] = trigs.get(i);
                s[i] = trigscripts.get(i);
                Gdx.app.log("", "c: " + t[i].getVertices().length);
            }
            stage.setTriggers(t, s);
        }

        Gdx.app.log("readCases", "cases got: " + cases.size);
        if(cases.isEmpty() == false){
            Polygon[] c = new Polygon[cases.size];
            String [] s = new String[cases.size];

            for(int i = 0; i!= cases.size; i++){
                c[i] = cases.get(i);
                s[i] = casesscripts.get(i);
                Gdx.app.log("", "c: " + c[i].getVertices().length);
            }
            stage.setCases(c, s);
        }

        return null;
    }

    public Sprite loadimg(int n){
        Sprite s = null;
/*
src="data/007.png"		x="0"	y="0"
src="data/005.png"		x="35"	y="124"
src="data/003.png"		x="355"	y="268"
src="data/004.png"		x="17"	y="282"
src="data/006.png"		x="0"	y="551"
src="data/001.png"		x="0"	y="568"
src="data/000.png"		x="164"	y="613"
src="data/002.png"		x="505"	y="661"

* */
        switch (n) {
            case 0:
                s = new Sprite( loadTex("data/scene1/000.png"));
                s.setPosition(164, 613); //x="164" y="613"
                break;
            case 1:
                s = new Sprite( loadTex("data/scene1/001.png"));
                s.setPosition(0, 568); //x="0" y="568"
                break;
            case 2:
                s = new Sprite( loadTex("data/scene1/002.png"));
                s.setPosition(505, 661); //x="505" y="661"
                break;

            case 3:
                s = new Sprite( loadTex("data/scene1/003.png"));
                s.setPosition(355, 268); // x="355" y="268"
                break;

            case 4:
                s = new Sprite( loadTex("data/scene1/004.png"));
                s.setPosition(17, 282); //x="17" y="282"
                break;

            case 5:
                s = new Sprite( loadTex("data/scene1/005.png"));
                s.setPosition(35, 124); //x="35" y="124"
                break;
            case 6:
                s = new Sprite( loadTex("data/scene1/006.png"));
                s.setPosition(0, 551); //x="10" y="551"
                break;
            case 7:
                s = new Sprite( loadTex("data/scene1/007.png"));
                s.setPosition(0, 0);
                break;
        }
/*<layer composite-op="svg:src-over" name="Pasted Layer #2" opacity="1.0" src="data/001.png" visibility="visible" x="0" y="678" />
<layer composite-op="svg:src-over" name="Pasted Layer #1" opacity="1.0" src="data/002.png" visibility="visible" x="0" y="0" />
<layer composite-op="svg:src-over" name="Pasted Layer   " opacity="1.0" src="data/003.png" visibility="visible" x="505" y="126" />
<layer composite-op="svg:src-over" name="scene1-pond.png" opacity="1.0" src="data/004.png" visibility="visible" x="355" y="697" />
<layer composite-op="svg:src-over" name="Pasted Layer #4" opacity="1.0" src="data/005.png" visibility="visible" x="17" y="756" />
<layer composite-op="svg:src-over" name="scene1-pond.png" opacity="1.0" src="data/006.png" visibility="visible" x="10" y="675" />
<layer composite-op="svg:src-over" name="scene1-bg.png  " opacity="1.0" src="data/007.png" visibility="visible" x="0" y="0" />* */
        if(s == null) System.out.println("Sprite is null");
        return s;
    }

    public BitmapFont getFont(){
        return fontImage;
    }

    public Sprite     getSpritesImage() {
        return spritesImage;
    }

    public Sound      getDropSound() {
        return dropSound;
    }

    public Music      getRainMusic() {
        return rainMusic;
    }

    public Element[]  getElements() {
        return null;
    }

    /* this thing should do magic to return the correct stage */
    Stage currentStage;
    public Stage getStage(){
        if(currentStage == null) currentStage = new stage1(this);
        Gdx.app.log(TAG, "res currentStage: " + currentStage.getStageFilePath());
        return currentStage;
    }

    public void setStage(Stage current){
        this.currentStage = current;
    }

    Element player;
    public Element loadPlayer(float x, float y){
        if(player == null) player = new Element(x,y,64f,64f,8, this.getSpritesImage(), "player");
        player.x = x;
        player.y = y;
        return  player;
    }
}
