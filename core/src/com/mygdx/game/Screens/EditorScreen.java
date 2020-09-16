// @formatter:off

package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.viewport.*;
import com.mygdx.game.Configuration;
import com.mygdx.game.Eventyr.stage1;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.Resources;
import com.mygdx.game.UI.AssetTB;
import com.mygdx.game.UI.EditorTB;
import com.mygdx.game.UI.Textbox;
import com.mygdx.game.World.Element;
import com.mygdx.game.World.Stage;
import com.mygdx.game.World.World;
import jdk.nashorn.internal.parser.JSONParser;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.StringBuilder;
//import java.nio.file.FileSystems;

import static com.badlogic.gdx.math.MathUtils.round;

/*
 * EditorScreen deals with editing the map
 * Handles positioning vector points, creating new polygon boundaries, setting their type, and writing a map file
 * Shift + Middle mouse: zoom
 * Middle mouse: pan
 * key +, -: zoom in or out
 * key 1: 100% zoom
 * key 2: 50% zoom
 * */

//@formatter:off        - IntelliJ

public class EditorScreen extends DefaultScreen {
    static String TAG = "EditorScreen";

    private static final int DIR_STOP  = 0;
    private static final int DIR_LEFT  = 1;
    private static final int DIR_UP    = 2;
    private static final int DIR_RIGHT = 4;
    private static final int DIR_DOWN  = 8;

    private static int VIRTUAL_WIDTH  = 800;
    private static int VIRTUAL_HEIGHT = 400;

    //libGdx
    private SpriteBatch        batch;
    private Viewport           viewport;
    private OrthographicCamera camera;
    private OrthographicCamera overlayCamera;
    private ShapeRenderer      sr;

    //game system
    public  World     world;
    private Element   player;
    private Resources r;
    private Configuration config;

    String  editingStage;

    Vector3 reusable = new Vector3();
    Vector2 selected = new Vector2();
    Vector3 clicked  = new Vector3();


    EditorTB  textbox;
    AssetTB   assetbox;
    int       inputMode = 0;
    public final static int INPUT_MOVEVIEW        = 1;
    public final static int INPUT_DRAWPOLY        = 2; // create boundary
    public final static int INPUT_EDITACTOR       = 3; // create/edit image/object/boundary
    public final static int INPUT_EDITPROP        = 4; // create/edit image/object/boundary
    public final static int INPUT_EDITVERTBOUND   = 5;
    public final static int INPUT_EDITVERTTRIGGER = 6;
    public final static int INPUT_EDITVERTCASES   = 7;
    public final static int INPUT_MOVEPOSITION    = 8;
    public final static int INPUT_SELECTANY       = 9;

    public void changeMode(int newMode){
//        if(textbox.getMode() == newMode) return;
        textbox.setMode(newMode);
        recreateString=true;
    }

    public void changeInput(int mode){
        if(mode==inputMode){
//            Gdx.app.log(TAG, "Trying to set same inputMode");
            return;
        }
        inputMode = mode;
        Gdx.app.log(TAG, "inputMode: " + getInputModeText() + " old: " + inputMode +" new "+mode);
//        setSelectNothing();
        switch(inputMode){
            case INPUT_DRAWPOLY:        textbox.setText("Click to Draw, Ins to add polygon after the last selected, BckSpc to delete selected vert, ≡ to End");
                                        break;
            case INPUT_EDITVERTBOUND:   textbox.setText("Red Box->select boundary, drag->move selected. Ins->add, BkcSpc->del, tab->cycle, ≡ to End");
                                        setActivePolygonArray(ARRAY_BOUNDS);
                                        break;
            case INPUT_EDITVERTTRIGGER: textbox.setText("Red Box->select trigger, drag->move selected. Ins->add, BkcSpc->del, tab->cycle, ≡ to End");
                                        setActivePolygonArray(ARRAY_TRIGGER);
                                        break;
            case INPUT_EDITVERTCASES:   textbox.setText("Red Box->select case, drag->move selected. Ins->add, BkcSpc->del, tab->cycle, ≡ to End");
                                        setActivePolygonArray(ARRAY_CASE);
                                        break;
            case INPUT_EDITPROP:        textbox.setText("Click in boundary to select prop, drag to move selected.");
                                        setActiveElementArray(ARRAY_PROPS);
                                        break;
            case INPUT_EDITACTOR:       textbox.setText("Click in boundary to select actor, drag to move selected.");
                                        setActiveElementArray(ARRAY_ACTORS);
                                        break;
            default:
        }
        recreateString=true;
    }

    String getInputModeText(){
        switch(inputMode){
            case INPUT_MOVEVIEW       : return "INPUT_MOVEVIEW";
            case INPUT_DRAWPOLY       : return "INPUT_DRAWPOLY";
            case INPUT_EDITACTOR      : return "INPUT_EDITACTOR";
            case INPUT_EDITPROP       : return "INPUT_EDITPROP";
            case INPUT_EDITVERTBOUND  : return "INPUT_EDITVERTBOUND";
            case INPUT_EDITVERTTRIGGER: return "INPUT_EDITVERTTRIGGER";
            case INPUT_EDITVERTCASES  : return "INPUT_EDITVERTCASES";
            case INPUT_MOVEPOSITION   : return "INPUT_MOVEPOSITION";
            case INPUT_SELECTANY      : return "INPUT_SELECTANY";
            default                   : return "Unknown input mode";
        }
    }

    public final static int SCREEN_PLAY   = 1;

    void changeScreen(int screen){
        if(screen == SCREEN_PLAY) this.game.changeScreen(new GameScreen(game));
    }

    public final static int ARRAY_PROPS   = 1;
    public final static int ARRAY_ACTORS  = 2;
    public final static int ARRAY_BOUNDS  = 3;
    public final static int ARRAY_TRIGGER = 4;
    public final static int ARRAY_CASE    = 5;
    public final static int ARRAY_NONE    = 10;

    public int activePolygonArray   = ARRAY_NONE; //tells which is the active
    int        selectedVerticeIndex = 0;
    int        selectedPolygonIndex = 0;
    Polygon    selectedPolygon      = null;
    Polygon[]  selectedPolygonArray = null; //pointer to current working array

    public int     activeElementArray   = ARRAY_NONE; //tells which is active
    int            selectedElementIndex = 0;
    Element        selectedElement      = null;
    Array<Element> selectedElementArray = null; //pointer to current working data


    Element selectElementFromWorldXY(float x, float y){
//        Gdx.app.log("sEFWxy", "selectedElementArray is " + (selectedElementArray==null ? "null" : "set"));

        if(getActiveElementArray()==null) return null;
        Gdx.app.log("", "array size: " + selectedElementArray.size);
        for(int i = 0; i<selectedElementArray.size; i++){
            Element e = selectedElementArray.get(i);
            if (e.contains(x, y)){
//                Gdx.app.log("", "matched with " + e.name);
                setElementSelected(e, i);
                return e;
            }
//            Gdx.app.log("i: "+i, "dint match with " + e.name);
        }
        return null;
    }

    Polygon selectPolygonFromWorldXY(float x, float y){
//        Gdx.app.log("sEFWxy", "selectedElementArray is " + (selectedElementArray==null ? "null" : "set"));

        if(getActivePolygonArray()==null) return null;
        Gdx.app.log("", "array size: " + selectedPolygonArray.length);
        for(int i = 0; i<selectedPolygonArray.length; i++){
            Polygon p = selectedPolygonArray[i];
            if (p.contains(x, y)){
//                Gdx.app.log("", "matched with " + e.name);
                setPolygonSelected(p, i, 0);
                return p;
            }
//            Gdx.app.log("i: "+i, "dint match with " + e.name);
        }
        return null;
    }


