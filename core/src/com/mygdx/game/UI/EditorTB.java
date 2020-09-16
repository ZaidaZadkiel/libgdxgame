package com.mygdx.game.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.game.Screens.EditorScreen;

/*
* TODO: asset manager, either on this class or a stand alone class to show what is loaded (1,in memory and 2,in stage file)
*       figure out the assets management so that i can query what is there or not
* */

public class EditorTB extends Textbox {
    static String TAG = "EditorTB";

    int editorMode = 0;
    public final static int MODE_SELECT = 1;
    public final static int MODE_CREATE = 2;
    public final static int MODE_EDIT   = 3;
    public final static int MODE_DELETE = 4;
    public final static int MODE_VIEW   = 5;
    public final static int MODE_LOAD   = 6;
    public final static int MODE_ASSETS = 7;
    public final static int MODE_SAVE   = 8;
    public final static int MODE_SPAWNS = 9;

    ShapeRenderer      sr;
    OrthographicCamera camera;
    public EditorTB(SpriteBatch b) {
        super(b);
        camera = new OrthographicCamera(super.width, super.height);
        camera.position.set(Math.round(super.width/2f), Math.round(super.height/2f), 0);
        sr     = new ShapeRenderer();
//        setPath(new FileHandle("data"), "");
        setMode(MODE_VIEW);
    }

    EditorScreen editorScreen;
    public void setClickHandler(EditorScreen editor){
        editorScreen = editor;
    }



    boolean setPath(FileHandle newPath, String filter){
//        Gdx.app.log(TAG+":setPath", "newPath: " + newPath.path());
        if(newPath.path().equals("/")) newPath = new FileHandle(".");
        if(newPath.path().equals(""))  newPath = new FileHandle(".");

        parent = newPath.parent();
        path   = newPath;
        if(parent == null || parent.path().equals(""))  parent = new FileHandle(".");

        fileHandles = path.list();
        printFileList(filter);
        if(fileHandles==null || fileHandles.length==0) return false;
        parent = path.parent();
//        Gdx.app.log(TAG+":setPath", "parent: " + parent.path() + ", path: " + path.path());

        return true;
    }
    FileHandle parent = new FileHandle(".");
    FileHandle path   = new FileHandle("data");
    public boolean click(int screenX, int screenY, int button){
//        Gdx.app.log(TAG, "index: " + mouseIndex);
        if(mouseIndex >= 0 && mouseIndex < shownItems.size){
            ListItem li = shownItems.get(mouseIndex);
//            Gdx.app.log(TAG, "? " + (li==null ? "null" : li.display));
            if(editorMode == MODE_LOAD) return  clickLoad((FileHandle) li.variant, button);
            if(editorMode == MODE_SAVE) return  clickSave((FileHandle) li.variant, button);

//            if(editorMode == MODE_CREATE && li.intcode==Input.Keys.F5) setMode(MODE_LOAD);//clickLoad(new FileHandle("."), button);//  return clickCreateProp();
//            Gdx.app.log(TAG, "li.shift: " + li.boolshift);
            editorScreen.handleEditorTBAction(li);
        }
        return false;
    }

    boolean clickCreateProp(){
        return true;
    }

    public boolean clickSave(FileHandle f, int button){
        // navigate
        Gdx.app.log("", "f: " + f.path());
        if(f.path().equals(path.path())) editorScreen.selectFile(f);
        if(f.isDirectory()){
            return setPath(f, "directory");
        }
        return true;
    }

    public boolean clickLoad(FileHandle f, int button){
        if(f.isDirectory()){
            return setPath(f, "");
        }
        editorScreen.selectFile(f);
        return true;
    }


//    StringBuilder     filelist   = new StringBuilder();
//    Array<FileHandle> shownFiles = new Array<FileHandle>(20);
    FileHandle[]      fileHandles;

    String printFileList(String filter){
//        shownFiles.clear();
        super.clearText();

        super.listHeader = "Load file";
        if(getMode() == MODE_SAVE) {
            super.listHeader = "Save file"; //default is load, overwrite on save
            super.addListItem("<Select directory>", path);
        }
        super.addListItem("..",    path.parent());

        for(FileHandle f : fileHandles) {
            String displayname = f.name();
            if(     filter.equals("directory"))    if(!f.isDirectory()) continue;
//            if(    !filter.equals("")
//                && !filter.equals(f.extension()))  continue;

            if(f.isDirectory()) displayname +="/";
            super.addListItem(displayname, f);

//            shownFiles.add(f);
        }

        update();
        return "";
    }

//    String printFileList2(String filter){
//        filelist.setLength(0);
//        filelist.append("┌─┤").append(path).append(String.format("%1$"+(24-path.name().length())+"s"," ")).append("├─╖\n");
//
//        if(filter.equals("directory")) filelist.append("│ <select this path>").append(String.format("%11s", "║\n"));
//        filelist.append("│").append("..").append(String.format("%28s", "║\n"));
//
//        shownFiles.clear();
//        for(FileHandle f : fileHandles) {
//            String displayname = f.name();
//            if(     filter.equals("directory"))    if(!f.isDirectory()) continue;
////            if(    !filter.equals("")
////                && !filter.equals(f.extension()))  continue;
//
//            if(f.isDirectory()) displayname +="/";
//            int len = 24; // text box width
//            if( displayname.length() > len ) displayname = displayname.substring(0,len)+"(..)";
//            filelist.append("│").append(displayname).append(String.format("%"+(30-displayname.length())+"s", "║\n"));
//            shownFiles.add(f);
//        }
//
//        filelist.append("╘════════════════════════════╝");
//        setText(filelist.toString());
//        return filelist.toString();
//    }

