package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

import java.io.StringWriter;

public class Configuration {
    String TAG = "Configuration";

    JsonValue config;

    public Configuration() {
        config = readConfig();
        Gdx.app.log(TAG, "loaded config file\n"+config );
    }

    JsonValue readConfig(){
        FileHandle fhconfig= Gdx.files.local("config.json");
        return new JsonReader().parse(fhconfig.readString());
    }

    void writeConfig(String name, Object value){
        FileHandle fhconfig= Gdx.files.local("config.json");
        Json config = new Json(JsonWriter.OutputType.json);
        config.setWriter(new JsonWriter(new StringWriter()));

        config.writeObjectStart();
        config.writeValue(name, value);
        config.writeObjectEnd();

        fhconfig.writeString( config.getWriter().getWriter().toString() , false);
        Gdx.app.log(TAG, "created config file\n" + config.getWriter().getWriter().toString());
    }

    String stageFile = "editing";
    public FileHandle getStageFile()            { return new FileHandle(config.getString(stageFile)); }
    public void       setStageFile(String file) { writeConfig(stageFile, file); }


}
