package com.mygdx.game.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.Screens.EditorScreen;
import com.mygdx.game.World.Element;
import com.mygdx.game.World.Stage;
import com.mygdx.game.World.World;

public class AssetTB extends Textbox {
    static String TAG = "AssetTB";

    World world;
    int mode;
    public final static int MODE_LISTSUMMARY  = 1;
    public final static int MODE_SELECTANI    = 2;
    public final static int MODE_SELECTPROP   = 9;
    public final static int MODE_ELEMENT      = 3;
    public final static int MODE_POLYGON      = 4;
    public final static int MODE_LISTANIS     = 5;
    public final static int MODE_LISTACTORS   = 6;
    public final static int MODE_LISTPROPS    = 7;
    public final static int MODE_STAGESUMMARY = 8;

    private final String LI_BACK     = "back";
    private final String LI_NEWACTOR = "newactor";
    private final String LI_NEWPROP  = "newprop";
    private final String LI_SUMMARY  = "summary";


    public String  getModeText(){
        switch(mode){
            case MODE_LISTSUMMARY : return "MODE_LISTSUMMARY";
            case MODE_SELECTANI   : return "MODE_SELECTASSET";
            case MODE_ELEMENT     : return "MODE_ELEMENT";
            case MODE_POLYGON     : return "MODE_POLYGON";
            case MODE_LISTANIS    : return "MODE_LISTANIS";
            case MODE_LISTACTORS  : return "MODE_LISTACTORS";
            case MODE_LISTPROPS   : return "MODE_LISTPROPS";
            case MODE_STAGESUMMARY: return "MODE_STAGESUMMARY";
        }
        return "Unknown";
    }
    public int     getMode(){return mode;}
    public boolean changeMode(int newMode){
        mode = newMode;
        Gdx.app.log(TAG, "changed " + getModeText());
        switch(mode){
            case MODE_LISTSUMMARY : return updateAssetList();
            case MODE_SELECTANI   :
                //list loaded assets, add "load new asset" which sets mode ListAnis
                break;
            case MODE_LISTANIS    : return updateAniList();
            case MODE_LISTPROPS   : return updatePropsList();
            case MODE_LISTACTORS  : return updateActorsList();
            case MODE_STAGESUMMARY: return updateStageSummary();
        }
        return false;
    }

    public AssetTB(SpriteBatch b, World world) {
        super(b);
        this.world = world;
        changeMode(MODE_STAGESUMMARY);
    }