    void cycleSelection(int direction){
        if(direction != 1 && direction != -1) throw new IllegalArgumentException("can only cycle +1 or -1, received " + direction);

        recreateString=true;
        Gdx.app.log(TAG, "direction: " + direction + " selectedElementIndex: " + selectedElementIndex);

        if( getActiveElementArray() != null
            && (inputMode==INPUT_EDITACTOR || inputMode==INPUT_EDITPROP)
        ){
            int index = (selectedElementIndex+direction) % selectedElementArray.size;
            if(index < 0) index = selectedElementArray.size + index;
            setElementSelected(selectedElementArray.get(index), index);
            world.camera=camera;
            world.smoothCamera(selectedElement.x+(selectedElement.width/2), selectedElement.y + (selectedElement.height / 2));
            Gdx.app.log(TAG, "direction: " + direction + ", index: " + index + "selectedElementIndex: " + selectedElementIndex);
            return;
        }

        //cycle selected vertes within poly
        if(selectedPolygon != null && (inputMode==INPUT_EDITVERTBOUND
                                   ||  inputMode==INPUT_EDITVERTTRIGGER
                                   ||  inputMode==INPUT_DRAWPOLY
                                   ||  inputMode==INPUT_EDITVERTCASES)
        ){
            float[] verts        = selectedPolygon.getVertices();
            int vertslen         = verts.length;
            int index            = (selectedVerticeIndex+(direction*2)) % vertslen;
            if(index < 0) index  = vertslen + index;
            selectedVerticeIndex = index;
            Gdx.app.log(TAG, "direction: " + direction + ", index: " + index + ", selectedVerticeIndex: " + selectedPolygonIndex);
            world.smoothCamera(verts[selectedVerticeIndex], verts[selectedVerticeIndex+1]);

            return;
        } // if(selectedPolygon != null ...

        //cycle selected polygon object
        if(    getActivePolygonArray() != null
            && (inputMode==INPUT_SELECTANY ||  inputMode==INPUT_MOVEVIEW)
        ){
            int index = (selectedPolygonIndex+direction) % selectedPolygonArray.length;
            if(index < 0) index = selectedPolygonArray.length + index;
            selectedPolygonIndex = index;
            setPolygonSelected(selectedPolygonArray[selectedPolygonIndex], selectedPolygonIndex, 0);
//            Gdx.app.log(TAG, "direction: " + direction + ", index: " + index + "selectedPolygonIndex: " + selectedPolygonIndex);
            return;
        }
    }



    void setSelectNothing(){
        selectedElement      = null;
        selectedElementIndex = -1;
        selectedElementArray = null;
        selectedPolygon      = null;
        selectedPolygonArray = null;
        selectedPolygonIndex = 0;
        selectedVerticeIndex = 0;
        recreateString       = true;
        activeElementArray   = ARRAY_NONE;
        activePolygonArray   = ARRAY_NONE;
    }

    public String         getElementArrayName(){
        if(activeElementArray == ARRAY_PROPS)   return "ARRAY_PROPS";
        if(activeElementArray == ARRAY_ACTORS)  return "ARRAY_ACTORS";
        if(activeElementArray == ARRAY_NONE)    return "ARRAY_NONE";
        return "unknown";
    }
    public Array<Element> getActiveElementArray(){
        if(activeElementArray == ARRAY_PROPS)   selectedElementArray = world.getStage().getProps();
        if(activeElementArray == ARRAY_ACTORS)  selectedElementArray = world.getStage().getActors();
        if(activeElementArray == ARRAY_NONE)    selectedElementArray = null;
        return selectedElementArray;
    }
    public Array<Element> setActiveElementArray(int which) {
        recreateString     = true;
        activeElementArray = which;
//        Gdx.app.log("ELEMARY", "element array: "+getElementArrayName());
        return getActiveElementArray();
    }

    public String    getPolygonArrayName(){
        if(activePolygonArray == ARRAY_BOUNDS)  return "ARRAY_BOUNDS";
        if(activePolygonArray == ARRAY_TRIGGER) return "ARRAY_TRIGGER";
        if(activePolygonArray == ARRAY_CASE)    return "ARRAY_CASE";
        if(activePolygonArray == ARRAY_NONE)    return "ARRAY_NONE";
        return "unknown";
    }
    public Polygon[] getActivePolygonArray(){
        if(activePolygonArray == ARRAY_BOUNDS)  {selectedPolygonArray=world.getStage().boundaries; }
        if(activePolygonArray == ARRAY_TRIGGER) {selectedPolygonArray=world.getStage().triggers;   }
        if(activePolygonArray == ARRAY_CASE)    {selectedPolygonArray=world.getStage().cases;      }
        if(activePolygonArray == ARRAY_NONE)    {selectedPolygonArray=null; ; selectedPolygon=null;}
        return selectedPolygonArray;
    }
    public Polygon[] setActivePolygonArray(int which) {
        recreateString     = true;
        activePolygonArray = which;
        setPolygonSelected(null);
//        Gdx.app.log("POLYARY", "polygon array: " +getPolygonArrayName());
        return getActivePolygonArray();
    }

    public void setPolygonSelected(Polygon p, int polyIndex, int vertIndex){
        assetbox.setPolygon(p);
        selectedPolygon      = p;
        selectedPolygonIndex = polyIndex;
        selectedVerticeIndex = vertIndex; // ??
        recreateString       = true;

//        world.camera         = camera;
//        world.smoothCamera(selected.x, selected.y);

    }

    public void setPolygonSelected(Polygon p){
        if(p == null){
            selectedPolygon      = null;
            selectedVerticeIndex = 0;
            return;
        }

        if(setActivePolygonArray(ARRAY_CASE) != null){
            for(int i = 0; i != selectedPolygonArray.length; i++)
                if(selectedPolygonArray[i] == p) {
                    setPolygonSelected(p, i, 0);
                    return;
                }
        } // cases

        if(setActivePolygonArray(ARRAY_TRIGGER) != null){
            for(int i = 0; i != selectedPolygonArray.length; i++)
                if(selectedPolygonArray[i] == p) {
                    setPolygonSelected(p, i, 0);
                    return;
                }
        } // triggers

        if(setActivePolygonArray(ARRAY_BOUNDS) != null){
            for(int i = 0; i != selectedPolygonArray.length; i++)
                if(selectedPolygonArray[i] == p) {
                    setPolygonSelected(p, i, 0);
                    return;
                }
        } // boundaries
    }

    public void setElementSelected(Element e, int index){
        String TAG = "setElSel";
        if(selectedElement==e) {
//            Gdx.app.log(TAG, "same selection");
            assetbox.setElement(e); //update display
            return;
        }
//        Gdx.app.log(TAG, "selected " + e.name + " in " + getElementArrayName());
        changeMode(EditorTB.MODE_EDIT);
        if(activeElementArray == ARRAY_ACTORS) {
            changeInput(INPUT_EDITACTOR);
        } else {
            changeInput(INPUT_EDITPROP);
        }
        recreateString       = true;
        selectedElement      = e;
        selectedElementIndex = index;
        assetbox.setElement(e); //set mode

//        world.camera         = camera;
//        world.smoothCamera(selected.x, selected.y);

    }
    public void setElementSelected(Element e){
        recreateString = true;
        if(e == null) {
            selectedElementIndex = -1;
            selectedElement      = null;
            return;
        }

        if(this.inputMode == INPUT_EDITACTOR){
            if(setActiveElementArray(ARRAY_ACTORS) != null){
                for(int i = 0; i != selectedElementArray.size; i++){
                    if(selectedElementArray.get(i) == e){
                        setElementSelected(e, i);
                        return;
                    }
                }
            } // if(selectedElementArray != null)
        }

        if(this.inputMode==INPUT_EDITPROP){
            if(setActiveElementArray(ARRAY_PROPS) != null){
                for(int i = 0; i != selectedElementArray.size; i++){
                    if(selectedElementArray.get(i) == e){
                        setElementSelected(e, i);
                        return;
                    }
                }
            } // if(selectedElementArray != null)
        }
    }


