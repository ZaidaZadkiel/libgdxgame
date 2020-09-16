package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.UI.AniTB;
import com.mygdx.game.UI.Textbox;
import com.mygdx.game.World.Element;

public class AniEditorScreen extends DefaultScreen implements InputProcessor {
    static final String TAG = "AniEditor";

    AniTB anibox;
    public  Element editable;
    private Element[] previews;

    //libGdx
    private SpriteBatch        batch;
    private OrthographicCamera camera;
    private OrthographicCamera overlayCamera;
    private ShapeRenderer      sr;
    private BitmapFont         font;
    private GlyphLayout        textOverlay;

    private static final int VIRTUAL_WIDTH  = 800;
    private static final int VIRTUAL_HEIGHT = 400;

    boolean aniboxNeedsUpdate = false;

    Sprite sprite;
    Array<Rectangle> sets;
    Array<Array<Rectangle>> allFrames = new Array<Array<Rectangle>>();

    public void updateInternalFromElement(){
        anibox.clearText();
        anibox.listHeader="Element Data";
        anibox.addListItem("Name: " + editable.name,                  editable.name);
        anibox.addListItem("Frame:"+ (  editable.anim.framesetCount
                                       * editable.anim.frameCount),   null);
        anibox.addListItem("sets: " + editable.anim.framesetCount,    editable.anim.framesetCount);
        anibox.addListItem("line: " + editable.anim.frameCount,       editable.anim.frameCount);
        anibox.addListItem("delay:" + editable.anim.frameTime,        editable.anim.frameTime);
        anibox.addListSeparator();
        anibox.addListItem("<Save>", -1);
        aniboxNeedsUpdate = true;
    }

    public void updateElementFromInternal(){
        // this must be sync'd to above function bcz the getListItem is dependant on index from add order
        editable.name               =                  anibox.getListItem(0).variant.toString();
        editable.anim.framesetCount = Integer.parseInt(anibox.getListItem(2).variant.toString());
        editable.anim.frameCount    = Integer.parseInt(anibox.getListItem(3).variant.toString());
        editable.anim.frameTime     = Float.parseFloat(anibox.getListItem(4).variant.toString());

        editable.anim.splitFrames(editable.sprite);
        editable.width              = editable.anim.frameWidth;
        editable.height             = editable.anim.frameHeight;

        genPreviews();
        updateInternalFromElement();
    }

    void genPreviews(){
        Gdx.app.log(TAG, "prevs");
        if(editable.anim.frameCount>0 && editable.anim.framesetCount>0){
            previews = new Element[4];
            previews[0] = getPreviewElement();
            previews[0].anim.anim_action   = 1;
            previews[0].anim.framesetIndex = 0;
            previews[1] = getPreviewElement();
            previews[1].x +=  64+10;
            previews[1].anim.anim_action   = 1;
            previews[1].anim.framesetIndex = 1;
            previews[2] = getPreviewElement();
            previews[2].x += 128+20;
            previews[2].anim.anim_action   = 1;
            previews[2].anim.framesetIndex = 2;
            previews[3] = getPreviewElement();
            previews[3].x += 182+30;
            previews[3].anim.anim_action   = 1;
            previews[3].anim.framesetIndex = 3;
        }
    }

    Element getPreviewElement(){
        Element e = new
            Element( Math.round(Gdx.graphics.getWidth()/2.0)-editable.width, Gdx.graphics.getHeight()-10-editable.height,
                     editable.width,
                     editable.height,
                     editable.anim.frameCount,
                     editable.sprite,
                     editable.name);
        e.anim.frameTime = editable.anim.frameTime;
        return e;
    }

    public AniEditorScreen(MyGdxGame game) {
        super(game);

        // setup the cameras
        camera = new OrthographicCamera();
        camera.setToOrtho(
                false,
                VIRTUAL_WIDTH,
                VIRTUAL_HEIGHT
        );

        overlayCamera = new OrthographicCamera();
        overlayCamera.setToOrtho(
                false,
                VIRTUAL_WIDTH,
                VIRTUAL_HEIGHT
        );

        batch          = new SpriteBatch();
        sr             = new ShapeRenderer();
        font           = new BitmapFont();
        textOverlay    = new GlyphLayout(font, "test");
        anibox         = new AniTB(batch);
        anibox.aniedit = this;
//        anibox.

        Gdx.input.setInputProcessor(this);

    }


    void loadElement(Textbox.ListItem li){
        if(li != null){
            Gdx.app.log(TAG, li.display);
            FileHandle anifile = (FileHandle)li.variant;

            if(anifile.exists() == true){
                editable = new Element(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, anifile.name());
            } else {
                editable = new Element();
            }
            if(editable.sprite == null) anibox.changeMode(AniTB.MODE_LISTSPRITES);
        }
        update();
    }

    void loadSprite(Textbox.ListItem li){
        if(editable != null){
            sprite = new Sprite(new Texture(Gdx.files.internal("spr/" + li.display)));;
            editable   = new Element((VIRTUAL_WIDTH  + editable.width)  / 2,
                                     (VIRTUAL_HEIGHT + editable.height) / 2,
                                     64, 64, 8, sprite, li.display.split("\\.")[0]);
//                                     spr.getWidth(), spr.getHeight(), 0, spr, li.display.split("\\.")[0]);
            genPreviews();
            anibox.changeMode(AniTB.MODE_SPRITEEDIT);
        } else {
            anibox.setText("Error: Element is null");
        }
        update();
    }