    public boolean click(int screenX, int screenY, int button){
        if(mouseIndex >= 0 && mouseIndex < shownItems.size){
            ListItem li = shownItems.get(mouseIndex);

            String s;
            if(li.variant == null) { s = "variant is null";
            } else                 { s = li.variant.getClass().getSimpleName(); }
            Gdx.app.log(TAG, "s: " + s + ", display: " +li.display+ ", mode: " + getModeText() );



            if("String".equals(s)) {
                String v = (String)li.variant;
                if(LI_BACK    .equals(v)) return changeMode(MODE_LISTSUMMARY);
                if(LI_NEWACTOR.equals(v)) return changeMode(MODE_LISTANIS);
                if(LI_SUMMARY .equals(v)) return changeMode(MODE_STAGESUMMARY);
            }

            switch(mode) {
                case MODE_STAGESUMMARY:
                    if("props" .equals(li.variant)) { changeMode(MODE_LISTPROPS);  return true; }
                    if("actors".equals(li.variant)) { changeMode(MODE_LISTACTORS); return true; }
                    if("bounds".equals(li.variant)) { changeMode(MODE_POLYGON);    return true; }
                    if("trigs" .equals(li.variant)) { changeMode(MODE_POLYGON);    return true; }
                    if("cases" .equals(li.variant)) { changeMode(MODE_POLYGON);    return true; }
                    break;
                case MODE_LISTSUMMARY:
                    Gdx.app.log("", "li flag " + li.intcode);
                    editorScreen.changeMode(EditorTB.MODE_EDIT);
                    editorScreen.changeInput(li.intcode == EditorScreen.ARRAY_ACTORS
                                                ? EditorScreen.INPUT_EDITACTOR
                                                : EditorScreen.INPUT_EDITPROP);
                    editorScreen.setActiveElementArray(li.intcode); //HAX: intcode is set after adding ListItem

                    Gdx.app.log("", "li flag " + editorScreen.getElementArrayName());

                    editorScreen.setElementSelected((Element) li.variant);
                    return true;
                case MODE_LISTACTORS:
                    editorScreen.changeMode(EditorTB.MODE_EDIT);
                    editorScreen.changeInput(EditorScreen.INPUT_EDITACTOR);
                    editorScreen.setActiveElementArray(EditorScreen.ARRAY_ACTORS);
                    editorScreen.setElementSelected((Element) li.variant);
                    return true;
                case MODE_LISTPROPS:
                    editorScreen.changeMode(EditorTB.MODE_EDIT);
                    editorScreen.changeInput(EditorScreen.INPUT_EDITPROP);
                    editorScreen.setActiveElementArray(EditorScreen.ARRAY_PROPS);
                    editorScreen.setElementSelected((Element) li.variant);
                    return true;
                case MODE_LISTANIS:
                    Gdx.app.log(TAG, "wat: " + li.display);
                    Vector3 p = new Vector3(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, 0);
                    world.camera.unproject(p);
                    Element e = new Element((int) p.x, (int) p.y, li.display);
                    world.getStage().addActor(e);
                    editorScreen.setActiveElementArray(EditorScreen.ARRAY_ACTORS);
                    editorScreen.setElementSelected(e, world.getStage().getActors().size - 1);
                    changeMode(MODE_ELEMENT);
                    break;
                case MODE_POLYGON:
                    Gdx.app.log("", "click on poly "+s );
                    break;
            }

        }
        return false;
    }



    Stage stage;
    public void setStage(Stage stage){
        this.stage = stage;
        changeMode(MODE_STAGESUMMARY);
    }

    boolean updateStageSummary(){
        super.clearText();
        super.listHeader = "STAGE";
        if(stage==null) {
            super.addListItem("Stage is null", null);
        } else {
            super.addListItem(stage.getStageFilePath(), null);
            super.addListItem((stage.getProps()  == null ? "-" : stage.getProps().size)    + " Props",      "props");
            super.addListItem((stage.getActors() == null ? "-" : stage.getActors().size)   + " Actors",     "actors");
            super.addListItem((stage.boundaries  == null ? "-" : stage.boundaries.length ) + " Boundaries", "bounds");
            super.addListItem((stage.triggers    == null ? "-" : stage.triggers.length   ) + " Triggers",   "trigs");
            super.addListItem((stage.cases       == null ? "-" : stage.cases.length      ) + " Cases",      "cases");
        }
        update();
        return true;
    }

        Element element;
    public boolean setElement(Element e){
        Gdx.app.log("", "flag");
        if(e == null) return false;
        clearText();
        mode       = MODE_ELEMENT;
        element    = e;
        listHeader = "Element";
        addListItem("<back>",   LI_BACK);
        addListItem("X: "       +String.format("%1$7s", String.format("%3.2f", element.x)), e);
        addListItem("Y: "       +String.format("%1$7s", String.format("%3.2f", element.y)), e);
        addListItem("Name: "    +element.name, e);
        addListItem("Script:   "+element.name, e);
        addListItem("SprFile:  "+element.sprite.getTexture().toString(), e);
        addListItem("frames:   "+element.anim.frameCount, e);
        addListItem("frameSets:"+element.anim.framesetCount, e);
        update();
        return true;
    }

    Polygon polygon;
    String getPolygonType(){
        Stage s = this.stage;
        if(s.boundaries != null) for(Polygon p : s.boundaries) if(p==polygon) return "boundary";
        if(s.triggers   != null) for(Polygon p : s.triggers)   if(p==polygon) return "triggers";
        if(s.cases      != null) for(Polygon p : s.cases)      if(p==polygon) return "cases";
        return "undefined";
    }

