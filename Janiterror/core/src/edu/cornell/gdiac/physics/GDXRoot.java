/*
 * GDXRoot.java
 *
 * This is the primary class file for running the game.  It is the "static main" of
 * LibGDX.  In the first lab, we extended ApplicationAdapter.  In previous lab
 * we extended Game.  This is because of a weird graphical artifact that we do not
 * understand.  Transparencies (in 3D only) is failing when we use ApplicationAdapter. 
 * There must be some undocumented OpenGL code in setScreen.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
 package edu.cornell.gdiac.physics;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import com.badlogic.gdx.assets.loaders.*;
import com.badlogic.gdx.assets.loaders.resolvers.*;

import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.physics.floor.*;

/**
 * Root class for a LibGDX.  
 * 
 * This class is technically not the ROOT CLASS. Each platform has another class above
 * this (e.g. PC games use DesktopLauncher) which serves as the true root.  However, 
 * those classes are unique to each platform, while this class is the same across all 
 * plaforms. In addition, this functions as the root class all intents and purposes, 
 * and you would draw it as a root class in an architecture specification.  
 */
public class GDXRoot extends Game implements ScreenListener {
	/** AssetManager to load game assets (textures, sounds, etc.) */
	private AssetManager manager;
	/** Drawing context to display graphics (VIEW CLASS) */
	private GameCanvas canvas; 
	/** Player mode for the asset loading screen (CONTROLLER CLASS) */
	private LoadingMode loading;
	private LevelSelectMode select;
	private CreditsMode credits;
	/** Player mode for the the game proper (CONTROLLER CLASS) */
	private int current;
	/** List of all WorldControllers */
	private WorldController[] controllers;
	private String[] levelNames;
	private ScoreMode[] scores;

	/** The sound file for background music */
	private static final String BACKGROUND_TRACK_FILE = "floor/background-track.mp3";
	private static final String LOADING_TRACK_FILE = "floor/sound/track2.mp3";
	protected Array<String> assets;

