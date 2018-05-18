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
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.PovDirection;
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
public abstract class WorldController implements Screen, InputProcessor {
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

	private static final String WALL_STL_FILE = "shared/corner-top-left-special.png";
	private static final String WALL_STR_FILE = "shared/corner-top-right-special.png";
	private static final String WALL_SBR_FILE = "shared/corner-bottom-right-special.png";
	private static final String WALL_SBL_FILE = "shared/corner-bottom-left-special.png";

	private static final String WALL_DTL_FILE = "shared/horizontal-diagonal-top-left.png";
	private static final String WALL_DTR_FILE = "shared/horizontal-diagonal-top-right.png";
	private static final String WALL_DBR_FILE = "shared/horizontal-diagonal-bottom-right.png";
	private static final String WALL_DBL_FILE = "shared/horizontal-diagonal-bottom-left.png";

	private static final String WALL_LIGHT_FILE = "shared/horizontal-wall-lantern.png";


	/** File to texture for level completion */
	private static final String VICTORY_FILE = "shared/victory.png";

	/** File to texture for the win door */
	private static final String GOAL_FILE = "shared/stairs-down.png";
	/** File to texture for the mop cart */
	private static final String MOP_CART_FILE = "shared/mop-cart.png";
	private static final String EMPTY_MOP_CART_FILE = "shared/mop-cart-empty.png";

	/** File to texture for power-ups */
	private static final String SPECIAL_HEALTH_FILE = "shared/chips.png";
	private static final String SPECIAL_DURABILITY_FILE = "shared/duct-tape.png";
	/** File to texture for special tiles */
	private static final String BEAKER_FILE = "shared/beaker-table.png";
	private static final String COMPUTER_FILE = "shared/computer.png";
	private static final String PLANT_FILE = "shared/plant.png";
	/** Retro font for displaying messages */
	private static final String FONT_FILE = "shared/Title.ttf";
	private static final String FONT_BODY_FILE = "shared/Francois.ttf";
	private static final int FONT_SIZE = 32;
	/** The texture files for characters/attacks */
	private static final String JANITOR_IDLE_FILE  = "floor/janitor/janitor-idle.png";
	private static final String JANITOR_WALKR_FILE  = "floor/janitor/janitor-walk-R.png";
	private static final String JANITOR_WALKU_FILE  = "floor/janitor/janitor-walk-U.png";
	private static final String JANITOR_WALKD_FILE  = "floor/janitor/janitor-walk-D.png";
	private static final String JANITOR_MOPR_FILE  = "floor/janitor/janitor-attack-mop-R.png";
	private static final String JANITOR_MOPL_FILE  = "floor/janitor/janitor-attack-mop-L.png";
	private static final String JANITOR_MOPU_FILE  = "floor/janitor/janitor-attack-mop-U.png";
	private static final String JANITOR_MOPD_FILE  = "floor/janitor/janitor-attack-mop-D.png";
    private static final String JANITOR_LIDR_FILE  = "floor/janitor/janitor-attack-lid-R.png";
	private static final String JANITOR_LIDL_FILE  = "floor/janitor/janitor-attack-lid-L.png";
    private static final String JANITOR_LIDU_FILE  = "floor/janitor/janitor-attack-lid-U.png";
    private static final String JANITOR_LIDD_FILE  = "floor/janitor/janitor-attack-lid-D.png";
	private static final String JANITOR_SPRAYR_FILE  = "floor/janitor/janitor-attack-spray-R.png";
	private static final String JANITOR_SPRAYL_FILE  = "floor/janitor/janitor-attack-spray-L.png";
	private static final String JANITOR_SPRAYU_FILE  = "floor/janitor/janitor-attack-spray-U.png";
	private static final String JANITOR_SPRAYD_FILE  = "floor/janitor/janitor-attack-spray-D.png";
    private static final String JANITOR_VACUUMR_FILE  = "floor/janitor/janitor-attack-vacuum-R.png";
    private static final String JANITOR_VACUUML_FILE  = "floor/janitor/janitor-attack-vacuum-L.png";
    private static final String JANITOR_VACUUMU_FILE  = "floor/janitor/janitor-attack-vacuum-U.png";
    private static final String JANITOR_VACUUMD_FILE  = "floor/janitor/janitor-attack-vacuum-D.png";
	private static final String JANITOR_DEATH_FILE  = "floor/janitor/janitor-death.png";
	/** The texture files for mad scientist */
	private static final String MAD_ATTACKR_FILE  = "floor/enemy_long/mad-attack-right.png";
	private static final String MAD_ATTACKL_FILE  = "floor/enemy_long/mad-attack-left.png";
	private static final String MAD_ATTACKU_FILE  = "floor/enemy_long/mad-attack-up.png";
	private static final String MAD_ATTACKD_FILE  = "floor/enemy_long/mad-attack-down.png";
	private static final String MAD_WALKR_FILE  = "floor/mad-walk-side-v2.png";
	private static final String MAD_WALKU_FILE  = "floor/mad-walk-back-v2.png";
	private static final String MAD_WALKD_FILE  = "floor/mad-walk-front-v2.png";
	private static final String MAD_IDLE_FILE  = "floor/mad-idle.png";
	private static final String MAD_DEATH_FILE  = "floor/enemy_long/mad-death.png";
	private static final String MAD_STUN_FILE  = "floor/mad-stunned.png";
	/** The texture files for robot */
	private static final String ROBOT_ATTACKL_FILE  = "floor/enemy_long/robot-attack-left.png";
	private static final String ROBOT_ATTACKR_FILE  = "floor/enemy_long/robot-attack-right.png";
	private static final String ROBOT_ATTACKU_FILE  = "floor/enemy_long/robot-attack-up.png";
	private static final String ROBOT_ATTACKD_FILE  = "floor/enemy_long/robot-attack-down.png";
	private static final String ROBOT_WALKR_FILE  = "floor/robot-walk-side-recolor.png";
	private static final String ROBOT_WALKU_FILE  = "floor/robot-walk-back-recolor.png";
	private static final String ROBOT_WALKD_FILE  = "floor/robot-walk-front-recolor.png";
	private static final String ROBOT_IDLE_FILE  = "floor/robot-idle-v2.png";
	private static final String ROBOT_DEATH_FILE  = "floor/R-death.png";
	private static final String ROBOT_STUN_FILE  = "floor/enemy_long/robot-stunned.png";
	/** The texture files for slime */
	private static final String SLIME_ATTACKL_FILE  = "floor/slime-attack-L.png";
	private static final String SLIME_ATTACKR_FILE  = "floor/slime-attack-R.png";
	private static final String SLIME_ATTACKU_FILE  = "floor/slime-attack-U.png";
	private static final String SLIME_ATTACKD_FILE  = "floor/slime-attack-D.png";
	private static final String SLIME_WALKR_FILE  = "floor/slime-walk-R.png";
	private static final String SLIME_WALKU_FILE  = "floor/slime-walk-U.png";
	private static final String SLIME_WALKD_FILE  = "floor/slime-walk-D.png";
	private static final String SLIME_IDLE_FILE  = "floor/slime-walk-D.png";
	private static final String SLIME_DEATH_FILE  = "floor/slime-dead.png";
	private static final String SLIME_STUN_FILE  = "floor/slime-stunned.png";
	/** The texture files for Turretslime */
	private static final String TURRET_ATTACKL_FILE  = "floor/turret-slime-attack-L.png";
	private static final String TURRET_ATTACKR_FILE  = "floor/turret-slime-attack-R.png";
	private static final String TURRET_ATTACKU_FILE  = "floor/turret-slime-attack-U.png";
	private static final String TURRET_ATTACKD_FILE  = "floor/turret-slime-attack-D.png";
	private static final String TURRET_IDLE_FILE  = "floor/turret-slime-idle.png";
	private static final String TURRET_DEATH_FILE  = "floor/turret-slime-dead.png";
	private static final String TURRET_STUN_FILE  = "floor/turret-slime-stunned.png";
	/** The texture files for lizardman */
	private static final String LIZARD_ATTACKL_FILE  = "floor/enemy_long/lizard-attack-left.png";
	private static final String LIZARD_ATTACKR_FILE  = "floor/enemy_long/lizard-attack-right.png";
	private static final String LIZARD_ATTACKU_FILE  = "floor/enemy_long/lizard-attack-up.png";
	private static final String LIZARD_ATTACKD_FILE  = "floor/enemy_long/lizard-attack-down.png";
	private static final String LIZARD_WALKR_FILE  = "floor/L-walk-side.png";
	private static final String LIZARD_WALKU_FILE  = "floor/L-walk-back.png";
	private static final String LIZARD_WALKD_FILE  = "floor/L-walk-front.png";
	private static final String LIZARD_IDLE_FILE  = "floor/L-idle.png";
	private static final String LIZARD_DEATH_FILE  = "floor/L-death.png";
	private static final String LIZARD_STUN_FILE  = "floor/L-stunned.png";

