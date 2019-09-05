package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.mygdx.game.Eventyr.stage1;
import com.mygdx.game.World.Element;
import com.mygdx.game.World.Stage;

import java.util.Iterator;
import java.util.StringTokenizer;

public class Resources {
    private Sprite  spritesImage;
    private Sound   dropSound;
    private Music   rainMusic;


    public boolean ready = false;

    private Sprite loadTex(String path){
        System.out.println("loadTex: " + path);
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
        spritesImage     = loadTex("anim.png");

        // load the drop sound effect and the rain background "music"
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("level1.ogg"));
        ready = true;
    }

    public void dispose(){
        // dispose of all the native resources
        spritesImage.getTexture().dispose();
        dropSound.dispose();
        rainMusic.dispose();
    }

    // opens xml for reading
    private FileHandle xmlfile;
    private XmlReader xml;
    private XmlReader.Element xmle;
    public XmlReader.Element openXML(String path){
        System.out.println("Reading xml from " + path);
        xmlfile = Gdx.files.internal(path);
        xml     = new XmlReader();
        xmle    = xml.parse(xmlfile);
        return xmle;
    }

    public Element[] readProps() {
        Array<XmlReader.Element> layer = xmle.getChildByName("stack").getChildrenByName("layer");
        Iterator i = layer.iterator();
        Element[] xmlelements = new Element[layer.size];
        int c = 0;

        while (i.hasNext()) {
            XmlReader.Element level_element = (XmlReader.Element) i.next();
            // TODO_ Figure if name is needed for something
            String name       = level_element.getAttribute("name");
            String src        = level_element.getAttribute("src");
            String visibility = level_element.getAttribute("visibility");

            int x = level_element.getInt("x");
            int y = level_element.getInt("y");

            System.out.println("prop name: " + name);
            Sprite s = loadTex(src);
            s.setPosition(x, y);

            //TODO: make something to visibilty allowing different ways to display
            if(false && visibility.compareTo("fixed") == 0){
                xmlelements[c] = xmlelements[0];
                xmlelements[0] = new Element(x, y, s.getWidth(), s.getHeight(), 0, s);
            } else {
                xmlelements[c] = new Element(x, y, s.getWidth(), s.getHeight(), 0, s);
            }
            c++;
        }
        return xmlelements;
    } // Element[] readProps()

    public Polygon[] readBoundaries(){
        Array<XmlReader.Element> layer = xmle.getChildByName("boundary").getChildrenByName("area");
        Iterator i = layer.iterator();
        Polygon[] poly = new Polygon[layer.size];;
        int c = 0;

        while(i.hasNext()){
            XmlReader.Element level_element = (XmlReader.Element)i.next();
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

    public Sprite  getSpritesImage() {
        return spritesImage;
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
