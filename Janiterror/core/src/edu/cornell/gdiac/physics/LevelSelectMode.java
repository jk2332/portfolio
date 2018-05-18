/*
 * ScoreMode.java
 */
package edu.cornell.gdiac.physics;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.controllers.*;
import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.util.*;

/**
 * Class that provides a level select screen for the game
 */
public class LevelSelectMode implements Screen, InputProcessor, ControllerListener {

    // Textures necessary to support the loading screen
    private static final String BACKGROUND_FILE = "shared/inter-menu-v2.png";

    private static final String FONT_FILE = "shared/Title.ttf";
    private static final String FONT_BODY_FILE = "shared/Francois.ttf";

    private static final String LEVEL_FILE = "shared/mop-bucket-menu.png";

    private static final String MAIN_BTN_FILE = "shared/menu-button.png";

    private static final String ARROW_FILE = "shared/menu-selector.png";

    private static final int FONT_SIZE = 30;

    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH  = 1024;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 576;
    /** Amount to scale the play button */
    private float buttonScale  = 0.75f;

    private static float CENTER_X_RATIO = 0.67f;

    private static float TITLE_Y_RATIO = 0.07f;

    private static float MENU_Y_RATIO = 0.1f;

    private static float OFFSET_RATIO = 0.20f;

    private static int NUM_ROWS = 5;

    private static int LEVELS_PER_PAGE = NUM_ROWS * 2;

    /** The x-coordinate of the center of the progress bar */
    private int centerX;

    private int centerYUp;
    private int centerYDown;

    private int titleY;

    private int menuY;

    private int marginX;

    private String[] levelNames;

    private Texture mainButton;

    /** The font for giving messages to the player */
    protected BitmapFont displayFont;
    protected BitmapFont bodyFont;

    /** Start button for XBox controller on Windows */
    private static int WINDOWS_START = 7;
    /** Start button for XBox controller on Mac OS X */
    private static int MAC_OS_X_START = 4;

    /** Exit code */
    public int exit;

    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    private GlyphLayout layout = new GlyphLayout();


    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;
    /** Scaling factor for when the student changes the resolution. */
    private float scale;

    /** The current state of the play button */
    private int   pressState;
    /** Support for the X-Box start button in place of play button */
    private int   startButton;

    private TextureRegion levelButton;

    private Animation <TextureRegion> bgAnimation;
    private Array<TextureRegion> levelFrames;

    private TextureRegion bg;

    private float stateTimer;

    /** Whether or not this player mode is still active */
    private boolean active;

    private String subtitle;

    private static final String TITLE = "LEVEL SELECT";

    private static final String DEFAULT_SUBTITLE = "SELECT A LEVEL";

    private int hoverIndex;

    private int curr_page;

    private Texture arrowButton;



    /**
     * Returns true if all assets are loaded and the player is ready to go.
     *
     * @return true if the player is ready to go
     */
    public boolean isReady() {
        return pressState == 2;
    }


    /**
     * Creates a ScoreMode with the default size and position
     *
     */
    public LevelSelectMode(GameCanvas canvas, String[] levelNames) {
        // Compute the dimensions from the canvas
        resize(canvas.getWidth(),canvas.getHeight());
        this.canvas  = canvas;
        this.levelNames = levelNames;
        // Load the next two images immediately.

        mainButton = new Texture(MAIN_BTN_FILE);
        mainButton.setFilter(TextureFilter.Linear, TextureFilter.Linear);

        stateTimer = 0.0f;

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(FONT_FILE));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = FONT_SIZE;

        displayFont = generator.generateFont(parameter);
        generator.dispose();
        displayFont.getData().setScale(scale);

        FreeTypeFontGenerator generator2 = new FreeTypeFontGenerator(Gdx.files.internal(FONT_BODY_FILE));
        FreeTypeFontParameter parameter2 = new FreeTypeFontParameter();
        parameter2.size = FONT_SIZE;

        bodyFont = generator2.generateFont(parameter2);
        generator2.dispose();
        bodyFont.getData().setScale(scale);

        startButton = (System.getProperty("os.name").equals("Mac OS X") ? MAC_OS_X_START : WINDOWS_START);