    public void setPolygon(Polygon p){
        if(p == null) return;

        clearText();
        String type = getPolygonType();
        mode        = MODE_POLYGON;
        polygon     = p;
        listHeader  = type;
        addListItem("X: "      +String.format("%1$7s", String.format("%3.2f", polygon.getOriginX())), polygon);
        addListItem("Y: "      +String.format("%1$7s", String.format("%3.2f", polygon.getOriginY())), polygon);
        addListItem("#Verts:  "+polygon.getVertices().length, polygon);
        if(type.equals("cases") || type.equals("triggers"))
            addListItem("script:  "+"", polygon);
        update();
    }

    boolean updateActorsList(){
        clearText();
        listHeader = "ACTORS";
        Array<Element> actors = stage.getActors();
        if(actors == null) {
            addListItem("<Select Ani File>", LI_NEWACTOR);
            return false;
        }

        addListItem("<Back>", LI_BACK);
        for(Element actor : actors){
            addListItem(actor.name, actor).intcode=EditorScreen.ARRAY_ACTORS;
        }
        update();
        return false;
    }

    boolean updatePropsList(){
        clearText();
        listHeader = "PROPS";
        Array<Element> props = stage.getProps();
        addListItem("<Back>", LI_BACK);
        for(Element prop : props){
            addListItem(prop.name, prop).intcode=EditorScreen.ARRAY_PROPS;
        }
        update();
        return true;
    }

    boolean updateAniList(){
        FileHandle[] anilist = Gdx.files.internal("ani").list();

        clearText();
        listHeader = "ANI filez";
        if(anilist.length == 0){
            Gdx.app.log(TAG, "there are no ani filez");
            addListItem("No ani files found", null);
            update();
            return false;
        }
        Gdx.app.log(TAG, "there are filez " + anilist.length);

        for(FileHandle f : anilist){
            addListItem(f.name().split("\\.")[0], f);
        }
        update();
        return true;
    }

    boolean updateAssetList(){
        mode = MODE_LISTSUMMARY;
        Stage stage = world.getStage();
        clearText();
        listHeader="Asset list";
        addListItem("<Stage Summary>", LI_SUMMARY);
        addListItem("─┤ ACTORS ├─", null);
        if(stage.getActors() != null){
            for(int i = 0; i != stage.getActors().size; i++){
                Element e = stage.getActors().get(i);
                if(e != null)
                    addListItem(String.format("%3d", i) + ": " + e.name, e).intcode=EditorScreen.ARRAY_ACTORS;
            }// for(int i = 0; i != stage.getActors().size; i++)
        } else {
//            s.append("Actor list empty\n");
            addListItem("<Add New Actor>", LI_NEWACTOR);
        }

        addListItem("─┤ PROPS ├─", null);
        if(stage.getProps() != null){
            for(int i = 0; i != stage.getProps().size; i++){
                Element e = stage.getProps().get(i);
                if(e != null)
                    addListItem(String.format("%3d", i)+": "+e.name, e).intcode=EditorScreen.ARRAY_PROPS;
            } // for(int i = 0; i != stage.getProps().size; i++)
        } else {
            addListItem("<Add New Prop>", null);
        }

        update();
//        Gdx.app.log(TAG, "s: " + s);
        return true;
    } // void updateAssetList()


    EditorScreen editorScreen;
    public void setClickHandler(EditorScreen editor){
        editorScreen = editor;
    }

    void printAssetList(){
        Stage s = this.editorScreen.world.getStage();
//        super.setText(printedText.toString());
    }

    @Override
    public boolean setText(CharSequence text) {
        super.setText(text);
        super.x = (int)(Gdx.graphics.getWidth()-super.glyphLayout.width)-3;
        return true;
    }

    @Override
    public void update() {
        super.update();
        x = (int)(Gdx.graphics.getWidth()-super.glyphLayout.width)-3;
//        Gdx.app.log(TAG, "new x: "+x+", width "+ super.glyphLayout.width);
    }
}