	private static final String SCIENTIST_FILE  = "floor/scientist.png";
	private static final String SLIME_FILE  = "floor/slime.png";
	private static final String LIZARD_FILE  = "floor/lizard.png";
	private static final String ROBOT_FILE = "floor/robot.png";
	private static final String BULLET_FILE  = "floor/lid.png";
	private static final String SLIMEBALL_FILE = "floor/slimeball.png";
	private static final String SLIMEBALL_ANI_FILE = "floor/enemy_long/slimeball-ani.png";
	private static final String TURRET_SLIMEBALL_FILE = "floor/enemy_long/slimeball-turret-ani.png";
	private static final String VACUUM_SUCK_ANI_FILE = "floor/vacuum-suck-animation.png";

	/** The texture files for the Tutorial Keys */
	private static final String Q_KEY_FILE  = "floor/tutorial/keys-q.png";
	private static final String E_KEY_FILE  = "floor/tutorial/keys-e.png";
	private static final String WASD_KEY_FILE  = "floor/tutorial/keys-movement.png";
	private static final String ARROW_KEY_FILE  = "floor/tutorial/keys-attack.png";
	/** The texture files for the Mop Cart UI icons */
	private static final String MOP_FILE  = "floor/old_ui/ui-mop.png";
	private static final String SPRAY_FILE  = "floor/old_ui/ui-spray.png";
	private static final String VACUUM_FILE  = "floor/old_ui/ui-vacuum.png";
	private static final String LID_FILE  = "floor/old_ui/ui-lid.png";
	private static final String LID_ANI_FILE  = "floor/lid-ani.png";
	private static final String NONE_FILE  = "floor/ui/ui-none.png";
	/** The texture files for the UI icons */
	private static final String HEALTH_BAR_FILE  = "floor/ui/ui-health.png";
	private static final String MOP_BAR_FILE  = "floor/ui/ui-mop.png";
	private static final String SPRAY_BAR_FILE  = "floor/ui/ui-spray.png";
	private static final String VACUUM_BAR_FILE  = "floor/ui/ui-vacuum.png";
	private static final String LID_BAR_FILE  = "floor/ui/ui-lid.png";
	private static final String NO_LID_BAR_FILE  = "floor/ui/ui-lid-empty.png";
	private static final String NONE_BAR_FILE  = "floor/ui/ui-none.png";
	//	private static final String HEALTH_BAR_SMALL_FILE  = "floor/ui-health.png";
	private static final String MOP_BAR_SMALL_FILE  = "floor/ui/ui-mop-small.png";
	private static final String SPRAY_BAR_SMALL_FILE  = "floor/ui/ui-spray-small.png";
	private static final String VACUUM_BAR_SMALL_FILE  = "floor/ui/ui-vacuum-small.png";
	private static final String LID_BAR_SMALL_FILE  = "floor/ui/ui-lid-small.png";
	private static final String NO_LID_BAR_SMALL_FILE  = "floor/ui/ui-lid-empty-small.png";
	private static final String NONE_BAR_SMALL_FILE  = "floor/ui/ui-none-small.png";