    public String getModeText(){
        switch(editorMode){
            case MODE_SELECT : return "MODE_SELECT";
            case MODE_CREATE : return "MODE_CREATE";
            case MODE_EDIT   : return "MODE_EDIT";
            case MODE_DELETE : return "MODE_DELETE";
            case MODE_VIEW   : return "MODE_VIEW";
            case MODE_LOAD   : return "MODE_FILES";
            case MODE_ASSETS : return "MODE_ASSETS";
            case MODE_SAVE   : return "MODE_SAVE";
            case MODE_SPAWNS : return "MODE_SPAWNS";
        }
        return "Unknown mode";
    }

    public void setMode(int newMode){
        // TODO: fix file list mode to create list properly
        // figure out whether to add a second drawing function

        editorMode = newMode;
        Gdx.app.log("x", "setMode " + getModeText());
        super.clearText();
        switch(editorMode){
            case MODE_LOAD:
                setPath(path,"");
                printFileList("");
                break;
            case MODE_SAVE:
                setPath(path,"directory");
                printFileList("directory");
                break;
            case MODE_CREATE:
                super.clearText();
                super.listHeader = "CREATE";
                super.addListItem(false, Input.Keys.F1, "boundary");
                super.addListItem(false, Input.Keys.F2, "trigger");
                super.addListItem(false, Input.Keys.F3, "case");
                super.addListSeparator();
                super.addListItem(false, Input.Keys.F4, "actor");
                super.addListItem(false, Input.Keys.F5, "prop");
                super.addListSeparator();
                super.addListItem(false,  Input.Keys.ESCAPE, "BACK");
                super.update();
                break;
            case MODE_DELETE:
                super.clearText();
                super.listHeader = "DELETE";
                super.addListItem(false, Input.Keys.F1,      "object");
                super.addListItem(false, Input.Keys.F2,      "data");
                super.addListSeparator();
                super.addListItem(false, Input.Keys.F9,      "revert all");
                super.addListSeparator();
                super.addListItem(false,  Input.Keys.ESCAPE, "BACK");
                super.update();
                break;
            case MODE_EDIT:
                super.clearText();
                super.listHeader = "EDIT";
                super.addListItem(false, Input.Keys.F1,  "boundary");
                super.addListItem(false, Input.Keys.F2,  "trigger");
                super.addListItem(false, Input.Keys.F3,  "case");
                super.addListSeparator();
                super.addListItem(false, Input.Keys.F4,  "actor");
                super.addListItem(false, Input.Keys.F5,  "prop");
                super.addListSeparator();
                super.addListItem(false, Input.Keys.TAB, "next obj");
                super.addListItem(true,  Input.Keys.TAB, "prev obj");
                super.addListSeparator();
                super.addListItem(false,  Input.Keys.ESCAPE, "BACK");
                super.update();
                break;

            case MODE_SELECT:

                super.clearText();
                super.listHeader = "SELECT";
//                super.addListItem(false, Input.Keys.F1,    "sel all");
                super.addListItem(false, Input.Keys.F2,    "sel none");
                super.addListItem(false, Input.Keys.TAB,   "sel next");
                super.addListItem(true,  Input.Keys.TAB,   "sel prev");
                super.addListSeparator();
                super.addListItem(false,  Input.Keys.ESCAPE, "BACK");
                super.update();
                break;
//            default:
            case MODE_VIEW:
                super.clearText();
                super.listHeader = "MAIN";
                super.addListItem(false, Input.Keys.F1, "SELECT");
                super.addListItem(false, Input.Keys.F2, "create");
                super.addListItem(false, Input.Keys.F3, "edit obj");
                super.addListItem(false, Input.Keys.F4, "delete");
                super.addListItem(false, Input.Keys.F5, "spawn pt");
                super.addListSeparator();
                super.addListItem(true,  Input.Keys.L, "load");
                super.addListItem(true,  Input.Keys.S, "save");
                super.addListItem(true,  Input.Keys.P, "play");
                super.update();
                break;
            case MODE_SPAWNS:
                super.clearText();
                super.listHeader = "SPAWN";
                super.addListItem(false, Input.Keys.F1,     "MOVE");
                super.addListItem(false, Input.Keys.ESCAPE, "BACK");
                super.update();
                break;
        } // switch(editorMode)
//        update();
    }


    public int getMode(){ return editorMode; }

}