    void modifyValue(final Textbox.ListItem li){
//        li.variant
        if(li.variant == null) return;

        Gdx.input.getTextInput(new Input.TextInputListener() {
            @Override
            public void input(String text) {
                li.variant = text;
                updateElementFromInternal();
            }

            @Override
            public void canceled() {

            }
        }, "Enter new value for " + li.display, li.variant.toString(), "");
    }

    public void click(Textbox.ListItem li){
        switch(anibox.getMode()){
            case AniTB.MODE_LISTANI:     loadElement(li); break;
            case AniTB.MODE_LISTSPRITES: loadSprite (li);  break;
            case AniTB.MODE_SPRITEEDIT:
                if(li.variant instanceof Integer && (Integer)li.variant == -1){
                   Gdx.app.log(TAG, "save");
                   saveElement();
                }else{ modifyValue(li); }
                break;
        }
    }

    void saveElement(){
        if(editable == null) return;

        FileHandle file = Gdx.files.local("ani/" + editable.name);
        StringBuilder builder = new StringBuilder();
        builder.append(editable.name).append("\t").append(editable.script).append("\n");
        builder.append(((FileTextureData)editable.sprite.getTexture().getTextureData()).getFileHandle().path()).append("\n");
        builder.append(editable.speed).append("\t");
        builder.append(editable.anim.frameCount).append("\t").append(editable.anim.framesetCount).append('\t').append(editable.anim.frameTime).append("\n");
        //frameset stops
        builder.append(2).append(' ').append(3).append(' ').append(6).append(' ').append(8).append("\n"); //0..2: starting, 3..6: walking, 6..8: stopping
        builder.append("up").append(" ").append("down").append(" ").append("left").append(" ").append("right"); //frameset order names
        builder.append("\n");
        file.writeString(builder.toString(), false);
    }

    void updateText(){
        textOverlay.setText(font, "sprite is " + (editable.sprite == null ? "null" : "set") +
                " x: "+ editable.x +", y: "+ editable.y +
                " time: " + editable.anim.getTime() +
                " keyes: " + keys
        );
    }

    void update(){
        editable.x = (VIRTUAL_WIDTH  - editable.width)  / 2;
        editable.y = (VIRTUAL_HEIGHT - editable.height) / 2;
        updateText();
        Gdx.app.log(TAG, "editable.anim.framesetCount " + editable.anim.framesetCount);
        if(sprite != null && editable.anim.framesetCount>0){
            int y = (int)sprite.getHeight() / editable.anim.framesetCount;
            int w = (int)sprite.getWidth()  / editable.anim.frameCount;
//            sets = new Array<Rectangle>(editable.anim.framesetCount);
//            Gdx.app.log(TAG, "sets " + sets.size);
            Gdx.app.log(TAG, "y,w " + y+", "+w);

            for(int i = 0; i != editable.anim.framesetCount; i++) {
                Array<Rectangle> framesInSet = new Array<Rectangle>();
                for(int f = 0; f != editable.anim.frameCount; f++) framesInSet.add(new Rectangle(f*w, i*y, 64,64));
                allFrames.add(framesInSet);
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        camera.viewportHeight = height;
        camera.viewportWidth  = width;

        camera.update();

        overlayCamera.setToOrtho(
                false,
                VIRTUAL_WIDTH,
                VIRTUAL_HEIGHT
        );
        anibox.update();
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        anibox.delta(delta);
        if(editable != null) editable.update(delta);

        if(aniboxNeedsUpdate == true){
            anibox.update();
            aniboxNeedsUpdate = false;
        }

        Gdx.gl.glClear( Gdx.gl20.GL_COLOR_BUFFER_BIT | Gdx.gl20.GL_DEPTH_BUFFER_BIT );

        batch.begin();
            anibox.draw(batch);
            if(editable != null && editable.anim.getFrame() != null) editable.draw(batch);
            font.draw(batch, textOverlay, 5, textOverlay.height+5);

            if(previews != null){
                for(Element e : previews){
                    if(e != null){
                        e.anim.addTime(delta);
                        e.draw(batch);
                    }
                }
            }

            if(sprite != null) sprite.draw(batch);

        batch.end();

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        sr.setProjectionMatrix(camera.combined);
        sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(Color.WHITE);
            if(editable != null){
                sr.rect(editable.x-1, editable.y-1, editable.width+1, editable.height+1);
                sr.circle(editable.getBounds().x, editable.getBounds().y, editable.getBounds().z);
            }

            for(Array<Rectangle> frames: allFrames)
                for(Rectangle r : frames) sr.rect(r.x, r.y, r.width, r.height);
        sr.end();
    }

    int keys;
    static int kUp   = 1;
    static int kDown = 2;
    static int kLeft = 4;
    static int kRight= 8;

    @Override
    public boolean keyDown(int keycode) {
        switch(keycode){
            case Input.Keys.UP   :  keys |= kUp;    editable.moveUp();    break;
            case Input.Keys.DOWN :  keys |= kDown;  editable.moveDown();  break;
            case Input.Keys.LEFT :  keys |= kLeft;  editable.moveLeft();  break;
            case Input.Keys.RIGHT:  keys |= kRight; editable.moveRight(); break;
        }
        Gdx.app.log(TAG, "key" + keys);
        updateText();
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch(keycode){
            case Input.Keys.UP   :  keys &= ~kUp;    break;
            case Input.Keys.DOWN :  keys &= ~kDown;  break;
            case Input.Keys.LEFT :  keys &= ~kLeft;  break;
            case Input.Keys.RIGHT:  keys &= ~kRight; break;
        }
        updateText();
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if(anibox.hitbox().contains(screenX, screenY))
            if(anibox.click(screenX, screenY, button)){

            } else {

            }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        anibox.mouseMoved(screenX,screenY);
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