	private static final String ENEMY_HEALTH_3_FILE  = "floor/enemy-health-3.png";
    private static final String ENEMY_HEALTH_5_FILE  = "floor/enemy-health-5.png";
	private static final String EMOTICON_EXCLAMATION_FILE  = "floor/emoticon-exclamation.png";
	private static final String EMOTICON_QUESTION_FILE  = "floor/emoticon-question.png";

	private static final String MOPCART_INDEX_FILE  = "floor/mopcart-index.png";
	private static final String BACKGROUND_FILE = "shared/loading.png";
	private static final String MOPCART_BACKGROUND_FILE = "shared/mop-cart-ui.png";
	private static final String TILE_FILE = "shared/basic-tile-32.png";
	private static final String BROKEN1_TILE_FILE = "shared/broken-tile-1-32.png";
	private static final String BROKEN2_TILE_FILE = "shared/broken-tile-2-32.png";
	private static final String BROKEN3_TILE_FILE = "shared/broken-tile-3-32.png";
	private static final String BROKEN4_TILE_FILE = "shared/broken-tile-4-32.png";
	private static final String GRATE_TILE_FILE = "shared/grate-tile-32.png";
	private static final String STAIRS_TILE_FILE = "shared/stairs-down.png";
	private static final String UNDER_TILE_FILE = "shared/undertile-32.png";
	private static final String HAZARD_TILE_FILE = "shared/hazard-tile.png";

	//private static final String PLAY_BTN_FILE = "shared/play.png";
	private static final String CONTINUE_BTN_FILE = "shared/continue-button.png";
	private static final String MAIN_BTN_FILE = "shared/menu-button.png";
	private static final String JOE_NEXT_FILE = "shared/janitor-level-complete-3x.png";
	private static final String JOE_MAIN_FILE = "shared/janitor-sleeping-3x.png";

	//private static final String PAUSE_BTN_FILE = "floor/janitor-sleeping.png";

	/** Standard window size (for scaling) */
	private static int STANDARD_WIDTH  = 800;
	/** Standard window height (for scaling) */
	private static int STANDARD_HEIGHT = 700;
	private static float BUTTON_SCALE  = 0.75f;
	/** Ration of the bar height to the screen */
	private static float BAR_HEIGHT_RATIO = 0.25f;

	private static float JOE_HEIGHT_RATIO = 0.5f;

	private static float OFFSET_X_RATIO = 0.15f;

	/** Texture assets for characters/attacks */
	protected TextureRegion wallRightTexture;
	protected TextureRegion wallLeftTexture;
	protected TextureRegion wallMidTexture;
	protected TextureRegion wallTLTexture;
	protected TextureRegion wallTRTexture;
	protected TextureRegion wallBLTexture;
	protected TextureRegion wallBRTexture;
	protected TextureRegion wallSTLTexture;
	protected TextureRegion wallSTRTexture;
	protected TextureRegion wallSBLTexture;
	protected TextureRegion wallSBRTexture;
	protected TextureRegion wallDTLTexture;
	protected TextureRegion wallDTRTexture;
	protected TextureRegion wallDBLTexture;
	protected TextureRegion wallDBRTexture;
    protected TextureRegion wallSLTexture;
    protected TextureRegion wallSRTexture;
	protected TextureRegion wallELTexture;
	protected TextureRegion wallERTexture;
	protected TextureRegion wallLightTexture;

	protected TextureRegion victoryTexture;

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
	protected TextureRegion avatarDeathTexture;
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
	protected TextureRegion scientistStunTexture;
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
	protected TextureRegion robotStunTexture;
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
	/** Texture assets for turret slime */
	protected TextureRegion turretTexture;
	protected TextureRegion turretAttackLTexture;
	protected TextureRegion turretAttackRTexture;
	protected TextureRegion turretAttackUTexture;
	protected TextureRegion turretAttackDTexture;