    public boolean handleEditorTBAction(Textbox.ListItem li){
        if(li != null) {
//            Gdx.app.log(TAG, "li: " + li.display +", " + Input.Keys.toString(li.intcode)  + ", shift: " + li.boolshift);

            if(li.intcode==Input.Keys.ESCAPE){
                Gdx.app.log(TAG, "li esc");

                changeMode(EditorTB.MODE_VIEW);
                changeInput(INPUT_MOVEVIEW);
                recreateString = true;

//                setSelectNothing(); // deselect all
            }

            switch(textbox.getMode()){
                case EditorTB.MODE_SELECT :
                    this.changeInput(INPUT_SELECTANY);
                    if(li.intcode == Input.Keys.F1) {Gdx.app.log(TAG, "select all"); break;}
                    if(li.intcode == Input.Keys.F2) {setSelectNothing(); changeMode(EditorTB.MODE_VIEW); changeInput(INPUT_MOVEVIEW); break;}
                    if(li.intcode == Input.Keys.TAB) {
                        if(li.boolshift == true){
                            cycleSelection(-1);
                        } else {
                            cycleSelection(1);
                        }
                    }
                    break;
                case EditorTB.MODE_CREATE :
                    //boundary, trigger, case
                    if(li.intcode == Input.Keys.F1) {setActivePolygonArray(ARRAY_BOUNDS);  changeInput(INPUT_DRAWPOLY); break;}
                    if(li.intcode == Input.Keys.F2) {setActivePolygonArray(ARRAY_TRIGGER); changeInput(INPUT_DRAWPOLY); break;}
                    if(li.intcode == Input.Keys.F3) {setActivePolygonArray(ARRAY_CASE);    changeInput(INPUT_DRAWPOLY); break;}
                    //actor
                    if(li.intcode == Input.Keys.F4) {
                        //TODO: verify this thing
                        Gdx.app.log(TAG, "actors is null? " + (world.getStage().getActors()==null) );
//                        assetbox.changeMode(AssetTB.MODE_SELECTANI);
//                        if(world.getStage().getActors() != null){
//                        } else {
                            if(assetbox.changeMode(AssetTB.MODE_LISTANIS) == false){
                                changeMode(EditorTB.MODE_LOAD);
                            }
//                        }
                        break; }
                    //prop
                    //TODO: this should create new prop
                    if(li.intcode == Input.Keys.F5) {
//                        changeInput(INPUT_EDITPROP);
//                        if(
                        changeMode(EditorTB.MODE_LOAD);
                        break;}
                    break;
                case EditorTB.MODE_EDIT   :
                    //Move obj from rect
                    if(li.intcode == Input.Keys.F1) {changeInput(INPUT_EDITVERTBOUND);   break;}
                    // set data
                    if(li.intcode == Input.Keys.F2) {changeInput(INPUT_EDITVERTTRIGGER); break;}
                    //edit poly
                    if(li.intcode == Input.Keys.F3) {changeInput(INPUT_EDITVERTCASES);   break;}
                    //edit actor
                    if(li.intcode == Input.Keys.F4) {changeInput(INPUT_EDITACTOR);       break;}
                    //edit prop
                    if(li.intcode == Input.Keys.F5) {changeInput(INPUT_EDITPROP);        break;}

                    //cycle
                    if(li.intcode == Input.Keys.TAB) {
                        if(li.boolshift == true){
                            cycleSelection(-1);
                        } else {
                            cycleSelection(1);
                        }
                        break;
                    }
                    break;
                case EditorTB.MODE_VIEW   :
                    //select
                    if(li.intcode == Input.Keys.F1)   {changeMode(EditorTB.MODE_SELECT);
                        changeInput(INPUT_SELECTANY);       break;}
                    //create
                    if(li.intcode == Input.Keys.F2)   {changeMode(EditorTB.MODE_CREATE);   break;}
                    //edit obj
                    if(li.intcode == Input.Keys.F3)   {changeMode(EditorTB.MODE_EDIT);     break;}
                    //delete
                    if(li.intcode == Input.Keys.F4)   {changeMode(EditorTB.MODE_DELETE);   break;}
                    if(li.intcode == Input.Keys.F5)   {changeMode(EditorTB.MODE_SPAWNS);   break;}
                    if(li.boolshift){
                        if(li.intcode == Input.Keys.S){changeMode(EditorTB.MODE_SAVE);     break;}
                        if(li.intcode == Input.Keys.L){changeMode(EditorTB.MODE_LOAD);     break;}
                        if(li.intcode == Input.Keys.P){changeScreen(SCREEN_PLAY);     break;}
                    }
                    break;
                case EditorTB.MODE_DELETE:
                    if(selectedElement!=null) {
                        getActiveElementArray().removeIndex(selectedElementIndex);
                        for(Element e : selectedElementArray){
                            Gdx.app.log("del", e.name);
                        }
                        setSelectNothing();
//                        world.getStage().setActors(selectedElementArray);
                        world.getStage().setReady();
                    }
                    if(selectedPolygon!=null) {
                        Polygon[] newPoly = new Polygon[selectedPolygonArray.length-1];
                        System.arraycopy(selectedPolygonArray, 0,newPoly,0,selectedPolygonIndex-1);
                        System.arraycopy(selectedPolygonArray, selectedPolygonIndex,
                                         newPoly, selectedPolygonIndex, selectedPolygonArray.length-selectedPolygonIndex);
                        world.getStage().setReady();

                    }
                    break;
                case EditorTB.MODE_SPAWNS:
                    setSelectNothing();
                    Gdx.app.log(TAG, "wat");
                    break;
//                            case EditorTB.MODE_FILES  : return "MODE_FILES";
//                            case EditorTB.MODE_ASSETS : return "MODE_ASSETS";
//                            case EditorTB.MODE_SAVE   : return "MODE_SAVE";
            } // switch(textbox.getMode())
        } // if(li != null)
        return  false;
    }
    public EditorScreen(MyGdxGame game) {
        super(game);

        // setup the cameras
        camera = new OrthographicCamera();
        camera.setToOrtho(
                false,
                VIRTUAL_WIDTH,
                VIRTUAL_HEIGHT
        );

//        viewport = new ExtendViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);

        overlayCamera = new OrthographicCamera();
        overlayCamera.setToOrtho(
                false,
                VIRTUAL_WIDTH,
                VIRTUAL_HEIGHT
        );

        batch   = new SpriteBatch();
        sr      = new ShapeRenderer();
        textbox = new EditorTB(batch);
        textbox.setClickHandler(this);



        changeMode (EditorTB.MODE_VIEW);
        changeInput(INPUT_MOVEVIEW);

        Gdx.input.setInputProcessor(new InputProcessor() {
                int       shift      = 0; //shift pressed == 1

                int       polypoints = 0;    //drawing poly len
                Polygon   editPoly   = null; //specific poly to edit
                Polygon[] polylist   = null; //poly array to add to

                Rectangle clickRect  = new Rectangle();
                Vector3   clickPos   = new Vector3();
                Vector3   downWorld  = new Vector3();
                Vector3   dragWorld  = new Vector3();

                @Override
                public boolean keyDown(int keycode) {
                    recreateString=true;

                    //prevent changing selected mode when creating new poly
                    if(inputMode==INPUT_DRAWPOLY){
                        if(keycode>=Input.Keys.F1 && keycode<= Input.Keys.F3) return false;
                        if(keycode==Input.Keys.TAB)       if(shift>0) cycleSelection(-1); else cycleSelection(1);
                        if(keycode==Input.Keys.INSERT)    insertVert();
                        if(keycode==Input.Keys.BACKSPACE) deleteVert();
                    }

                    if(inputMode==INPUT_EDITVERTBOUND||inputMode==INPUT_EDITVERTTRIGGER||inputMode==INPUT_EDITVERTCASES){
                        if(keycode==Input.Keys.INSERT)    {polylist=getActivePolygonArray(); editPoly=selectedPolygon; insertVert();}
                        if(keycode==Input.Keys.BACKSPACE) {polylist=getActivePolygonArray(); editPoly=selectedPolygon; deleteVert();}
                    }

                    switch(keycode){
                        case Input.Keys.ESCAPE:
                            Gdx.app.log(TAG, "kd esc");
                            if(this.editPoly!=null){
                                if(polylist == null) Gdx.app.log("" , "polylist empty");
                                Gdx.app.log("", (polylist == null ? "null" : "polylist") + getPolygonArrayName() );
                                if(activePolygonArray==ARRAY_BOUNDS)   world.getStage().setBoundaries(polylist);
                                if(activePolygonArray==ARRAY_TRIGGER)  world.getStage().setTriggers  (polylist, null);
                                if(activePolygonArray==ARRAY_CASE)     world.getStage().setCases     (polylist, null);
                                editPoly=null;
                                polylist=null;
                                world.getStage().setReady();
                                assetbox.changeMode(AssetTB.MODE_STAGESUMMARY);
                            }
                            changeMode(EditorTB.MODE_VIEW);
                            changeInput(INPUT_MOVEVIEW);
                            recreateString = true;
                            break;
                        case Input.Keys.SHIFT_LEFT:
                        case Input.Keys.SHIFT_RIGHT:  shift = 1; break;
                        case Input.Keys.NUM_1:
                            camera.zoom    = 1;
                            recreateString = true;
                            break;
                        case Input.Keys.NUM_2:
                            camera.zoom    = 2;
                            recreateString = true;
                            break;
                        case Input.Keys.NUM_3:
                            camera.zoom    = 3;
                            recreateString = true;
                            break;

                        case Input.Keys.MINUS:
                            camera.zoom    *= 1.5f;
                            recreateString =  true;
                            break;
                        case Input.Keys.PLUS:
                            camera.zoom    *= 0.75f;
                            recreateString =  true;
                            break;
                        case Input.Keys.UP   : {if(selectedElement!=null) selectedElement.y+=25; break;}
                        case Input.Keys.DOWN : {if(selectedElement!=null) selectedElement.y-=25; break;}
                        case Input.Keys.LEFT : {if(selectedElement!=null) selectedElement.x-=25; break;}
                        case Input.Keys.RIGHT: {if(selectedElement!=null) selectedElement.x+=25; break;}

                        default:
                        if(handleEditorTBAction(textbox.keyDown(shift>0, keycode))) return true;
                    }

                    return false;
                } // public boolean keyDown(int keycode)

                @Override
                public boolean keyUp(int keycode) {
                    if(keycode == Input.Keys.SHIFT_LEFT ||
                       keycode == Input.Keys.SHIFT_RIGHT)
                            return (shift = 0) == 0;
                    return false;
                }

                @Override
                public boolean keyTyped(char character) {
                    return false;
                }

            float elemX, elemY;
            boolean findVertex(float x, float y){
                clicked .set      (x,   y,   0);
                camera  .unproject(clicked);
                camera  .project  (clicked);

                clickPos.set(x, y, 0);
                camera.unproject(clickPos);

                clickRect.set(clicked.x-8, clicked.y-8, 16, 16);

                if(selectedPolygon != null && selectedVerticeIndex>=0){
                    float points[] = selectedPolygon.getVertices();
                    clicked.set    (points[selectedVerticeIndex], points[selectedVerticeIndex+1], 0);
                    camera .project(clicked);

                    if(clickRect.contains(clicked.x, clicked.y)) return true;
                    for(int i = 0; i != points.length; i=i+2){
                        clicked.set    (points[i], points[i+1], 0);
                        camera .project(clicked);

                        if(clickRect.contains(clicked.x, clicked.y)){
                            Gdx.app.log("","flag");
                            setPolygonSelected(selectedPolygon, selectedPolygonIndex, i);

                            elemX=clickPos.x-clicked.x;
                            elemY=clickPos.y-clicked.y;
//                            elemX=clickPos.x-points[i];
//                            elemY=clickPos.y-points[i+1];

                            return true;
                        }
                    } // for(int i = 0; i != points.length; i=i+2)
                }

                polylist = getActivePolygonArray();
                if(polylist != null)
                    for( int n = 0; n != polylist.length; n++){
                        Polygon p = polylist[n];
                        float points[] = p.getVertices();
                        for(int i = 0; i != points.length; i=i+2){
                            clicked.set    (points[i], points[i+1], 0);
                            camera .project(clicked);

                            if(clickRect.contains(clicked.x, clicked.y)){
                                Gdx.app.log("","flag");
                                setPolygonSelected(p, n, i);

                                elemX=clickPos.x-points[i];
                                elemY=clickPos.y-points[i+1];

                                return true;
                            } else {
                                setPolygonSelected(null);
                            }
                        } // for(int i = 0; i != points.length; i=i+2)
                    } // for( int n = 0; n != polylist.length; n++)
                return false;
            }

            boolean findSelection(int screenX, int screenY, int button){
                String TAG = "SWM";
                if(button != Input.Buttons.LEFT) return false;

                clickPos.set(screenX, screenY,0);
                camera.unproject(clickPos);

                Polygon p=null;

//                if(p.contains(clickPos.x, clickPos.y)){ return true; }

                if(getActivePolygonArray() != null){
                    p = selectPolygonFromWorldXY(clickPos.x, clickPos.y);
                }

                if(p==null){
                    setActivePolygonArray(ARRAY_BOUNDS);
                    p = selectPolygonFromWorldXY(clickPos.x, clickPos.y);
                }

                if(p==null){
                    setActivePolygonArray(ARRAY_TRIGGER);
                    p = selectPolygonFromWorldXY(clickPos.x, clickPos.y);
                }

                if(p==null){
                    setActivePolygonArray(ARRAY_CASE);
                    p = selectPolygonFromWorldXY(clickPos.x, clickPos.y);
                }

                if(p != null){
                    elemX=clickPos.x;
                    elemY=clickPos.y;
                    return true;
                } else {
//                    setElementSelected(null);
                    setPolygonSelected(null);
                }

                Element e=null;
//                Gdx.app.log(TAG, "selectedElem is " + (selectedElement==null ? "null" : "set"));
//                if(e != null && selectedElement.contains(clickPos.x, clickPos.y)) return true;
//
                if(e==null && getActiveElementArray() != null){
                    e = selectElementFromWorldXY(clickPos.x, clickPos.y);
                }

                if(e == null && (inputMode==INPUT_EDITACTOR || inputMode==INPUT_SELECTANY)){
                    Gdx.app.log(TAG, "searching actor");
                    setActiveElementArray(ARRAY_ACTORS);
                    e = selectElementFromWorldXY(clickPos.x, clickPos.y);
                }

                if(e == null && (inputMode==INPUT_EDITPROP || inputMode==INPUT_SELECTANY)){
                    Gdx.app.log(TAG, "searching props");
                    setActiveElementArray(ARRAY_PROPS);
                    e = selectElementFromWorldXY(clickPos.x, clickPos.y);
                }

                if(e != null){
                    elemX=clickPos.x-e.x;
                    elemY=clickPos.y-e.y;
                    return true;
                } else {
                    setElementSelected(null);
                }


                Gdx.app.log(TAG, "nothing found, sE is " + (selectedElement==null ? "null" : "set"));
                return false;
            }

            void    movePolyPoint(int screenX, int screenY){
                if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
                    clickPos.set(screenX,screenY,0);
                    camera.unproject(clickPos);
                    selected.x = clickPos.x;
                    selected.y = clickPos.y;
                    //drag vertice to new position
                    selectedPolygon.getVertices()[selectedVerticeIndex]   = selected.x;
                    selectedPolygon.getVertices()[selectedVerticeIndex+1] = selected.y;
                }
            }
            void deleteVert(){
                if(editPoly==null || selectedVerticeIndex<0) return;

                float[] points    = editPoly.getVertices();
                if(points.length==6) {
                    if(polylist.length > 1){
                        Gdx.app.log("", "delet poly flag");
                        Polygon[] newList = new Polygon[getActivePolygonArray().length-1];
                        System.arraycopy(getActivePolygonArray(), 0,
                                         newList,0,
                                         selectedPolygonIndex);
                        System.arraycopy(getActivePolygonArray(), selectedPolygonIndex+1,
                                         newList,selectedPolygonIndex,
                                         getActivePolygonArray().length-selectedPolygonIndex-1);
                        if(activePolygonArray==ARRAY_BOUNDS)  world.getStage().setBoundaries(newList);
                        if(activePolygonArray==ARRAY_TRIGGER) world.getStage().setTriggers  (newList, null);
                        if(activePolygonArray==ARRAY_CASE)    world.getStage().setCases     (newList, null);

                        selectedPolygon      = null;
                        selectedPolygonArray = null;
                        selectedPolygonIndex = 0;
                        selectedVerticeIndex = 0;

                    } else { // if(polylist.length > 1)
                        if(activePolygonArray==ARRAY_BOUNDS)  world.getStage().setBoundaries(null);
                        if(activePolygonArray==ARRAY_TRIGGER) world.getStage().setTriggers  (null, null);
                        if(activePolygonArray==ARRAY_CASE)    world.getStage().setCases     (null, null);
                    }
                    editPoly=null;
                    polypoints=0;
                    polylist=null;
                    selectedPolygon=null;
                    return;
                }

                float[] newPoints = new float[points.length-2];
                System.arraycopy(points, 0,                      newPoints, 0, selectedVerticeIndex);
                if(selectedVerticeIndex+2<points.length-1)
                System.arraycopy(points, selectedVerticeIndex+2, newPoints, selectedVerticeIndex, newPoints.length-selectedVerticeIndex);
                editPoly.setVertices(newPoints);
                selectedVerticeIndex-=2;
                if(selectedVerticeIndex<0) selectedVerticeIndex+=selectedPolygonArray.length;
                Gdx.app.log("", "selectedVerticeIndex " + selectedVerticeIndex);
            }

            void insertVert(){
                if(editPoly==null) return;

                float[] points    = editPoly.getVertices();
                int insertedIndex = selectedVerticeIndex+2;

                if(selectedVerticeIndex>=points.length-2 && dragging==true){
                    float[] newPoints = new float[points.length+2];
                    Gdx.app.log("", "newPoints: "+ newPoints.length+", points: "+points.length+", polypoints: "+polypoints);
                    System.arraycopy(points, 0, newPoints, 0, points.length);

                    Gdx.app.log("", "newpoint len: " + newPoints.length);
                    newPoints[insertedIndex]   = clickPos.x;
                    newPoints[insertedIndex+1] = clickPos.y;
                    editPoly.setVertices(newPoints);
                    selectedVerticeIndex=insertedIndex;
                    polypoints+=2;
                } else {
                    Gdx.app.log("", "add poly at " + selectedVerticeIndex/2);

                    Vector2 m   = new Vector2(points[selectedVerticeIndex],   points[selectedVerticeIndex+1]);
                    Vector2 n   = null;
                    if(insertedIndex+1 < points.length){
                        Gdx.app.log("" , " point flag");
                        n   = new Vector2(points[insertedIndex], points[insertedIndex+1]);
                    } else {
                        Gdx.app.log("" , "not point flag");
                        n   = new Vector2(points[0], points[1]);
//                            n   = new Vector2(points[insertedIndex+2], points[insertedIndex+3]);
                    }
                    Vector2 avg = new Vector2().set(m);
                    avg.add(n).scl(0.5f);

                    float[] newPoints = new float[points.length+2];
                    Gdx.app.log("", "newPoints: "+ newPoints.length+", points: "+points.length+", polypoints: "+polypoints);
                    System.arraycopy(points, 0, newPoints, 0, selectedVerticeIndex+2);
                    newPoints[insertedIndex+0] = avg.x;
                    newPoints[insertedIndex+1] = avg.y;
                    System.arraycopy(points, insertedIndex, newPoints, insertedIndex+2, points.length-selectedVerticeIndex-2);
                    editPoly.setVertices(newPoints);
                    selectedVerticeIndex=insertedIndex;
                }

            }

            boolean drawPolygon(float x, float y, int pointer, int button){
                clickPos.set(x,y,0);
                camera.unproject(clickPos);

                if(editPoly==null){
                    Gdx.app.log("", "drawpoly flag");

                    Polygon[] oldBoundaries = getActivePolygonArray();
                    Polygon[] newBoundaries = new Polygon[oldBoundaries==null ? 1 : oldBoundaries.length+1];

                    //copy elements, idk if this is a memory leak
                    if(oldBoundaries != null && oldBoundaries.length >= 0)
                        System.arraycopy(oldBoundaries, 0, newBoundaries, 0, oldBoundaries.length);

                    float[] points = new float[] {  clickPos.x,clickPos.y,
                                                    clickPos.x,clickPos.y,
                                                    clickPos.x,clickPos.y };
                    editPoly = new Polygon(points);
                    newBoundaries[oldBoundaries == null ? 0 : oldBoundaries.length] = editPoly;

                    polypoints      = 2;
                    polylist        = newBoundaries;

                    selectedPolygon      = editPoly;
                    selectedVerticeIndex = 0;
                    return true;
                } else {
                    Gdx.app.log("", "second flag " + getPolygonArrayName());
                    float[] points = editPoly.getVertices();

                    for(int i = 0; i != points.length; i+=2){
                        if(    (Math.abs(clickPos.x-points[i]  ) < 17)
                            && (Math.abs(clickPos.y-points[i+1]) < 17)
                        ){
                            selectedVerticeIndex=i;
                            return true;
                        }
                    }

                    if(selectedVerticeIndex+2<points.length){
                        selectedVerticeIndex+=2;
                        points[selectedVerticeIndex]   = clickPos.x;
                        points[selectedVerticeIndex+1] = clickPos.y;
                    } else {
                        Gdx.app.log("", "insert flag");
                        insertVert();
                    }
//                    if(polypoints>=points.length){
//                        float[] newPoints = new float[polypoints+2];
//                        System.arraycopy(points, 0, newPoints, 0, points.length);
//
//                        editPoly.setVertices(newPoints);
//                        points = editPoly.getVertices();
//                    }
//                    selectedVerticeIndex=polypoints;
//
//                    points[polypoints]   = clickPos.x;
//                    points[polypoints+1] = clickPos.y;
//                    polypoints+=2;

//                    selectedPolygon = editPoly;
                }
                return false;
            }

            boolean moveSpawnPoint(float x, float y){
                clickPos.set(x, y, 0f);
                camera.unproject(clickPos);
                world.getStage().spawn.set(clickPos.x, clickPos.y);
                world.getStage().getPlayer().setPos(world.getStage().spawn);
                return true;
            }

            float lastX, lastY;
            float distX, distY;
            boolean dragging = false;
            boolean draggingView = false;
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if(textbox .hitbox().contains(screenX, screenY))  return textbox .click(screenX,screenY,button);
                if(assetbox.hitbox().contains(screenX, screenY))  return assetbox.click(screenX,screenY,button);

                downWorld.set(camera.position.x, camera.position.y, 0);
                lastX        = screenX;
                lastY        = screenY;
                dragging     = true;
                draggingView = button==Input.Buttons.MIDDLE;

                if(button != Input.Buttons.LEFT)                  return false;

                reusable.set(screenX, screenY,0);
                camera.unproject(reusable);
                selected.x = reusable.x;
                selected.y = reusable.y;

                //TODO: add mouse click function for other modes

                if(textbox.getMode() == EditorTB.MODE_EDIT) {
                    if(inputMode == INPUT_EDITACTOR)       return this.findSelection(screenX, screenY, button);
                    if(inputMode == INPUT_EDITPROP)        return this.findSelection(screenX, screenY, button);
                    if(inputMode == INPUT_EDITVERTBOUND)   return this.findVertex(screenX, screenY);
                    if(inputMode == INPUT_EDITVERTTRIGGER) return this.findVertex(screenX, screenY);
                    if(inputMode == INPUT_EDITVERTCASES)   return this.findVertex(screenX, screenY);
                }

                if(textbox.getMode() == EditorTB.MODE_CREATE && inputMode == INPUT_DRAWPOLY)  return this.drawPolygon(screenX, screenY, pointer, button);
                if(textbox.getMode() == EditorTB.MODE_SELECT && inputMode == INPUT_SELECTANY) return this.findSelection(screenX, screenY, button);
                if(textbox.getMode() == EditorTB.MODE_SPAWNS) return this.moveSpawnPoint(screenX, screenY);
                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if(dragging == false){return true;}

                    dragWorld.x = screenX-lastX;
                    dragWorld.y = screenY-lastY;
                    lastX = screenX;
                    lastY = screenY;

                    reusable.set(screenX, screenY, 0);
                    camera.unproject(reusable);
                    selected.x = reusable.x ;
                    selected.y = reusable.y ;

                if(draggingView){
                    if(shift>0){ camera.zoom += dragWorld.y/100f; }
                    downWorld.x -= dragWorld.x*camera.zoom;
                    downWorld.y += dragWorld.y*camera.zoom;
                    camera.position.set(downWorld);
                    return true;
                }


                if(selectedElement != null && (inputMode == INPUT_EDITACTOR || inputMode == INPUT_EDITPROP) ) {
//                    if(Math.abs(dragWorld.x) > 10*camera.zoom || Math.abs(downWorld.y) > 10*camera.zoom) return false; // HAX: sometimes deltaX = > 30 .. 160 idk why
                    reusable.set(screenX, screenY,0);
                    camera.unproject(reusable);
                    selectedElement.x = selected.x - elemX;
                    selectedElement.y = selected.y - elemY;
                    assetbox.setElement(selectedElement); //update text
                } else
                if(    selectedPolygon != null
                    && (inputMode==INPUT_EDITVERTBOUND || inputMode==INPUT_EDITVERTCASES || inputMode==INPUT_EDITVERTTRIGGER || inputMode==INPUT_DRAWPOLY)
                ) {
                    //drag vertice to new position
                    selectedPolygon.getVertices()[selectedVerticeIndex]   = selected.x; // - elemX;
                    selectedPolygon.getVertices()[selectedVerticeIndex+1] = selected.y; // - elemY;
                }
//                    if(selectedElement != null && textbox.getMode() == EditorTB.MODE_EDIT) this.
                return false;
            }



            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                dragging=false;
                if(draggingView) draggingView=false;

                if(button!=Input.Buttons.LEFT) return false;

                Gdx.app.log("", assetbox.getModeText());
                //EditBox doestn have touchUp action, prevent from autoscrolling
                if(textbox .hitbox().contains(screenX, screenY))  return true;// textbox .click(screenX,screenY,button);
                if( assetbox.getMode() != AssetTB.MODE_ELEMENT
                 && assetbox.hitbox().contains(screenX, screenY))  return true;//assetbox.click(screenX,screenY,button);


                if(selectedElement != null
                    && (inputMode==INPUT_EDITACTOR || inputMode==INPUT_EDITPROP)
                ){
                    selectedElement.x = round(selectedElement.x);
                    selectedElement.y = round(selectedElement.y);
                    assetbox.update();
                    selected.set (selectedElement.x + (selectedElement.width/2), selectedElement.y + (selectedElement.height/2));
                    world.camera         = camera;
                    world.smoothCamera(selected.x, selected.y);
                }

                if(selectedPolygon != null
                    && (inputMode==INPUT_EDITVERTBOUND || inputMode==INPUT_EDITVERTTRIGGER || inputMode==INPUT_EDITVERTCASES)
                ){
                    world.camera         = camera;
                    selected.set(
                      (selectedPolygon.getVertices()[selectedVerticeIndex]),
                      (selectedPolygon.getVertices()[selectedVerticeIndex+1]));
                    world.camera         = camera;
                    world.smoothCamera(selected.x, selected.y);

                }
                return false;
            }

