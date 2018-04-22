/*
 * WorldController.java
 *
 * This is the most important new class in this lab.  This class serves as a combination 
 * of the CollisionController and GameplayController from the previous lab.  There is not 
 * much to do for collisions; Box2d takes care of all of that for us.  This controller 
 * invokes Box2d and then performs any after the fact modifications to the data 
 * (e.g. gameplay).
 *
 * If you study this class, and the contents of the edu.cornell.cs3152.physics.obstacles
 * package, you should be able to understand how the Physics engine works.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.physics;

import java.util.Iterator;

import com.badlogic.gdx.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.physics.obstacle.*;

/**
 * Base class for a world-specific controller.
 *
 *
 * A world has its own objects, assets, and input controller.  Thus this is 
 * really a mini-GameEngine in its own right.  The only thing that it does
 * not do is create a GameCanvas; that is shared with the main application.
 *
 * You will notice that asset loading is not done with static methods this time.  
 * Instance asset loading makes it easier to process our game modes in a loop, which 
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public abstract class WorldController implements Screen {
	
	/** 
	 * Tracks the asset state.  Otherwise subclasses will try to load assets 
	 */
	protected enum AssetState {
		/** No assets loaded */
		EMPTY,
		/** Still loading assets */
		LOADING,
		/** Assets are complete */
		COMPLETE
	}

	/** Track asset loading from all instances and subclasses */
	protected AssetState worldAssetState = AssetState.EMPTY;
	/** Track all loaded assets (for unloading purposes) */
	protected Array<String> assets;	
	
	// Pathnames to shared assets
	/** File to texture for walls and platforms */
	private static final String WALL_LEFT_FILE = "shared/vertical-left.png";
	private static final String WALL_RIGHT_FILE = "shared/vertical-right.png";
	private static final String WALL_MID_FILE = "shared/horizontal-wall.png";
	private static final String WALL_TL_FILE = "shared/corner-top-left.png";
	private static final String WALL_TR_FILE = "shared/corner-top-right.png";
	private static final String WALL_BR_FILE = "shared/corner-bottom-right.png";
	private static final String WALL_BL_FILE = "shared/corner-bottom-left.png";
    private static final String WALL_SR_FILE = "shared/vertical-left-special.png";
    private static final String WALL_SL_FILE = "shared/vertical-right-special.png";
	private static final String WALL_ER_FILE = "shared/horizontal-wall-rightend.png";
	private static final String WALL_EL_FILE = "shared/horizontal-wall-leftend.png";

	/** File to texture for the win door */
	private static final String GOAL_FILE = "shared/stairs-down.png";
	/** File to texture for the mop cart */
	private static final String MOP_CART_FILE = "shared/mop-cart.png";
	/** File to texture for the mop cart */
	private static final String SPECIAL_HEALTH_FILE = "shared/chips.png";
	/** Retro font for displaying messages */
	private static final String FONT_FILE = "shared/RetroGame.ttf";
	private static final int FONT_SIZE = 32;
	/** The texture files for characters/attacks */
	private static final String JANITOR_IDLE_FILE  = "floor/janitor-idle.png";
	private static final String JANITOR_WALKR_FILE  = "floor/janitor-walk-R.png";
	private static final String JANITOR_WALKU_FILE  = "floor/janitor-walk-U.png";
	private static final String JANITOR_WALKD_FILE  = "floor/janitor-walk-D.png";
	private static final String JANITOR_MOPR_FILE  = "floor/janitor-attack-R-mop.png";
	private static final String JANITOR_MOPL_FILE  = "floor/janitor-attack-L-mop.png";
	private static final String JANITOR_MOPU_FILE  = "floor/janitor-attack-U-mop.png";
	private static final String JANITOR_MOPD_FILE  = "floor/janitor-attack-D-mop.png";
    private static final String JANITOR_LIDR_FILE  = "floor/janitor-attack-R-garbage-lid.png";
	private static final String JANITOR_LIDL_FILE  = "floor/janitor-attack-L-garbage-lid.png";
    private static final String JANITOR_LIDU_FILE  = "floor/janitor-attack-U-garbage-lid.png";
    private static final String JANITOR_LIDD_FILE  = "floor/janitor-attack-D-garbage-lid.png";
	private static final String JANITOR_SPRAYR_FILE  = "floor/janitor-attack-R-spray.png";
	private static final String JANITOR_SPRAYL_FILE  = "floor/janitor-attack-L-spray.png";
	private static final String JANITOR_SPRAYU_FILE  = "floor/janitor-attack-U-spray.png";
	private static final String JANITOR_SPRAYD_FILE  = "floor/janitor-attack-D-spray.png";
    private static final String JANITOR_VACUUMR_FILE  = "floor/janitor-attack-R-vacuum.png";
    private static final String JANITOR_VACUUML_FILE  = "floor/janitor-attack-L-vacuum.png";
    private static final String JANITOR_VACUUMU_FILE  = "floor/janitor-attack-U-vacuum.png";
    private static final String JANITOR_VACUUMD_FILE  = "floor/janitor-attack-D-vacuum.png";
	/** The texture files for mad scientist */
	private static final String MAD_ATTACKR_FILE  = "floor/mad-attack-side-v2.png";
	private static final String MAD_ATTACKL_FILE  = "floor/mad-attack-side-flip.png";
	private static final String MAD_ATTACKU_FILE  = "floor/mad-attack-back-v2.png";
	private static final String MAD_ATTACKD_FILE  = "floor/mad-attack-front-v2.png";
	private static final String MAD_WALKR_FILE  = "floor/mad-walk-side-v2.png";
	private static final String MAD_WALKU_FILE  = "floor/mad-walk-back-v2.png";
	private static final String MAD_WALKD_FILE  = "floor/mad-walk-front-v2.png";
	private static final String MAD_IDLE_FILE  = "floor/mad-idle.png";
	private static final String MAD_DEATH_FILE  = "floor/mad-death-v2.png";
	/** The texture files for robot */
	private static final String ROBOT_ATTACKL_FILE  = "floor/robot-attack-left.png";
	private static final String ROBOT_ATTACKR_FILE  = "floor/robot-attack-right.png";
	private static final String ROBOT_ATTACKU_FILE  = "floor/robot-attack-back-recolor.png";
	private static final String ROBOT_ATTACKD_FILE  = "floor/robot-attack-front-recolor.png";
	private static final String ROBOT_WALKR_FILE  = "floor/robot-walk-side-recolor.png";
	private static final String ROBOT_WALKU_FILE  = "floor/robot-walk-back-recolor.png";
	private static final String ROBOT_WALKD_FILE  = "floor/robot-walk-front-recolor.png";
	private static final String ROBOT_IDLE_FILE  = "floor/robot-idle-v2.png";
	private static final String ROBOT_DEATH_FILE  = "floor/R-death.png";
	/** The texture files for slime */
	private static final String SLIME_ATTACKL_FILE  = "floor/slime-attack-L.png";
	private static final String SLIME_ATTACKR_FILE  = "floor/slime-attack-R.png";
	private static final String SLIME_ATTACKU_FILE  = "floor/slime-attack-U.png";
	private static final String SLIME_ATTACKD_FILE  = "floor/slime-attack-D.png";
	private static final String SLIME_WALKR_FILE  = "floor/slime-walk-R.png";
	private static final String SLIME_WALKU_FILE  = "floor/slime-walk-U.png";
	private static final String SLIME_WALKD_FILE  = "floor/slime-walk-D.png";
	private static final String SLIME_IDLE_FILE  = "floor/robot-idle-v2.png";
		//slime idle
	private static final String SLIME_DEATH_FILE  = "floor/slime-dead.png";
	private static final String SLIME_STUN_FILE  = "floor/slime-stunned.png";
	/** The texture files for lizardman */
	private static final String LIZARD_ATTACKL_FILE  = "floor/L-attack-L.png";
	private static final String LIZARD_ATTACKR_FILE  = "floor/L-attack-R.png";
	private static final String LIZARD_ATTACKU_FILE  = "floor/L-attack-back.png";
	private static final String LIZARD_ATTACKD_FILE  = "floor/L-attack-front.png";
	private static final String LIZARD_WALKR_FILE  = "floor/L-walk-side.png";
	private static final String LIZARD_WALKU_FILE  = "floor/L-walk-back.png";
	private static final String LIZARD_WALKD_FILE  = "floor/L-walk-front.png";
	private static final String LIZARD_IDLE_FILE  = "floor/L-idle.png";
	private static final String LIZARD_DEATH_FILE  = "floor/L-death.png";


	private static final String SCIENTIST_FILE  = "floor/scientist.png";
	private static final String SLIME_FILE  = "floor/slime.png";
	private static final String LIZARD_FILE  = "floor/lizard.png";
	private static final String ROBOT_FILE = "floor/robot.png";
	private static final String BULLET_FILE  = "floor/lid.png";
	private static final String SLIMEBALL_FILE = "floor/slimeball.png";

	/** The texture files for the UI icons */
	private static final String MOP_FILE  = "floor/ui-mop.png";
	private static final String SPRAY_FILE  = "floor/ui-spray.png";
	private static final String VACUUM_FILE  = "floor/ui-vacuum.png";
	private static final String LID_FILE  = "floor/ui-lid.png";

	/** The texture files for the UI icons */
	private static final String HEALTH_BAR_FILE  = "floor/ui-bar-health.png";
	private static final String HEALTH_BAR_FILE2  = "floor/ui-bar-health-upgrade1.png";

	private static final String MOP_BAR_FILE  = "floor/ui-bar-mop.png";
	private static final String SPRAY_BAR_FILE  = "floor/ui-bar-spray.png";
	private static final String VACUUM_BAR_FILE  = "floor/ui-bar-vacuum.png";
	private static final String LID_BAR_FILE  = "floor/ui-bar-lid.png";
	private static final String MOPCART_INDEX_FILE  = "floor/mopcart-index.png";

	private static final String BACKGROUND_FILE = "shared/loading.png";
	private static final String TILE_FILE = "shared/basic-tile-32.png";
	private static final String BROKEN1_TILE_FILE = "shared/broken-tile-1-32.png";
	private static final String BROKEN2_TILE_FILE = "shared/broken-tile-2-32.png";
	private static final String BROKEN3_TILE_FILE = "shared/broken-tile-3-32.png";
	private static final String BROKEN4_TILE_FILE = "shared/broken-tile-4-32.png";
	private static final String GRATE_TILE_FILE = "shared/grate-tile-32.png";
	private static final String STAIRS_TILE_FILE = "shared/stairs-down.png";
	private static final String UNDER_TILE_FILE = "shared/undertile-32.png";
	private static final String HAZARD_TILE_FILE = "shared/hazard-tile.png";

	private static final String PLAY_BTN_FILE = "shared/play.png";

	/** Texture assets for characters/attacks */
	protected TextureRegion wallRightTexture;
	protected TextureRegion wallLeftTexture;
	protected TextureRegion wallMidTexture;
	protected TextureRegion wallTLTexture;
	protected TextureRegion wallTRTexture;
	protected TextureRegion wallBLTexture;
	protected TextureRegion wallBRTexture;
    protected TextureRegion wallSLTexture;
    protected TextureRegion wallSRTexture;
	protected TextureRegion wallELTexture;
	protected TextureRegion wallERTexture;
	protected TextureRegion avatarIdleTexture;
	protected TextureRegion avatarWalkRTexture;
	protected TextureRegion avatarWalkUTexture;
	protected TextureRegion avatarWalkDTexture;
	protected TextureRegion avatarMopRTexture;
	protected TextureRegion avatarMopLTexture;
	protected TextureRegion avatarMopUTexture;
	protected TextureRegion avatarMopDTexture;
    protected TextureRegion avatarLidRTexture;
	protected TextureRegion avatarLidLTexture;
    protected TextureRegion avatarLidUTexture;
    protected TextureRegion avatarLidDTexture;
	protected TextureRegion avatarSprayRTexture;
	protected TextureRegion avatarSprayLTexture;
	protected TextureRegion avatarSprayUTexture;
	protected TextureRegion avatarSprayDTexture;
    protected TextureRegion avatarVacuumRTexture;
    protected TextureRegion avatarVacuumLTexture;
    protected TextureRegion avatarVacuumUTexture;
    protected TextureRegion avatarVacuumDTexture;
	/** Texture assets for mad scientist */
	protected TextureRegion scientistTexture;
	protected TextureRegion scientistAttackRTexture;
	protected TextureRegion scientistAttackLTexture;
	protected TextureRegion scientistAttackUTexture;
	protected TextureRegion scientistAttackDTexture;
	protected TextureRegion scientistWalkRTexture;
	protected TextureRegion scientistWalkUTexture;
	protected TextureRegion scientistWalkDTexture;
	protected TextureRegion scientistIdleTexture;
	protected TextureRegion scientistDeathTexture;
	/** Texture assets for robot */
	protected TextureRegion robotTexture;
	protected TextureRegion robotAttackLTexture;
	protected TextureRegion robotAttackRTexture;
	protected TextureRegion robotAttackUTexture;
	protected TextureRegion robotAttackDTexture;
	protected TextureRegion robotWalkRTexture;
	protected TextureRegion robotWalkUTexture;
	protected TextureRegion robotWalkDTexture;
	protected TextureRegion robotIdleTexture;
	protected TextureRegion robotDeathTexture;
	/** Texture assets for slime */
	protected TextureRegion slimeTexture;
	protected TextureRegion slimeAttackLTexture;
	protected TextureRegion slimeAttackRTexture;
	protected TextureRegion slimeAttackUTexture;
	protected TextureRegion slimeAttackDTexture;
	protected TextureRegion slimeWalkRTexture;
	protected TextureRegion slimeWalkUTexture;
	protected TextureRegion slimeWalkDTexture;
	protected TextureRegion slimeIdleTexture;
	protected TextureRegion slimeDeathTexture;
	protected TextureRegion slimeStunTexture;
	/** Texture assets for lizard */
	protected TextureRegion lizardTexture;
	protected TextureRegion lizardAttackLTexture;
	protected TextureRegion lizardAttackRTexture;
	protected TextureRegion lizardAttackUTexture;
	protected TextureRegion lizardAttackDTexture;
	protected TextureRegion lizardWalkRTexture;
	protected TextureRegion lizardWalkUTexture;
	protected TextureRegion lizardWalkDTexture;
	protected TextureRegion lizardIdleTexture;
	protected TextureRegion lizardDeathTexture;

	protected TextureRegion bulletTexture;
	protected TextureRegion slimeballTexture;

	/** Texture assets for UI Icons */
	protected Texture mopTexture;
	protected Texture sprayTexture;
	protected Texture vacuumTexture;
	protected Texture lidTexture;
    protected Texture mopcartIndexTexture;

	protected TextureRegion healthBarTexture;
	protected TextureRegion healthBarTexture2;

	protected TextureRegion mopBarTexture;
	protected TextureRegion sprayBarTexture;
	protected TextureRegion vacuumBarTexture;
	protected TextureRegion lidBarTexture;

	/** Texture Asset for tiles */
	protected Texture tileTexture;
	protected Texture broken1TileTexture;
	protected Texture broken2tileTexture;
	protected Texture broken3tileTexture;
	protected Texture broken4tileTexture;
	protected Texture grateTileTexture;
	protected Texture stairsTileTexture;
	protected Texture underTileTexture;
	protected Texture hazardTileTexture;
	/** Texture asset for the mop cart background */
	protected Texture backgroundTexture;
	/** The texture for the exit condition */
	protected TextureRegion goalTile;
	/** The texture for the mop cart*/
	protected TextureRegion mopTile;
	/** The texture for the mop cart*/
	protected TextureRegion specialHealthTile;
	/** The font for giving messages to the player */
	protected BitmapFont displayFont;

	/**
	 * Preloads the assets for this controller.
	 *
	 * To make the game modes more for-loop friendly, we opted for nonstatic loaders
	 * this time.  However, we still want the assets themselves to be static.  So
	 * we have an AssetState that determines the current loading state.  If the
	 * assets are already loaded, this method will do nothing.
	 * 
	 * @param manager Reference to global asset manager.
	 */
	public void preLoadContent(AssetManager manager) {
		if (worldAssetState != AssetState.EMPTY) {
			return;
		}
		
		worldAssetState = AssetState.LOADING;
		// Load the shared tiles.
		manager.load(WALL_LEFT_FILE, Texture.class);
		assets.add(WALL_LEFT_FILE);
		manager.load(WALL_RIGHT_FILE, Texture.class);
		assets.add(WALL_RIGHT_FILE);
		manager.load(WALL_MID_FILE, Texture.class);
		assets.add(WALL_MID_FILE);
		manager.load(WALL_TL_FILE, Texture.class);
		assets.add(WALL_TL_FILE);
		manager.load(WALL_TR_FILE, Texture.class);
		assets.add(WALL_TR_FILE);
		manager.load(WALL_BL_FILE, Texture.class);
		assets.add(WALL_BL_FILE);
		manager.load(WALL_BR_FILE, Texture.class);
		assets.add(WALL_BR_FILE);
        manager.load(WALL_SL_FILE, Texture.class);
        assets.add(WALL_SL_FILE);
        manager.load(WALL_SR_FILE, Texture.class);
        assets.add(WALL_SR_FILE);
		manager.load(WALL_EL_FILE, Texture.class);
		assets.add(WALL_EL_FILE);
		manager.load(WALL_ER_FILE, Texture.class);
		assets.add(WALL_ER_FILE);
		manager.load(JANITOR_IDLE_FILE, Texture.class);
		assets.add(JANITOR_IDLE_FILE);
		manager.load(JANITOR_WALKR_FILE, Texture.class);
		assets.add(JANITOR_WALKR_FILE);
		manager.load(JANITOR_WALKU_FILE, Texture.class);
		assets.add(JANITOR_WALKU_FILE);
		manager.load(JANITOR_WALKD_FILE, Texture.class);
		assets.add(JANITOR_WALKD_FILE);
		manager.load(JANITOR_MOPR_FILE, Texture.class);
		assets.add(JANITOR_MOPR_FILE);
		manager.load(JANITOR_MOPL_FILE, Texture.class);
        assets.add(JANITOR_MOPL_FILE);
        manager.load(JANITOR_MOPU_FILE, Texture.class);
        assets.add(JANITOR_MOPU_FILE);
        manager.load(JANITOR_MOPD_FILE, Texture.class);
        assets.add(JANITOR_MOPD_FILE);
        manager.load(JANITOR_LIDR_FILE, Texture.class);
        assets.add(JANITOR_LIDR_FILE);
		manager.load(JANITOR_LIDL_FILE, Texture.class);
		assets.add(JANITOR_LIDL_FILE);
        manager.load(JANITOR_LIDU_FILE, Texture.class);
        assets.add(JANITOR_LIDU_FILE);
        manager.load(JANITOR_LIDD_FILE, Texture.class);
        assets.add(JANITOR_LIDD_FILE);
		manager.load(JANITOR_SPRAYR_FILE, Texture.class);
		assets.add(JANITOR_SPRAYR_FILE);
		manager.load(JANITOR_SPRAYL_FILE, Texture.class);
		assets.add(JANITOR_SPRAYL_FILE);
		manager.load(JANITOR_SPRAYU_FILE, Texture.class);
		assets.add(JANITOR_SPRAYU_FILE);
		manager.load(JANITOR_SPRAYD_FILE, Texture.class);
		assets.add(JANITOR_SPRAYD_FILE);
        manager.load(JANITOR_VACUUMR_FILE, Texture.class);
        assets.add(JANITOR_VACUUMR_FILE);
        manager.load(JANITOR_VACUUML_FILE, Texture.class);
        assets.add(JANITOR_VACUUML_FILE);
        manager.load(JANITOR_VACUUMU_FILE, Texture.class);
        assets.add(JANITOR_VACUUMU_FILE);
        manager.load(JANITOR_VACUUMD_FILE, Texture.class);
        assets.add(JANITOR_VACUUMD_FILE);
		manager.load(SCIENTIST_FILE, Texture.class);
		assets.add(SCIENTIST_FILE);
		manager.load(MAD_ATTACKR_FILE, Texture.class);
		assets.add(MAD_ATTACKR_FILE);
		manager.load(MAD_ATTACKL_FILE, Texture.class);
		assets.add(MAD_ATTACKL_FILE);
		manager.load(MAD_ATTACKU_FILE, Texture.class);
		assets.add(MAD_ATTACKU_FILE);
		manager.load(MAD_ATTACKD_FILE, Texture.class);
		assets.add(MAD_ATTACKD_FILE);
		manager.load(MAD_WALKR_FILE, Texture.class);
		assets.add(MAD_WALKR_FILE);
		manager.load(MAD_WALKU_FILE, Texture.class);
		assets.add(MAD_WALKU_FILE);
		manager.load(MAD_WALKD_FILE, Texture.class);
		assets.add(MAD_WALKD_FILE);
		manager.load(MAD_IDLE_FILE, Texture.class);
		assets.add(MAD_IDLE_FILE);
		manager.load(MAD_DEATH_FILE, Texture.class);
		assets.add(MAD_DEATH_FILE);
		manager.load(ROBOT_FILE, Texture.class);
		assets.add(ROBOT_FILE);
		manager.load(ROBOT_ATTACKL_FILE, Texture.class);
		assets.add(ROBOT_ATTACKL_FILE);
		manager.load(ROBOT_ATTACKR_FILE, Texture.class);
		assets.add(ROBOT_ATTACKR_FILE);
		manager.load(ROBOT_ATTACKU_FILE, Texture.class);
		assets.add(ROBOT_ATTACKU_FILE);
		manager.load(ROBOT_ATTACKD_FILE, Texture.class);
		assets.add(ROBOT_ATTACKD_FILE);
		manager.load(ROBOT_WALKR_FILE, Texture.class);
		assets.add(ROBOT_WALKR_FILE);
		manager.load(ROBOT_WALKU_FILE, Texture.class);
		assets.add(ROBOT_WALKU_FILE);
		manager.load(ROBOT_WALKD_FILE, Texture.class);
		assets.add(ROBOT_WALKD_FILE);
		manager.load(ROBOT_IDLE_FILE, Texture.class);
		assets.add(ROBOT_IDLE_FILE);
		manager.load(ROBOT_DEATH_FILE, Texture.class);
		assets.add(ROBOT_DEATH_FILE);
		manager.load(SLIME_FILE, Texture.class);
		assets.add(SLIME_FILE);
		manager.load(SLIME_ATTACKL_FILE, Texture.class);
		assets.add(SLIME_ATTACKL_FILE);
		manager.load(SLIME_ATTACKR_FILE, Texture.class);
		assets.add(SLIME_ATTACKR_FILE);
		manager.load(SLIME_ATTACKU_FILE, Texture.class);
		assets.add(SLIME_ATTACKU_FILE);
		manager.load(SLIME_ATTACKD_FILE, Texture.class);
		assets.add(SLIME_ATTACKD_FILE);
		manager.load(SLIME_WALKR_FILE, Texture.class);
		assets.add(SLIME_WALKR_FILE);
		manager.load(SLIME_WALKU_FILE, Texture.class);
		assets.add(SLIME_WALKU_FILE);
		manager.load(SLIME_WALKD_FILE, Texture.class);
		assets.add(SLIME_WALKD_FILE);
		manager.load(SLIME_IDLE_FILE, Texture.class);
		assets.add(SLIME_IDLE_FILE);
		manager.load(SLIME_DEATH_FILE, Texture.class);
		assets.add(SLIME_DEATH_FILE);
		manager.load(SLIME_STUN_FILE, Texture.class);
		assets.add(SLIME_STUN_FILE);
		manager.load(LIZARD_FILE, Texture.class);
		assets.add(LIZARD_FILE);
		manager.load(LIZARD_ATTACKL_FILE, Texture.class);
		assets.add(LIZARD_ATTACKL_FILE);
		manager.load(LIZARD_ATTACKR_FILE, Texture.class);
		assets.add(LIZARD_ATTACKR_FILE);
		manager.load(LIZARD_ATTACKU_FILE, Texture.class);
		assets.add(LIZARD_ATTACKU_FILE);
		manager.load(LIZARD_ATTACKD_FILE, Texture.class);
		assets.add(LIZARD_ATTACKD_FILE);
		manager.load(LIZARD_WALKR_FILE, Texture.class);
		assets.add(LIZARD_WALKR_FILE);
		manager.load(LIZARD_WALKU_FILE, Texture.class);
		assets.add(LIZARD_WALKU_FILE);
		manager.load(LIZARD_WALKD_FILE, Texture.class);
		assets.add(LIZARD_WALKD_FILE);
		manager.load(LIZARD_IDLE_FILE, Texture.class);
		assets.add(LIZARD_IDLE_FILE);
		manager.load(LIZARD_DEATH_FILE, Texture.class);
		assets.add(LIZARD_DEATH_FILE);


		manager.load(BULLET_FILE, Texture.class);
		assets.add(BULLET_FILE);
		manager.load(SLIMEBALL_FILE, Texture.class);
		assets.add(SLIMEBALL_FILE);

		//UI Icons
		manager.load(MOP_FILE, Texture.class);
		assets.add(MOP_FILE);
		manager.load(SPRAY_FILE, Texture.class);
		assets.add(SPRAY_FILE);
		manager.load(VACUUM_FILE, Texture.class);
		assets.add(VACUUM_FILE);
		manager.load(LID_FILE, Texture.class);
		assets.add(LID_FILE);
        manager.load(MOPCART_INDEX_FILE, Texture.class);
        assets.add(MOPCART_INDEX_FILE);

		manager.load(HEALTH_BAR_FILE, Texture.class);
		assets.add(HEALTH_BAR_FILE);
		manager.load(HEALTH_BAR_FILE2, Texture.class);
		assets.add(HEALTH_BAR_FILE2);

		manager.load(MOP_BAR_FILE, Texture.class);
		assets.add(MOP_BAR_FILE);
		manager.load(SPRAY_BAR_FILE, Texture.class);
		assets.add(SPRAY_BAR_FILE);
		manager.load(VACUUM_BAR_FILE, Texture.class);
		assets.add(VACUUM_BAR_FILE);
		manager.load(LID_BAR_FILE, Texture.class);
		assets.add(LID_BAR_FILE);

		manager.load(TILE_FILE, Texture.class);
		assets.add(TILE_FILE);
		manager.load(BROKEN1_TILE_FILE, Texture.class);
		assets.add(BROKEN1_TILE_FILE);
		manager.load(BROKEN2_TILE_FILE, Texture.class);
		assets.add(BROKEN2_TILE_FILE);
		manager.load(BROKEN3_TILE_FILE, Texture.class);
		assets.add(BROKEN3_TILE_FILE);
		manager.load(BROKEN4_TILE_FILE, Texture.class);
		assets.add(BROKEN4_TILE_FILE);
		manager.load(GRATE_TILE_FILE, Texture.class);
		assets.add(GRATE_TILE_FILE);
		manager.load(UNDER_TILE_FILE, Texture.class);
		assets.add(UNDER_TILE_FILE);
		manager.load(STAIRS_TILE_FILE, Texture.class);
		assets.add(STAIRS_TILE_FILE);
		manager.load(HAZARD_TILE_FILE, Texture.class);
		assets.add(HAZARD_TILE_FILE);

		manager.load(GOAL_FILE,Texture.class);
		assets.add(GOAL_FILE);
		manager.load(MOP_CART_FILE,Texture.class);
		assets.add(MOP_CART_FILE);

		//Load Special Power Up Tiles
		manager.load(SPECIAL_HEALTH_FILE,Texture.class);
		assets.add(SPECIAL_HEALTH_FILE);

		// Load the font
		FreetypeFontLoader.FreeTypeFontLoaderParameter size2Params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
		size2Params.fontFileName = FONT_FILE;
		size2Params.fontParameters.size = FONT_SIZE;
		manager.load(FONT_FILE, BitmapFont.class, size2Params);
		assets.add(FONT_FILE);
	}

	/**
	 * Loads the assets for this controller.
	 *
	 * To make the game modes more for-loop friendly, we opted for nonstatic loaders
	 * this time.  However, we still want the assets themselves to be static.  So
	 * we have an AssetState that determines the current loading state.  If the
	 * assets are already loaded, this method will do nothing.
	 * 
	 * @param manager Reference to global asset manager.
	 */
	public void loadContent(AssetManager manager) {
		if (worldAssetState != AssetState.LOADING) {
			return;
		}
		
		// Allocate the tiles
		wallRightTexture = createTexture(manager,WALL_RIGHT_FILE,false);
		wallLeftTexture = createTexture(manager,WALL_LEFT_FILE,false);
		wallMidTexture = createTexture(manager,WALL_MID_FILE,false);
		wallTRTexture = createTexture(manager,WALL_TR_FILE,false);
		wallTLTexture = createTexture(manager,WALL_TL_FILE,false);
		wallBRTexture = createTexture(manager,WALL_BR_FILE,false);
		wallBLTexture = createTexture(manager,WALL_BL_FILE,false);
        wallSRTexture = createTexture(manager,WALL_SR_FILE,false);
        wallSLTexture = createTexture(manager,WALL_SL_FILE,false);
		wallERTexture = createTexture(manager,WALL_ER_FILE,false);
		wallELTexture = createTexture(manager,WALL_EL_FILE,false);
		avatarWalkRTexture = createTexture(manager,JANITOR_WALKR_FILE,false);
		avatarWalkUTexture = createTexture(manager,JANITOR_WALKU_FILE,false);
		avatarWalkDTexture = createTexture(manager,JANITOR_WALKD_FILE,false);
		avatarIdleTexture = createTexture(manager,JANITOR_IDLE_FILE,false);
		avatarMopRTexture = createTexture(manager,JANITOR_MOPR_FILE,false);
		avatarMopLTexture = createTexture(manager,JANITOR_MOPL_FILE,false);
		avatarMopUTexture = createTexture(manager,JANITOR_MOPU_FILE,false);
		avatarMopDTexture = createTexture(manager,JANITOR_MOPD_FILE,false);
        avatarLidRTexture = createTexture(manager,JANITOR_LIDR_FILE,false);
		avatarLidLTexture = createTexture(manager,JANITOR_LIDL_FILE,false);
		avatarLidUTexture = createTexture(manager,JANITOR_LIDU_FILE,false);
		avatarLidDTexture = createTexture(manager,JANITOR_LIDD_FILE,false);
		avatarSprayRTexture = createTexture(manager,JANITOR_SPRAYR_FILE,false);
		avatarSprayLTexture = createTexture(manager,JANITOR_SPRAYL_FILE,false);
		avatarSprayUTexture = createTexture(manager,JANITOR_SPRAYU_FILE,false);
		avatarSprayDTexture = createTexture(manager,JANITOR_SPRAYD_FILE,false);
        avatarVacuumRTexture = createTexture(manager,JANITOR_VACUUMR_FILE,false);
        avatarVacuumLTexture = createTexture(manager,JANITOR_VACUUML_FILE,false);
        avatarVacuumUTexture = createTexture(manager,JANITOR_VACUUMU_FILE,false);
        avatarVacuumDTexture = createTexture(manager,JANITOR_VACUUMD_FILE,false);
		scientistAttackRTexture = createTexture(manager,MAD_ATTACKR_FILE,false);
		scientistAttackLTexture = createTexture(manager,MAD_ATTACKL_FILE,false);
		scientistAttackUTexture = createTexture(manager,MAD_ATTACKU_FILE,false);
		scientistAttackDTexture = createTexture(manager,MAD_ATTACKD_FILE,false);
		scientistWalkRTexture = createTexture(manager,MAD_WALKR_FILE,false);
		scientistWalkUTexture = createTexture(manager,MAD_WALKU_FILE,false);
		scientistWalkDTexture = createTexture(manager,MAD_WALKD_FILE,false);
		scientistIdleTexture = createTexture(manager,MAD_IDLE_FILE,false);
		scientistDeathTexture = createTexture(manager,MAD_DEATH_FILE,false);
		robotTexture = createTexture(manager,ROBOT_FILE,false);
		robotAttackLTexture = createTexture(manager,ROBOT_ATTACKL_FILE,false);
		robotAttackRTexture = createTexture(manager,ROBOT_ATTACKR_FILE,false);
		robotAttackUTexture = createTexture(manager,ROBOT_ATTACKU_FILE,false);
		robotAttackDTexture = createTexture(manager,ROBOT_ATTACKD_FILE,false);
		robotWalkRTexture = createTexture(manager,ROBOT_WALKR_FILE,false);
		robotWalkUTexture = createTexture(manager,ROBOT_WALKU_FILE,false);
		robotWalkDTexture = createTexture(manager,ROBOT_WALKD_FILE,false);
		robotIdleTexture = createTexture(manager,ROBOT_IDLE_FILE,false);
		robotDeathTexture = createTexture(manager,ROBOT_DEATH_FILE,false);
		slimeTexture = createTexture(manager,SLIME_FILE, false);
		slimeAttackLTexture = createTexture(manager,SLIME_ATTACKL_FILE,false);
		slimeAttackRTexture = createTexture(manager,SLIME_ATTACKR_FILE,false);
		slimeAttackUTexture = createTexture(manager,SLIME_ATTACKU_FILE,false);
		slimeAttackDTexture = createTexture(manager,SLIME_ATTACKD_FILE,false);
		slimeWalkRTexture = createTexture(manager,SLIME_WALKR_FILE,false);
		slimeWalkUTexture = createTexture(manager,SLIME_WALKU_FILE,false);
		slimeWalkDTexture = createTexture(manager,SLIME_WALKD_FILE,false);
		slimeIdleTexture = createTexture(manager,SLIME_IDLE_FILE,false);
		slimeDeathTexture = createTexture(manager,SLIME_DEATH_FILE,false);
		slimeStunTexture = createTexture(manager,SLIME_STUN_FILE,false);
		lizardTexture = createTexture(manager,LIZARD_FILE, false);
		lizardAttackLTexture = createTexture(manager,LIZARD_ATTACKL_FILE,false);
		lizardAttackRTexture = createTexture(manager,LIZARD_ATTACKR_FILE,false);
		lizardAttackUTexture = createTexture(manager,LIZARD_ATTACKU_FILE,false);
		lizardAttackDTexture = createTexture(manager,LIZARD_ATTACKD_FILE,false);
		lizardWalkRTexture = createTexture(manager,LIZARD_WALKR_FILE,false);
		lizardWalkUTexture = createTexture(manager,LIZARD_WALKU_FILE,false);
		lizardWalkDTexture = createTexture(manager,LIZARD_WALKD_FILE,false);
		lizardIdleTexture = createTexture(manager,LIZARD_IDLE_FILE,false);
		lizardDeathTexture = createTexture(manager,LIZARD_DEATH_FILE,false);
		bulletTexture = createTexture(manager,BULLET_FILE,false);
		slimeballTexture = createTexture(manager,SLIMEBALL_FILE,false);
		backgroundTexture = new Texture(BACKGROUND_FILE);

		//UI Icons
		mopTexture = new Texture(MOP_FILE);
		sprayTexture = new Texture(SPRAY_FILE);
		vacuumTexture = new Texture(VACUUM_FILE);
		lidTexture = new Texture(LID_FILE);

		healthBarTexture = createTexture(manager,HEALTH_BAR_FILE,false);
		healthBarTexture2 = createTexture(manager,HEALTH_BAR_FILE2,false);

		mopBarTexture = createTexture(manager,MOP_BAR_FILE,false);
		sprayBarTexture = createTexture(manager,SPRAY_BAR_FILE,false);
		vacuumBarTexture = createTexture(manager,VACUUM_BAR_FILE,false);
		lidBarTexture = createTexture(manager,LID_BAR_FILE,false);
        mopcartIndexTexture = new Texture(MOPCART_INDEX_FILE);

        tileTexture = new Texture(TILE_FILE);
		broken1TileTexture = new Texture(BROKEN1_TILE_FILE);
		broken2tileTexture = new Texture(BROKEN2_TILE_FILE);
		broken3tileTexture = new Texture(BROKEN3_TILE_FILE);
		broken4tileTexture = new Texture(BROKEN4_TILE_FILE);
		grateTileTexture = new Texture(GRATE_TILE_FILE);
		underTileTexture = new Texture(STAIRS_TILE_FILE);
		stairsTileTexture = new Texture(UNDER_TILE_FILE);
		hazardTileTexture = new Texture(HAZARD_TILE_FILE);

		goalTile  = createTexture(manager,GOAL_FILE,true);
		mopTile = createTexture(manager,MOP_CART_FILE, true);
		specialHealthTile = createTexture(manager,SPECIAL_HEALTH_FILE, true);

		// Allocate the font
		if (manager.isLoaded(FONT_FILE)) {
			displayFont = manager.get(FONT_FILE,BitmapFont.class);
		} else {
			displayFont = null;
		}

		worldAssetState = AssetState.COMPLETE;
	}
	
	/**
	 * Returns a newly loaded texture region for the given file.
	 *
	 * This helper methods is used to set texture settings (such as scaling, and
	 * whether or not the texture should repeat) after loading.
	 *
	 * @param manager 	Reference to global asset manager.
	 * @param file		The texture (region) file
	 * @param repeat	Whether the texture should be repeated
	 *
	 * @return a newly loaded texture region for the given file.
	 */
	protected TextureRegion createTexture(AssetManager manager, String file, boolean repeat) {
		if (manager.isLoaded(file)) {
			TextureRegion region = new TextureRegion(manager.get(file, Texture.class));
			region.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
			if (repeat) {
				region.getTexture().setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
			}
			return region;
		}
		return null;
	}
	
	/**
	 * Returns a newly loaded filmstrip for the given file.
	 *
	 * This helper methods is used to set texture settings (such as scaling, and
	 * the number of animation frames) after loading.
	 *
	 * @param manager 	Reference to global asset manager.
	 * @param file		The texture (region) file
	 * @param rows 		The number of rows in the filmstrip
	 * @param cols 		The number of columns in the filmstrip
	 * @param size 		The number of frames in the filmstrip
	 *
	 * @return a newly loaded texture region for the given file.
	 */
	protected FilmStrip createFilmStrip(AssetManager manager, String file, int rows, int cols, int size) {
		if (manager.isLoaded(file)) {
			FilmStrip strip = new FilmStrip(manager.get(file, Texture.class),rows,cols,size);
			strip.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
			return strip;
		}
		return null;
	}
	
	/** 
	 * Unloads the assets for this game.
	 * 
	 * This method erases the static variables.  It also deletes the associated textures 
	 * from the asset manager. If no assets are loaded, this method does nothing.
	 * 
	 * @param manager Reference to global asset manager.
	 */
	public void unloadContent(AssetManager manager) {
    	for(String s : assets) {
    		if (manager.isLoaded(s)) {
    			manager.unload(s);
    		}
    	}
	}
	
	/** Exit code for quitting the game */
	public static final int EXIT_QUIT = 0;
	/** Exit code for advancing to next level */
	public static final int EXIT_NEXT = 1;
	/** Exit code for jumping back to previous level */
	public static final int EXIT_PREV = 2;
    /** How many frames after winning/losing do we continue? */
	public static final int EXIT_COUNT = 120;

	/** The amount of time for a physics engine step. */
	public static final float WORLD_STEP = 1/60.0f;
	/** Number of velocity iterations for the constrain solvers */
	public static final int WORLD_VELOC = 6;
	/** Number of position iterations for the constrain solvers */
	public static final int WORLD_POSIT = 2;
	
	/** Width of the game world in Box2d units */
	protected static final float DEFAULT_WIDTH  = 32.0f;
	/** Height of the game world in Box2d units */
	protected static final float DEFAULT_HEIGHT = 18.0f;
	/** The default value of gravity (going down) */
	protected static final float DEFAULT_GRAVITY = 0f;
	
	/** Reference to the game canvas */
	protected GameCanvas canvas;
	/** All the objects in the world. */
	protected PooledList<Obstacle> objects  = new PooledList<Obstacle>();
	/** Queue for adding objects */
	protected PooledList<Obstacle> addQueue = new PooledList<Obstacle>();
	/** Listener that will update the player mode when we are done */
	private ScreenListener listener;

	/** The Box2D world */
	protected World world;
	/** The boundary of the world */
	protected Rectangle bounds;
	/** The world scale */
	protected Vector2 scale;
	
	/** Whether or not this is an active controller */
	private boolean active;
	/** Whether we have completed this level */
	private boolean complete;
	/** What level we are currently on */
	private int currentLevel;
	/** Whether a player has lost in this level*/
	private boolean lost;
	/** Whether we have failed at this world (and need a reset) */
	private boolean failed;
	/** Whether or not debug mode is active */
	private boolean debug;
	/** Countdown active for winning or losing */
	private int countdown;
	/**
	 * Returns true if debug mode is active.
	 *
	 * If true, all objects will display their physics bodies.
	 *
	 * @return true if debug mode is active.
	 */
	public boolean isDebug( ) {
		return debug;
	}

	/**
	 * Sets whether debug mode is active.
	 *
	 * If true, all objects will display their physics bodies.
	 *
	 * @param value whether debug mode is active.
	 */
	public void setDebug(boolean value) {
		debug = value;
	}

	/**
	 * Returns true if the level is completed.
	 *
	 * If true, the level will advance after a countdown
	 *
	 * @return true if the level is completed.
	 */
	public boolean isComplete( ) {
		return complete;
	}

	/**
	 * Sets whether the level is completed.
	 *
	 * If true, the level will advance after a countdown
	 *
	 * @param value whether the level is completed.
	 */
	public void setComplete(boolean value) {
		if (value) {
			countdown = EXIT_COUNT;
		}
		complete = value;
	}

	/**
	 * Returns true if the level is failed.
	 *
	 * If true, the level will reset after a countdown
	 *
	 * @return true if the level is failed.
	 */
	public boolean isFailure( ) {
		return failed;
	}

	/**
	 * Sets whether the level is failed.
	 *
	 * If true, the level will reset after a countdown
	 *
	 * @param value whether the level is failed.
	 */
	public void setFailure(boolean value) {
		if (value) {
			countdown = EXIT_COUNT;
		}
		failed = value;
	}
	
	/**
	 * Returns true if this is the active screen
	 *
	 * @return true if this is the active screen
	 */
	public boolean isActive( ) {
		return active;
	}

	/**
	 * Returns the canvas associated with this controller
	 *
	 * The canvas is shared across all controllers
	 *
	 * @param the canvas associated with this controller
	 */
	public GameCanvas getCanvas() {
		return canvas;
	}
	
	/**
	 * Sets the canvas associated with this controller
	 *
	 * The canvas is shared across all controllers.  Setting this value will compute
	 * the drawing scale from the canvas size.
	 *
	 * @param value the canvas associated with this controller
	 */
	public void setCanvas(GameCanvas canvas) {
		this.canvas = canvas;
		this.scale.x = canvas.getWidth()/bounds.getWidth();
		this.scale.y = canvas.getHeight()/bounds.getHeight();
	}
	
	/**
	 * Creates a new game world with the default values.
	 *
	 * The game world is scaled so that the screen coordinates do not agree
	 * with the Box2d coordinates.  The bounds are in terms of the Box2d
	 * world, not the screen.
	 */
	protected WorldController() {
		this(new Rectangle(0,0,DEFAULT_WIDTH,DEFAULT_HEIGHT), 
			 new Vector2(0,DEFAULT_GRAVITY));
	}

	/**
	 * Creates a new game world
	 *
	 * The game world is scaled so that the screen coordinates do not agree
	 * with the Box2d coordinates.  The bounds are in terms of the Box2d
	 * world, not the screen.
	 *
	 * @param width  	The width in Box2d coordinates
	 * @param height	The height in Box2d coordinates
	 * @param gravity	The downward gravity
	 */
	protected WorldController(float width, float height, float gravity) {
		this(new Rectangle(0,0,width,height), new Vector2(0,gravity));
	}

	/**
	 * Creates a new game world
	 *
	 * The game world is scaled so that the screen coordinates do not agree
	 * with the Box2d coordinates.  The bounds are in terms of the Box2d
	 * world, not the screen.
	 *
	 * @param bounds	The game bounds in Box2d coordinates
	 * @param gravity	The gravitational force on this Box2d world
	 */
	protected WorldController(Rectangle bounds, Vector2 gravity) {
		assets = new Array<String>();
		world = new World(gravity,false);
		this.bounds = new Rectangle(bounds);
		this.scale = new Vector2(1,1);
		complete = false;
		failed = false;
		debug  = false;
		active = false;
		countdown = -1;
	}
	
	/**
	 * Dispose of all (non-static) resources allocated to this mode.
	 */
	public void dispose() {
		for(Obstacle obj : objects) {
			obj.deactivatePhysics(world);
		}
		objects.clear();
		addQueue.clear();
		world.dispose();
		objects = null;
		addQueue = null;
		bounds = null;
		scale  = null;
		world  = null;
		canvas = null;
	}

	/**
	 *
	 * Adds a physics object in to the insertion queue.
	 *
	 * Objects on the queue are added just before collision processing.  We do this to 
	 * control object creation.
	 *
	 * param obj The object to add
	 */
	public void addQueuedObject(Obstacle obj) {
		assert inBounds(obj) : "Object is not in bounds";
		addQueue.add(obj);
	}

	/**
	 * Immediately adds the object to the physics world
	 *
	 * param obj The object to add
	 */
	protected void addObject(Obstacle obj) {
		assert inBounds(obj) : "Object is not in bounds";
		objects.add(obj);
		obj.activatePhysics(world);
	}

	/**
	 * Returns true if the object is in bounds.
	 *
	 * This assertion is useful for debugging the physics.
	 *
	 * @param obj The object to check.
	 *
	 * @return true if the object is in bounds.
	 */
	public boolean inBounds(Obstacle obj) {
		boolean horiz = (bounds.x <= obj.getX() && obj.getX() <= bounds.x+bounds.width);
		boolean vert  = (bounds.y <= obj.getY() && obj.getY() <= bounds.y+bounds.height);
		return horiz && vert;
	}
	
	/**
	 * Resets the status of the game so that we can play again.
	 *
	 * This method disposes of the world and creates a new one.
	 */
	public abstract void reset();
	
	/**
	 * Returns whether to process the update loop
	 *
	 * At the start of the update loop, we check if it is time
	 * to switch to a new game mode.  If not, the update proceeds
	 * normally.
	 *
	 * @param delta Number of seconds since last animation frame
	 * 
	 * @return whether to process the update loop
	 */
	public boolean preUpdate(float dt) {
		InputController input = InputController.getInstance();
		input.readInput(bounds, scale);
		if (listener == null) {
			return true;
		}

		// Toggle debug
		if (input.didDebug()) {
			debug = !debug;
		}
		
		// Handle resets
		if (input.didReset()) {
			reset();
		}
		
		// Now it is time to maybe switch screens.
		if (input.didExit()) {
			listener.exitScreen(this, EXIT_QUIT);
			return false;
		} else if (input.didAdvance()) {
			listener.exitScreen(this, EXIT_NEXT);
			return false;
		} else if (input.didRetreat()) {
			listener.exitScreen(this, EXIT_PREV);
			return false;
		} else if (countdown > 0) {
			countdown--;
		} else if (countdown == 0) {
			if (failed) {
				reset();
			} else if (complete) {
				listener.exitScreen(this, EXIT_NEXT);
				return false;
			}
		}
		return true;
	}
	
	/**
	 * The core gameplay loop of this world.
	 *
	 * This method contains the specific update code for this mini-game. It does
	 * not handle collisions, as those are managed by the parent class WorldController.
	 * This method is called after input is read, but before collisions are resolved.
	 * The very last thing that it should do is apply forces to the appropriate objects.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	public abstract void update(float dt);
	
	/**
	 * Processes physics
	 *
	 * Once the update phase is over, but before we draw, we are ready to handle
	 * physics.  The primary method is the step() method in world.  This implementation
	 * works for all applications and should not need to be overwritten.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	public void postUpdate(float dt) {
		// Add any objects created by actions
		while (!addQueue.isEmpty()) {
			addObject(addQueue.poll());
		}
		
		// Turn the physics engine crank.
		world.step(WORLD_STEP,WORLD_VELOC,WORLD_POSIT);

		// Garbage collect the deleted objects.
		// Note how we use the linked list nodes to delete O(1) in place.
		// This is O(n) without copying.
		Iterator<PooledList<Obstacle>.Entry> iterator = objects.entryIterator();
		while (iterator.hasNext()) {
			PooledList<Obstacle>.Entry entry = iterator.next();
			Obstacle obj = entry.getValue();
			if (obj.isRemoved()) {
				obj.deactivatePhysics(world);
				entry.remove();
			} else {
				// Note that update is called last!
				obj.update(dt);
			}
		}
	}
	
	/**
	 * Draw the physics objects to the canvas
	 *
	 * For simple worlds, this method is enough by itself.  It will need
	 * to be overriden if the world needs fancy backgrounds or the like.
	 *
	 * The method draws all objects in the order that they were added.
	 *
	 * @param canvas The drawing context
	 */
	public void draw(float delta) {

		if (debug) {
			canvas.beginDebug();
			for(Obstacle obj : objects) {
				obj.drawDebug(canvas);
			}
			canvas.endDebug();
		}
            // Final message
		if (complete && !failed) {
			displayFont.setColor(Color.YELLOW);
			canvas.begin(); // DO NOT SCALE
			canvas.drawTextCentered("VICTORY!", displayFont, 0.0f);
			canvas.end();
		} else if (failed) {
			displayFont.setColor(Color.RED);
			canvas.begin(); // DO NOT SCALE
			canvas.drawTextCentered("FAILURE!", displayFont, 0.0f);
			canvas.end();
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
		// IGNORE FOR NOW
	}

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
			if (preUpdate(delta)) {
				update(delta); // This is the one that must be defined.
				postUpdate(delta);
			}
			draw(delta);
		}
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

}