	protected TextureRegion turretIdleTexture;
	protected TextureRegion turretDeathTexture;
	protected TextureRegion turretStunTexture;
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
	protected TextureRegion lizardStunTexture;

	protected TextureRegion bulletTexture;
	protected TextureRegion slimeballTexture;
	protected TextureRegion slimeballAniTexture;
	protected TextureRegion slimeballTurretAniTexture;
	protected TextureRegion lidAniTexture;
	protected TextureRegion vacSuckAniTexture;

	/** Texture assets for Tutorial Keys */
	protected Texture qKeyTexture;
	protected Texture arrowKeyTexture;
	protected Texture wasdKeyTexture;
	protected Texture eKeyTexture;

	/** Texture assets for UI Icons */
	protected Texture mopTexture;
	protected Texture sprayTexture;
	protected Texture vacuumTexture;
	protected Texture lidTexture;
	protected Texture noneTexture;
	protected Texture mopcartIndexTexture;

	protected TextureRegion healthBarTexture;
	protected TextureRegion healthBarTexture2;

    protected TextureRegion enemyHealth3Texture;
    protected TextureRegion enemyHealth5Texture;
	protected Texture emoticonExclamationTexture;
	protected Texture emoticonQuestionTexture;

    protected TextureRegion mopBarTexture;
	protected TextureRegion sprayBarTexture;
	protected TextureRegion vacuumBarTexture;
	protected TextureRegion lidBarTexture;
	protected TextureRegion noLidBarTexture;
	protected TextureRegion noneBarTexture;

	protected TextureRegion mopBarSmallTexture;
	protected TextureRegion sprayBarSmallTexture;
	protected TextureRegion vacuumBarSmallTexture;
	protected TextureRegion lidBarSmallTexture;
	protected TextureRegion noLidBarSmallTexture;
	protected TextureRegion noneBarSmallTexture;

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
	protected Texture mopcartBackgroundTexture;
	/** The texture for the exit condition */
	protected TextureRegion goalTile;
	/** The texture for the mop cart*/
	protected TextureRegion mopCartTile;
	protected TextureRegion emptyMopCartTile;
	/** The texture for the mop cart*/
	protected TextureRegion specialHealthTile;
	protected TextureRegion specialDurabilityTile;
	/** The font for giving messages to the player */
	protected BitmapFont displayFont;

	protected TextureRegion beakerTile;
	protected TextureRegion computerTile;
	protected TextureRegion plantTile;

	/** Camera coordinates to draw Victory or Failure */
	protected float cameraX;
	protected float cameraY;

	protected int pressState;
	//private Animation <TextureRegion> joeNext;
	private Animation <TextureRegion> joeNext;
	private Animation <TextureRegion> joeMain;
	//private TextureRegion current;

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
		System.out.println("hereeee: "+worldAssetState);
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

		manager.load(WALL_STL_FILE, Texture.class);
		assets.add(WALL_STL_FILE);
		manager.load(WALL_STR_FILE, Texture.class);
		assets.add(WALL_STR_FILE);
		manager.load(WALL_SBL_FILE, Texture.class);
		assets.add(WALL_SBL_FILE);
		manager.load(WALL_SBR_FILE, Texture.class);
		assets.add(WALL_SBR_FILE);

		manager.load(WALL_LIGHT_FILE, Texture.class);
		assets.add(WALL_LIGHT_FILE);

		manager.load(VICTORY_FILE, Texture.class);
		assets.add(VICTORY_FILE);

		manager.load(WALL_DTL_FILE, Texture.class);
		assets.add(WALL_DTL_FILE);
		manager.load(WALL_DTR_FILE, Texture.class);
		assets.add(WALL_DTR_FILE);
		manager.load(WALL_DBL_FILE, Texture.class);
		assets.add(WALL_DBL_FILE);
		manager.load(WALL_DBR_FILE, Texture.class);
		assets.add(WALL_DBR_FILE);

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
		manager.load(JANITOR_DEATH_FILE, Texture.class);
		assets.add(JANITOR_DEATH_FILE);

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
		manager.load(MAD_STUN_FILE, Texture.class);
		assets.add(MAD_STUN_FILE);
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
		manager.load(ROBOT_STUN_FILE, Texture.class);
		assets.add(ROBOT_STUN_FILE);
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

		manager.load(TURRET_ATTACKL_FILE, Texture.class);
		assets.add(TURRET_ATTACKL_FILE);
		manager.load(TURRET_ATTACKR_FILE, Texture.class);
		assets.add(TURRET_ATTACKR_FILE);
		manager.load(TURRET_ATTACKU_FILE, Texture.class);
		assets.add(TURRET_ATTACKU_FILE);
		manager.load(TURRET_ATTACKD_FILE, Texture.class);
		assets.add(TURRET_ATTACKD_FILE);
		manager.load(TURRET_IDLE_FILE, Texture.class);
		assets.add(TURRET_IDLE_FILE);
		manager.load(TURRET_DEATH_FILE, Texture.class);
		assets.add(TURRET_DEATH_FILE);
		manager.load(TURRET_STUN_FILE, Texture.class);
		assets.add(TURRET_STUN_FILE);

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
		manager.load(LIZARD_STUN_FILE, Texture.class);
		assets.add(LIZARD_STUN_FILE);


