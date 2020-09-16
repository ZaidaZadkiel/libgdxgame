package com.mygdx.game.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class Textbox {
    static String TAG = "Textbox";
    //do things to show text on screen on a fbo
    FrameBuffer fbo;
    public BitmapFont  font;
    SpriteBatch batch;
    GlyphLayout glyphLayout;
    OrthographicCamera cam;

    //initial size, should update to the drawn area on each update
    public int width  = 8*200;
    public int height = 16*40;
    public int x = 0;
    public int y = 0;

    public TextureRegion texture;
    ShapeRenderer sr = new ShapeRenderer();

    public class ListItem{
        public String  display;
        public String  shift;
        public String  keycode;
        public int     intcode;
        public boolean boolshift;
        public Object  variant;
        public ListItem(){};
        public ListItem(String display, Object ref){variant=ref; this.display=display;};
        public ListItem(boolean shift, int keycode, String display){
            intcode   = keycode;
            boolshift = shift;
            variant   = display;
            //modifier or long name keys to be replaced by single-cell characters go here
            switch(keycode){
                case Input.Keys.ESCAPE: this.keycode = "≡"; break;
                case Input.Keys.TAB:    this.keycode = (shift ? "←" : "→"); break;
                default:                this.keycode = Input.Keys.toString(keycode);
            }
            this.shift = (shift ? "Σ" : (this.keycode.length() == 1 ? " " : "") );
            this.display = display.toUpperCase().trim();
//            Gdx.app.log(TAG, this.display + ", " + variant);
        }
    }


    public Textbox(SpriteBatch b) {
        batch = new SpriteBatch();
        batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0,0,width,height));
        fbo   = new FrameBuffer(Pixmap.Format.RGB565, width, height, false);

        font  = new BitmapFont( Gdx.files.internal("data/font/font.fnt") );
        font.setColor(Color.WHITE);
        glyphLayout = new GlyphLayout(font, "", Color.GREEN, width, Align.left, true);

//        cam = new OrthographicCamera(width, height);
//        cam.update();
//        Vector3 v = new Vector3(1,1,0);

//        sr.getProjectionMatrix().getScale(v);//setProjectionMatrix(cam.combined);
//        Gdx.app.log("", "v: " + v);

