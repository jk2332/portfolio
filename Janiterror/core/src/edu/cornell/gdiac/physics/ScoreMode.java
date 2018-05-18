/*
 * ScoreMode.java
 */
package edu.cornell.gdiac.physics;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.controllers.*;
import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.util.*;

/**
 * Class that provides a score screen for the state of the game.
 */
public class ScoreMode implements Screen, InputProcessor, ControllerListener {
    // Textures necessary to support the loading screen
    private static final String BACKGROUND_FILE = "shared/opacity-block.png";
    private static final String LEVELCOMPLETE_FILE = "shared/level-complete.png";
    private static final String PROGRESS_FILE = "shared/progressbar.png";
    private static final String PLAY_BTN_FILE = "shared/continue-button.png";
    private static final String MAIN_BTN_FILE = "shared/menu-button.png";

    private static final String JOE_NEXT_FILE = "shared/janitor-level-complete-3x.png";
    private static final String JOE_MAIN_FILE = "shared/janitor-sleeping-3x.png";

    /** The font for giving messages to the player */
    protected BitmapFont bodyFont;

    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH  = 800;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 700;
    /** Amount to scale the play button */
    private static float BUTTON_SCALE  = 0.75f;

    /** Ration of the bar height to the screen */
    private static float BAR_HEIGHT_RATIO = 0.25f;

    private static float JOE_HEIGHT_RATIO = 0.5f;

    private static float OFFSET_X_RATIO = 0.15f;

    /** The y-coordinate of the center of the progress bar */
    private int centerY;
    /** The x-coordinate of the center of the progress bar */
    private int centerX;

    private int centerXMain;
    private int centerXNext;

    private int centerYJoe;
    /** Background texture for start-up */
    private Texture background;
    /** Play button to display when done */
    public Texture playButton;
    public Texture mainButton;

    /** Start button for XBox controller on Windows */
    private static int WINDOWS_START = 7;
    /** Start button for XBox controller on Mac OS X */
    private static int MAC_OS_X_START = 4;

    /** Exit code for main menu */
    public static final int EXIT_MENU = 0;
    /** Exit code for advancing to next level */
    public static final int EXIT_NEXT = 1;

    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;


    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;
    /** Scaling factor for when the student changes the resolution. */
    private float scale;

    /** The current state of the play button */
    private int   pressState;
    private int choose;

    /** The amount of time to devote to loading assets (as opposed to on screen hints, etc.) */
    private int   budget;
    /** Support for the X-Box start button in place of play button */
    private int   startButton;
    /** Whether or not this player mode is still active */
    private boolean active;

    private Animation <TextureRegion> joeNext;
    private Animation <TextureRegion> joeMain;

    private float stateTimer;

    private float stateTimerbg;

    private Animation <TextureRegion> bgAnimation;

    private TextureRegion bg;

    private TextureRegion current;

    private String next_level_name;
    private static final String FONT_BODY_FILE = "shared/Francois.ttf";


    /**
     * Returns true if all assets are loaded and the player is ready to go.
     *
     * @return true if the player is ready to go
     */
    public boolean isMain() { return choose == 0;}
    public boolean isReady() {
        return choose == 1;
    }