		manager.load(BULLET_FILE, Texture.class);
		assets.add(BULLET_FILE);
		manager.load(SLIMEBALL_FILE, Texture.class);
		assets.add(SLIMEBALL_FILE);
		manager.load(SLIMEBALL_ANI_FILE, Texture.class);
		assets.add(SLIMEBALL_ANI_FILE);
		manager.load(TURRET_SLIMEBALL_FILE, Texture.class);
		assets.add(TURRET_SLIMEBALL_FILE);
		manager.load(VACUUM_SUCK_ANI_FILE, Texture.class);
		assets.add(VACUUM_SUCK_ANI_FILE);

		//Tutorial
		manager.load(Q_KEY_FILE, Texture.class);
		assets.add(Q_KEY_FILE);
		manager.load(E_KEY_FILE, Texture.class);
		assets.add(E_KEY_FILE);
		manager.load(WASD_KEY_FILE, Texture.class);
		assets.add(WASD_KEY_FILE);
		manager.load(ARROW_KEY_FILE, Texture.class);
		assets.add(ARROW_KEY_FILE);

		//UI Icons
		manager.load(MOP_FILE, Texture.class);
		assets.add(MOP_FILE);
		manager.load(SPRAY_FILE, Texture.class);
		assets.add(SPRAY_FILE);
		manager.load(VACUUM_FILE, Texture.class);
		assets.add(VACUUM_FILE);
		manager.load(LID_FILE, Texture.class);
		assets.add(LID_FILE);
		manager.load(LID_ANI_FILE, Texture.class);
		assets.add(LID_ANI_FILE);
        manager.load(MOPCART_INDEX_FILE, Texture.class);
        assets.add(MOPCART_INDEX_FILE);

		manager.load(HEALTH_BAR_FILE, Texture.class);
		assets.add(HEALTH_BAR_FILE);
//		manager.load(HEALTH_BAR_FILE2, Texture.class);
//		assets.add(HEALTH_BAR_FILE2);

        manager.load(ENEMY_HEALTH_3_FILE, Texture.class);
        assets.add(ENEMY_HEALTH_3_FILE);
        manager.load(ENEMY_HEALTH_5_FILE, Texture.class);
        assets.add(ENEMY_HEALTH_5_FILE);
		manager.load(EMOTICON_EXCLAMATION_FILE, Texture.class);
		assets.add(EMOTICON_EXCLAMATION_FILE);
		manager.load(EMOTICON_QUESTION_FILE, Texture.class);
		assets.add(EMOTICON_QUESTION_FILE);

		manager.load(MOP_BAR_FILE, Texture.class);
		assets.add(MOP_BAR_FILE);
		manager.load(SPRAY_BAR_FILE, Texture.class);
		assets.add(SPRAY_BAR_FILE);
		manager.load(VACUUM_BAR_FILE, Texture.class);
		assets.add(VACUUM_BAR_FILE);
		manager.load(LID_BAR_FILE, Texture.class);
		assets.add(LID_BAR_FILE);
		manager.load(NO_LID_BAR_FILE, Texture.class);
		assets.add(NO_LID_BAR_FILE);
		manager.load(NONE_BAR_FILE, Texture.class);
		assets.add(NONE_BAR_FILE);

		manager.load(MOP_BAR_SMALL_FILE, Texture.class);
		assets.add(MOP_BAR_SMALL_FILE);
		manager.load(SPRAY_BAR_SMALL_FILE, Texture.class);
		assets.add(SPRAY_BAR_SMALL_FILE);
		manager.load(VACUUM_BAR_SMALL_FILE, Texture.class);
		assets.add(VACUUM_BAR_SMALL_FILE);
		manager.load(LID_BAR_SMALL_FILE, Texture.class);
		assets.add(LID_BAR_SMALL_FILE);
		manager.load(NO_LID_BAR_SMALL_FILE, Texture.class);
		assets.add(NO_LID_BAR_SMALL_FILE);
		manager.load(NONE_BAR_SMALL_FILE, Texture.class);
		assets.add(NONE_BAR_SMALL_FILE);

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

		manager.load(BEAKER_FILE, Texture.class);
		assets.add(BEAKER_FILE);
		manager.load(COMPUTER_FILE, Texture.class);
		assets.add(COMPUTER_FILE);
		manager.load(PLANT_FILE, Texture.class);
		assets.add(PLANT_FILE);

		manager.load(GOAL_FILE,Texture.class);
		assets.add(GOAL_FILE);
		manager.load(MOP_CART_FILE,Texture.class);
		assets.add(MOP_CART_FILE);
		manager.load(EMPTY_MOP_CART_FILE,Texture.class);
		assets.add(EMPTY_MOP_CART_FILE);

		//Load Special Power Up Tiles
		manager.load(SPECIAL_HEALTH_FILE,Texture.class);
		assets.add(SPECIAL_HEALTH_FILE);
		manager.load(SPECIAL_DURABILITY_FILE,Texture.class);
		assets.add(SPECIAL_DURABILITY_FILE);

		// Load the font
		FreetypeFontLoader.FreeTypeFontLoaderParameter size2Params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
		size2Params.fontFileName = FONT_FILE;
		size2Params.fontFileName = FONT_BODY_FILE;
		size2Params.fontParameters.size = FONT_SIZE;
		manager.load(FONT_FILE, BitmapFont.class, size2Params);
		assets.add(FONT_FILE);
		manager.load(FONT_BODY_FILE, BitmapFont.class, size2Params);
		assets.add(FONT_BODY_FILE);
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