//
//        cs += "size: " + glyphLayout.width + ", " + glyphLayout.height;
//        addText(
//                "A quick brown fox jumps over the lazy dog.\n"+
//                "0123456789¿?¡!`'\"., <>()[]{} &@%*^#$\\/\n"+
//                "\n"+
//                "* Wieniläinen sioux'ta puhuva ökyzombie diggaa Åsan roquefort-tacoja.\n"+
//                "* Ça me fait peur de fêter noël là, sur cette île bizarroïde où une mère et sa môme essaient de me tuer avec un gâteau à la cigüe brûlé.\n"+
//                "* Zwölf Boxkämpfer jagten Eva quer über den Sylter Deich.\n"+
//                "* El pingüino Wenceslao hizo kilómetros bajo exhaustiva lluvia y frío, añoraba a su querido cachorro.\n"+
//                "\n"+
//                "┌─┬─┐ ╔═╦═╗ ╒═╤═╕ ╓─╥─╖\n"+
//                "│ │ │ ║ ║ ║ │ │ │ ║ ║ ║\n"+
//                "├─┼─┤ ╠═╬═╣ ╞═╪═╡ ╟─╫ ├─┼──╢\n"+
//                "└─┴─┘ ╚═╩═╝ ╘═╧═╛ ╙─╨─╜\n"+
//                "\n"+
//                "░░░░░ ▐▀█▀▌ .·∙•○°○•∙·.\n"+
//                "▒▒▒▒▒ ▐ █ ▌ ☺ ☻ ♥ ♦ ♣ ♠ ♪ ♫ ☼\n"+
//                "▓▓▓▓▓ ▐▀█▀▌  $ ¢ £ ¥ ₧\n"+
//                "█████ ▐▄█▄▌ ◄►▲▼ ←→↑↓↕↨\n"+
//                "\n"+
//                "⌠\n"+
//                "│dx ≡ Σ √x²ⁿ·δx\n"+
//                "⌡",
//                0,0);
//        addText("this is a test", 0,0);
//        update();
        fbo.begin();
            batch.begin();
                Gdx.gl.glClearColor(0.08f, 0.08f, 1f, 1);
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//                font.draw(batch, glyphLayout, 0, height);
                Gdx.gl.glClearColor(0f, 0f, 0f, 1);
            batch.end();
        fbo.end();
        texture = new TextureRegion(fbo.getColorBufferTexture());
        texture.flip(false,true);
    }

    Rectangle bounds        = new Rectangle();
    Rectangle selectionRect = new Rectangle();

    int getRealYFromMouse(int mousey){return (height-mousey+y);}

    public Rectangle hitbox(){
        return bounds.set(x,y,width,height);
    }

    int mouseIndex = 0;
    public boolean click(int screenX, int screenY, int button){return false;}

    public void mouseMoved(int mousex, int mousey){
        mouseIndex = ((mousey+y-7)/14)-1;
        int realY  = 2+(getRealYFromMouse(mousey)/14)*14;
        selectionRect.set(5, realY, 0, 15);

        if(hitbox().contains(mousex, mousey)){
            if(mouseIndex >= 0 && this.shownItems.size>=0 && mouseIndex<this.shownItems.size){
                ListItem li = shownItems.get(mouseIndex);
                if(li.display != null && li.variant!=null) selectionRect.width=width-12;
            }
            this.update();
        }
    }

    Array<ListItem> shownItems  = new Array<ListItem>();
    StringBuilder   printedText = new StringBuilder();
    int             itemsMaxLength = 0;
    public String   listHeader;
    int             listStyle = 0;
    public static int LIST_STYLE_FLAT  = 1; // single box
    public static int LIST_STYLE_TABLE = 2; // box with key division

    public ListItem getListItem(int index) {return shownItems.get(index);}
    public ListItem addListSeparator(){
        ListItem t = new Textbox.ListItem();
        shownItems.add(t);
        return t;
    }
    public ListItem addListItem(String displayText, Object reference){
        if(listStyle==0) {
            listStyle = LIST_STYLE_FLAT;
//            Gdx.app.log(TAG, "style flat: LIST_STYLE_FLAT");
        }
        ListItem t =
            new Textbox.ListItem(
                displayText,
//                (displayText.length() > 25 ? displayText.substring(displayText.length()-27) : displayText).trim(),
                reference
            );
        shownItems.add(t);
        itemsMaxLength = max(displayText.length(), itemsMaxLength);
//        Gdx.app.log(TAG, displayText);
        return t;
    }

    public ListItem addListItem(boolean shift, int keyCode, String displayText){
        if(listStyle==0) {
            listStyle = LIST_STYLE_TABLE;
//            Gdx.app.log(TAG, "style table: LIST_STYLE_TABLE");
        }
        ListItem t = new Textbox.ListItem(shift, keyCode, displayText);
        shownItems.add(t);
//        int itemLength = displayText.length(); // 7 is: box-line \b (\b \b KeyLetter) || (\bF1..\bF9 || F10..F12) \b box-line \b
        itemsMaxLength = max(displayText.length(), itemsMaxLength);
        return t;
    }


    public ListItem keyDown(boolean shift, int keycode){
        for(ListItem li : shownItems){
            if((li.intcode == keycode) && (li.boolshift == shift)) return li;
        }
        return null;
    }


    public boolean setText(CharSequence text){
        return setText(text,0,0);
    }
    public boolean setText(CharSequence text, int x, int y){
        printedText.delete(0, printedText.length()); //setLength(0);
        printedText.append(text);
//        clearText();

        glyphLayout.setText(font,printedText);
        selectionRect.y = -100;

        width =(int)glyphLayout.width +2;
        height=(int)glyphLayout.height+6;

//        Gdx.app.log("", "width: " + width);
//        Gdx.app.log("", printedText.toString());
        update();
        return false;
    }

    public boolean clearText(){
        printedText.delete(0, printedText.length()); //setLength(0);
        printedText.setLength(0);

        shownItems.clear();
        itemsMaxLength = 0;
        listHeader     = null;
        listStyle      = 0;

        glyphLayout.setText(font,"");
        return false;
    }

    void printItemlist(){
        String linesep       = "────────────────────────────────────────────────────────────";
        String doublelinesep = "════════════════════════════════════════════════════════════";

        if(shownItems.size == 0) return;
//        Gdx.app.log(TAG, "printitemlist: " + listStyle);
        if(listStyle==LIST_STYLE_TABLE) {
//            Gdx.app.log(TAG, "table header len : " + itemsMaxLength);
            if(listHeader!=null) printedText.append("┌──┬┤ ")
                                            .append(listHeader)
                                            .append(" ├");
            int boxSize = max(itemsMaxLength+8, printedText.length()+2);
//            Gdx.app.log(TAG, "box "+ boxSize + ", len " + printedText.length());
            printedText.append(linesep, 0, boxSize-printedText.length()-2 ).append("╖\n");
            String format = "%-"+(boxSize-8)+"s";

            for(ListItem text : this.shownItems){
                if(text.display==null){
                    printedText.append("├──┼").append(linesep, 0, boxSize-6).append("╢\n");
                } else {
                    printedText.append("│")
                               .append(text.shift)
                               .append(text.keycode)
                               .append("│ ")
                               .append(String.format(format, text.display))
                               .append(" ║\n");
                }
            }

            printedText.append("╘══╧").append(doublelinesep, 0, boxSize-6).append("╝");
        }

        if(listStyle == LIST_STYLE_FLAT){
            if(listHeader!=null) printedText.append("┌──┤ ") .append(listHeader).append(" ├");
            int boxSize = max(itemsMaxLength+4, printedText.length()+2);
            printedText.append(linesep, 0, boxSize-printedText.length()-2 ).append("╖\n");

            String format = "%-"+(boxSize-4)+"s";

            for(ListItem text : this.shownItems){
                if(text.display==null){
                    printedText.append("├").append(linesep, 0, boxSize-3).append("╢\n");
                } else {
                    printedText.append("│")
                               .append(String.format(format, text.display))
                               .append(" ║\n");
                }
            }

            printedText.append("╘══").append(doublelinesep, 0, boxSize-5).append("╝");
        }

        glyphLayout.setText(font,printedText);
        selectionRect.y = -100;

        width =(int)glyphLayout.width +2;
        height=(int)glyphLayout.height+6;
    }

    //redraw fbo contents
    public void update(){
//        Gdx.app.log("", "X: " + selectionRect.x +", y: "+ selectionRect.y + ", w: " + selectionRect.width +", h:" + selectionRect.height);

        if(printedText.length() == 0) printItemlist();

        sr.setProjectionMatrix(batch.getProjectionMatrix());
        fbo.begin();
            Gdx.gl.glClearColor(0.08f, 0.08f, 1f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            sr.begin(ShapeRenderer.ShapeType.Filled);
                sr.setColor(Color.GREEN);
                sr.rect(selectionRect.x, selectionRect.y, selectionRect.width, selectionRect.height);
            sr.end();

            batch.begin();
               font.draw(batch, glyphLayout,  0, height);
               Gdx.gl.glClearColor(0f, 0f, 0f, 1);
            batch.end();
        fbo.end();
    }

    //blit fbo into game screen
    public void draw(SpriteBatch batch){
        draw(batch, width, height);
    }

    public void draw(SpriteBatch batch, int width, int height){
//        int textHeight = Gdx.graphics.getHeight();//fbo.getColorBufferTexture().getHeight();
//        int textWidth  = fbo.getColorBufferTexture().getWidth();;
        int textHeight = height;
        int textWidth  = width;
        int ypos       = Gdx.graphics.getHeight()-textHeight-y;
        batch.draw(
            fbo.getColorBufferTexture(),        //texture
            x, ypos,                            //x, y
            0, 0,                               //originX, originY
            textWidth, textHeight,              //dstWidth, dstHeight
            1,1,                                //scaleX, scaleY
            0,                                  //rotation
            0,0,                                //srcX, srcY
            textWidth,textHeight,               //srcWidth, srcHeight
            false,true                          //flipX, flipY
        );
    }

    float time;
    public void delta(float delta){
        time += delta;
    }
}