    /**
     * Creates a ScoreMode with the default size and position
     *
     */
    public ScoreMode(GameCanvas canvas, String next_level_name) {
        // Compute the dimensions from the canvas
        resize(canvas.getWidth(),canvas.getHeight());
        this.canvas  = canvas;
        choose = 1;

        this.next_level_name = next_level_name;

        FreeTypeFontGenerator generator2 = new FreeTypeFontGenerator(Gdx.files.internal(FONT_BODY_FILE));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter2 = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter2.size = 30; //font size
        bodyFont = generator2.generateFont(parameter2);
        generator2.dispose();
        bodyFont.getData().setScale(scale);

        stateTimer = 0.0f;
        stateTimerbg = 0.0f;
        // Load the next two images immediately.
        playButton = new Texture(PLAY_BTN_FILE);
        playButton.setFilter(TextureFilter.Linear, TextureFilter.Linear);

        mainButton = new Texture(MAIN_BTN_FILE);
        mainButton.setFilter(TextureFilter.Linear, TextureFilter.Linear);

        background = new Texture(BACKGROUND_FILE);

        startButton = (System.getProperty("os.name").equals("Mac OS X") ? MAC_OS_X_START : WINDOWS_START);

        Texture joeMainT = new Texture(JOE_MAIN_FILE);
        Texture joeNextT = new Texture(JOE_NEXT_FILE);
        TextureRegion joeMainTexture = new TextureRegion(joeMainT, joeMainT.getWidth(), joeMainT.getHeight());
        TextureRegion joeNextTexture = new TextureRegion(joeNextT, joeNextT.getWidth(), joeNextT.getHeight());
        // Let ANY connected controller start the game.
        /*for(Controller controller : Controllers.getControllers()) {
            controller.addListener(this);
        }*/
        Texture backgroundT = new Texture(LEVELCOMPLETE_FILE);
        TextureRegion backgroundTexture = new TextureRegion(backgroundT, backgroundT.getWidth(), backgroundT.getHeight());
        Array<TextureRegion> frames = new Array<TextureRegion>();
        for (int i=0; i < backgroundT.getWidth()/1024; i++){
            frames.add (new TextureRegion(backgroundTexture,i*1024,0,1024,576));
        }
        bgAnimation = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();
        for (int i=0; i < joeNextT.getWidth()/192; i++){
            frames.add (new TextureRegion(joeNextTexture,i*192,0,192,192));
        }
        joeNext = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i < joeMainT.getWidth()/192; i++){
            frames.add (new TextureRegion(joeMainTexture,i*192,0,192,192));
        }
        joeMain = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();
    }

    public void reset() {
        pressState = 0;
        choose = 1;
        active = false;
        Gdx.input.setInputProcessor(this);
    }

    /**
     * Called when this screen should release all resources.
     */
    public void dispose() {
        background.dispose();
        background = null;
        bgAnimation = null;
        bg = null;
        if (playButton != null) {
            playButton.dispose();
            playButton = null;
        }
        if (mainButton != null) {
            mainButton.dispose();
            mainButton = null;
        }
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
        current = getFrameJoe(delta);
        bg = bgAnimation.getKeyFrame(stateTimer,true);
        stateTimerbg = stateTimerbg + delta;
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
        canvas.draw(background, 0, 0);
        canvas.draw(bg, 0, 0);

        if (next_level_name.equals("none")) {
            canvas.drawTextCentered("Congratulations! You've beat Janiterror!", bodyFont, 140);
        }
        else {
            canvas.drawTextCentered("Next Level: " + next_level_name, bodyFont, 140);
        }

        Color mainTint = (choose == 0 ? new Color(0.117f, 0.459f, 0.776f, 1f): Color.WHITE);
        float mainScale = choose == 0 ? 0.85f : 0.75f;
        canvas.draw(mainButton, mainTint, mainButton.getWidth()/2, mainButton.getHeight()/2,
                centerXMain, centerY, 0, BUTTON_SCALE*scale, mainScale*scale);

        System.out.println(choose);
        Color playTint = (choose == 1 ? new Color(0.117f, 0.459f, 0.776f, 1f): Color.WHITE);
        float playScale = choose == 1 ? 0.85f : 0.75f;
        canvas.draw(playButton, playTint, playButton.getWidth()/2, playButton.getHeight()/2,
                centerXNext, centerY, 0, BUTTON_SCALE*scale, playScale*scale);

        canvas.draw(current, centerX - current.getRegionWidth()/2, centerYJoe - current.getRegionHeight()/2);
        canvas.end();
    }

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

            if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) && choose==0){
                choose=1;
            }
            else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) && choose==1){
                choose=0;
            }

//            // We are are ready, notify our listener
//            if ((Gdx.input.isKeyJustPressed(Input.Keys.ENTER))) {
//                if (listener != null && isReady()) {
//                    System.out.println("next level");
//                    reset();
//                    listener.exitScreen(this, EXIT_NEXT);
//                } else if (listener != null && isMain()) {
//                    System.out.println("main menu real");
//                    reset();
//                    listener.exitScreen(this, EXIT_MENU);
//                }
//            }

//            if ((Gdx.input.isKeyJustPressed(Input.Keys.G))) {


            InputController input = InputController.getInstance();
            input.readInput(new Rectangle(0f, 0f, 32.0f, 18.0f), new Vector2(32.0f, 32.0f));
            if (input.didEnter()) {
                // We are are ready, notify our listener
                if (listener != null && isReady()) {
                    reset();
                    listener.exitScreen(this, EXIT_NEXT);
                } else if (listener != null && isMain()) {
                    reset();
                    listener.exitScreen(this, EXIT_MENU);
                }
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
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);
        heightY = height;

        centerY = (int)(BAR_HEIGHT_RATIO*height);
        centerX = width/2;
        centerXNext = (int) (width/2 + width * OFFSET_X_RATIO);
        heightY = height;

        centerXMain = (int) (width/2 - width * OFFSET_X_RATIO);
        centerYJoe = (int) (JOE_HEIGHT_RATIO*height);
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
        if (playButton == null || pressState == 2 || pressState == 4) {
            return true;
        }

        // Flip to match graphics coordinates
        screenY = heightY-screenY;

        // TODO: Fix scaling
        // Play button is a circle.
        float radius = BUTTON_SCALE*scale*playButton.getWidth()/2.0f;
        float dist = (screenX-centerXNext)*(screenX-centerXNext)+(screenY-centerY)*(screenY-centerY);
        if (dist < radius*radius) {
            pressState = 1;
        }

        radius = BUTTON_SCALE*scale*mainButton.getWidth()/2.0f;
        dist = (screenX-centerXMain)*(screenX-centerXMain)+(screenY-centerY)*(screenY-centerY);
        if (dist < radius*radius) {
            pressState = 3;
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

        if (pressState == 3) {
            pressState = 4;
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

    public enum StateJoe {MAIN, NEXT}

    public StateJoe currentState;
    public StateJoe previousState;

    public StateJoe getStateJoe(){
        if (choose == 0) { return StateJoe.MAIN; }
        return StateJoe.NEXT;
    }

    public TextureRegion getFrameJoe (float dt){
        currentState = getStateJoe();
        TextureRegion region;
        switch (currentState){
            case MAIN:
                region = joeMain.getKeyFrame(stateTimer,true);
                break;
            case NEXT:
                region = joeNext.getKeyFrame(stateTimer, true);
                break;
            default:
                region = joeNext.getKeyFrame(stateTimer, true);
                break;
        }

        stateTimer = currentState == previousState ? stateTimer + dt : 0;
        previousState = currentState;
        return region;
    }

}