		wallSTRTexture = createTexture(manager,WALL_STR_FILE,false);
		wallSTLTexture = createTexture(manager,WALL_STL_FILE,false);
		wallSBRTexture = createTexture(manager,WALL_SBR_FILE,false);
		wallSBLTexture = createTexture(manager,WALL_SBL_FILE,false);

		wallDTRTexture = createTexture(manager,WALL_DTR_FILE,false);
		wallDTLTexture = createTexture(manager,WALL_DTL_FILE,false);
		wallDBRTexture = createTexture(manager,WALL_DBR_FILE,false);
		wallDBLTexture = createTexture(manager,WALL_DBL_FILE,false);

		wallLightTexture = createTexture(manager,WALL_LIGHT_FILE,false);

		victoryTexture = createTexture(manager,VICTORY_FILE,false);

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
		avatarDeathTexture = createTexture(manager,JANITOR_DEATH_FILE,false);

		scientistAttackRTexture = createTexture(manager,MAD_ATTACKR_FILE,false);
		scientistAttackLTexture = createTexture(manager,MAD_ATTACKL_FILE,false);
		scientistAttackUTexture = createTexture(manager,MAD_ATTACKU_FILE,false);
		scientistAttackDTexture = createTexture(manager,MAD_ATTACKD_FILE,false);
		scientistWalkRTexture = createTexture(manager,MAD_WALKR_FILE,false);
		scientistWalkUTexture = createTexture(manager,MAD_WALKU_FILE,false);
		scientistWalkDTexture = createTexture(manager,MAD_WALKD_FILE,false);
		scientistIdleTexture = createTexture(manager,MAD_IDLE_FILE,false);
		scientistDeathTexture = createTexture(manager,MAD_DEATH_FILE,false);
		scientistStunTexture = createTexture(manager,MAD_STUN_FILE,false);
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
		robotStunTexture = createTexture(manager,ROBOT_STUN_FILE,false);
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

		turretAttackLTexture = createTexture(manager,TURRET_ATTACKL_FILE,false);
		turretAttackRTexture = createTexture(manager,TURRET_ATTACKR_FILE,false);
		turretAttackUTexture = createTexture(manager,TURRET_ATTACKU_FILE,false);
		turretAttackDTexture = createTexture(manager,TURRET_ATTACKD_FILE,false);
		turretIdleTexture = createTexture(manager,TURRET_IDLE_FILE,false);
		turretDeathTexture = createTexture(manager,TURRET_DEATH_FILE,false);
		turretStunTexture = createTexture(manager,TURRET_STUN_FILE,false);
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
		lizardStunTexture = createTexture(manager,LIZARD_STUN_FILE,false);
		bulletTexture = createTexture(manager,BULLET_FILE,false);
		slimeballTexture = createTexture(manager,SLIMEBALL_FILE,false);
		slimeballAniTexture = createTexture(manager,SLIMEBALL_ANI_FILE,false);
		slimeballTurretAniTexture = createTexture(manager,TURRET_SLIMEBALL_FILE,false);
		lidAniTexture = createTexture(manager,LID_ANI_FILE,false);
		vacSuckAniTexture = createTexture(manager,VACUUM_SUCK_ANI_FILE,false);
		backgroundTexture = new Texture(BACKGROUND_FILE);
		mopcartBackgroundTexture = new Texture(MOPCART_BACKGROUND_FILE);

		beakerTile = createTexture(manager,BEAKER_FILE,false);
		computerTile = createTexture(manager,COMPUTER_FILE,false);
		plantTile = createTexture(manager,PLANT_FILE,false);

		//Tutorial Icons
		qKeyTexture  = new Texture(Q_KEY_FILE);
		eKeyTexture = new Texture(E_KEY_FILE);
		wasdKeyTexture = new Texture(WASD_KEY_FILE);
		arrowKeyTexture = new Texture(ARROW_KEY_FILE);

		//UI Icons
		mopTexture = new Texture(MOP_FILE);
		sprayTexture = new Texture(SPRAY_FILE);
		vacuumTexture = new Texture(VACUUM_FILE);
		lidTexture = new Texture(LID_FILE);
		noneTexture = new Texture(NONE_FILE);

		healthBarTexture = createTexture(manager,HEALTH_BAR_FILE,false);
//		healthBarTexture2 = createTexture(manager,HEALTH_BAR_FILE2,false);

        enemyHealth3Texture = createTexture(manager,ENEMY_HEALTH_3_FILE,false);
        enemyHealth5Texture = createTexture(manager,ENEMY_HEALTH_5_FILE,false);
		emoticonExclamationTexture = new Texture(EMOTICON_EXCLAMATION_FILE);
		emoticonQuestionTexture = new Texture(EMOTICON_QUESTION_FILE);

		mopBarTexture = createTexture(manager,MOP_BAR_FILE,false);
		sprayBarTexture = createTexture(manager,SPRAY_BAR_FILE,false);
		vacuumBarTexture = createTexture(manager,VACUUM_BAR_FILE,false);
		lidBarTexture = createTexture(manager,LID_BAR_FILE,false);
		noLidBarTexture = createTexture(manager,NO_LID_BAR_FILE,false);
		noneBarTexture = createTexture(manager,NONE_BAR_FILE,false);

