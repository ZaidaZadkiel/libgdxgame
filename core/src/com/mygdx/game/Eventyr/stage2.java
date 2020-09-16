package com.mygdx.game.Eventyr;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Resources;
import com.mygdx.game.World.Element;
import com.mygdx.game.World.Stage;

public class stage2 extends Stage {
    @Override
    public String getStageFilePath(){return "data/scene2.xml";}

    public stage2() {
        super();
    }

    public stage2(Resources resources) {
        super(resources);
    }

    @Override
    public void setReady() {

    }

    @Override
    public void update(float delta) {

    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);
    }

    @Override
    public Element getPlayer() {
        return null;
    }

    @Override
    public void setPlayer(Element player) {

    }


}