            @Override
                public boolean mouseMoved(int screenX, int screenY) {
                    assetbox.mouseMoved(screenX, screenY);
                    textbox .mouseMoved(screenX, screenY);
                    return false;
                }

                @Override
                public boolean scrolled(int amount) {
                    return false;
                }
            });

        config   = game.configuration;
        r        = game.resources;
        world    = new World(r);
        assetbox = new AssetTB(batch, world);
        assetbox.setClickHandler(this);

        try{
            loadFileStage(config.getStageFile());

            world.start(r);

            assetbox.setStage(world.getStage());
            world.camera = camera;
            player = world.getStage().getPlayer();
            if(player == null) System.out.println("player is left null");
        } catch(Exception ex){
            ex.printStackTrace();
            textbox.setText("Error:\n"+ex.getMessage()+'\n'+ex.getStackTrace()[0].getFileName()+" at " + ex.getStackTrace()[0].getLineNumber()+"\n\nPress ≡ to continue");
        }


    }



    public void selectFile(FileHandle path){
        if(textbox.getMode() == EditorTB.MODE_LOAD) loadFile(path);
        if(textbox.getMode() == EditorTB.MODE_SAVE) saveStageFile(path);
    }

    Element loadFileProp(FileHandle image){
        Texture texture = new Texture(image);
        Sprite sprite   = new Sprite(texture);
        Element e       = new Element(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2, texture.getWidth(), texture.getHeight(), 0, sprite, "noname");

        world.getStage().addProp(e);
        changeMode(EditorTB.MODE_EDIT);
        changeInput(INPUT_EDITPROP);
        setElementSelected(e);

        return e;

    }

    void loadFileStage(final FileHandle stagefile){
        if(stagefile.exists() == false) {
            Gdx.app.log(TAG, stagefile.name() + " not found");

            setSelectNothing();

            Stage stage = new Stage();
            r.setStage(stage);
            world.setStage(stage);

            editingStage = "";
            assetbox.setStage(stage);
            config.setStageFile("");
            setTextFuture("Stage file " + stagefile.name() + ", not found\nLoaded empty stage\n\nPress ≡");
            return;
        }

        config.setStageFile(stagefile.path());
        Gdx.app.log(TAG, "Loading stage " + stagefile.path() );

        setSelectNothing();
        Stage newstage = new Stage(){
            @Override
            public String getStageFilePath() {
                return stagefile.path();
            }

            @Override
            public void setReady() {
                super.setReady();
            }

            @Override
            public void update(float delta) {
                super.update(delta);
            }

            @Override
            public void draw(SpriteBatch batch) {
                super.draw(batch);
            }

            @Override
            public Element getPlayer() {
                return super.getPlayer();
            }

            @Override
            public void setPlayer(Element player) {
                super.setPlayer(player);
            }

            @Override
            public Array<Element> getActors() {
                return super.getActors();
            }

            @Override
            public void setActors(Array<Element> elements) {
                super.setActors(elements);
            }

            @Override
            public void addActor(Element element) {
                super.addActor(element);
            }

            @Override
            public Element getPropByName(String name) {
                return super.getPropByName(name);
            }

            @Override
            public Array<Element> getProps() {
                return super.getProps();
            }

            @Override
            public void setProps(Array<Element> elements) {
                super.setProps(elements);
            }

            @Override
            public void addProp(Element prop) {
                super.addProp(prop);
            }
        };

        r.setStage(newstage);
        world.setStage(newstage);
        editingStage = stagefile.name();
        assetbox.setStage(newstage);
        setTextFuture("Loaded stage file: " + stagefile.path() + "\n\nPress ≡");

    }

    Element loadFileActor(FileHandle file){
        try{
            Element e = new Element(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, file.nameWithoutExtension());
            changeMode(EditorTB.MODE_EDIT);
            changeInput(INPUT_EDITACTOR);
            setElementSelected(e);
        }catch(Exception ex){
            ex.printStackTrace();
            textbox.setText(ex.getStackTrace()[0].toString());
        }
        return null;
    }

    void loadFile(FileHandle path){
        Gdx.app.log("", "recv " + path);
        try{
            String extension = path.extension().toLowerCase();
            if(   "jpg".equals(extension)
               || "png".equals(extension)
            ) {
                loadFileProp(path);
            }
            if(   "xml".equals(extension))
                loadFileStage(path);
            if(   "ani".equals(path.parent().name())){
                Gdx.app.log(TAG, "ani file");
                loadFileActor(path);
            }

        } catch(Exception ex){
            ex.printStackTrace();
            textbox.setText("Error:\n"+ex.getMessage()+'\n'+ex.getStackTrace()[0].getFileName()+" at " + ex.getStackTrace()[0].getLineNumber()+"\n\nPress ≡ to continue");

        }
    }

    void saveStageFile(final FileHandle path){

        Gdx.app.log(TAG, "saving to: "+ path.path());
        Input.TextInputListener til = new Input.TextInputListener() {
            String stringify(float [] points){
                StringBuilder s = new StringBuilder();
                for(float p : points) s.append(Math.round(p)).append(",");
                return s.toString();
            }
            @Override
            public void input(String text) {
                Gdx.app.log(TAG, "input: " + text);

                if(world.getStage() == null) {setTextFuture("Cannot save an empty stage\n\nPress ≡"); return;}

                final Textbox res   = textbox;
                StringWriter writer = new StringWriter();
                XmlWriter xml       = new XmlWriter(writer);
                try {
                    XmlWriter piece = xml.element("image")
                            .attribute("h", 100).attribute("w", 1000); //get bgimage size

                    int propscount     = 0;
                    int actorscount    = 0;
                    int triggerscount  = 0;
                    int boundaryscount = 0;
                    int casescount     = 0;

                    Stage stage = world.getStage();
                    if(stage.getProps() != null){
                        piece.element("stack");
                        for( Element prop : stage.getProps())
                            if(prop != null){
                                propscount++;
                                piece.element("layer")
                                        .attribute("name",       prop.name)
                                        .attribute("opacity",    1.0)
                                        .attribute("src",        prop.sprite.getTexture().toString())
                                        .attribute("visibility", prop.visible)
                                        .attribute("x",          prop.x)
                                        .attribute("y",          prop.y)
                                        .pop();
                            }
                        piece.pop();
                    }

                    if(stage.getActors() != null){
                        piece.element("cast");
                        for(Element actor : stage.getActors()){
                            if(actor != null){
                                actorscount++;
                                piece.element("actor")
                                        .attribute("name",      actor.name)
                                        .attribute("x",         actor.x)
                                        .attribute("y",         actor.y)
                                        .attribute("script",    actor.script)
                                        .pop();
                            }
                        }
                        piece.pop();
                    }

                    piece.element("boundary");

                        if(stage.boundaries != null){
                            for( Polygon p : stage.boundaries)
                                if(p != null){
                                    boundaryscount++;
                                    piece.element("area")
                                         .attribute("shape", "bound")
                                         .attribute("coords", stringify(p.getVertices()))
                                         .pop();
                                }
                        }

                        if(stage.triggers != null){
                            for( Polygon p : stage.triggers)
                                if(p != null){
                                    triggerscount++;
                                    piece.element("area")
                                         .attribute("shape", "trigger")
                                         .attribute("script", "nothing")
                                         .attribute("coords", stringify(p.getVertices()))
                                         .pop();
                                }
                        }

                        if(stage.cases != null){
                            for( Polygon p : stage.cases)
                                if(p != null){
                                    casescount++;
                                    piece.element("area")
                                         .attribute("shape", "case")
                                         .attribute("script", "nothing")
                                         .attribute("coords", stringify(p.getVertices()))
                                         .pop();
                                }
                        }
                    piece.pop();

                    piece.element("spawnpoint")
                            .attribute("x", stage.spawn.x)
                            .attribute("y", stage.spawn.y)
                            .pop();

                    piece.pop();


                    Gdx.app.log(TAG, "path: " + path.path());

                    FileHandle f = Gdx.files.local( path.file().getPath() +"/"+text);
                    Gdx.app.log(TAG, "path: " + f.file().getAbsolutePath());
                    f.writeString(writer.toString(), false);
                    config.setStageFile(f.path());

                    setTextFuture("Saved successfully to " + f.file().getAbsolutePath() +"\n\n"+
                                  propscount     +" props\n"+
                                  actorscount    +" actors\n"+
                                  boundaryscount +" boundaries\n"+
                                  triggerscount  +" triggers\n"+
                                  casescount     +" cases\n"+
                                  "Press ≡"
                                 );
                } catch(IOException ex) {
                    ex.printStackTrace();
                    setTextFuture("Error:\n"+ex.getMessage()+'\n'+ex.getStackTrace()[0].getFileName()+" at " + ex.getStackTrace()[0].getLineNumber()+"\n\nPress ≡ to continue");
                }
            }

            @Override
            public void canceled() {
                Gdx.app.log(TAG, "canceled");
            }
        };
        Gdx.input.getTextInput(til, "Save stage file", "stage3.xml", "");
    }

    boolean recreateString = true;
    boolean needsUpdate   = false;
    String  textUpdate;
    public void setTextFuture(String s){
        needsUpdate = true;
        textUpdate = s;
    }



    @Override
    public void show() {
        super.show();
    }


    StringBuilder printedText = new StringBuilder();
    @Override
    public void render(float delta) {
        camera .update();
        world.camera = camera;
        world  .update(delta);
        textbox.delta(delta);
        Stage s = world.getStage();

        if(needsUpdate) {
            textbox.setText(this.textUpdate);
            needsUpdate = false;
        }

        if(recreateString){
//            Element scenebg = s.getPropByName("scenebg");
            printedText.delete(0, printedText.length());
            printedText.append(editingStage).append("> ");

            printedText.append("zoom: ").append(String.format("%.2f", camera.zoom))
                       .append(" | ").append(textbox.getModeText()).append(" ");

            if(activePolygonArray != ARRAY_NONE){
                printedText.append(" actPolyA: ").append(getPolygonArrayName());
            }
            if(selectedPolygon != null)
                printedText.append(" Poly(")
                           .append(selectedPolygonIndex).append(":").append(selectedPolygonArray == null ? "-" : selectedPolygonArray.length-1)
                           .append(":").append(selectedPolygonArray==null ? "ERROR" : selectedPolygonArray.length-1).append(")");

            if(activeElementArray != ARRAY_NONE){
                printedText.append(getElementArrayName())
                           .append("(").append(selectedElementIndex<0?"-":selectedElementIndex)
                           .append(":").append(selectedElementArray==null ? "ERROR" :   selectedElementArray.size-1).append(")");
            }
            if(selectedElement != null)
                printedText.append(" Elem(")
                        .append(selectedElementIndex).append(':').append(selectedElementArray == null ? "-" : selectedElementArray.size-1)
                        .append(selectedElement == null ? "" : selectedElement.name).append(")");
            recreateString = false;
        } // if(recreateString)

        Gdx.gl.glClear( Gdx.gl20.GL_COLOR_BUFFER_BIT | Gdx.gl20.GL_DEPTH_BUFFER_BIT );

        batch.setProjectionMatrix(camera.combined);
        sr   .setProjectionMatrix(camera.combined);

        /** draw **/

        world.present(batch);

        /** bounding boxes **/
        sr.begin(ShapeRenderer.ShapeType.Line);
            if(    s != null
                && s.getActors() != null
                && (inputMode== INPUT_EDITACTOR || inputMode== INPUT_SELECTANY)
            ) {
                sr.setColor(Color.WHITE);
                for(Element el : s.getActors()) {
                    if(el == selectedElement) continue;
                    sr.rect(el.x, el.y,
                            el.width, el.height);
                }
            }

            if(    s != null
                && s.getProps() != null
                && (inputMode==INPUT_EDITPROP || inputMode==INPUT_SELECTANY)
            ) {
                sr.setColor(Color.CYAN);
//                for(Element el : s.getProps() ) {
                for(Element el : s.getProps() ) {
                    if(el == selectedElement) continue;
                    sr.rect(el.x, el.y,
                            el.width, el.height
                    );
                }
            }

            //TODO: fix on selectany they all should render, but it only renders the activePolygonArray

            sr.circle(s.spawn.x+player.getBounds().x, s.spawn.y-player.getBounds().y, player.getBounds().z);

            if(    s.boundaries != null
                && (   inputMode==INPUT_EDITVERTBOUND
                    || inputMode==INPUT_MOVEVIEW
                    || inputMode==INPUT_DRAWPOLY
                    || textbox.getMode() == EditorTB.MODE_SELECT
                    || activePolygonArray==ARRAY_BOUNDS
                )
            ) {
                sr.setColor(Color.GREEN);
                for(Polygon p : s.boundaries){
                    if(p == null) continue;
                    sr.polygon(p.getVertices());
                    sr.polygon(p.getVertices());
                }
            }

            if(    s.triggers != null
                && (   inputMode==INPUT_EDITVERTTRIGGER
                    || inputMode==INPUT_MOVEVIEW
                    || textbox.getMode() == EditorTB.MODE_SELECT
                    || inputMode==INPUT_DRAWPOLY
                    || activePolygonArray==ARRAY_TRIGGER)
            ) {
                sr.setColor(Color.LIGHT_GRAY);
                for(Polygon p : s.triggers){
                    sr.polygon(p.getVertices());
                    sr.polygon(p.getVertices());
                }
            }

            if(    s.cases != null
                && (   inputMode==INPUT_EDITVERTCASES
                    || inputMode==INPUT_MOVEVIEW
                    || textbox.getMode() == EditorTB.MODE_SELECT
                    || inputMode==INPUT_DRAWPOLY
                    || activePolygonArray==ARRAY_CASE)
            ) {
                sr.setColor(Color.FIREBRICK);
                for(Polygon p : s.cases){
                    sr.polygon(p.getVertices());
                    sr.polygon(p.getVertices());
                }
            }

            sr.setColor(Color.YELLOW);
            if(selected        != null) sr.circle(selected.x, selected.y, 10);
            if(selectedPolygon != null) sr.polygon(selectedPolygon.getVertices());
            if(selectedElement != null){
                sr.rect(selectedElement.x, selectedElement.y,
                        selectedElement.width, selectedElement.height);
            }

//            sr.setColor(Color.RED);
//            sr.rect(selected.x-7, selected.y-7,
//                    14,14);

            //            sr.circle(world.targetpos.x, world.targetpos.y, 30);
//            sr.rect(world.moveBounds.x,     world.moveBounds.y,
//                    world.moveBounds.width, world.moveBounds.height,
//                    Color.BLUE,  Color.YELLOW,Color.BLUE, Color.YELLOW);

        sr.end();

        /** overlay **/

        batch.setProjectionMatrix(overlayCamera.combined);
        batch.begin();

        if(inputMode==INPUT_DRAWPOLY && selectedPolygon != null){
            float[] points = selectedPolygon.getVertices();
            for(int i = 0; i != points.length; i = i + 2) {
                reusable.set(points[i], points[i + 1], 0);
                    camera.project(reusable);
                textbox.font.draw(batch, ""+(i/2), reusable.x, reusable.y);
            } // for(int i = 0; i != points.length; i=i+2)
        }

        if(inputMode==INPUT_EDITPROP)
            for(int i=0;i!=s.getProps().size; i++){
                Element e = s.getProps().get(i);
                reusable.set(e.x, e.y, 0);
                camera.project(reusable);
                textbox.font.draw(batch, ""+(i+1), reusable.x+2, reusable.y+17);
//                textbox.font.draw(batch, ""+(i+1), e.x+2, e.y+17);
            }
        batch.end();

        sr.setProjectionMatrix(overlayCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(Color.RED);

            if(inputMode==INPUT_DRAWPOLY && selectedPolygon != null){
                float[] points = selectedPolygon.getVertices();
                sr.setColor(Color.RED);
                for(int i = 0; i != points.length; i = i + 2) {
                    reusable.set(points[i], points[i + 1], 0);
                    camera.project(reusable);
                    if(i == selectedVerticeIndex) {
                        sr.setColor(Color.YELLOW);
                        sr.rect(reusable.x - 5, reusable.y - 5, 10, 10);
                    } else {
                        sr.setColor(Color.RED);
                        sr.rect(reusable.x - 3, reusable.y - 3, 6, 6);
                    }
                } // for(int i = 0; i != points.length; i=i+2)
            }

            Polygon[] list = getActivePolygonArray();
            if(list != null) {
                float[] points;
                if(    inputMode == INPUT_EDITVERTBOUND
                    || inputMode == INPUT_EDITVERTTRIGGER
                    || inputMode == INPUT_EDITVERTCASES
                ) {// polygon handles
                    for(Polygon p : list) {
                        if(p==null) continue;
                        points = p.getVertices();
                        for(int i = 0; i != points.length; i = i + 2) {
                            reusable.set(points[i], points[i + 1], 0);
                            camera.project(reusable);
                            if(selectedPolygon == p && i == selectedVerticeIndex) {
                                sr.setColor(Color.YELLOW);
                                sr.rect(reusable.x - 5, reusable.y - 5, 10, 10);
                            } else {
                                sr.setColor(Color.RED);
                                sr.rect(reusable.x - 3, reusable.y - 3, 6, 6);
                            }
                        } // for(int i = 0; i != points.length; i=i+2)
                    } // for(Polygon p : world.boundaries)
                }
            } // if(list != null)

        sr.end();


        batch.setProjectionMatrix(overlayCamera.combined);
        batch.begin();
            r.getFont().draw(batch,printedText.toString(),10,20);
            textbox.draw(batch);
            assetbox.draw(batch);
        batch.end();
    }

    @Override
    public void dispose() {
        r.dispose();
        batch.dispose();
        super.dispose();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        VIRTUAL_HEIGHT = Gdx.graphics.getHeight();
        VIRTUAL_WIDTH  = Gdx.graphics.getWidth();

        camera.viewportHeight = height;
        camera.viewportWidth  = width;

        camera.update();

        overlayCamera.setToOrtho(
                false,
                VIRTUAL_WIDTH,
                VIRTUAL_HEIGHT
        );
        textbox.update();
        assetbox.update();
        this.render(0);
    }
}