		mopBarSmallTexture = createTexture(manager,MOP_BAR_SMALL_FILE,false);
		sprayBarSmallTexture = createTexture(manager,SPRAY_BAR_SMALL_FILE,false);
		vacuumBarSmallTexture = createTexture(manager,VACUUM_BAR_SMALL_FILE,false);
		lidBarSmallTexture = createTexture(manager,LID_BAR_SMALL_FILE,false);
		noLidBarSmallTexture = createTexture(manager,NO_LID_BAR_SMALL_FILE,false);
		noneBarSmallTexture = createTexture(manager,NONE_BAR_SMALL_FILE,false);

		mopcartIndexTexture = new Texture(MOPCART_INDEX_FILE);
        tileTexture = new Texture(TILE_FILE);
		broken1TileTexture = new Texture(BROKEN1_TILE_FILE);
		broken2tileTexture = new Texture(BROKEN2_TILE_FILE);
		broken3tileTexture = new Texture(BROKEN3_TILE_FILE);
		broken4tileTexture = new Texture(BROKEN4_TILE_FILE);
		grateTileTexture = new Texture(GRATE_TILE_FILE);
		underTileTexture = new Texture(STAIRS_TILE_FILE); //why is this backwards lol
		stairsTileTexture = new Texture(UNDER_TILE_FILE);
		hazardTileTexture = new Texture(HAZARD_TILE_FILE);

		goalTile  = createTexture(manager,GOAL_FILE,true);
		mopCartTile = createTexture(manager,MOP_CART_FILE,false);
		emptyMopCartTile = createTexture(manager,EMPTY_MOP_CART_FILE,false);

		specialHealthTile = createTexture(manager,SPECIAL_HEALTH_FILE, false);
		specialDurabilityTile = createTexture(manager,SPECIAL_DURABILITY_FILE, false);

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
	public static final int EXIT_QUIT = 4;
	/** Exit code for advancing to next level */
	public static final int EXIT_NEXT = 1;
	/** Exit code for jumping back to previous level */
	public static final int EXIT_PREV = 2;
	public static final int EXIT_MENU = 100;

	/** How many frames after winning/losing do we continue? */
	public static final int COMPLETE_EXIT_COUNT = 100;
	public static final int FAILURE_EXIT_COUNT = 60;

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
	/** Scaling factor for when the student changes the resolution. */
	private float scale2;
	private int heightY;
	
	/** Whether or not this is an active controller */
	private boolean active;
	/** Whether we have completed this level */
	private boolean complete;
	private boolean backToMenu;
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
	//private PauseMenu pauseMode;
	private boolean paused;
	private Texture background;
	private Texture playButton;
	private Texture mainButton;
	/** The y-coordinate of the center of the progress bar */
	private int centerY;
	/** The x-coordinate of the center of the progress bar */
	private int centerX;

	private int centerXMain;
	private int centerXNext;
	private int centerYJoe;
	private int choose;

	public enum StateJoe {MAIN, NEXT}

	public ScoreMode.StateJoe currentState;
	public ScoreMode.StateJoe previousState;

