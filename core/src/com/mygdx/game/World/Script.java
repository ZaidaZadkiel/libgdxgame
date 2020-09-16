package com.mygdx.game.World;

import com.badlogic.gdx.files.FileHandle;

/*
* script:
* type   actor, trigger, case
*
* actor
*   moves guy on screen (cutscenes)
*   dialogs and trade items
*   conditions of events, set/read conditions
*
* triggers unconditionally run
*   change scene*     - enter door
*   activate cutscene - turn on lights
*   actor walk path*  - move actor through the boundary of open/closed poly
*
* cases conditional run
*   change scene      - got key ? open door
*   activate cutscene - condition met ? run play
*   do things once    - is character in a spot ? run play
*
* player walks around, on each movement has a check for which polygons do they touch
* if its a trigger, executes the command
*
* ex: walk to an exit of the stage, load the next stage
* ex: on a stage with several grounds, toggle visibility between one and the others
* // this might be done automatically
* */

public class Script {
    FileHandle file;

    public Script(FileHandle file) {
        this.file = file;
    }

    public Script(String path) {
        file = new FileHandle(path);
    }

    void read(){
        String s = file.readString();
    }


}