        arrowButton = new Texture(ARROW_FILE);
        Texture backgroundT = new Texture(BACKGROUND_FILE);
        Texture levelT = new Texture(LEVEL_FILE);
        TextureRegion backgroundTexture = new TextureRegion(backgroundT, backgroundT.getWidth(), backgroundT.getHeight());
        TextureRegion levelTexture = new TextureRegion(levelT, levelT.getWidth(), levelT.getHeight());
        Array<TextureRegion> frames = new Array<TextureRegion>();
        for (int i=0; i < backgroundT.getWidth()/1024; i++){
            frames.add (new TextureRegion(backgroundTexture,i*1024,0,1024,576));
        }
        bgAnimation = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        levelFrames = new Array<TextureRegion>();
        for (int i=0; i < levelT.getWidth()/96; i++){
            levelFrames.add (new TextureRegion(levelTexture,i*96,0,96,96));
        }

        subtitle = DEFAULT_SUBTITLE;

        curr_page = 0;
        hoverIndex = 0;

    }

    public void reset() {
        pressState = 0;
        active = false;
        hoverIndex = 0;
        curr_page = 0;
        Gdx.input.setInputProcessor(this);
    }

    /**
     * Called when this screen should release all resources.
     */
    public void dispose() {
        levelFrames.clear();
        levelFrames = null;
        bgAnimation = null;
        bg = null;
        mainButton.dispose();
        mainButton = null;
        levelNames = null;
    }

    /**
     * Update the status of this player mode.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     *
     * @param delta Number of seconds since last animation frame
     */
    private void update(float delta) {
        canvas.setCameraPosition(canvas.getWidth()/2.0f,canvas.getHeight()/2.0f);
        bg = bgAnimation.getKeyFrame(stateTimer,true);
        stateTimer = stateTimer + delta;


        /*float screenX = Gdx.input.getX();
        float screenY = heightY - Gdx.input.getY();

        int centerY;
        int centerX;

        float radius;
        float dist;*/

        subtitle = DEFAULT_SUBTITLE;
        /*hoverIndex = -1;
        for (int i = curr_page * LEVELS_PER_PAGE; i < Math.min((curr_page + 1) * LEVELS_PER_PAGE, levelNames.length); i++) {
            if (i < NUM_ROWS + curr_page * LEVELS_PER_PAGE) {
                centerY = centerYUp;
            } else {
                centerY = centerYDown;
            }

            //s = "LVL " + i + ": " + levelNames[i - 1];
            centerX = (i % NUM_ROWS - NUM_ROWS/2) * marginX + this.centerX;

            radius = levelFrames.get(0).getRegionWidth()/2.0f;
            dist = (screenX-centerX)*(screenX-centerX)+(screenY-centerY)*(screenY-centerY);
            if (dist < radius*radius) {
                subtitle = levelNames[i];
                hoverIndex = i;
            }
        }*/

        InputController input = InputController.getInstance();
        input.readInput(new Rectangle(0f, 0f, 32.0f, 18.0f), new Vector2(32.0f, 32.0f));

        if (input.didDownArrow()) {
            if (hoverIndex == levelNames.length) {

            }
            else if (hoverIndex % LEVELS_PER_PAGE < NUM_ROWS && hoverIndex + NUM_ROWS < levelNames.length) {
                hoverIndex = hoverIndex + NUM_ROWS;

            } else {
                //hoverIndex = (curr_page + 1) % (int) Math.ceil(levelNames.length/(LEVELS_PER_PAGE * 1.0f)) * LEVELS_PER_PAGE;
                hoverIndex = levelNames.length;
            }
        } else if (input.didUpArrow()) {
            if (hoverIndex == levelNames.length) {
                hoverIndex = (curr_page) % (int) Math.ceil(levelNames.length/(LEVELS_PER_PAGE * 1.0f)) * LEVELS_PER_PAGE;
            }
            else if (hoverIndex % LEVELS_PER_PAGE >= NUM_ROWS) {
                hoverIndex = hoverIndex - NUM_ROWS;
            } else {
                hoverIndex = (curr_page - 1 + (int) Math.ceil(levelNames.length/(LEVELS_PER_PAGE * 1.0f))) % (int) Math.ceil(levelNames.length/(LEVELS_PER_PAGE * 1.0f)) * LEVELS_PER_PAGE;
            }
        } else if (input.didLeftArrow()) {
            if (hoverIndex == levelNames.length) {

            } else {
                hoverIndex = (hoverIndex - 1 + levelNames.length) % levelNames.length;
            }
        } else if (input.didRightArrow()) {
            if (hoverIndex == levelNames.length) {

            } else {
                hoverIndex = (hoverIndex + 1) % levelNames.length;
            }

    }

        if (input.didEnter() || input.didSpace()) {
            pressState = 2;
            if (hoverIndex == levelNames.length) {
                exit = 0;
            } else {
                exit = hoverIndex + 1;
            }

        }

        if (hoverIndex == levelNames.length) {
            subtitle = DEFAULT_SUBTITLE;
        } else {
            subtitle = levelNames[hoverIndex];
            curr_page = hoverIndex / LEVELS_PER_PAGE;
        }

        System.out.println(hoverIndex);

    }

    /**
     * Draw the status of this player mode.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     */
    private void draw() {
        canvas.begin();
        canvas.draw(bg, 0, 0);
        Color color;

        int centerX;
        int centerY;

        int radiusX;
        int radiusY;

        displayFont.setColor(Color.SKY);
        displayFont.getData().setScale(scale * 1.8f);
        layout.setText(displayFont, TITLE);
        radiusX = (int) (layout.width / 2.0f);
        radiusY = (int) (layout.height / 2.0f);
        canvas.drawText(TITLE, displayFont, this.centerX - radiusX, titleY - radiusY);
        displayFont.setColor(Color.WHITE);
        displayFont.getData().setScale(scale);

        bodyFont.setColor(Color.WHITE);
        bodyFont.getData().setScale(1f);
        layout.setText(bodyFont, subtitle);
        radiusX = (int) (layout.width / 2.0f);
        radiusY = (int) (layout.height / 2.0f);
        canvas.drawText(subtitle, bodyFont, this.centerX- radiusX, centerYUp + marginX - radiusY);

        for (int i = curr_page * LEVELS_PER_PAGE; i < Math.min((curr_page + 1) * LEVELS_PER_PAGE, levelNames.length); i++) {
            if (i < NUM_ROWS + curr_page * LEVELS_PER_PAGE) {
                centerY = centerYUp;
            } else {
                centerY = centerYDown;
            }

            //s = "LVL " + i + ": " + levelNames[i - 1];
            centerX = (i % NUM_ROWS - NUM_ROWS/2) * marginX + this.centerX;
            color = (pressState == 1 && exit == (i + 1) ? new Color(0.117f, 0.459f, 0.776f, 1f): Color.WHITE);
            displayFont.setColor(color);
            displayFont.getData().setScale(scale * 1.5f);
            layout.setText(displayFont, i + 1 + "");
            radiusX = (int) (layout.width / 2.0f);
            radiusY = (int) (layout.height / 2.0f);
            levelButton = hoverIndex == i ? levelFrames.get(2) : levelFrames.get(0);
            canvas.draw(levelButton, Color.WHITE, levelFrames.get(0).getRegionWidth()/2,
                    levelFrames.get(0).getRegionHeight()/2, centerX, centerY, 0, 1.0f, 1.0f);
            canvas.drawText(i + 1 + "", displayFont, centerX - radiusX, centerY + radiusY);
        }

        buttonScale = 0.75f;
        color = Color.WHITE;
        if (curr_page > 0) {
            canvas.draw(arrowButton, color, arrowButton.getWidth()/2, arrowButton.getHeight()/2,
                     this.centerX - (NUM_ROWS/2 + 0.6f) * marginX, (centerYUp + centerYDown)/2, (float) Math.PI, buttonScale*scale, buttonScale*scale);
        }

        if (curr_page < Math.ceil(levelNames.length/(LEVELS_PER_PAGE * 1.0f)) - 1) {
            canvas.draw(arrowButton, color, arrowButton.getWidth()/2, arrowButton.getHeight()/2,
                    this.centerX + (NUM_ROWS/2 + 0.6f) * marginX, (centerYUp + centerYDown)/2, 0, buttonScale*scale, buttonScale*scale);
        }



        if (hoverIndex == levelNames.length) {
            buttonScale = 0.85f;
            color = Color.YELLOW;
        }
        canvas.draw(mainButton, color, mainButton.getWidth()/2, mainButton.getHeight()/2,
                STANDARD_WIDTH/2, menuY, 0, buttonScale*scale, buttonScale*scale);

        canvas.end();
    }
    private static final String LOADING_TRACK_FILE = "floor/sound/track2.mp3";

    // ADDITIONAL SCREEN METHODS
    /**
     * Called when the Screen should render itself.
     *
     * We defer to the other methods update() and draw().  However, it is VERY important
     * that we only quit AFTER a draw.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void render(float delta) {
        if (active) {
            update(delta);
            draw();

            // We are are ready, notify our listener
            if (listener != null && isReady()) {
                //SoundController.getInstance().stop(LOADING_TRACK_FILE);
                listener.exitScreen(this, exit);
            }
        }
    }

    /**
     * Called when the Screen is resized.
     *
     * This can happen at any point during a non-paused state but will never happen
     * before a call to show().
     *
     * @param width  The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        // Compute the drawing scale
        float sx = ((float)width)/800;
        float sy = ((float)height)/700;
        scale = (sx < sy ? sx : sy);
        heightY = height;

        menuY = (int) (height * MENU_Y_RATIO);

        titleY = (int) (height * (1 - TITLE_Y_RATIO));

        centerX = (int) (width * CENTER_X_RATIO);

        centerYDown = (int) (height/2 - height * OFFSET_RATIO /2);
        centerYUp = (int) (height/2 + height * OFFSET_RATIO/2);

        marginX = (int) (height * OFFSET_RATIO);
    }

    /**
     * Called when the Screen is paused.
     *
     * This is usually when it's not active or visible on screen. An Application is
     * also paused before it is destroyed.
     */
    public void pause() {
        // TODO Auto-generated method stub

    }

    /**
     * Called when the Screen is resumed from a paused state.
     *
     * This is usually when it regains focus.
     */
    public void resume() {
        // TODO Auto-generated method stub

    }

    /**
     * Called when this screen becomes the current screen for a Game.
     */
    public void show() {
        // Useless if called in outside animation loop
        active = true;
    }

    /**
     * Called when this screen is no longer the current screen for a Game.
     */
    public void hide() {
        // Useless if called in outside animation loop
        active = false;
    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    // PROCESSING PLAYER INPUT
    /**
     * Called when the screen was touched or a mouse button was pressed.
     *
     * This method checks to see if the play button is available and if the click
     * is in the bounds of the play button.  If so, it signals the that the button
     * has been pressed and is currently down. Any mouse button is accepted.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        if (pressState == 2) {
            return true;
        }

        // Flip to match graphics coordinates
        screenY = heightY-screenY;

        // TODO: Fix scaling
        // Play button is a circle.
        float centerX;
        float centerY;


        float radius = buttonScale*scale*mainButton.getWidth()/2.0f;
        float dist = (screenX-STANDARD_WIDTH/2)*(screenX-STANDARD_WIDTH/2)+(screenY-menuY)*(screenY-menuY);
        if (dist < radius*radius) {
            pressState = 1;
            exit = 0;
        }

        radius = buttonScale*scale*arrowButton.getWidth()/2.0f;
        dist = (screenX-(this.centerX - (NUM_ROWS/2 + 0.6f) * marginX))*(screenX-(this.centerX - (NUM_ROWS/2 + 0.6f) * marginX))+(screenY-(centerYUp + centerYDown)/2)*(screenY-(centerYUp + centerYDown)/2);

        if (curr_page > 0 && dist < radius * radius) {
            curr_page--;
        }


        dist = (screenX-(this.centerX + (NUM_ROWS/2 + 0.6f) * marginX))*(screenX-(this.centerX + (NUM_ROWS/2 + 0.6f) * marginX))+(screenY-(centerYUp + centerYDown)/2)*(screenY-(centerYUp + centerYDown)/2);

        if (curr_page < Math.ceil(levelNames.length/(LEVELS_PER_PAGE * 1.0f)) - 1 && dist < radius * radius) {
            curr_page++;
        }

        for (int i = curr_page * LEVELS_PER_PAGE; i < Math.min((curr_page + 1) * LEVELS_PER_PAGE, levelNames.length); i++) {
            if (i < NUM_ROWS + curr_page * LEVELS_PER_PAGE) {
                centerY = centerYUp;
            } else {
                centerY = centerYDown;
            }

            //s = "LVL " + i + ": " + levelNames[i - 1];
            centerX = (i % NUM_ROWS - NUM_ROWS/2) * marginX + this.centerX;

            radius = levelFrames.get(0).getRegionWidth()/2.0f;
            dist = (screenX-centerX)*(screenX-centerX)+(screenY-centerY)*(screenY-centerY);
            if (dist < radius*radius) {
                pressState = 1;
                exit = i + 1;
            }
        }
        return false;
    }

    /**
     * Called when a finger was lifted or a mouse button was released.
     *
     * This method checks to see if the play button is currently pressed down. If so,
     * it signals the that the player is ready to go.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pressState == 1) {
            pressState = 2;
            return false;
        }
        return true;
    }

    /**
     * Called when a button on the Controller was pressed.
     *
     * The buttonCode is controller specific. This listener only supports the start
     * button on an X-Box controller.  This outcome of this method is identical to
     * pressing (but not releasing) the play button.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonDown (Controller controller, int buttonCode) {
        if (buttonCode == startButton && pressState == 0) {
            pressState = 1;
            return false;
        }
        return true;
    }

    /**
     * Called when a button on the Controller was released.
     *
     * The buttonCode is controller specific. This listener only supports the start
     * button on an X-Box controller.  This outcome of this method is identical to
     * releasing the the play button after pressing it.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonUp (Controller controller, int buttonCode) {
        if (pressState == 1 && buttonCode == startButton) {
            pressState = 2;
            return false;
        }
        return true;
    }

    // UNSUPPORTED METHODS FROM InputProcessor

    /**
     * Called when a key is pressed (UNSUPPORTED)
     *
     * @param keycode the key pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean keyDown(int keycode) {
        return true;
    }

    /**
     * Called when a key is typed (UNSUPPORTED)
     *
     * @param keycode the key typed
     * @return whether to hand the event to other listeners.
     */
    public boolean keyTyped(char character) {
        return true;
    }

    /**
     * Called when a key is released.
     *
     * We allow key commands to start the game this time.
     *
     * @param keycode the key released
     * @return whether to hand the event to other listeners.
     */
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.N || keycode == Input.Keys.P) {
            pressState = 2;
            return false;
        }
        return true;
    }

    /**
     * Called when the mouse was moved without any buttons being pressed. (UNSUPPORTED)
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @return whether to hand the event to other listeners.
     */
    public boolean mouseMoved(int screenX, int screenY) {
        return true;
    }

    /**
     * Called when the mouse wheel was scrolled. (UNSUPPORTED)
     *
     * @param amount the amount of scroll from the wheel
     * @return whether to hand the event to other listeners.
     */
    public boolean scrolled(int amount) {
        return true;
    }

    /**
     * Called when the mouse or finger was dragged. (UNSUPPORTED)
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return true;
    }

    // UNSUPPORTED METHODS FROM ControllerListener

    /**
     * Called when a controller is connected. (UNSUPPORTED)
     *
     * @param controller The game controller
     */
    public void connected (Controller controller) {}

    /**
     * Called when a controller is disconnected. (UNSUPPORTED)
     *
     * @param controller The game controller
     */
    public void disconnected (Controller controller) {}

    /**
     * Called when an axis on the Controller moved. (UNSUPPORTED)
     *
     * The axisCode is controller specific. The axis value is in the range [-1, 1].
     *
     * @param controller The game controller
     * @param axisCode 	The axis moved
     * @param value 	The axis value, -1 to 1
     * @return whether to hand the event to other listeners.
     */
    public boolean axisMoved (Controller controller, int axisCode, float value) {
        return true;
    }

    /**
     * Called when a POV on the Controller moved. (UNSUPPORTED)
     *
     * The povCode is controller specific. The value is a cardinal direction.
     *
     * @param controller The game controller
     * @param povCode 	The POV controller moved
     * @param value 	The direction of the POV
     * @return whether to hand the event to other listeners.
     */
    public boolean povMoved (Controller controller, int povCode, PovDirection value) {
        return true;
    }

    /**
     * Called when an x-slider on the Controller moved. (UNSUPPORTED)
     *
     * The x-slider is controller specific.
     *
     * @param controller The game controller
     * @param sliderCode The slider controller moved
     * @param value 	 The direction of the slider
     * @return whether to hand the event to other listeners.
     */
    public boolean xSliderMoved (Controller controller, int sliderCode, boolean value) {
        return true;
    }

    /**
     * Called when a y-slider on the Controller moved. (UNSUPPORTED)
     *
     * The y-slider is controller specific.
     *
     * @param controller The game controller
     * @param sliderCode The slider controller moved
     * @param value 	 The direction of the slider
     * @return whether to hand the event to other listeners.
     */
    public boolean ySliderMoved (Controller controller, int sliderCode, boolean value) {
        return true;
    }

    /**
     * Called when an accelerometer value on the Controller changed. (UNSUPPORTED)
     *
     * The accelerometerCode is controller specific. The value is a Vector3 representing
     * the acceleration on a 3-axis accelerometer in m/s^2.
     *
     * @param controller The game controller
     * @param accelerometerCode The accelerometer adjusted
     * @param value A vector with the 3-axis acceleration
     * @return whether to hand the event to other listeners.
     */
    public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value) {
        return true;
    }

}