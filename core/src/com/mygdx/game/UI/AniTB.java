package com.mygdx.game.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Screens.AniEditorScreen;

public class AniTB extends Textbox {
    public AniEditorScreen aniedit;

    int mode;
    public final static int MODE_LISTANI     = 1;
    public final static int MODE_LISTSPRITES = 2;
    public final static int MODE_SPRITEEDIT  = 3;

    public AniTB(SpriteBatch b) {
        super(b);
        changeMode(MODE_LISTANI);
    }

    public int getMode(){return mode;}

    public String getModeText(){
        switch(mode){
            case MODE_LISTANI:     return "MODE_LISTANI";
            case MODE_LISTSPRITES: return "MODE_LISTSPRITES";
            case MODE_SPRITEEDIT:  return "MODE_SPRITEEDIT";
        }
        return "unknown";
    }




    public void changeMode(int newMode){
        mode = newMode;
        Gdx.app.log(TAG, "newMode: " + getModeText());

        switch(mode){
            case MODE_LISTANI:
                listAniFiles();
                break;
            case MODE_LISTSPRITES:
                listSpriteFiles();
                break;
            case MODE_SPRITEEDIT:
                listElementData();
                break;
        }
    }

    void listElementData(){
        aniedit.updateInternalFromElement();
    }

    void listSpriteFiles(){
        FileHandle[] anis = Gdx.files.internal("spr").list();

        clearText();
        listHeader = "Sprites";
        if(anis.length == 0){
            addListItem("No sprites found", null);
        } else {
            for(FileHandle f : anis){
                addListItem(f.name(), f.name());
            }
        }
        update();
    }

    void listAniFiles(){
        FileHandle[] anis = Gdx.files.internal("ani").list();

        listHeader = "Ani Files";
        addListItem("Create new Ani", new FileHandle("ani/new") );
        for(FileHandle f : anis){
            addListItem(f.name(), f);
        }
        update();
    }

    @Override
    public boolean click(int screenX, int screenY, int button) {
        if(mouseIndex >= 0 && mouseIndex < shownItems.size){
           aniedit.click(shownItems.get(mouseIndex));
        }
        return false;
    }
}