	/**
	 * Creates a new game from the configuration settings.
	 *
	 * This method configures the asset manager, but does not load any assets
	 * or assign any screen.
	 */
	public GDXRoot() {
		// Start loading with the asset manager
		manager = new AssetManager();

		// Add font support to the asset manager
		FileHandleResolver resolver = new InternalFileHandleResolver();
		manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
		manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));
	}

	/**
	 * Called when the Application is first created.
	 * 
	 * This is method immediately loads assets for the loading screen, and prepares
	 * the asynchronous loader for all other assets.
	 */
	public void create() {
		canvas  = new GameCanvas();
		loading = new LoadingMode(canvas,manager,14);


		//Attempt to play music during loading
//		manager.load(BACKGROUND_TRACK_FILE, Sound.class);
//		assets.add(BACKGROUND_TRACK_FILE);
//		SoundController sounds = SoundController.getInstance();
//		sounds.allocate(manager, BACKGROUND_TRACK_FILE);
//		SoundController.getInstance().play(BACKGROUND_TRACK_FILE, BACKGROUND_TRACK_FILE, true, 0.4f);

		/*
		// Initialize the three game worlds

		controllers = new WorldController[3];
		controllers[0] = new RocketController();
		controllers[1] = new PlatformController();
		controllers[2] = new RagdollController();
		*/
		int number_of_levels = 14;

		controllers = new WorldController[number_of_levels];
		controllers[0] = new FloorController(1);
		controllers[1] = new FloorController(2);
		controllers[2] = new FloorController(3);
		controllers[3] = new FloorController(4);
        controllers[4] = new FloorController(5);
        controllers[5] = new FloorController(6);
		controllers[6] = new FloorController(7);
		controllers[7] = new FloorController(8);
		controllers[8] = new FloorController(9);
		controllers[9] = new FloorController(10);
		controllers[10] = new FloorController(11);
		controllers[11] = new FloorController(12);
		controllers[12] = new FloorController(13);
		controllers[13] = new FloorController(14);
//		controllers[14] = new FloorController(15);
//		controllers[15] = new FloorController(15);

		levelNames = new String[number_of_levels];
		levelNames[0] = "Spring Cleaning";
		levelNames[1] = "Sweeping with the Enemy";
		levelNames[2] = "This Level is a Febreeze";
		levelNames[3] = "This Level is Trash";
		levelNames[4] = "This Level Sucks";
        levelNames[5] = "Garbage Gladiator";
        levelNames[6] = "Old Calamari";
		levelNames[7] = "A Slimey Situation";
		levelNames[8] = "Ninjanitor";
		levelNames[9] = "Another One Bites the Dust";
		levelNames[10] = "Let The Slimefall";
		levelNames[11] = "All Washed Up";
		levelNames[12] = "The A-mop-calypse";
		levelNames[13] = "Janiterror Juggernaut";
//		levelNames[14] = "Janiterror";
//		levelNames[15] = "The A-mop-calypse15";

		select = new LevelSelectMode(canvas, levelNames);

		credits = new CreditsMode(canvas);

		scores = new ScoreMode[controllers.length];
		for(int ii = 0; ii < controllers.length; ii++) {
			controllers[ii].preLoadContent(manager);

			String level_name = "";
			if (ii == controllers.length-1) { level_name = "none"; }
			else { level_name = levelNames[ii + 1]; }

			scores[ii] = new ScoreMode(canvas, level_name);
			//scores[ii].setScreenListener(this);
		}
		current = 0;
		loading.setScreenListener(this);
		select.setScreenListener(this);
		//pause.setScreenListener(this);

		credits.setScreenListener(this);
		setScreen(loading);
	}

	/** 
	 * Called when the Application is destroyed. 
	 *
	 * This is preceded by a call to pause().
	 */
	public void dispose() {
		// Call dispose on our children
		loading.dispose();
		loading = null;

		select.dispose();
		select = null;

		//pause.dispose();
		//pause=null;
		credits.dispose();
		credits = null;


		setScreen(null);
		for(int ii = 0; ii < controllers.length; ii++) {
			controllers[ii].unloadContent(manager);
			controllers[ii].dispose();

			scores[ii].dispose();
		}

		canvas.dispose();
		canvas = null;
	
		// Unload all of the resources
		manager.clear();
		manager.dispose();
		super.dispose();
	}
	
	/**
	 * Called when the Application is resized. 
	 *
	 * This can happen at any point during a non-paused state but will never happen 
	 * before a call to create().
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		canvas.resize();
		super.resize(width,height);
	}
	
	/**
	 * The given screen has made a request to exit its player mode.
	 *
	 * The value exitCode can be used to implement menu options.
	 *
	 * @param screen   The screen requesting to exit
	 * @param exitCode The state of the screen upon exit
	 */
	public void exitScreen(Screen screen, int exitCode) {

		if (screen == loading) {
			for(int ii = 0; ii < controllers.length; ii++) {
				controllers[ii].loadContent(manager);
				controllers[ii].setScreenListener(this);
				controllers[ii].setCanvas(canvas);

				scores[ii].setScreenListener(this);
			}
			if (exitCode == 0) {
				//SoundController.getInstance().stop(LOADING_TRACK_FILE);
				//controllers[current].reset2();
				//controllers[current].preLoadContent(manager);
				current = 0;
				controllers[current].reset();
				//controllers[current].preLoadContent(manager);
				setScreen(controllers[current]);
			} else if (exitCode == 1) {
				select.reset();
				setScreen(select);
			} else if (exitCode == 2) {
				credits.reset();
				setScreen(credits);
			}
		} else if (screen instanceof WorldController && exitCode == WorldController.EXIT_MENU) {
			//((WorldController) screen).reset2();
			SoundController.getInstance().stop(BACKGROUND_TRACK_FILE);
			loading.reset();
			setScreen(loading);
		} else if (screen instanceof WorldController && exitCode == WorldController.EXIT_NEXT) {
			System.out.println("load next score" + current);
			scores[current].reset();
			setScreen(scores[current]);
		} else if (screen instanceof WorldController && exitCode == WorldController.EXIT_PREV) {
			current = (current+controllers.length-1) % controllers.length;
			controllers[current].reset();
			setScreen(controllers[current]);
		} else if (screen instanceof ScoreMode && exitCode == WorldController.EXIT_NEXT) {
			current = (current+1) % controllers.length;
			System.out.println("load next level" + current);
			controllers[current].reset();
			setScreen(controllers[current]);
		} else if (screen instanceof ScoreMode && exitCode == 0) {
			// main menu
			System.out.println("main menu");
			SoundController.getInstance().stop(BACKGROUND_TRACK_FILE);
			loading.reset();
			setScreen(loading);
		} else if (screen instanceof LevelSelectMode && exitCode == 0) {
			System.out.println("main");
			SoundController.getInstance().stop(BACKGROUND_TRACK_FILE);
			loading.reset();
			setScreen(loading);
		}/** else if (screen instanceof PauseMenu && exitCode == 1) { //continue on the game
 setScreen(controllers[current]);
 } else if (screen instanceof PauseMenu && exitCode==0){ //return back to main menu
 loading.reset();
 setScreen(loading);
 } **/
		else if (screen instanceof LevelSelectMode) {
			System.out.println("select level" + exitCode);
			current = exitCode - 1;
			controllers[current].reset();
			loading.getTrack().stop();
			setScreen(controllers[current]);
		} else if (screen instanceof  CreditsMode) {
			SoundController.getInstance().stop(BACKGROUND_TRACK_FILE);
			loading.reset();
			setScreen(loading);
		} else if (exitCode == WorldController.EXIT_QUIT) {
			// We quit the main application
			Gdx.app.exit();
		}
	}
}