	private TextureRegion current;
	private float stateTimer;

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
			countdown = COMPLETE_EXIT_COUNT;
		}
		complete = value;
	}

	/**
	 * Set Camera Value for Failure
	 * */
	public void setCameraX(float value) {
		cameraX = value;
	}
	public void setCameraY(float value) {
		cameraY = value;
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
			countdown = FAILURE_EXIT_COUNT;
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
	public boolean touchDown(int screenX, int screenY, int pointer, int button){
		if (playButton == null || pressState == 2 || pressState == 4) {
			return true;
		}

		// Flip to match graphics coordinates
		screenY = heightY-screenY;

		// TODO: Fix scaling
		// Play button is a circle.
		float radius = BUTTON_SCALE*scale2*playButton.getWidth()/2.0f;
		float dist = (screenX-centerXNext)*(screenX-centerXNext)+(screenY-centerY)*(screenY-centerY);
		if (dist < radius*radius) {
			pressState = 1;
		}

		radius = BUTTON_SCALE*scale2*mainButton.getWidth()/2.0f;
		dist = (screenX-centerXMain)*(screenX-centerXMain)+(screenY-centerY)*(screenY-centerY);
		if (dist < radius*radius) {
			pressState = 3;
		}
		return false;
	}
	public boolean touchUp(int screenX, int screenY, int pointer, int button){
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
	public boolean keyDown(int keycode) {
		return true;
	}
	public boolean keyTyped(char character) {
		return true;
	}
	public boolean keyUp(int keycode) { return true; } //fix this?
	public boolean mouseMoved(int screenX, int screenY) {
		return true;
	}
	public boolean scrolled(int amount) {
		return true;
	}
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return true;
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
		//pauseMode = new PauseMenu(canvas);
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
		paused=false;
		choose=1;
		playButton = new Texture(CONTINUE_BTN_FILE);
		playButton.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

		mainButton = new Texture(MAIN_BTN_FILE);
		mainButton.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

		background = new Texture("shared/opacity-block.png");

		pressState=0;

		assets = new Array<String>();
		world = new World(gravity,false);
		this.bounds = new Rectangle(bounds);
		this.scale = new Vector2(1,1);
		complete = false;
		failed = false;
		debug  = false;
		active = false;
		countdown = -1;
		/**
		Texture joeMainT = new Texture(JOE_MAIN_FILE);
		Texture joeNextT = new Texture(JOE_NEXT_FILE);
		TextureRegion joeMainTexture = new TextureRegion(joeMainT, joeMainT.getWidth(), joeMainT.getHeight());
		TextureRegion joeNextTexture = new TextureRegion(joeNextT, joeNextT.getWidth(), joeNextT.getHeight());
		 **/
		// Let ANY connected controller start the game.
        /*for(Controller controller : Controllers.getControllers()) {
            controller.addListener(this);
        }*/
        /**
		Array<TextureRegion> frames = new Array<TextureRegion>();
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
		 **/
	}

	public void pauseDispose(){
		background.dispose();
		background = null;
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
	 * Dispose of all (non-static) resources allocated to this mode.
	 */
	public void dispose() {
		for(Obstacle obj : objects) {
			obj.deactivatePhysics(world);
		}
		pauseDispose();
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
		Gdx.input.setInputProcessor(this);

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
		} /**else if (backToMenu){
			backToMenu=false;
			listener.exitScreen(this, EXIT_MENU);
			return false;
		} **/else if (input.didAdvance()) {
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

	public void reset2(){
		active=false;
		pressState=0;
		//Gdx.input.setInputProcessor(this);
	}

	/**
	public TextureRegion getFrameJoe (float dt){
		currentState = getStateJanitor();
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

	public ScoreMode.StateJoe getStateJanitor(){
		float screenX = Gdx.input.getX();
		float screenY = Gdx.input.getY();

		screenY = heightY-screenY;

		float radius = BUTTON_SCALE*scale2*mainButton.getWidth()/2.0f;
		float dist = (screenX-centerXMain)*(screenX-centerXMain)+(screenY-centerY)*(screenY-centerY);
		if (dist < radius*radius) {
			return ScoreMode.StateJoe.MAIN;
		}

		return ScoreMode.StateJoe.NEXT;
	}**/

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
		if (paused){
			canvas.begin();
//			System.out.println(cameraX);
//			System.out.println(cameraY);
			canvas.draw(background, cameraX - 512, cameraY - 288);
			Color mainTint = choose==0 ? Color.YELLOW : Color.WHITE;
			Color playTint = choose==1 ? Color.YELLOW : Color.WHITE;
			//cameraX and cameraY are exactly the middle points of the current screen
			canvas.draw(mainButton, mainTint, mainButton.getWidth()/2, mainButton.getHeight()/2,
					cameraX - 156, cameraY - 104, 0, BUTTON_SCALE*scale2, BUTTON_SCALE*scale2);
			canvas.draw(playButton, playTint, playButton.getWidth()/2, playButton.getHeight()/2,
					cameraX + 156, cameraY - 104, 0, BUTTON_SCALE*scale2, BUTTON_SCALE*scale2);

			//canvas.draw(joeMain, Color.WHITE, joeMain.getWidth()/2, joeMain.getHeight()/2,
			//		centerXMain, centerYJoe, 0, 1, 1);
			/**
			canvas.draw(current, Color.WHITE, current.getRegionWidth()/2, current.getRegionHeight()/2,
					centerX, centerYJoe, 0, 2, 2);**/
			canvas.end();
		}
            // Final message
		else if (complete && !failed) {
			displayFont.setColor(Color.YELLOW);
			canvas.begin(); // DO NOT SCALE
			canvas.draw(victoryTexture, cameraX - 250, cameraY - 220);
			canvas.end();
		} else if (failed) {
			displayFont.setColor(Color.RED);
			canvas.begin(); // DO NOT SCALE
			System.out.println(cameraX);
			System.out.println(cameraY);
			canvas.drawText("FAILURE!", displayFont, cameraX - 100, cameraY + 20);
//			canvas.drawTextCentered("FAILURE!", displayFont, 0.0f);
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
		float sx = ((float) width) / STANDARD_WIDTH;
		float sy = ((float) height) / STANDARD_HEIGHT;
		scale2 = (sx < sy ? sx : sy);
		heightY = height;

		centerY = (int) (BAR_HEIGHT_RATIO * height);
		centerX = width / 2;
		centerXNext = (int) (width / 2 + width * OFFSET_X_RATIO);

		centerXMain = (int) (width / 2 - width * OFFSET_X_RATIO);
		centerYJoe = (int) (JOE_HEIGHT_RATIO * height);
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
		//current = getFrameJoe(delta);
		if (active) {
			boolean b = preUpdate(delta);
			if (!paused) {
				pressState = 0;
				choose=1;
				//backToMenu=false;
				if (InputController.getInstance().getDidPause()) paused=true;
				if (b) {
					update(delta); // This is the one that must be defined.
					postUpdate(delta);
				}
			}
			else {
				if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) && choose==1){
					choose=0;
				}
				else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) && choose==0){
					choose=1;
				}
				//what to do here?
				if ((Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && choose==1) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
					//pauseDispose();
					paused=false;
				} else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && choose==0) {
					//pauseDispose();
					backToMenu=true;
					paused=false;
					choose=1;
					reset2();
					listener.exitScreen(this, EXIT_MENU);
				}
			}
			if (!backToMenu) {
				draw(delta);
			}
			else {backToMenu=false;}
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