/*
 * FloorController.java
 *
 */
package edu.cornell.gdiac.physics.floor;

import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;
import edu.cornell.gdiac.physics.floor.weapon.*;
import edu.cornell.gdiac.physics.lights.ConeSource;
import edu.cornell.gdiac.physics.lights.LightSource;
import edu.cornell.gdiac.physics.lights.PointSource;
import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.physics.*;
import edu.cornell.gdiac.physics.obstacle.*;
import edu.cornell.gdiac.physics.floor.character.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Gameplay specific controller for the platformer game.
 *
 * You will notice that asset loading is not done with static methods this time.
 * Instance asset loading makes it easier to process our game modes in a loop, which
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public class FloorController extends WorldController implements ContactListener {
    private static int currentLevel;

    private String LEVEL;
    /** The sound file for background music */
    private static final String BACKGROUND_TRACK_FILE = "floor/background-track.mp3";
    /** The sound file for a jump */
    private static final String JUMP_FILE = "floor/jump.mp3";
    /** The sound file for a bullet fire */
    private static final String PEW_FILE = "floor/pew.mp3";
    /** The sound file for a bullet collision */
    private static final String POP_FILE = "floor/plop.mp3";
    /** The sound file for a reload */
    private static final String RELOAD_FILE = "floor/reload.mp3";
    /** The sound file for out of weapon */
    private static final String NO_WEAPON_FILE = "floor/no_weapon.mp3";
    /** The sound file for getting hurt */
    private static final String OUCH_FILE = "floor/ouch.mp3";
    /** The sound file for vacuum attack */
    private static final String VACUUM_FILE = "floor/vacuum.mp3";

    private static final int TILE_SIZE = 32;

//    private static final int BOARD_WIDTH=1024;
//    private static final int BOARD_HEIGHT=576;
//    private static final int NUM_OF_TILES_X = BOARD_WIDTH/TILE_SIZE;
//    private static final int NUM_OF_TILES_Y = BOARD_HEIGHT/TILE_SIZE;

    private static int horizontalMargin = (1024/2);
    private static int verticalMargin = (576/2);
    private static int LEFT_SCROLL_CLAMP; //0 + horizontalMargin
    private static int RIGHT_SCROLL_CLAMP; //level - horizontalMargin
    private static int BOTTOM_SCROLL_CLAMP; //0 + verticalMargin
    private static int TOP_SCROLL_CLAMP; //level - verticalMargin
    private float cameraX; //where the camera is located (for drawing and scrolling)
    private float cameraY; //where the camera is located (for drawing and scrolling)

    private static final float WALL_THICKNESS_SCALE = 0.33f;
    private static final float WALL_HEIGHT_SCALE = 0.9f;

    private static final float OBJ_OFFSET_X = 1f;
    private static final float OBJ_OFFSET_Y = 1f;

    public static int CONTROL_NO_ACTION = 0;
    public static int CONTROL_MOVE_LEFT = 1;
    public static int CONTROL_MOVE_RIGHT = 2;
    public static int CONTROL_MOVE_UP = 4;
    public static int CONTROL_MOVE_DOWN = 8;
    public static int CONTROL_FIRE = 16;

    /** Weapon Name -> Texture Dictionary*/
    /*TODO maybe move info to weapons class */
    private String[] list_of_weapons = new String[4];
    private HashMap<String, WeaponModel> wep_to_model = new HashMap<String, WeaponModel>();
    private HashMap<String, Boolean> wep_in_use = new HashMap<String, Boolean>();
    private HashMap<String, Texture> wep_to_texture = new HashMap<String, Texture>();
    private HashMap<String, Texture> wep_to_small_texture = new HashMap<String, Texture>();
    private HashMap<String, TextureRegion[]> wep_to_bartexture = new HashMap<String, TextureRegion[]>();

    private TextureRegion[][] allHeartTextures = new TextureRegion[2][];
    private TextureRegion[][] allEnemyHeartTextures = new TextureRegion[2][];

    private String[] mopcart_menu = new String[2];
    private int mopcart_index = 0;
    private int[] mopcart_index_xlocation = new int[2];

    /** Track asset loading from all instances and subclasses */
    private AssetState platformAssetState = AssetState.EMPTY;

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
        if (platformAssetState != AssetState.EMPTY) {
            return;
        }

        platformAssetState = AssetState.LOADING;

        manager.load(BACKGROUND_TRACK_FILE, Sound.class);
        assets.add(BACKGROUND_TRACK_FILE);
        manager.load(JUMP_FILE, Sound.class);
        assets.add(JUMP_FILE);
        manager.load(PEW_FILE, Sound.class);
        assets.add(PEW_FILE);
        manager.load(POP_FILE, Sound.class);
        assets.add(POP_FILE);
        manager.load(RELOAD_FILE, Sound.class);
        assets.add(RELOAD_FILE);
        manager.load(NO_WEAPON_FILE, Sound.class);
        assets.add(NO_WEAPON_FILE);
        manager.load(OUCH_FILE, Sound.class);
        assets.add(OUCH_FILE);
        manager.load(VACUUM_FILE, Sound.class);
        assets.add(VACUUM_FILE);

        super.preLoadContent(manager);
    }

    /**
     * Load the assets for this controller.
     *
     * To make the game modes more for-loop friendly, we opted for nonstatic loaders
     * this time.  However, we still want the assets themselves to be static.  So
     * we have an AssetState that determines the current loading state.  If the
     * assets are already loaded, this method will do nothing.
     *
     * @param manager Reference to global asset manager.
     */
    public void loadContent(AssetManager manager) {
        if (platformAssetState != AssetState.LOADING) {
            return;
        }

        SoundController sounds = SoundController.getInstance();
        sounds.allocate(manager, BACKGROUND_TRACK_FILE);
        sounds.allocate(manager, JUMP_FILE);
        sounds.allocate(manager, PEW_FILE);
        sounds.allocate(manager, POP_FILE);
        sounds.allocate(manager, RELOAD_FILE);
        sounds.allocate(manager, NO_WEAPON_FILE);
        sounds.allocate(manager, OUCH_FILE);
        sounds.allocate(manager, VACUUM_FILE);

        super.loadContent(manager);
        platformAssetState = AssetState.COMPLETE;
    }

    // Physics constants for initialization
    /** The new heavier gravity for this world (so it is not so floaty) */
    private static final float  DEFAULT_GRAVITY = 0.0f;
    /** The density for most physics objects */
    private static final float  BASIC_DENSITY = 0.0f;
    /** The density for a bullet */
    private static final float  HEAVY_DENSITY = 10.0f;
    /** Friction of most platforms */
    private static final float  BASIC_FRICTION = 0.4f;
    /** The restitution for all physics objects */
    private static final float  BASIC_RESTITUTION = 0.1f;
    /** Offset for bullet when firing */
    private static final float  BULLET_OFFSET = 1.5f;
    /** The speed of the bullet after firing */
    private static final float  BULLET_SPEED = 20.0f;
    /** The speed of the slimeball after firing */
    private static final float  SLIMEBALL_SPEED = 8.5f;
    /** The volume for sound effects */
    private static final float EFFECT_VOLUME = 0.8f;
    /** Attack total time frames*timerperframe for mop */
    private static final float ATTACK_DURATION_MOP = 0.2f;
    /** Attack total time frames*timerperframe for lid */
    private static final float ATTACK_DURATION_LID = 0.2f;
    /** Attack total time frames*timerperframe for spray */
    private static final float ATTACK_DURATION_SPRAY= 0.4f;
    /** Attack total time frames*timerperframe for vacuum */
    private static final float ATTACK_DURATION_VACUUM= 0.4f;
    private static final float DEATH_ANIMATION_TIME = 2f;
    /** The timer for animations*/
    private float stateTimer;
    private float stateTimerM;
    private float stateTimerR;
    private float stateTimerL;
    private float stateTimerS;
    private float stateTimerT;
    private boolean scientistMovedLeft;
    private boolean robotMovedLeft;
    private boolean slimeMovedLeft;
    private boolean lizardMovedLeft;
    /** The cooldown for attack animations*/
    private float attackTimer;
    private float deathTimer;
    private float joeDeathTimer;
    /** The "range" for the lid */
    private static final float LID_RANGE = 0.5f;
    /** The timer for lid range*/
    private float lidTimer;
    /** The boolean for whether lid is on ground*/
    private boolean lidGround;


    // TODO reform weapon class and move to mop class
    /** Disables setVelocity until knockback is finished */
    private static final int KNOCKBACK_TIMER = 15;

    LevelEditorParser level;

    ArrayList<Vector2> scientistPos;
    ArrayList<Vector2> robotPos;
    ArrayList<Vector2> lizardPos;
    ArrayList<Vector2> slimePos;
    ArrayList<Vector2> slimeTurretPos;
    ArrayList<String> slimeTurretDirections;
    ArrayList<Integer> slimeTurretDelays;

    ArrayList<ArrayList<Vector2>> scientistPatrol;
    ArrayList<ArrayList<Vector2>> slimePatrol;
    ArrayList<ArrayList<Vector2>> slimeTurretPatrol;
    ArrayList<ArrayList<Vector2>> robotPatrol;
    ArrayList<ArrayList<Vector2>> lizardPatrol;

    ArrayList<Vector2> wallRightPos;
    ArrayList<Vector2> wallLeftPos;
    ArrayList<Vector2> wallMidPos;
    ArrayList<Vector2> wallTLPos;
    ArrayList<Vector2> wallTRPos;
    ArrayList<Vector2> wallBLPos;
    ArrayList<Vector2> wallBRPos;
    ArrayList<Vector2> wallSLPos;
    ArrayList<Vector2> wallSRPos;
    ArrayList<Vector2> wallELPos;
    ArrayList<Vector2> wallERPos;
    ArrayList<Vector2> wallSTLPos;
    ArrayList<Vector2> wallSTRPos;
    ArrayList<Vector2> wallSBLPos;
    ArrayList<Vector2> wallSBRPos;
    ArrayList<Vector2> wallDTLPos;
    ArrayList<Vector2> wallDTRPos;
    ArrayList<Vector2> wallDBLPos;
    ArrayList<Vector2> wallDBRPos;
    ArrayList<Vector2> wallLightPos;
    ArrayList<Vector2> hazardPos;
    ArrayList<Vector2> specialHealthPos;
    ArrayList<Vector2> specialDurabilityPos;
    ArrayList<Vector2> plantPos;
    ArrayList<Vector2> computerPos;
    ArrayList<Vector2> beakerPos;

    ArrayList<Vector2> mopCartPos;
    ArrayList<Boolean> mopCartVisitedBefore;

    int [][] tiles;

    /** The camera defining the RayHandler view; scale is in physics coordinates */
    protected OrthographicCamera raycamera;
    /** The rayhandler for storing lights, and drawing them (SIGH) */
    protected RayHandler rayhandler;
    /** All of the active lights that we loaded from the JSON file */
    private Array<LightSource> lights = new Array<LightSource>();

    private boolean lightIsActive;

    // Physics objects for the game
    /** Reference to the character avatar */
    private JoeModel avatar;
    /** All the character Animations */
    private Animation <TextureRegion> joeStand;
    private Animation <TextureRegion> joeRunR;
    private Animation <TextureRegion> joeRunU;
    private Animation <TextureRegion> joeRunD;
    private Animation <TextureRegion> joeMopR;
    private Animation <TextureRegion> joeMopL;
    private Animation <TextureRegion> joeMopU;
    private Animation <TextureRegion> joeMopD;
    private Animation <TextureRegion> joeLidR;
    private Animation <TextureRegion> joeLidL;
    private Animation <TextureRegion> joeLidU;
    private Animation <TextureRegion> joeLidD;
    private Animation <TextureRegion> joeSprayR;
    private Animation <TextureRegion> joeSprayL;
    private Animation <TextureRegion> joeSprayU;
    private Animation <TextureRegion> joeSprayD;
    private Animation <TextureRegion> joeVacuumR;
    private Animation <TextureRegion> joeVacuumL;
    private Animation <TextureRegion> joeVacuumU;
    private Animation <TextureRegion> joeVacuumD;
    private Animation <TextureRegion> joeDeath;
    private Animation <TextureRegion> madStand;
    private Animation <TextureRegion> madRunR;
    private Animation <TextureRegion> madRunU;
    private Animation <TextureRegion> madRunD;
    private Animation <TextureRegion> madAttackR;
    private Animation <TextureRegion> madAttackL;
    private Animation <TextureRegion> madAttackU;
    private Animation <TextureRegion> madAttackD;
    private Animation <TextureRegion> madDeath;
    private Animation <TextureRegion> madStun;
    private Animation <TextureRegion> robotStand;
    private Animation <TextureRegion> robotRunR;
    private Animation <TextureRegion> robotRunU;
    private Animation <TextureRegion> robotRunD;
    private Animation <TextureRegion> robotAttackL;
    private Animation <TextureRegion> robotAttackR;
    private Animation <TextureRegion> robotAttackU;
    private Animation <TextureRegion> robotAttackD;
    private Animation <TextureRegion> robotDeath;
    private Animation <TextureRegion> robotStun;
    private Animation <TextureRegion> slimeStand;
    private Animation <TextureRegion> slimeRunR;
    private Animation <TextureRegion> slimeRunU;
    private Animation <TextureRegion> slimeRunD;
    private Animation <TextureRegion> slimeAttackL;
    private Animation <TextureRegion> slimeAttackR;
    private Animation <TextureRegion> slimeAttackU;
    private Animation <TextureRegion> slimeAttackD;
    private Animation <TextureRegion> slimeDeath;
    private Animation <TextureRegion> slimeStun;
    private Animation <TextureRegion> turretStand;
    private Animation <TextureRegion> turretRunR;
    private Animation <TextureRegion> turretRunU;
    private Animation <TextureRegion> turretRunD;
    private Animation <TextureRegion> turretAttackL;
    private Animation <TextureRegion> turretAttackR;
    private Animation <TextureRegion> turretAttackU;
    private Animation <TextureRegion> turretAttackD;
    private Animation <TextureRegion> turretDeath;
    private Animation <TextureRegion> turretStun;
    private Animation <TextureRegion> lizardStand;
    private Animation <TextureRegion> lizardRunR;
    private Animation <TextureRegion> lizardRunU;
    private Animation <TextureRegion> lizardRunD;
    private Animation <TextureRegion> lizardAttackL;
    private Animation <TextureRegion> lizardAttackR;
    private Animation <TextureRegion> lizardAttackU;
    private Animation <TextureRegion> lizardAttackD;
    private Animation <TextureRegion> lizardDeath;
    private Animation <TextureRegion> lizardStun;


    /** Reference to the goalDoor (for collision detection) */
    private BoxObstacle goalDoor;
    /** Reference to the monsters */
    private EnemyModel[] enemies;
    /** List of all the input AI controllers */
    protected AIController[] controls;
    /** Game sectioned off into tiles for AI purposes */
    private Board board;
    /** ticks for update loop */
    private long ticks;

    /** Reference to the mopCart (for collision detection) */
    //List of all mopcarts in level, should check if each one was used or not
    private ArrayList<BoxObstacle> mopCartList = new ArrayList<BoxObstacle>();

    /** Reference to the special health power up (for collision detection) */
    private BoxObstacle specialHealth;
    private BoxObstacle specialDurability;

    /** Mark set to handle more sophisticated collision callbacks */
    protected ObjectSet<Fixture> sensorFixtures;

    /** For mop knockback force calculations*/
    private Vector2 knockbackForce = new Vector2();

    /** Saved lid velocity before colliding with slimeball */
    private Vector2 lidVel = new Vector2();

    /** frame in which Joe just got hit by slime balls */
    private long gotHit;


    private final class CollideBits {
        public static final short BIT_ENEMY = 1;
        public static final short BIT_SLIMEBALL = 2;
    }

    /**
     * Creates and initialize a new instance of the platformer game
     *
     * The game has default gravity and other settings
     */
    public FloorController(int input_level) {
        super(DEFAULT_WIDTH,DEFAULT_HEIGHT,DEFAULT_GRAVITY);
        currentState = StateJoe.STANDING;
        previousState = StateJoe.STANDING;
        deathTimer = DEATH_ANIMATION_TIME;
        joeDeathTimer = DEATH_ANIMATION_TIME;
        stateTimer = 0.0f;
        stateTimerM = 0.0f;
        stateTimerR = 0.0f;
        stateTimerS = 0.0f;
        stateTimerT = 0.0f;
        stateTimerL = 0.0f;
        lidTimer = LID_RANGE;
        lidGround = false;
        gotHit = -1;
        setDebug(false);
        setComplete(false);
        setFailure(false);
        world.setContactListener(this);
        sensorFixtures = new ObjectSet<Fixture>();

        currentLevel = input_level;
        LEVEL = "level" + input_level + ".tmx";
        //LEVEL = "level1.tmx";

        level = new LevelEditorParser(LEVEL);
        scientistPos = level.getScientistPos();
        slimePos = level.getSlimePos();
        slimeTurretPos = level.getSlimeTurretPos();
        slimeTurretDirections = level.getSlimeTurretDirections();
        slimeTurretDelays = level.getSlimeTurretDelays();
        robotPos = level.getRobotPos();
        lizardPos = level.getLizardPos();

        //Make empty arrays if you don't want the enemies to appear
//        scientistPos=new ArrayList<Vector2>();
//        slimePos = new ArrayList<Vector2>();
//        robotPos = new ArrayList<Vector2>();
//        lizardPos = new ArrayList<Vector2>();

        scientistPatrol = level.getScientistPatrol();
        slimePatrol = level.getSlimePatrol();
        slimeTurretPatrol = level.getSlimeTurretPatrol(); //this actually shouldn't do anything
        robotPatrol = level.getRobotPatrol();
        lizardPatrol = level.getLizardPatrol();

        wallLeftPos = level.getWallLeftPos();
        wallRightPos = level.getWallRightPos();
        wallMidPos = level.getWallMidPos();
        wallTLPos = level.getWallTLPos();
        wallTRPos = level.getWallTRPos();
        wallBLPos = level.getWallBLPos();
        wallBRPos = level.getWallBRPos();
        wallSLPos = level.getWallSLPos();
        wallSRPos = level.getWallSRPos();
        wallELPos = level.getWallELPos();
        wallERPos = level.getWallERPos();
        wallSTLPos = level.getWallSTLPos();
        wallSTRPos = level.getWallSTRPos();
        wallSBLPos = level.getWallSBLPos();
        wallSBRPos = level.getWallSBRPos();
        wallDTLPos = level.getWallDTLPos();
        wallDTRPos = level.getWallDTRPos();
        wallDBLPos = level.getWallDBLPos();
        wallDBRPos = level.getWallDBRPos();
        wallLightPos = level.getWallLightPos();

        computerPos = level.getComputerPos();
        plantPos = level.getPlantPos();
        beakerPos = level.getBeakerPos();

        hazardPos = level.getHazardPos();
        specialHealthPos = level.getSpecialHealthPos();
        specialDurabilityPos = level.getSpecialDurabilityPos();
        mopCartPos = level.getMopCartPos();
        mopCartVisitedBefore = new ArrayList(level.getMopCartVisitedBefore());
        //make new array because if level restarts we need original level values
        //can't make alias / same reference

        tiles = level.getTiles();
    }

    /**
     * Resets the status of the game so that we can play again.
     *
     * This method disposes of the world and creates a new one.
     */
    public void reset() {
        for(LightSource light : lights) {
            light.remove();
        }
        lights.clear();
        if (rayhandler != null) {
            rayhandler.dispose();
            rayhandler = null;
        }

        SoundController.getInstance().play(BACKGROUND_TRACK_FILE, BACKGROUND_TRACK_FILE, true, 0.4f);
        //avatar has never visited mopcart before
        mopCartVisitedBefore = new ArrayList(level.getMopCartVisitedBefore());

        Vector2 gravity = new Vector2(world.getGravity() );

        for(Obstacle obj : objects) {
            obj.deactivatePhysics(world);
        }
        objects.clear();
        addQueue.clear();
        world.dispose();

        world = new World(gravity,false);
        world.setContactListener(this);
        setComplete(false);
        setFailure(false);
        currentState = StateJoe.STANDING;
        previousState = StateJoe.STANDING;
        deathTimer = DEATH_ANIMATION_TIME;
        joeDeathTimer = DEATH_ANIMATION_TIME;
        stateTimer = 0.0f;
        stateTimerM = 0.0f;
        stateTimerR = 0.0f;
        stateTimerS = 0.0f;
        stateTimerL = 0.0f;
        stateTimerT = 0.0f;
        attackTimer = 0.0f;
        lidTimer = LID_RANGE;
        lidGround = false;

        enemies=new EnemyModel[scientistPos.size() + robotPos.size() + slimePos.size() + lizardPos.size() + slimeTurretPos.size()];
        controls = new AIController[scientistPos.size() + robotPos.size() + slimePos.size() + lizardPos.size() + slimeTurretPos.size()];

        board = new Board(level.getBoardWidth(), level.getBoardHeight(), TILE_SIZE);

        LEFT_SCROLL_CLAMP = 0 + horizontalMargin;
        RIGHT_SCROLL_CLAMP = (level.getBoardWidth() * 32) -  horizontalMargin;
        BOTTOM_SCROLL_CLAMP = 0 + verticalMargin;
        TOP_SCROLL_CLAMP = (level.getBoardHeight() * 32) -  verticalMargin;
        populateLevel();
    }

    /**
     * Lays out the game geography.
     */
    private void populateLevel() {

        initLighting();
        createLights();
        // Add level goal
        float dwidth  = goalTile.getRegionWidth()/scale.x;
        float dheight = goalTile.getRegionHeight()/scale.y;
        goalDoor = new BoxObstacle(level.getGoalDoorX()/32+OBJ_OFFSET_X,level.getGoalDoorY()/32+OBJ_OFFSET_Y,dwidth,dheight);
        goalDoor.setBodyType(BodyDef.BodyType.StaticBody);
        goalDoor.setDensity(0.0f);
        goalDoor.setFriction(0.0f);
        goalDoor.setRestitution(0.0f);
        goalDoor.setSensor(true);
        goalDoor.setDrawScale(scale);
        goalDoor.setTexture(goalTile);
        goalDoor.setName("goal");
        addObject(goalDoor);

        // Add mopcart
        float mopwidth  = mopTile.getRegionWidth()/scale.x;
        float mopheight= mopTile.getRegionHeight()/scale.y;
        for (int ii=0; ii<mopCartPos.size(); ii++) {
            Vector2 vec = mopCartPos.get(ii);
            BoxObstacle mopCart = new BoxObstacle(vec.x/32+OBJ_OFFSET_X, vec.y/32+OBJ_OFFSET_X,mopwidth,mopheight);
            mopCart.setBodyType(BodyDef.BodyType.StaticBody);
            mopCart.setDensity(ii);
            //save the index of the mop cart for checking specific mop carts
            mopCart.setFriction(0.0f);
            mopCart.setRestitution(0.0f);
            mopCart.setSensor(true);
            mopCart.setDrawScale(scale);
            mopCart.setTexture(mopTile);
            mopCart.setName("mopCart");
            addObject(mopCart);
            mopCartList.add(mopCart);
        }

        // Add special elements (power ups)
        System.out.println(specialHealthPos);
        System.out.println(specialDurabilityPos);

        float specialWidth  = specialHealthTile.getRegionWidth()/scale.x;
        float specialHeight = specialHealthTile.getRegionHeight()/scale.y;
        for (int ii=0; ii<specialHealthPos.size(); ii++) {
            Vector2 vec = specialHealthPos.get(ii);
            specialHealth = new BoxObstacle(vec.x/32+OBJ_OFFSET_X, vec.y/32+OBJ_OFFSET_Y, specialWidth, specialHeight);
            specialHealth.setBodyType(BodyDef.BodyType.StaticBody);
            specialHealth.setDensity(0.0f);
            specialHealth.setFriction(0.0f);
            specialHealth.setRestitution(0.0f);
            specialHealth.setSensor(true);
            specialHealth.setDrawScale(scale);
            specialHealth.setTexture(specialHealthTile);
            specialHealth.setName("specialHealth");
            addObject(specialHealth);
        }
        for (int ii=0; ii<specialDurabilityPos.size(); ii++) {
            Vector2 vec = specialDurabilityPos.get(ii);
            specialDurability = new BoxObstacle(vec.x/32+OBJ_OFFSET_X, vec.y/32+OBJ_OFFSET_Y, specialWidth, specialHeight);
            specialDurability.setBodyType(BodyDef.BodyType.StaticBody);
            specialDurability.setDensity(0.0f);
            specialDurability.setFriction(0.0f);
            specialDurability.setRestitution(0.0f);
            specialDurability.setSensor(true);
            specialDurability.setDrawScale(scale);
            specialDurability.setTexture(specialDurabilityTile);
            specialDurability.setName("specialDurability");
            addObject(specialDurability);
        }

        Texture[] tileTextures = {null, tileTexture, broken1TileTexture,
                broken2tileTexture, broken3tileTexture, grateTileTexture,
                broken4tileTexture,underTileTexture,stairsTileTexture};

        board.setTileTextures(tileTextures);
        board.setHazardTileTexture(hazardTileTexture);
        setHazardTiles();
        addUIInfo();
        addWalls();

        for (int ii=0; ii<plantPos.size(); ii++) {
            Vector2 vec = plantPos.get(ii);
            BoxObstacle plant = new BoxObstacle(vec.x+0.5f, vec.y+0.5f,mopwidth/2,mopheight/2);
            board.setBlocked(board.screenToBoardX(vec.x), board.screenToBoardY(vec.y));
            plant.setBodyType(BodyDef.BodyType.StaticBody);
            plant.setDensity(ii);
            plant.setDrawScale(scale);
            plant.setTexture(plantTile);
            plant.setName("plant");
            addObject(plant);
        }

        for (int ii=0; ii<computerPos.size(); ii++) {
            Vector2 vec = computerPos.get(ii);
            board.setBlocked(board.screenToBoardX(vec.x), board.screenToBoardY(vec.y));
            board.setBlocked(board.screenToBoardX(vec.x+1), board.screenToBoardY(vec.y));
            board.setBlocked(board.screenToBoardX(vec.x), board.screenToBoardY(vec.y)+1);
            board.setBlocked(board.screenToBoardX(vec.x)+1, board.screenToBoardY(vec.y)+1);

            BoxObstacle computer = new BoxObstacle(vec.x+OBJ_OFFSET_X, vec.y+OBJ_OFFSET_X,mopwidth,mopheight);
            computer.setBodyType(BodyDef.BodyType.StaticBody);
            computer.setDensity(ii);
            computer.setDrawScale(scale);
            computer.setTexture(computerTile);
            computer.setName("computer");
            addObject(computer);
        }

        for (int ii=0; ii<beakerPos.size(); ii++) {
            Vector2 vec = beakerPos.get(ii);
            BoxObstacle beaker = new BoxObstacle(vec.x+0.5f, vec.y+0.5f,mopwidth/2,mopheight/2);
            board.setBlocked(board.screenToBoardX(vec.x), board.screenToBoardY(vec.y));
            beaker.setBodyType(BodyDef.BodyType.StaticBody);
            beaker.setDensity(ii);
            beaker.setDrawScale(scale);
            beaker.setTexture(beakerTile);
            beaker.setName("beaker");
            addObject(beaker);
        }
        addCharacters();
    }


    private void initLighting() {
        raycamera = new OrthographicCamera(canvas.getWidth(),canvas.getHeight());
        raycamera.position.set(canvas.getWidth()/2.0f, canvas.getHeight()/2.0f, 0);
        /*raycamera = new OrthographicCamera(bounds.width, bounds.height);
        raycamera.position.set(bounds.width/2.0f, bounds.height/2.0f, 0);*/
        raycamera.update();

        RayHandler.setGammaCorrection(true);
        RayHandler.useDiffuseLight(true);
        rayhandler = new RayHandler(world, Gdx.graphics.getWidth(), Gdx.graphics.getWidth());
        rayhandler.setCombinedMatrix(raycamera);

        float[] color = {0.75f, 0.75f, 0.75f, 0.75f};
        rayhandler.setAmbientLight(color[0], color[0], color[0], color[0]);
        int blur = 3;
        rayhandler.setBlur(blur > 0);
        rayhandler.setBlurNum(blur);
        lightIsActive = false;
    }

    private void createLights() {
        float[] color = {0.2f, 0.5f, 1.0f, 1.0f};
        float dist  = 3.0f  * 32;
        int rays = 256;

        for (Vector2 hpos : wallLightPos) {
            /*ConeSource point = new ConeSource(rayhandler, rays, Color.YELLOW, dist, (hpos.x + 0.5f) * 32, (hpos.y - 0.1f)*32, 90f, 90f);
            */
            ConeSource point = new ConeSource(rayhandler, rays, Color.YELLOW, dist, (hpos.x + 0.5f) * 32, (hpos.y + 1.2f)*32, 270f, 50f);
            point.setColor(Color.YELLOW);
            point.setSoft(false);
            lights.add(point);
        }

        dist = 1.0f  * 32;
        rays = 256;

        for (Vector2 hpos : computerPos) {
            PointSource point = new PointSource(rayhandler, rays, Color.WHITE, dist - 0.2f * 32, (hpos.x + 0.4f) * 32, (hpos.y + 0.4f)*32);
            point.setColor(color[0], color[1], color[2], color[3]);
            point.setSoft(false);
            lights.add(point);

            point = new PointSource(rayhandler, rays, Color.WHITE, dist, (hpos.x + 1.2f) * 32, (hpos.y + 1.2f)*32);
            point.setColor(color[0], color[1], color[2], color[3]);
            point.setSoft(false);
            lights.add(point);
        }
        for(LightSource light : lights) {
            light.setActive(false);
        }
    }

    private void addUIInfo() {
        /** Pixel Locations of Weapon Icons in Mop Cart*/
        //added on to avatar.getX()
        mopcart_index_xlocation[0] = 375;
        mopcart_index_xlocation[1] = 450;
        /** Add names to list of weapons */
        list_of_weapons[0] = "mop";
        list_of_weapons[1] = "spray";
        list_of_weapons[2] = "vacuum";
        list_of_weapons[3] = "lid";
        mopcart_menu[0] = "vacuum";
        mopcart_menu[1] = "lid";
        /** Load name -> texture dictionary */

        TextureRegion[][] mopTextures = mopBarTexture.split(164, 64);
        TextureRegion[][] sprayTextures = sprayBarTexture.split(114, 64);
        TextureRegion[][] vacuumTextures = vacuumBarTexture.split(164, 64);
        TextureRegion[][] lidTextures = lidBarTexture.split(164, 64);

        //for temporary use to add to allHeartTextures
        TextureRegion[] heartTextures = healthBarTexture.split(114, 64)[0];
        TextureRegion[] heartTextures2 = healthBarTexture2.split(124, 64)[0];
        allHeartTextures[0] = heartTextures;
        allHeartTextures[1] = heartTextures2;

        //for temporary use to add to allEnemyHeartTextures
        TextureRegion[] enemyBar1 = enemyHealth3Texture.split(64, 64)[0];
        TextureRegion[] enemyBar2 = enemyHealth5Texture.split(64, 64)[0];
        allEnemyHeartTextures[0] = enemyBar1;
        allEnemyHeartTextures[1] = enemyBar2;

        wep_to_bartexture.put("mop", mopTextures[0]);
        wep_to_bartexture.put("spray", sprayTextures[0]);
        wep_to_bartexture.put("vacuum", vacuumTextures[0]);
        wep_to_bartexture.put("lid", lidTextures[0]);

        /** Load name -> texture dictionary */
        wep_to_texture.put("mop", mopTexture);
        wep_to_texture.put("spray", sprayTexture);
        wep_to_texture.put("vacuum", vacuumTexture);
        wep_to_texture.put("lid", lidTexture);
        /** Load name -> model dictionary */
        wep_to_model.put("mop", new MopModel(10, 2, 15));
        wep_to_model.put("spray", new SprayModel(5, 4, 150));
        wep_to_model.put("vacuum", new VacuumModel(10, 10));
        wep_to_model.put("lid", new LidModel(10, 10));
        /** Load name -> in use dictionary */
        wep_in_use.put("mop", true);
        wep_in_use.put("spray", true);
        wep_in_use.put("vacuum", false);
        wep_in_use.put("lid", false);
    }

    private void addCharacters() {
        Array<TextureRegion> frames = new Array<TextureRegion>();
        for (int i=0; i <= 7; i++){
            frames.add (new TextureRegion(avatarWalkRTexture,i*64,0,64,64));
        }
        joeRunR = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 23; i++){
            frames.add (new TextureRegion(avatarIdleTexture,i*64,0,64,64));
        }
        joeStand = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 7; i++){
            frames.add (new TextureRegion(avatarWalkUTexture,i*64,0,64,64));
        }
        joeRunU = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 7; i++){
            frames.add (new TextureRegion(avatarWalkDTexture,i*64,0,64,64));
        }
        joeRunD = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(avatarMopRTexture,i*192,0,192,64));
        }
        joeMopR = new Animation<TextureRegion>(0.05f, frames);
        frames.clear();

        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(avatarMopLTexture,i*192,0,192,64));
        }
        joeMopL = new Animation<TextureRegion>(0.05f, frames);
        frames.clear();

        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(avatarMopUTexture,i*64,0,64,192));
        }
        joeMopU = new Animation<TextureRegion>(0.05f, frames);
        frames.clear();

        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(avatarMopDTexture,i*64,0,64,192));
        }
        joeMopD = new Animation<TextureRegion>(0.05f, frames);
        frames.clear();

        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(avatarLidRTexture,i*64,0,64,64));
        }
        joeLidR = new Animation<TextureRegion>(0.05f, frames);
        frames.clear();

        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(avatarLidLTexture,i*64,0,64,64));
        }
        joeLidL = new Animation<TextureRegion>(0.05f, frames);
        frames.clear();

        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(avatarLidUTexture,i*64,0,64,64));
        }
        joeLidU = new Animation<TextureRegion>(0.05f, frames);
        frames.clear();

        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(avatarLidDTexture,i*64,0,64,64));
        }
        joeLidD = new Animation<TextureRegion>(0.05f, frames);
        frames.clear();

        for (int i=0; i <= 7; i++){
            frames.add (new TextureRegion(avatarSprayRTexture,i*320,0,320,64));
        }
        joeSprayR = new Animation<TextureRegion>(0.05f, frames);
        frames.clear();

        for (int i=0; i <= 7; i++){
            frames.add (new TextureRegion(avatarSprayLTexture,i*320,0,320,64));
        }
        joeSprayL = new Animation<TextureRegion>(0.05f, frames);
        frames.clear();

        for (int i=0; i <= 7; i++){
            frames.add (new TextureRegion(avatarSprayUTexture,i*64,0,64,320));
        }
        joeSprayU = new Animation<TextureRegion>(0.05f, frames);
        frames.clear();

        for (int i=0; i <= 7; i++){
            frames.add (new TextureRegion(avatarSprayDTexture,i*64,0,64,320));
        }
        joeSprayD = new Animation<TextureRegion>(0.05f, frames);
        frames.clear();

        for (int i=0; i <= 0; i++){
            frames.add (new TextureRegion(avatarVacuumRTexture,i*64,0,64,64));
        }
        joeVacuumR = new Animation<TextureRegion>(0.4f, frames);
        frames.clear();

        for (int i=0; i <= 0; i++){
            frames.add (new TextureRegion(avatarVacuumLTexture,i*64,0,64,64));
        }
        joeVacuumL = new Animation<TextureRegion>(0.4f, frames);
        frames.clear();

        for (int i=0; i <= 0; i++){
            frames.add (new TextureRegion(avatarVacuumUTexture,i*64,0,64,64));
        }
        joeVacuumU = new Animation<TextureRegion>(0.4f, frames);
        frames.clear();

        for (int i=0; i <= 0; i++){
            frames.add (new TextureRegion(avatarVacuumDTexture,i*64,0,64,64));
        }
        joeVacuumD = new Animation<TextureRegion>(0.4f, frames);
        frames.clear();

        for (int i=0; i <= 24; i++){
            frames.add (new TextureRegion(avatarDeathTexture,i*64,0,64,64));
        }
        joeDeath = new Animation<TextureRegion>(0.075f, frames);
        frames.clear();

        for (int i=0; i <= 7; i++){
            frames.add (new TextureRegion(scientistWalkRTexture,i*64,0,64,64));
        }
        madRunR = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 7; i++){
            frames.add (new TextureRegion(scientistWalkUTexture,i*64,0,64,64));
        }
        madRunU = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 7; i++){
            frames.add (new TextureRegion(scientistWalkDTexture,i*64,0,64,64));
        }
        madRunD = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

//        LONG SCIENTIST ANIMATIONS
        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(scientistAttackRTexture,i*96 - 16,0,96,64));
            // No clue why I subtract 16 from x but it looks like it works sort of

        }
        madAttackR = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(scientistAttackLTexture,i*96 - 16,0,96,64));
            // No clue why I subtract 16 from x but it looks like it works sort of
        }
        madAttackL = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(scientistAttackUTexture,i*64,0,64,64));
        }
        madAttackU = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(scientistAttackDTexture,i*64,-16,64,96));
            // No clue why I subtract 16 from y but it looks like it works sort of
        }
        madAttackD = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 7; i++){
            frames.add (new TextureRegion(scientistIdleTexture,i*64,0,64,64));
        }
        madStand = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 7; i++){
            frames.add (new TextureRegion(scientistDeathTexture,i*64,0,64,64));
        }
        madDeath = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 7; i++){
            frames.add (new TextureRegion(scientistStunTexture,i*64,0,64,64));
        }
        madStun = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 7; i++){
            frames.add (new TextureRegion(robotWalkRTexture,i*64,0,64,64));
        }
        robotRunR = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 7; i++){
            frames.add (new TextureRegion(robotWalkUTexture,i*64,0,64,64));
        }
        robotRunU = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 7; i++){
            frames.add (new TextureRegion(robotWalkDTexture,i*64,0,64,64));
        }
        robotRunD = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(robotAttackLTexture,i*64,0,64,64));
        }
        robotAttackL = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(robotAttackRTexture,i*64,0,64,64));
        }
        robotAttackR = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(robotAttackUTexture,i*64,0,64,64));
        }
        robotAttackU = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(robotAttackDTexture,i*64,0,64,64));
        }
        robotAttackD = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 7; i++){
            frames.add (new TextureRegion(robotIdleTexture,i*64,0,64,64));
        }
        robotStand = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 7; i++){
            frames.add (new TextureRegion(robotDeathTexture,i*64,0,64,64));
        }
        robotDeath = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 7; i++){
            frames.add (new TextureRegion(robotStunTexture,i*64,0,64,64));
        }
        robotStun = new Animation<TextureRegion>(0.25f, frames);
        frames.clear();

        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(slimeWalkRTexture,i*64,0,64,64));
        }
        slimeRunR = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(slimeWalkUTexture,i*64,0,64,64));
        }
        slimeRunU = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(slimeWalkDTexture,i*64,0,64,64));
        }
        slimeRunD = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(slimeAttackLTexture,i*64,0,64,64));
        }
        slimeAttackL = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(slimeAttackRTexture,i*64,0,64,64));
        }
        slimeAttackR = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(slimeAttackUTexture,i*64,0,64,64));
        }
        slimeAttackU = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(slimeAttackDTexture,i*64,0,64,64));
        }
        slimeAttackD = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(slimeIdleTexture,i*64,0,64,64));
        }
        slimeStand = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 7; i++){
            frames.add (new TextureRegion(slimeDeathTexture,i*64,0,64,64));
        }
        slimeDeath = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 7; i++){
            frames.add (new TextureRegion(slimeStunTexture,i*64,0,64,64));
        }
        slimeStun = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(turretAttackLTexture,i*64,0,64,64));
        }
        turretAttackL = new Animation<TextureRegion>(0.05f, frames);
        frames.clear();

        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(turretAttackRTexture,i*64,0,64,64));
        }
        turretAttackR = new Animation<TextureRegion>(0.05f, frames);
        frames.clear();

        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(turretAttackUTexture,i*64,0,64,64));
        }
        turretAttackU = new Animation<TextureRegion>(0.05f, frames);
        frames.clear();

        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(turretAttackDTexture,i*64,0,64,64));
        }
        turretAttackD = new Animation<TextureRegion>(0.05f, frames);
        frames.clear();

        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(turretIdleTexture,i*64,0,64,64));
        }
        turretStand = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 7; i++){
            frames.add (new TextureRegion(turretDeathTexture,i*64,0,64,64));
        }
        turretDeath = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 7; i++){
            frames.add (new TextureRegion(turretStunTexture,i*64,0,64,64));
        }
        turretStun = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 7; i++){
            frames.add (new TextureRegion(lizardWalkRTexture,i*64,0,64,64));
        }
        lizardRunR = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 7; i++){
            frames.add (new TextureRegion(lizardWalkUTexture,i*64,0,64,64));
        }
        lizardRunU = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 7; i++){
            frames.add (new TextureRegion(lizardWalkDTexture,i*64,0,64,64));
        }
        lizardRunD = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(lizardAttackLTexture,i*64,0,64,64));
        }
        lizardAttackL = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(lizardAttackRTexture,i*64,0,64,64));
        }
        lizardAttackR = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(lizardAttackUTexture,i*64,0,64,64));
        }
        lizardAttackU = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(lizardAttackDTexture,i*64,0,64,64));
        }
        lizardAttackD = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 7; i++){
            frames.add (new TextureRegion(lizardIdleTexture,i*64,0,64,64));
        }
        lizardStand = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 7; i++){
            frames.add (new TextureRegion(lizardDeathTexture,i*64,0,64,64));
        }
        lizardDeath = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i=0; i <= 7; i++){
            frames.add (new TextureRegion(lizardStunTexture,i*64,0,64,64));
        }
        lizardStun = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();


        float dwidth  = 64/scale.x;
        float dheight = 64/scale.y;
        avatar = new JoeModel(level.getJoePosX()/32+OBJ_OFFSET_X, level.getJoePosY()/32+OBJ_OFFSET_Y, dwidth, dheight,
                5, 200f, 5.0f);
        avatar.setWep1(wep_to_model.get("mop"));
        avatar.setWep2(wep_to_model.get("spray"));
        avatar.setDrawScale(scale);
        avatar.setTexture(avatarIdleTexture);
        avatar.setName("joe");
        //remove all hp bonuses gained during this level
        //TODO: Avatar in level only has 15 health because set in Tiled, however, we need to change the HP dynamically
        //depending on what the player has from the last level
        avatar.setCurrentMaxHP(avatar.getBaseHP());
        addObject(avatar);

        for (int ii=0; ii<scientistPos.size(); ii++) {
            EnemyModel mon =new ScientistModel(scientistPos.get(ii).x/32+OBJ_OFFSET_X, scientistPos.get(ii).y/32+OBJ_OFFSET_Y,
                    dwidth, dheight, ii, 3, 1.0f, 2.5f, 2,
                    StateMad.STANDING, StateMad.STANDING, CollideBits.BIT_ENEMY, CollideBits.BIT_ENEMY);
            mon.setPatrol(scientistPatrol.get(ii));
            mon.setDrawScale(scale);
            mon.setName("scientist");
            addObject(mon);
            enemies[ii]=mon;
        }

        for (int ii=0; ii<robotPos.size(); ii++) {
            EnemyModel mon =new RobotModel(robotPos.get(ii).x/32+OBJ_OFFSET_X, robotPos.get(ii).y/32+OBJ_OFFSET_Y,
                    dwidth, dheight, scientistPos.size()+ii, 5, 30.0f, 2.5f, 2,
                    StateRobot.STANDING, StateRobot.STANDING, CollideBits.BIT_ENEMY, CollideBits.BIT_ENEMY);
            mon.setPatrol(robotPatrol.get(ii));
            mon.setDrawScale(scale);
            mon.setName("robot");
            addObject(mon);
            enemies[scientistPos.size()+ii]=mon;
        }
        for (int ii=0; ii<slimePos.size(); ii++){
            EnemyModel mon =new SlimeModel(slimePos.get(ii).x/32+OBJ_OFFSET_X, slimePos.get(ii).y/32+OBJ_OFFSET_Y,
                    dwidth, dheight, scientistPos.size()+robotPos.size()+ii, 3, 1.0f, 1.5f, 8,
                    10.0f,StateSlime.STANDING,StateSlime.STANDING, CollideBits.BIT_ENEMY, CollideBits.BIT_ENEMY);
            mon.setPatrol(slimePatrol.get(ii));
            mon.setDrawScale(scale);
            mon.setName("slime");
            addObject(mon);
            enemies[scientistPos.size()+robotPos.size()+ii]=mon;
        }
        for (int ii=0; ii<lizardPos.size(); ii++){
            EnemyModel mon =new LizardModel(lizardPos.get(ii).x/32+OBJ_OFFSET_X, lizardPos.get(ii).y/32+OBJ_OFFSET_Y,
                    dwidth, dheight, scientistPos.size()+robotPos.size()+slimePos.size()+ii, 3, 1.0f, 4f, 1,
                    StateLizard.STANDING, StateLizard.STANDING, CollideBits.BIT_ENEMY, CollideBits.BIT_ENEMY);
            mon.setPatrol(lizardPatrol.get(ii));
            mon.setDrawScale(scale);
            mon.setName("lizard");
            addObject(mon);
            enemies[scientistPos.size()+robotPos.size()+slimePos.size()+ii]=mon;
        }
        for (int ii = 0; ii< slimeTurretPos.size(); ii++){
            String sdirec = slimeTurretDirections.get(ii);
            int direc = sdirec.equals("auto") ? 0 : (sdirec.equals("left") ? 1 : (sdirec.equals("right") ? 2 :
                    (sdirec.equals("up") ? 3 : (sdirec.equals("down") ? 4 : -1))));
            EnemyModel mon =new TurretModel(slimeTurretPos.get(ii).x/32+OBJ_OFFSET_X, slimeTurretPos.get(ii).y/32+OBJ_OFFSET_Y,
                    dwidth, dheight, scientistPos.size()+robotPos.size()+slimePos.size()+lizardPos.size()+ii, 3, 1.0f, 0, 8, 5f,
                    StateTurret.STANDING,StateTurret.STANDING, direc, slimeTurretDelays.get(ii), CollideBits.BIT_ENEMY, CollideBits.BIT_ENEMY);
            mon.setPatrol(slimeTurretPatrol.get(ii));
            mon.setDrawScale(scale);
            mon.setName("turret");
            addObject(mon);
            enemies[scientistPos.size()+robotPos.size()+slimePos.size()+lizardPos.size()+ii]=mon;
        }
        for (EnemyModel s: enemies){
            if (s!=null) {controls[s.getId()]=new AIController(s.getId(), board, enemies, avatar);}
        }
    }

    private void setHazardTiles() {
        for (int ii = 0; ii < hazardPos.size(); ii++) {
            board.setHazard((int) hazardPos.get(ii).x, (int) hazardPos.get(ii).y);
        }
    }

    private void addWalls() {
        String pname = "wall";
        float dwidth  = wallMidTexture.getRegionWidth()/scale.x;
        float dheight = wallMidTexture.getRegionHeight()/scale.y;
        float offset;
        float offsetCornerX;
        float offsetCornerY;

        offset = (TILE_SIZE * 2*(1 - WALL_THICKNESS_SCALE))/2;
        offsetCornerX = (TILE_SIZE * (1 - WALL_THICKNESS_SCALE))/2;
        offsetCornerY = (TILE_SIZE/2 + TILE_SIZE*3*(1 -WALL_HEIGHT_SCALE));
        BoxObstacle obj;
        float x;
        float y;
        for (int ii = 0; ii < wallMidPos.size(); ii++) {
            x = board.boardToScreenX((int) wallMidPos.get(ii).x);
            y = board.boardToScreenY((int) wallMidPos.get(ii).y) + offset/32 + 0.5f; //added 0.5f for offset due to wall dimensions
            board.setBlocked((int) wallMidPos.get(ii).x, (int) wallMidPos.get(ii).y + 1);

            obj = new BoxObstacle(x, y, dwidth, dheight * WALL_THICKNESS_SCALE / 2);
            obj.setTexture(wallMidTexture, 0, offset);
            obj.setName(pname+ii);
            addWallObject(obj);
        }
        for (int ii = 0; ii < wallLightPos.size(); ii++) {
            x = board.boardToScreenX((int) wallLightPos.get(ii).x);
            y = board.boardToScreenY((int) wallLightPos.get(ii).y) + offset/32 + 0.5f; //added 0.5f for offset due to wall dimensions
            board.setBlocked((int) wallLightPos.get(ii).x, (int) wallLightPos.get(ii).y + 1);

            obj = new BoxObstacle(x, y, dwidth, dheight * WALL_THICKNESS_SCALE / 2);
            obj.setTexture(wallLightTexture, 0, offset);
            obj.setName(pname+ii);
            addWallObject(obj);
        }
        for (int ii = 0; ii < wallTRPos.size(); ii++) {
            x = board.boardToScreenX((int) wallTRPos.get(ii).x);
            y = board.boardToScreenY((int) wallTRPos.get(ii).y) + offset/32 + 0.5f; //added 0.5f for offset due to wall dimensions
            board.setBlocked((int) wallTRPos.get(ii).x, (int) wallTRPos.get(ii).y);
            board.setBlocked((int) wallTRPos.get(ii).x, (int) wallTRPos.get(ii).y+1);
            obj = new BoxObstacle(x, y, dwidth, dheight * WALL_THICKNESS_SCALE / 2);
            obj.setTexture(wallTRTexture, 0, offset);
            obj.setName(pname+ii);
            addWallObject(obj);
            obj = new BoxObstacle(x+offsetCornerX/32, y-offsetCornerY/32, dwidth * WALL_THICKNESS_SCALE, dheight * WALL_HEIGHT_SCALE);
            obj.setName(pname+ii);
            addWallObject(obj);
        }
        for (int ii = 0; ii < wallTLPos.size(); ii++) {
            x = board.boardToScreenX((int) wallTLPos.get(ii).x);
            y = board.boardToScreenY((int) wallTLPos.get(ii).y) + offset/32 + 0.5f; //added 0.5f for offset due to wall dimensions
            board.setBlocked((int) wallTLPos.get(ii).x, (int) wallTLPos.get(ii).y);
            board.setBlocked((int) wallTLPos.get(ii).x, (int) wallTLPos.get(ii).y+1);

            obj = new BoxObstacle(x, y, dwidth, dheight * WALL_THICKNESS_SCALE / 2);
            obj.setTexture(wallTLTexture, 0, offset);
            obj.setName(pname+ii);
            addWallObject(obj);

            obj = new BoxObstacle(x-offsetCornerX/32, y-offsetCornerY/32, dwidth * WALL_THICKNESS_SCALE, dheight * WALL_HEIGHT_SCALE);
            obj.setName(pname+ii);
            addWallObject(obj);
        }
        for (int ii = 0; ii < wallBRPos.size(); ii++) {
            x = board.boardToScreenX((int) wallBRPos.get(ii).x);
            y = board.boardToScreenY((int) wallBRPos.get(ii).y) + offset/32 + 0.5f; //added 0.5f for offset due to wall dimensions
            //board.setBlocked((int) wallBRPos.get(ii).x, (int) wallBRPos.get(ii).y);
            board.setBlocked((int) wallBRPos.get(ii).x, (int) wallBRPos.get(ii).y+1);

            obj = new BoxObstacle(x, y, dwidth, dheight * WALL_THICKNESS_SCALE / 2);
            obj.setTexture(wallBRTexture, 0, offset);
            obj.setName(pname+ii);
            addWallObject(obj);
        }
        for (int ii = 0; ii < wallBLPos.size(); ii++) {
            x = board.boardToScreenX((int) wallBLPos.get(ii).x);
            y = board.boardToScreenY((int) wallBLPos.get(ii).y) + offset/32 + 0.5f; //added 0.5f for offset due to wall dimensions
            //board.setBlocked((int) wallBLPos.get(ii).x, (int) wallBLPos.get(ii).y);
            board.setBlocked((int) wallBLPos.get(ii).x, (int) wallBLPos.get(ii).y+1);

            obj = new BoxObstacle(x, y, dwidth, dheight * WALL_THICKNESS_SCALE / 2);
            obj.setTexture(wallBLTexture, 0, offset);
            obj.setName(pname+ii);
            addWallObject(obj);
        }

        for (int ii = 0; ii < wallSTRPos.size(); ii++) {
            x = board.boardToScreenX((int) wallSTRPos.get(ii).x) + 0.5f;
            y = board.boardToScreenY((int) wallSTRPos.get(ii).y) + offset/32 + 0.5f; //added 0.5f for offset due to wall dimensions

            board.setBlocked((int) wallSTRPos.get(ii).x+1, (int) wallSTRPos.get(ii).y);
            board.setBlocked((int) wallSTRPos.get(ii).x+1, (int) wallSTRPos.get(ii).y+1);
            board.setBlocked((int) wallSTRPos.get(ii).x, (int) wallSTRPos.get(ii).y+1);
            obj = new BoxObstacle(x - 0.5f, y, dwidth, dheight * WALL_THICKNESS_SCALE / 2);
            obj.setTexture(wallSTRTexture, -TILE_SIZE/2.0f, offset);
            obj.setName(pname+ii);
            addWallObject(obj);
            obj = new BoxObstacle(x+offsetCornerX/32-dwidth * WALL_THICKNESS_SCALE/2, y-offsetCornerY/32, dwidth * WALL_THICKNESS_SCALE, dheight * WALL_HEIGHT_SCALE);
            obj.setName(pname+ii);
            addWallObject(obj);
        }
        for (int ii = 0; ii < wallSTLPos.size(); ii++) {
            x = board.boardToScreenX((int) wallSTLPos.get(ii).x) + 0.5f;
            y = board.boardToScreenY((int) wallSTLPos.get(ii).y) + offset/32 + 0.5f; //added 0.5f for offset due to wall dimensions
            board.setBlocked((int) wallSTLPos.get(ii).x, (int) wallSTLPos.get(ii).y);
            board.setBlocked((int) wallSTLPos.get(ii).x+1, (int) wallSTLPos.get(ii).y+1);
            board.setBlocked((int) wallSTLPos.get(ii).x, (int) wallSTLPos.get(ii).y+1);

            obj = new BoxObstacle(x + 0.5f, y, dwidth, dheight * WALL_THICKNESS_SCALE / 2);
            obj.setTexture(wallSTLTexture, TILE_SIZE/2.0f, offset);
            obj.setName(pname+ii);
            addWallObject(obj);

            obj = new BoxObstacle(x-offsetCornerX/32+dwidth * WALL_THICKNESS_SCALE/2, y-offsetCornerY/32, dwidth * WALL_THICKNESS_SCALE, dheight * WALL_HEIGHT_SCALE);
            obj.setName(pname+ii);
            addWallObject(obj);
        }
        for (int ii = 0; ii < wallSBRPos.size(); ii++) {
            x = board.boardToScreenX((int) wallSBRPos.get(ii).x) + 0.5f;
            y = board.boardToScreenY((int) wallSBRPos.get(ii).y) + offset/32 + 0.5f; //added 0.5f for offset due to wall dimensions
            //board.setBlocked((int) wallBRPos.get(ii).x, (int) wallBRPos.get(ii).y);
            board.setBlocked((int) wallSBRPos.get(ii).x, (int) wallSBRPos.get(ii).y+1);
            board.setBlocked((int) wallSBRPos.get(ii).x + 1, (int) wallSBRPos.get(ii).y+1);

            obj = new BoxObstacle(x - 0.5f, y, dwidth, dheight * WALL_THICKNESS_SCALE / 2);
            obj.setTexture(wallSBRTexture, -TILE_SIZE/2.0f, offset);
            obj.setName(pname+ii);
            addWallObject(obj);
        }
        for (int ii = 0; ii < wallSBLPos.size(); ii++) {
            x = board.boardToScreenX((int) wallSBLPos.get(ii).x) + 0.5f;
            y = board.boardToScreenY((int) wallSBLPos.get(ii).y) + offset/32 + 0.5f; //added 0.5f for offset due to wall dimensions
            //board.setBlocked((int) wallBLPos.get(ii).x, (int) wallBLPos.get(ii).y);
            board.setBlocked((int) wallSBLPos.get(ii).x, (int) wallSBLPos.get(ii).y+1);
            board.setBlocked((int) wallSBLPos.get(ii).x+1, (int) wallSBLPos.get(ii).y+1);

            obj = new BoxObstacle(x + 0.5f, y, dwidth, dheight * WALL_THICKNESS_SCALE / 2);
            obj.setTexture(wallSBLTexture, TILE_SIZE/2.0f, offset);
            obj.setName(pname+ii);
            addWallObject(obj);
        }

        for (int ii = 0; ii < wallDTRPos.size(); ii++) {
            x = board.boardToScreenX((int) wallDTRPos.get(ii).x);
            y = board.boardToScreenY((int) wallDTRPos.get(ii).y) + offset/32 + 1.0f; //added 0.5f for offset due to wall dimensions
            board.setBlocked((int) wallDTRPos.get(ii).x, (int) wallDTRPos.get(ii).y);
            board.setBlocked((int) wallDTRPos.get(ii).x, (int) wallDTRPos.get(ii).y+1);
            board.setBlocked((int) wallDTRPos.get(ii).x, (int) wallDTRPos.get(ii).y+2);
            obj = new BoxObstacle(x, y+1.0f-dheight * WALL_THICKNESS_SCALE / 2, dwidth, dheight * WALL_THICKNESS_SCALE / 2);
            obj.setTexture(wallDTRTexture, 0, offset+TILE_SIZE-dheight * WALL_THICKNESS_SCALE * 16);
            obj.setName(pname+ii);
            addWallObject(obj);
            obj = new BoxObstacle(x+offsetCornerX/32, y-offsetCornerY/32 + dheight*WALL_THICKNESS_SCALE/2, dwidth * WALL_THICKNESS_SCALE, dheight * WALL_HEIGHT_SCALE / 2 * 3);
            obj.setName(pname+ii);
            addWallObject(obj);
        }
        for (int ii = 0; ii < wallDTLPos.size(); ii++) {
            x = board.boardToScreenX((int) wallDTLPos.get(ii).x);
            y = board.boardToScreenY((int) wallDTLPos.get(ii).y) + offset/32 + 1.0f; //added 0.5f for offset due to wall dimensions
            board.setBlocked((int) wallDTLPos.get(ii).x, (int) wallDTLPos.get(ii).y);
            board.setBlocked((int) wallDTLPos.get(ii).x, (int) wallDTLPos.get(ii).y+1);
            board.setBlocked((int) wallDTLPos.get(ii).x, (int) wallDTLPos.get(ii).y+2);

            obj = new BoxObstacle(x, y+1.0f-dheight * WALL_THICKNESS_SCALE / 2, dwidth, dheight * WALL_THICKNESS_SCALE / 2);
            obj.setTexture(wallDTLTexture, 0, offset+TILE_SIZE-dheight * WALL_THICKNESS_SCALE * 16);
            obj.setName(pname+ii);
            addWallObject(obj);

            obj = new BoxObstacle(x-offsetCornerX/32, y-offsetCornerY/32 + dheight*WALL_THICKNESS_SCALE/2, dwidth * WALL_THICKNESS_SCALE, dheight * WALL_HEIGHT_SCALE / 2 * 3);
            obj.setName(pname+ii);
            addWallObject(obj);
        }
        for (int ii = 0; ii < wallDBRPos.size(); ii++) {
            x = board.boardToScreenX((int) wallDBRPos.get(ii).x);
            y = board.boardToScreenY((int) wallDBRPos.get(ii).y) + offset/32 + 1.0f; //added 0.5f for offset due to wall dimensions
            //board.setBlocked((int) wallBRPos.get(ii).x, (int) wallBRPos.get(ii).y);
            board.setBlocked((int) wallDBRPos.get(ii).x, (int) wallDBRPos.get(ii).y+1);
            board.setBlocked((int) wallDBRPos.get(ii).x, (int) wallDBRPos.get(ii).y+2);

            obj = new BoxObstacle(x, y-1.0f+dheight * WALL_THICKNESS_SCALE, dwidth, dheight * WALL_THICKNESS_SCALE / 2);
            obj.setTexture(wallDBRTexture, 0, offset-TILE_SIZE+dheight * WALL_THICKNESS_SCALE * 16);
            obj.setName(pname+ii);
            addWallObject(obj);

            obj = new BoxObstacle(x+offsetCornerX/32, y-offsetCornerY/32+1.0f, dwidth * WALL_THICKNESS_SCALE, dheight * WALL_HEIGHT_SCALE/2);
            obj.setName(pname+ii);
            addWallObject(obj);
        }
        for (int ii = 0; ii < wallDBLPos.size(); ii++) {
            x = board.boardToScreenX((int) wallDBLPos.get(ii).x);
            y = board.boardToScreenY((int) wallDBLPos.get(ii).y) + offset/32 + 1.0f; //added 0.5f for offset due to wall dimensions
            //board.setBlocked((int) wallBLPos.get(ii).x, (int) wallBLPos.get(ii).y);
            board.setBlocked((int) wallDBLPos.get(ii).x, (int) wallDBLPos.get(ii).y+1);
            board.setBlocked((int) wallDBLPos.get(ii).x, (int) wallDBLPos.get(ii).y+2);

            obj = new BoxObstacle(x, y-1.0f+dheight * WALL_THICKNESS_SCALE, dwidth, dheight * WALL_THICKNESS_SCALE / 2);
            obj.setTexture(wallDBLTexture, 0, offset-TILE_SIZE+dheight * WALL_THICKNESS_SCALE * 16);
            obj.setName(pname+ii);
            addWallObject(obj);

            obj = new BoxObstacle(x-offsetCornerX/32, y-offsetCornerY/32+1.0f, dwidth * WALL_THICKNESS_SCALE, dheight * WALL_HEIGHT_SCALE / 2);
            obj.setName(pname+ii);
            addWallObject(obj);
        }

        for (int ii = 0; ii < wallERPos.size(); ii++) {
            x = board.boardToScreenX((int) wallERPos.get(ii).x);
            y = board.boardToScreenY((int) wallERPos.get(ii).y) + offset/32 + 0.5f; //added 0.5f for offset due to wall dimensions
            //board.setBlocked((int) wallERPos.get(ii).x, (int) wallERPos.get(ii).y);
            board.setBlocked((int) wallERPos.get(ii).x, (int) wallERPos.get(ii).y+1);

            obj = new BoxObstacle(x, y, dwidth, dheight * WALL_THICKNESS_SCALE / 2);
            obj.setTexture(wallERTexture, 0, offset);
            obj.setName(pname+ii);
            addWallObject(obj);
        }
        for (int ii = 0; ii < wallELPos.size(); ii++) {
            x = board.boardToScreenX((int) wallELPos.get(ii).x);
            y = board.boardToScreenY((int) wallELPos.get(ii).y) + offset/32 + 0.5f; //added 0.5f for offset due to wall dimensions
            //board.setBlocked((int) wallELPos.get(ii).x, (int) wallELPos.get(ii).y);
            board.setBlocked((int) wallELPos.get(ii).x, (int) wallELPos.get(ii).y+1);

            obj = new BoxObstacle(x, y, dwidth, dheight * WALL_THICKNESS_SCALE / 2);
            obj.setTexture(wallELTexture, 0, offset);
            obj.setName(pname+ii);
            addWallObject(obj);
        }


        float offsetY = offset;
        offset = (TILE_SIZE * (1 - WALL_THICKNESS_SCALE))/2;

        for (int ii = 0; ii < wallSLPos.size(); ii++) {
            x = board.boardToScreenX((int) wallSLPos.get(ii).x) + offset/32;
            y = board.boardToScreenY((int) wallSLPos.get(ii).y) + offsetY/32 + 0.5f; //added 0.5f for offset due to wall dimensions
            board.setBlocked((int) wallSLPos.get(ii).x, (int) wallSLPos.get(ii).y);
            board.setBlocked((int) wallSLPos.get(ii).x, (int) wallSLPos.get(ii).y+1);

            obj = new BoxObstacle(x, y, dwidth * WALL_THICKNESS_SCALE, dheight * WALL_THICKNESS_SCALE / 2);
            obj.setTexture(wallSLTexture, offset, offsetY);
            obj.setName(pname+ii);
            addWallObject(obj);

            obj = new BoxObstacle(x, y-offsetCornerY/32, dwidth * WALL_THICKNESS_SCALE, dheight * WALL_HEIGHT_SCALE);
            obj.setName(pname+ii);
            addWallObject(obj);
        }

        offset = -offset;
        for (int ii = 0; ii < wallSRPos.size(); ii++) {
            x = board.boardToScreenX((int) wallSRPos.get(ii).x) + offset/32;
            y = board.boardToScreenY((int) wallSRPos.get(ii).y) + offsetY/32 + 0.5f; //added 0.5f for offset due to wall dimensions
            board.setBlocked((int) wallSRPos.get(ii).x, (int) wallSRPos.get(ii).y);
            board.setBlocked((int) wallSRPos.get(ii).x, (int) wallSRPos.get(ii).y+1);

            obj = new BoxObstacle(x, y, dwidth * WALL_THICKNESS_SCALE, dheight * WALL_THICKNESS_SCALE / 2);
            obj.setTexture(wallSRTexture, offset, offsetY);
            obj.setName(pname+ii);
            addWallObject(obj);

            obj = new BoxObstacle(x, y-offsetCornerY/32, dwidth * WALL_THICKNESS_SCALE, dheight * WALL_HEIGHT_SCALE);
            obj.setName(pname+ii);
            addWallObject(obj);
        }

        dwidth = wallLeftTexture.getRegionWidth()/scale.x;
        dheight = wallLeftTexture.getRegionHeight()/scale.y;

        for (int ii = 0; ii < wallLeftPos.size(); ii++) {
            x = board.boardToScreenX((int) wallLeftPos.get(ii).x) + offset/32;
            y = board.boardToScreenY((int) wallLeftPos.get(ii).y);
            board.setBlocked((int) wallLeftPos.get(ii).x, (int) wallLeftPos.get(ii).y);

            obj = new BoxObstacle(x, y, dwidth * WALL_THICKNESS_SCALE, dheight);
            obj.setTexture(wallLeftTexture, offset, 0);
            obj.setName(pname+ii);
            addWallObject(obj);
        }

        offset = -offset;
        for (int ii = 0; ii < wallRightPos.size(); ii++) {
            x = board.boardToScreenX((int) wallRightPos.get(ii).x) + offset/32;
            y = board.boardToScreenY((int) wallRightPos.get(ii).y);
            board.setBlocked((int) wallRightPos.get(ii).x, (int) wallRightPos.get(ii).y);

            obj = new BoxObstacle(x, y, dwidth * WALL_THICKNESS_SCALE, dheight);
            obj.setName(pname+ii);
            obj.setTexture(wallRightTexture, offset, 0);
            addWallObject(obj);
        }
    }

    private void addWallObject(BoxObstacle obj) {
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(BASIC_DENSITY);
        obj.setFriction(BASIC_FRICTION);
        obj.setRestitution(BASIC_RESTITUTION);
        obj.setDrawScale(scale);
        addObject(obj);
    }

    /**
     * Returns whether to process the update loop
     *
     * At the start of the update loop, we check if it is time
     * to switch to a new game mode.  If not, the update proceeds
     * normally.
     *
     * @param dt Number of seconds since last animation frame
     *
     * @return whether to process the update loop
     */
    public boolean preUpdate(float dt) {
        if (!super.preUpdate(dt)) {
            return false;
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
     * @param dt Number of seconds since last animation frame
     */
    public void update(float dt) {
        //OrthographicCamera camera = canvas.getCamera();
        //System.out.println(avatar.getWep1().getDurability());
        if (gotHit > 0 && avatar.isRed() && gotHit +30 == ticks && avatar.isAlive()) {
            avatar.setRed(false);
            gotHit = -1;
        }
        ticks ++;
        avatar.setTexture(getFrameJoe(dt));
        if(avatar.getHP()<=0) {
            avatar.setAlive(false);
            avatar.setMovementX(0.0f);
            avatar.setMovementY(0.0f);
            avatar.setVelocity();
            if (joeDeathTimer <= 0 ) {
                //System.out.println("deathtimerset");
                if (!isFailure()) {
                    setFailure(true);
                    setCameraX(cameraX);
                    setCameraY(cameraY);
                }
            }
            else {
                joeDeathTimer -= dt;
            }

        }
        else if (board.isHazard(board.screenToBoardX(avatar.getX()), board.screenToBoardY(avatar.getY()) - 1) &&
                ticks % 30==0L) { //adjust this later
            //-1 on Y so that it deals if your feet are on the tile but not your head
            avatar.decrHP();
            gotHit = ticks;
            avatar.setRed(true);
//            avatar.setRed(true); //this might cause some bugs because of below else statement
            SoundController.getInstance().play(OUCH_FILE, OUCH_FILE,false,EFFECT_VOLUME);
        }
        else {
            if (gotHit > 0 && avatar.isRed() && gotHit + 30 == ticks && avatar.isAlive()) {
                avatar.setRed(false);
                gotHit = -1;
            }

            float playerPosX = avatar.getX() * 32;
            //getX only gets the tile #, multiply by 32 to get the pixel number
            float playerPosY = avatar.getY() * 32;

            float lightX;
            float lightY;

            cameraX = playerPosX;
            cameraY = playerPosY;
            lightX = canvas.getWidth()/2.0f;
            lightY = canvas.getHeight()/2.0f;

            if (playerPosX >= LEFT_SCROLL_CLAMP && playerPosX <= RIGHT_SCROLL_CLAMP) {
                //if player is inside clamped sections, center camera on player
                cameraX = playerPosX;
                lightX = canvas.getWidth()/2.0f;

            }
            else {
                //if it is going to show outside of level, set camera to clamp
                if (playerPosX < LEFT_SCROLL_CLAMP) {
                    cameraX = LEFT_SCROLL_CLAMP;
                    lightX = playerPosX;
                }
                else if (playerPosX > RIGHT_SCROLL_CLAMP) {
                    cameraX = RIGHT_SCROLL_CLAMP;
                    lightX = playerPosX;
                }
            }
            if (playerPosY <= TOP_SCROLL_CLAMP && playerPosY >= BOTTOM_SCROLL_CLAMP) {
                //if player is inside clamped sections, center camera on player
                cameraY = playerPosY;
                lightY = canvas.getHeight()/2.0f;
            }
            else {
                //if it is going to show outside of level, set camera to clamp
                if (playerPosY > TOP_SCROLL_CLAMP) {
                    cameraY = TOP_SCROLL_CLAMP;
                    lightY = playerPosY;
                }
                else if (playerPosY < BOTTOM_SCROLL_CLAMP) {
                    cameraY = BOTTOM_SCROLL_CLAMP;
                    lightY = playerPosY;
                }
            }
            canvas.setCameraPosition(cameraX, cameraY);

            if (InputController.getInstance().getDidLighting()) {
                toggleLighting();
            }

            raycamera.position.set(lightX, lightY, 0);
            raycamera.update();

            //light.setPosition(lightX, lightY);
            if (rayhandler != null) {
                rayhandler.update();
            }

            if (board.isHazard(board.screenToBoardX(avatar.getX()), board.screenToBoardY(avatar.getY())-1) &&
                    ticks % 30 == 0L) { //adjust this later
                avatar.decrHP();
                SoundController.getInstance().play(OUCH_FILE, OUCH_FILE, false, EFFECT_VOLUME);
            } else {
                // Process actions in object model

                avatar.setSwapping(InputController.getInstance().didTertiary());
                avatar.setPrimarySwapping(InputController.getInstance().didQKey());
//                System.out.println(attackTimer);
//                System.out.println(dt);
//

//                if (avatar.isRight()){
//                    System.out.println("right");
//                    System.out.println(avatar.isFacingRight());
//                }
//                avatar.setLeft(InputController.getInstance().didLeftArrow());
//                avatar.setRight(InputController.getInstance().didRightArrow());
//                avatar.setUp(InputController.getInstance().didUpArrow());
//                avatar.setDown(InputController.getInstance().didDownArrow());
//                System.out.println(attackTimer);
//                System.out.println(avatar.getY());


                if (avatar.isAtMopCart()) {
                    joeAtMopCartUpdate();
                } else {
                    joeNotAtMopCartUpdate();
                }
                if (attackTimer == 0) {
                    avatar.setMovementX(InputController.getInstance().getHorizontal() * avatar.getVelocity());
                    avatar.setMovementY(InputController.getInstance().getVertical() * avatar.getVelocity());
                    avatar.setLeft(InputController.getInstance().didLeftArrow());
                    avatar.setRight(InputController.getInstance().didRightArrow());
                    avatar.setUp(InputController.getInstance().didUpArrow());
                    avatar.setDown(InputController.getInstance().didDownArrow());
                    avatar.setVelocity();
                } else {
                    avatar.setMovementX(0.0f);
                    avatar.setMovementY(0.0f);
                    avatar.setVelocity();
                }
//                avatar.setTexture(getFrameJoe(dt));

            }
            lidRange(dt);
            enemyUpdate();
            for (EnemyModel s : enemies) {
                if (s.getName() == "scientist") {
                    s.setTexture(getFrameScientist(dt, s));
                } else if (s.getName() == "robot") {
                    s.setTexture(getFrameRobot(dt, s));
                } else if (s.getName() == "slime") {
                    s.setTexture(getFrameSlime(dt, s));
                } else if (s.getName() == "turret") {
                    s.setTexture(getFrameTurret(dt, s));
                } else if (s.getName() == "lizard") {
                    s.setTexture(getFrameLizard(dt, s));
                }
            }
            clearEnemy(dt);

            SoundController.getInstance().update();
        }
    }

    private void toggleLighting() {
        for(LightSource light : lights) {
            if (light.isActive()) {
                light.setActive(false);
            } else {
                light.setActive(true);
            }
        }
        lightIsActive = !lightIsActive;
    }

    /**
     * Update function for Joe when he at the mop cart
     */
    private void joeAtMopCartUpdate() {
        int mc_i = avatar.isAtWhichMopCart();
        if (!mopCartVisitedBefore.get(mc_i)) {
            if (avatar.getWep1().durability != avatar.getWep1().getMaxDurability()
                || (avatar.getWep2().durability != avatar.getWep2().getMaxDurability())) {
                //if you haven't reloaded at this mop cart and you need to reload, reload

                mopCartVisitedBefore.set(mc_i, true);
                SoundController.getInstance().play(RELOAD_FILE, RELOAD_FILE,false,EFFECT_VOLUME);
                //recharge durability of weapons
                avatar.getWep1().durability = avatar.getWep1().getMaxDurability();
                avatar.getWep2().durability = avatar.getWep2().getMaxDurability();
                //recharge cart weapons
                String cart1 = mopcart_menu[0];
                String cart2 = mopcart_menu[1];
                WeaponModel cart_weapon1 = wep_to_model.get(cart1);
                WeaponModel cart_weapon2 = wep_to_model.get(cart2);
                cart_weapon1.durability = cart_weapon1.getMaxDurability();
                cart_weapon2.durability = cart_weapon2.getMaxDurability();

                //recharge to max health
                avatar.setHP(avatar.getCurrentMaxHP());
            }
        }

        for(Obstacle obj : objects) {
            if (obj.getName() == "lid") {
                obj.markRemoved(true);
                avatar.setHasLid(true);
                lidGround = false;
                lidTimer = LID_RANGE;
            }
        }
        //move mop cart index
        if (avatar.isLeft()) {
            if (mopcart_index == 1) { mopcart_index = 0; }
        } else if (avatar.isRight()) {
            if (mopcart_index == 0) { mopcart_index = 1; }
        }
        // swapping weapon
        if (avatar.isPrimarySwapping()) {
            //get weapon at index
            String swapping_weapon_name = mopcart_menu[mopcart_index];
            WeaponModel swapping_weapon = wep_to_model.get(swapping_weapon_name);

            //set all new weapons
            WeaponModel old_primary = avatar.getWep1();
            avatar.setWep1(swapping_weapon);
            mopcart_menu[mopcart_index] = old_primary.name;
            if (swapping_weapon_name == "lid") {
                avatar.setHasLid(true);
            }
            if (old_primary.name == "lid") {
                avatar.setHasLid(false);
            }
        }
        // swapping all weapons
        if (avatar.isSwapping()) {
            //get weapon in cart
            String swapping_weapon_name = mopcart_menu[mopcart_index];
            WeaponModel swapping_weapon = wep_to_model.get(swapping_weapon_name);
            //get old secondary weapon
            WeaponModel old_secondary = avatar.getWep2();
            //set new weapons
            avatar.setWep2(swapping_weapon);
            mopcart_menu[mopcart_index] = old_secondary.name;

            if (swapping_weapon_name == "lid") {
                avatar.setHasLid(true);
            }
            if (old_secondary.name == "lid") {
                avatar.setHasLid(false);
            }
        }
    }

    /**
     * Update function for Joe when he is not using the mop cart
     */
    private void joeNotAtMopCartUpdate() {
        //if you're swapping between primary and secondary weapon
        if (avatar.isSwapping()) {
            WeaponModel current_wep1 = avatar.getWep1();
            WeaponModel current_wep2 = avatar.getWep2();
            avatar.setWep1(current_wep2);
            avatar.setWep2(current_wep1);
        }
        // attack

        if ((avatar.isUp()||avatar.isDown()||avatar.isRight()||avatar.isLeft())
                && avatar.getWep1().getDurability() < 0  && attackTimer == 0) {
            SoundController.getInstance().play(NO_WEAPON_FILE, NO_WEAPON_FILE, false, 0.5f);
        }
        if ((avatar.isUp()||avatar.isDown()||avatar.isRight()||avatar.isLeft())
                && avatar.getWep1().getDurability() == 0  && attackTimer == 0) {
            avatar.getWep1().decrDurability();
        }
    }

    /**
     * Update function for enemies
     */
    private void enemyUpdate() {
        //HashSet<Vector2> hs = new HashSet<Vector2>();
        for (EnemyModel s : enemies) {
            if (this.controls[s.getId()] != null) {
                //hs.add(new Vector2(board.screenToBoardX(s.getX()), board.screenToBoardY(s.getY())));
                int action = this.controls[s.getId()].getAction();
                //board.setStanding(board.getGoal().x, board.getGoal().y);
                if (s.getStunned()) {
                    s.incrStunTicks();
                    if (s.getStunTicks()<=150) {action=CONTROL_NO_ACTION; s.setMovementY(0); s.setMovementX(0);} //TODO change to get from sprayModel
                    else {s.resetStunTicks(); s.setStunned(false);}
                }
                else if (s.getStunnedVacuum()) {
                    s.incrStunTicksVacuum();
                    if (s.getStunTicksVacuum()<=75) {action=CONTROL_NO_ACTION; s.setMovementY(0); s.setMovementX(0);} //TODO change to get from sprayModel
                    else {s.resetStunTicksVacuum(); s.setStunnedVacuum(false);}
                }

                performAction(s, action);
                if (board.isHazard(board.screenToBoardX(s.getX()), board.screenToBoardY(s.getY() ))
                        && !(s instanceof RobotModel) && ticks % 30==0L ){ //adjust this later
                    //-1 so if they step feet on it they lose health
                    s.decrHP();
                    if (s.getHP()<0) {controls[s.getId()]=null;}
                }
            }
        }
    }
    private void clearEnemy (float dt) {
        for (EnemyModel s : enemies) {
            if (s.getHP() <= 0 && deathTimer <= 0) {
                deathTimer = DEATH_ANIMATION_TIME;
                s.markRemoved(true);
            } else if (s.getHP() <= 0 && deathTimer > 0) {
                deathTimer -= dt;
            }
        }
    }

    /**
     * Perform enemy s's action
     * @param s enemy that is performing the action
     * @param action action to be performed
     */
    private void performAction(EnemyModel s, int action) {
        int sx = this.board.screenToBoardX(s.getX());
        int sy = this.board.screenToBoardY(s.getY());
        int tx = this.board.screenToBoardX(avatar.getX());
        int ty = this.board.screenToBoardY(avatar.getY());

        AIController ai = controls[s.getId()];
        float vel = s.getName()=="lizard" && ai.getState()== AIController.FSMState.CHASE &&
                s.canHitTargetFrom(sx, sy, tx, ty, 2, 2, 2) ? 2.5f : s.getVelocity();
        if (action == CONTROL_NO_ACTION) {
            s.setMovementY(0);
            s.setMovementX(0);
        }
        if (action == CONTROL_MOVE_DOWN) {
            s.setMovementY(-vel);
            s.setMovementX(0);
            s.resetAttackAniFrame();
        }
        if (action == CONTROL_MOVE_LEFT) {
            s.setMovementX(-vel);
            s.setMovementY(0);
            s.resetAttackAniFrame();
        }
        if (action == CONTROL_MOVE_UP) {
            s.setMovementY(vel);
            s.setMovementX(0);
            s.resetAttackAniFrame();
        }
        if (action == CONTROL_MOVE_RIGHT) {
            s.setMovementX(vel);
            s.setMovementY(0);
            s.resetAttackAniFrame();

        }
        if (s.canAttack()) {
            if (action==CONTROL_FIRE){

                if (!(s instanceof SlimeModel) && !(s instanceof TurretModel)) {
                    //only do this for melee enemies
                    //can't be slime model otherwise lose health when slimes shoot (not when hit by bullet)
                    if (s.getAttackAnimationFrame()==0 && avatar.isAlive() && avatar.getHP() > 1) {
                        gotHit=ticks;
                        avatar.decrHP();
                        avatar.setRed(true);
                        SoundController.getInstance().play(OUCH_FILE, OUCH_FILE,false,EFFECT_VOLUME);
                    }
                    else if (s.getAttackAnimationFrame()==0 && avatar.isAlive() && avatar.getHP() <= 1) {
                        //don't set red on last shot
                        gotHit=ticks;
                        avatar.decrHP();
                        avatar.setRed(false);
                        SoundController.getInstance().play(OUCH_FILE, OUCH_FILE,false,EFFECT_VOLUME);
                    }
                }
                enemyAttack(s);
            }
        } else {
            s.decrAttackCooldown();
        }

        if (s.getKnockbackTimer() == 0) {
            s.setVelocity();
        } else {
            s.decrKnockbackTimer();
        }
    }

    /**
     * Perform enemy attack
     * @param s enemy that is attacking
     */
    private void enemyAttack(EnemyModel s) {
        s.setMovementX(0);
        s.setMovementY(0);
        s.startAttackCooldown();
        if (s instanceof ScientistModel || s instanceof RobotModel || s instanceof LizardModel) {
            s.incrAttackAniFrame();

            if (s.getAttackAnimationFrame()==4 && avatar.isAlive()){
                s.resetAttackAniFrame();
            }
        } else if (s instanceof SlimeModel ) {
            //System.out.println("shoot1");
            s.incrAttackAniFrame();

            if (s.getAttackAnimationFrame()==1 && avatar.isAlive()) {
                createBullet((SlimeModel) s);
            }
            if (s.getAttackAnimationFrame()==4 && avatar.isAlive()){
                s.resetAttackAniFrame();
            }
        }
        else if (s instanceof TurretModel ) {
            //System.out.println("shoot1");
            s.incrAttackAniFrame();

            if (s.getAttackAnimationFrame()==((TurretModel) s).getDelay() && avatar.isAlive()) {
                createBullet2((TurretModel)s);
            }
            if (s.getAttackAnimationFrame()==5 && avatar.isAlive()){
                s.resetAttackAniFrame();
            }
        }
    }

    /**
     * Add a new garbage lid to the world and send it in the right direction.
     */
    private void createBullet(JoeModel player) {
        float offsetx = 0;
        float offsety = 0;
        if (player.isLeft()){
            offsetx = -BULLET_OFFSET;
        }
        if (player.isRight()){
            offsetx = BULLET_OFFSET;
        }
        if (player.isDown()){
            offsety = -BULLET_OFFSET;
        }
        if (player.isUp()){
            offsety = BULLET_OFFSET;
        }

        float radius = bulletTexture.getRegionWidth()/(2.0f*scale.x);
        WheelObstacle bullet = new WheelObstacle(player.getX()+offsetx, player.getY()+offsety, radius);
        bullet.setName("lid");
        bullet.setDensity(HEAVY_DENSITY);
        bullet.setDrawScale(scale);
        bullet.setTexture(bulletTexture);
        bullet.setBullet(true);
        bullet.setGravityScale(0);

        // Compute position and velocity
        float speedx  = 0;
        float speedy  = 0;
        if (player.isLeft()){
            speedx = -BULLET_SPEED;
        }
        else
            speedx = BULLET_SPEED;
        if (player.isUp()){
            speedy = BULLET_SPEED;
        }
        else
            speedy = -BULLET_SPEED;
        if (Math.abs(offsetx)>0) {
            bullet.setVX(speedx);
            lidVel.x = speedx;
            lidVel.y = 0;
        }
        else {
            bullet.setVY(speedy);
            lidVel.x = 0;
            lidVel.y = speedy;
        }
        addQueuedObject(bullet);

        SoundController.getInstance().play(PEW_FILE, PEW_FILE, false, 0.5f);
    }

    /**
     * Add a new slimeball to the world and send it in the right direction.
     */
    private void createBullet(SlimeModel player) {
        // Compute position and velocity

        int dirX = board.screenToBoardX(avatar.getX()) - board.screenToBoardX(player.getX());
        int dirY =  board.screenToBoardY(avatar.getY()) - board.screenToBoardY(player.getY());

        float speedX  = 0;
        float speedY = 0;
        float offsetX = 0;
        float offsetY = 0;

        if (dirX > 0) {
            speedX = SLIMEBALL_SPEED;
            offsetX = BULLET_OFFSET;
        } else if (dirX < 0) {
            speedX = -SLIMEBALL_SPEED;
            offsetX = -BULLET_OFFSET;
        }

        if (dirY > 0) {
            speedY = SLIMEBALL_SPEED;
            offsetY = BULLET_OFFSET;
        } else if (dirY < 0) {
            speedY = -SLIMEBALL_SPEED;
            offsetY = -BULLET_OFFSET;
        }

        float radius = slimeballTexture.getRegionWidth()/(2.0f*scale.x);
        WheelObstacle bullet = new WheelObstacle(player.getX() + offsetX, player.getY() + offsetY, radius);

        bullet.setName("slimeball");
        bullet.setDensity(HEAVY_DENSITY);
        bullet.setDrawScale(scale);
        bullet.setTexture(slimeballTexture);
        bullet.setBullet(true);
        bullet.setGravityScale(0);
        bullet.setVX(speedX);
        bullet.setVY(speedY);
        bullet.setCategoryBits(CollideBits.BIT_SLIMEBALL);
        addQueuedObject(bullet);

        SoundController.getInstance().play(PEW_FILE, PEW_FILE, false, 0.5f);
    }

    private void createBullet2(TurretModel player) {

        // Compute position and velocity
        if (player.getDirection()==-1) assert false;
        float speedX = 0;
        float speedY = 0;
        float offsetX = 0;
        float offsetY = 0;
        int d= player.getDirection();
        System.out.println("direction: "+d);
        if (d==0) {
            int dirX = board.screenToBoardX(avatar.getX()) - board.screenToBoardX(player.getX());
            int dirY = board.screenToBoardY(avatar.getY()) - board.screenToBoardY(player.getY());

            if (dirX > 0) {
                speedX = SLIMEBALL_SPEED;
                offsetX = BULLET_OFFSET;
            } else if (dirX < 0) {
                speedX = -SLIMEBALL_SPEED;
                offsetX = -BULLET_OFFSET;
            }
            if (dirY > 0) {
                speedY = SLIMEBALL_SPEED;
                offsetY = BULLET_OFFSET;
            } else if (dirY < 0) {
                speedY = -SLIMEBALL_SPEED;
                offsetY = -BULLET_OFFSET;
            }
        }
        else {
            /**
            speedX = d==1 ? -SLIMEBALL_SPEED : d==2 ? SLIMEBALL_SPEED : 0;
            offsetX = d==1 ? -BULLET_OFFSET : d==2 ? BULLET_OFFSET : 0;
            speedY = d==3 ? SLIMEBALL_SPEED : d==4 ? -SLIMEBALL_SPEED :0 ;
            offsetY = d==3 ? BULLET_OFFSET : d==4 ? -BULLET_OFFSET :0 ;**/
            if (d==1) {
                speedX = -SLIMEBALL_SPEED;
                offsetX = -BULLET_OFFSET;
            }
            else if (d==2) {
                speedX = SLIMEBALL_SPEED;
                offsetX = BULLET_OFFSET;
            }
            else if (d==3) {
                speedY = SLIMEBALL_SPEED;
                offsetY= BULLET_OFFSET;
            }
            else if (d==4) {
                speedY = -SLIMEBALL_SPEED;
                offsetY = -BULLET_OFFSET;
            }
        }

        float radius = slimeballTexture.getRegionWidth()/(2.0f*scale.x);
        WheelObstacle bullet = new WheelObstacle(player.getX() + offsetX, player.getY() + offsetY, radius);

        bullet.setName("slimeball");
        bullet.setDensity(HEAVY_DENSITY);
        bullet.setDrawScale(scale);
        bullet.setTexture(slimeballTexture);
        bullet.setBullet(true);
        bullet.setGravityScale(0);
        bullet.setVX(speedX);
        bullet.setVY(speedY);
        bullet.setCategoryBits(CollideBits.BIT_SLIMEBALL);
        addQueuedObject(bullet);

        SoundController.getInstance().play(PEW_FILE, PEW_FILE, false, 0.5f);
    }

    /**
     * Remove a new bullet from the world.
     *
     * @param  bullet   the bullet to remove
     */
    public void removeBullet(Obstacle bullet) {
        bullet.markRemoved(true);
        SoundController.getInstance().play(POP_FILE,POP_FILE,false,EFFECT_VOLUME);
    }

    /**
     * Remove a garbage lid from the world and decrement the HP of the enemy the lid collided with.
     * Only called when the lid collides with an enemy.
     *
     * @param lid the lid to be removed
     * @param enemy the enemy that has been hit
     */
    public void removeLid(Obstacle lid,EnemyModel enemy) {
        if (avatar.getHasLid() == false) {
            avatar.setHasLid(true);
            float knockbackx = 10f;
            float knockbackx2 = (lid.getX() > enemy.getX() ? -knockbackx : knockbackx);
            knockbackForce.set(knockbackx2,0f);
            lid.markRemoved(true);
            SoundController.getInstance().play(POP_FILE,POP_FILE,false,EFFECT_VOLUME);
            enemy.decrHP();
            //if (enemy.getHP()<0) {controls[enemy.getId()]=null;}
            //System.out.println("was lidded");
            enemy.setKnockbackTimer(KNOCKBACK_TIMER);
            enemy.applyImpulse(knockbackForce);
            if (enemy.getHP() <= 0) {
                controls[enemy.getId()]=null;
                enemy.markRemoved(true);
            }
        }
    }

    /**
     * Drop the lid on the ground
     * @param lid lid to be dropped
     */
    public void dropLid(Obstacle lid) {
        lid.setVX(0.0f);
        lid.setVY(0.0f);
        SoundController.getInstance().play(POP_FILE,POP_FILE,false,EFFECT_VOLUME);
        lidGround = true;
    }

    public void lidRange (float dt){
        for(Obstacle obj : objects) {
            if (obj.getName() == "lid" && lidTimer <= 0 ){
                dropLid(obj);
                lidTimer = LID_RANGE;
            }
            else if (obj.getName() == "lid" && lidTimer >= 0 && !lidGround) {
                lidTimer -= dt;
            }
        }

//        if (lidTimer >= 0){
//            lidTimer -= dt;
//        }
//        else{
//            lidTimer = 2;
//            for(Obstacle obj : objects) {
//                if (obj.isBullet()){
//                    dropLid(obj);
//                }
//            }
//
//        }
    }

    public void attack(WeaponModel wep) {
        if (wep == null) {
            return;
        } else if (wep instanceof MopModel) {
            MopModel mop = (MopModel) wep;
            if (mop.getDurability() > 0) {
                SoundController.getInstance().play(PEW_FILE, PEW_FILE, false, 0.5f);

                boolean enemy_hit = false;
                for (EnemyModel s : enemies) {
                    if (s.isActive()) {
                        int horiGap = board.screenToBoardX(avatar.getX()) - board.screenToBoardX(s.getX());
                        int vertiGap = board.screenToBoardY(avatar.getY()) - board.screenToBoardY(s.getY());
//                        //implement isWallInWay method that says if wall is in way of gap and where that wall is
//
//                        boolean case1 = Math.abs(horiGap)<= mop.getRange() && horiGap>=0 && avatar.isLeft() && Math.abs(vertiGap)<= 1;
//                        boolean case2 = Math.abs(horiGap)<= mop.getRange() && horiGap<=0 && avatar.isRight() && Math.abs(vertiGap)<= 1;
//                        boolean case3 = Math.abs(vertiGap)<= mop.getRange() && vertiGap>=0 && avatar.isDown() && Math.abs(horiGap)<= 1;
//                        boolean case4 = Math.abs(vertiGap)<= mop.getRange() && vertiGap<=0 && avatar.isUp() && Math.abs(horiGap)<= 1;
//
                        boolean case1 = avatar.isLeft() && avatar.getX() >= s.getX() && avatar.getX() - s.getX() <= 3f &&
                                avatar.getY()- 1.0f <= s.getY() &&  s.getY() <= avatar.getY() + 1.0f;
                        boolean case2 = avatar.isRight() && avatar.getX() <= s.getX() && s.getX() - avatar.getX() <= 3f &&
                                avatar.getY()- 1.0f <= s.getY() &&  s.getY() <= avatar.getY() + 1.0f;
                        boolean case3 = avatar.isUp() && avatar.getY() <= s.getY() && s.getY() - avatar.getY() <= 3f &&
                                avatar.getX()- 1.0f <= s.getX() &&  s.getX() <= avatar.getX() + 1.0f;
                        boolean case4 = avatar.isDown() && s.getY() <= avatar.getY() && avatar.getY() - s.getY() <= 3f &&
                                avatar.getX()- 1.0f <= s.getX() &&  s.getX() <= avatar.getX() + 1.0f;
                        if ((case1 || case2 || case3 || case4)&& s.getAttacked() == false) {
                            enemy_hit = true;
//                        if (s.getHP() == 1) {
////                            s.markRemoved(true);
////                            controls[s.getId()]=null;
//                        } else {
                            //System.out.println("was mopped");
                            s.decrHP();
                            if (s.getHP()<0) {controls[s.getId()]=null;}
                            s.setAttacked(true);
//                        }
                            knockbackForce.set(horiGap * -7.5f, vertiGap * -7.5f);
                            //knockbackForce.nor();

                            s.applyImpulse(knockbackForce);
                            s.setKnockbackTimer(KNOCKBACK_TIMER);
                            //System.out.println(knockbackForce);
                        }
                    }
                }
                if (enemy_hit) {
                    mop.decrDurability();
                }
            }
            //hotfix xd
//            if (mop.getDurability() < 0 ){
//                SoundController.getInstance().play(NO_WEAPON_FILE, NO_WEAPON_FILE, false, 0.5f);
//            }
//            if (mop.getDurability() == 0 ){
//                mop.decrDurability();
//            }

        } else if (wep instanceof SprayModel) {
            SprayModel spray = (SprayModel) wep;
//            spray.decrDurability();
            if (spray.getDurability() >= 0) {
                SoundController.getInstance().play(PEW_FILE, PEW_FILE, false, 0.5f);
                for (EnemyModel s : enemies) {
                    if (!s.isRemoved()) {
                        int horiGap = board.screenToBoardX(avatar.getX()) - board.screenToBoardX(s.getX());
                        int vertiGap = board.screenToBoardY(avatar.getY()) - board.screenToBoardY(s.getY());
                        boolean case1 = avatar.isLeft() && avatar.getX() >= s.getX() && avatar.getX() - s.getX() <= 5.0f &&
                                avatar.getY()- 1.0f <= s.getY() &&  s.getY() <= avatar.getY() + 1.0f;
                        boolean case2 = avatar.isRight() && avatar.getX() <= s.getX() && s.getX() - avatar.getX() <= 5.0f &&
                                avatar.getY()- 1.0f <= s.getY() &&  s.getY() <= avatar.getY() + 1.0f;
                        boolean case3 = avatar.isUp() && avatar.getY() <= s.getY() && s.getY() - avatar.getY() <= 5.0f &&
                                avatar.getX()- 1.0f <= s.getX() &&  s.getX() <= avatar.getX() + 1.0f;
                        boolean case4 = avatar.isDown() && s.getY() <= avatar.getY() && avatar.getY() - s.getY() <= 5.0f &&
                                avatar.getX()- 1.0f <= s.getX() &&  s.getX() <= avatar.getX() + 1.0f;

                        if (!s.isRemoved() && (case1 || case2 || case3 || case4) && s.getAttacked() == false) {
                            if (s instanceof RobotModel){
                                System.out.println("was robot");
                                s.setStunned(true);
                                s.decrHP();
                                if (s.getHP()<0) {controls[s.getId()]=null;}
                                s.setAttacked(true);
                            } else {
                                s.setStunned(true);
                                s.setAttacked(true);
                            }
                        }
                    }
                }
            }
            //hotfix xd
//            if (spray.getDurability() < 0 ){
//                SoundController.getInstance().play(NO_WEAPON_FILE, NO_WEAPON_FILE, false, 0.5f);
//            }
//            if (spray.getDurability() == 0 ){
//                System.out.println(spray.getDurability());
//                spray.decrDurability();
//            }

        } else if (wep instanceof VacuumModel) {
            VacuumModel vacuum = (VacuumModel) wep;
//            vacuum.decrDurability();
            if (vacuum.getDurability() >= 0){
                SoundController.getInstance().play(VACUUM_FILE, VACUUM_FILE, false, 0.5f);
                for  (Obstacle obj : objects) {
                    if (obj.getName() == "lid") {
                        int horiGap = board.screenToBoardX(avatar.getX()) - board.screenToBoardX(obj.getX());
                        int vertiGap = board.screenToBoardY(avatar.getY()) - board.screenToBoardY(obj.getY());
                        boolean case1 = Math.abs(horiGap) <= vacuum.getRange() && horiGap >= 0 && avatar.isLeft() && Math.abs(vertiGap) <= 1;
                        boolean case2 = Math.abs(horiGap) <= vacuum.getRange() && horiGap <= 0 && avatar.isRight() && Math.abs(vertiGap) <= 1;
                        boolean case3 = Math.abs(vertiGap) <= vacuum.getRange() && vertiGap >= 0 && avatar.isDown() && Math.abs(horiGap) <= 1;
                        boolean case4 = Math.abs(vertiGap) <= vacuum.getRange() && vertiGap <= 0 && avatar.isUp() && Math.abs(horiGap) <= 1;
                        if ((case1)) {
                            obj.setVX(BULLET_SPEED);
                        }
                        if ((case2)) {
                            obj.setVX(-BULLET_SPEED);
                        }
                        if ((case3)) {
                            obj.setVY(BULLET_SPEED);
                        }
                        if ((case4)) {
                            obj.setVY(-BULLET_SPEED);
                        }
                    }
                }
                for (EnemyModel s : enemies){
                    if (!s.isRemoved()) {
                        if (!(s instanceof RobotModel)) {
                            int horiGap = board.screenToBoardX(avatar.getX()) - board.screenToBoardX(s.getX());
                            int vertiGap = board.screenToBoardY(avatar.getY()) - board.screenToBoardY(s.getY());
                            boolean case1 = Math.abs(horiGap) <= vacuum.getRange() && horiGap >= 0 && avatar.isLeft() && Math.abs(vertiGap) <= 1;
                            boolean case2 = Math.abs(horiGap) <= vacuum.getRange() && horiGap <= 0 && avatar.isRight() && Math.abs(vertiGap) <= 1;
                            boolean case3 = Math.abs(vertiGap) <= vacuum.getRange() && vertiGap >= 0 && avatar.isDown() && Math.abs(horiGap) <= 1;
                            boolean case4 = Math.abs(vertiGap) <= vacuum.getRange() && vertiGap <= 0 && avatar.isUp() && Math.abs(horiGap) <= 1;
                            if ((case1) && s.getAttacked() == false) {
                                knockbackForce.set(30f, 0f);
                                s.applyImpulse(knockbackForce);
                                s.setKnockbackTimer(KNOCKBACK_TIMER);
                                s.setStunnedVacuum(true);
                                s.setAttacked(true);
                            }
                            if ((case2)&& s.getAttacked() == false) {
                                knockbackForce.set(-30f, 0f);
                                s.applyImpulse(knockbackForce);
                                s.setKnockbackTimer(KNOCKBACK_TIMER);
                                s.setStunnedVacuum(true);
                                s.setAttacked(true);
                            }
                            if ((case3)&& s.getAttacked() == false) {
                                knockbackForce.set(0f, 30f);
                                s.applyImpulse(knockbackForce);
                                s.setKnockbackTimer(KNOCKBACK_TIMER);
                                s.setStunnedVacuum(true);
                                s.setAttacked(true);
                            }
                            if ((case4)&& s.getAttacked() == false) {
                                knockbackForce.set(0f, -30f);
                                s.applyImpulse(knockbackForce);
                                s.setKnockbackTimer(KNOCKBACK_TIMER);
                                s.setStunnedVacuum(true);
                                s.setAttacked(true);
                            }
                        }
                    }
                }
            }
            //hotfix xd
//            if (vacuum.getDurability() < 0 ){
//                SoundController.getInstance().play(NO_WEAPON_FILE, NO_WEAPON_FILE, false, 0.5f);
//            }
//            if (vacuum.getDurability() == 0 ){
//                vacuum.decrDurability();
//            }

        } else if (wep instanceof LidModel) {
            LidModel lid = (LidModel) wep;
//            System.out.println(lid.getDurability());
            //hotfix xd
//            if (lid.getDurability() < 0 ){
//                SoundController.getInstance().play(NO_WEAPON_FILE, NO_WEAPON_FILE, false, 0.5f);
//            }
//            if (lid.getDurability() == 0 ){
//                lid.decrDurability();
//            }

//            if (lid.getDurability() > 0 && avatar.getHasLid()) {
//                System.out.println("create bullet");
//
//            }
//            else {
////                SoundController.getInstance().play(NO_WEAPON_FILE, NO_WEAPON_FILE, false, 0.5f);
//            }
        }
    }


    /**
     * Callback method for the start of a collision
     *
     * This method is called when we first get a collision between two objects.  We use
     * this method to test if it is the "right" kind of collision.  In particular, we
     * use it to test if we made it to the win door.
     *
     * @param contact The two bodies that collided
     */
    public void beginContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        try {
            Obstacle bd1 = (Obstacle)body1.getUserData();
            Obstacle bd2 = (Obstacle)body2.getUserData();
            for (EnemyModel s : enemies){
                if (bd1.getName().equals("lid") && bd2 == s) {
                    removeLid(bd1,s);
                }
                if (bd2.getName().equals("lid") && bd1 == s) {
                    removeLid(bd2,s);
                }
                if (bd1.getName().equals("lid") && (bd2 != s) && (bd2 != avatar) && (bd2.getName() != "mopCart") ) {
                    //don't drop at mop cart
                    dropLid(bd1);
                }
                if (bd2.getName().equals("lid") && ((bd1 != s)) && (bd1 != avatar) && (bd1.getName() != "mopCart") ) {
                    //don't drop at mop cart
                    dropLid(bd2);
                }
            }

            if (bd1.getName().equals("lid") && (bd2 == avatar) ) {
                removeBullet(bd1);
                avatar.setHasLid(true);
                lidGround = false;
                lidTimer = LID_RANGE;
            } else if (bd2.getName().equals("lid") && (bd1 == avatar) ) {
                removeBullet(bd2);
                avatar.setHasLid(true);
                lidGround = false;
                lidTimer = LID_RANGE;
            }

            if (bd1.getName().equals("slimeball") && bd2 == avatar) {
                if (!bd1.isRemoved()) {
                    gotHit=ticks;
                    if (avatar.getHP() <= 1) { avatar.setRed(false); } //don't be red when dying
                    else { avatar.setRed(true); }
                    avatar.decrHP();
                    removeBullet(bd1);
                    SoundController.getInstance().play(OUCH_FILE, OUCH_FILE,false,EFFECT_VOLUME);
                }
            } else if (bd1.getName().equals("slimeball") && bd2.getName() == "mopCart") {
                //maybe combine this in below if statement, be careful of order or might break
                //do nothing, don't remove bullet if mop cart
            } else if (bd1.getName().equals("slimeball") && !(bd2 instanceof EnemyModel)) {
                removeBullet(bd1);
            }

            if (bd2.getName().equals("slimeball") && bd1 == avatar) {
                if (!bd2.isRemoved()) {
                    gotHit = ticks;
                    if (avatar.getHP() <= 1) { avatar.setRed(false); } //don't be red when dying
                    else { avatar.setRed(true); }
                    avatar.decrHP();
                    removeBullet(bd2);
                    SoundController.getInstance().play(OUCH_FILE, OUCH_FILE,false,EFFECT_VOLUME);
                }
            } else if (bd2.getName().equals("slimeball") && bd1.getName() == "mopCart") {
                //do nothing, don't remove bullet if mop cart
            } else if(bd2.getName().equals("slimeball") && !(bd1 instanceof EnemyModel)) {
                removeBullet(bd2);
            }

            if (bd2.getName().equals("slimeball") && bd1.getName().equals("lid")) {
                removeBullet(bd2);
            }

            if (bd1.getName().equals("slimeball") && bd2.getName().equals("lid")) {
                removeBullet(bd1);
            }

            //Check if avatar has reached a powerup
            if (bd1 == avatar && bd2 == specialHealth) {
                if (avatar.getHP() != avatar.getCurrentMaxHP()) {
                    bd2.markRemoved(true);
                    avatar.setHP(avatar.getCurrentMaxHP()); //full heal
                    SoundController.getInstance().play(RELOAD_FILE, RELOAD_FILE,false,EFFECT_VOLUME);
                }
            } else if (bd2 == avatar && bd1 == specialHealth) {
                if (avatar.getHP() != avatar.getCurrentMaxHP()) {
                    bd1.markRemoved(true);
                    avatar.setHP(avatar.getCurrentMaxHP()); //full heal
                    SoundController.getInstance().play(RELOAD_FILE, RELOAD_FILE, false, EFFECT_VOLUME);
                }
            }

            if (bd1 == avatar && bd2 == specialDurability) {
                if (avatar.getWep1().getDurability() != avatar.getWep1().getMaxDurability() ||
                    avatar.getWep2().getDurability() != avatar.getWep2().getMaxDurability()) {
                    //reload weapons
                    bd2.markRemoved(true);
                    avatar.getWep1().durability = avatar.getWep1().getMaxDurability();
                    avatar.getWep2().durability = avatar.getWep2().getMaxDurability();
                    SoundController.getInstance().play(RELOAD_FILE, RELOAD_FILE,false,EFFECT_VOLUME);
                }
            } else if (bd2 == avatar && bd1 == specialDurability) {
                if (avatar.getWep1().getDurability() != avatar.getWep1().getMaxDurability() ||
                    avatar.getWep2().getDurability() != avatar.getWep2().getMaxDurability()) {
                    //reload weapons
                    bd1.markRemoved(true);
                    avatar.getWep1().durability = avatar.getWep1().getMaxDurability();
                    avatar.getWep2().durability = avatar.getWep2().getMaxDurability();
                    SoundController.getInstance().play(RELOAD_FILE, RELOAD_FILE,false,EFFECT_VOLUME);
                }
            }

                // Check for win / victory condition
            if ((bd1 == avatar   && bd2 == goalDoor) ||
                    (bd1 == goalDoor && bd2 == avatar)) {
                //Perma upgrade player's base HP
                avatar.setBaseHP(avatar.getCurrentMaxHP());
                avatar.setCurrentMaxHP(avatar.getBaseHP());
                //System.out.println("base hp" + avatar.getBaseHP());
                //System.out.println("current max hp" + avatar.getCurrentMaxHP());
                setComplete(true);
                setCameraX(cameraX);
                setCameraY(cameraY);
            }
            //Check if player is at mop cart
            if (bd1 == avatar   && bd2.getName() == "mopCart") {
                int mc_i = (int) bd2.getDensity();

                //check for which one it is by parsing last number of name string?
                //get number of mop cart(which one are you at)
                //pass in with setAtMopCart
                //when checking in draw update loop,
                //see if you upgraded at that on before with MopCartUpgradedBefore list
                avatar.setAtMopCart(true, mc_i);
            }
            else if (bd1.getName() == "mopCart" && bd2 == avatar) {
                int mc_i = (int) bd1.getDensity();
                avatar.setAtMopCart(true, mc_i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Callback method for the start of a collision
     *
     * This method is called when two objects cease to touch.  The main use of this method
     * is to determine when the characer is NOT on the ground.  This is how we prevent
     * double jumping.
     */
    public void endContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        Obstacle bd1 = (Obstacle) body1.getUserData();
        Obstacle bd2 = (Obstacle) body2.getUserData();

        if ((bd1 == avatar   && bd2.getName() == "mopCart") ||
                (bd1.getName() == "mopCart" && bd2 == avatar)) {
            avatar.setAtMopCart(false, 0);
        }

        if (bd2.getName().equals("slimeball") && bd1.getName().equals("lid")) {
            bd1.setVX(lidVel.x);
            bd1.setVY(lidVel.y);
            //System.out.println("set lidvel");
            //System.out.println(bd1.getVX());
            //System.out.println(bd1.getVY());
            //System.out.println("");

        }


        if (bd1.getName().equals("slimeball") && bd2.getName().equals("lid")) {
            bd2.setVX(lidVel.x);
            bd2.setVY(lidVel.y);
            /*System.out.println("set lidvel");
            System.out.println(bd2.getVX());
            System.out.println(bd2.getVY());
            System.out.println("");*/
        }

        /*if ((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
                (avatar.getSensorName().equals(fd1) && avatar != bd2)) {
            sensorFixtures.remove(avatar == bd1 ? fix2 : fix1);
        }*/
    }


    public void draw(float delta) {
        GameCanvas canvas = super.getCanvas();

        canvas.clear();

        canvas.begin();

        board.draw(canvas, tiles);

        for(Obstacle obj : objects) {
            obj.draw(canvas);
        }

        canvas.end();
        // Now draw the shadows
        if (rayhandler != null && lightIsActive) {
            rayhandler.render();
        }
        canvas.begin();
        displayFont.setColor(Color.WHITE);

        //Draw Enemy Health
        displayFont.getData().setScale(0.5f);
        for (EnemyModel s : enemies) {
            if (!(s.isRemoved())) {
                int enemy_hp = s.getHP();
                if (enemy_hp < 0) {enemy_hp = 0;}

                if (s instanceof RobotModel) {
                    //draw 5 hp for robots, 3 for everyone else
                    canvas.draw(allEnemyHeartTextures[1][5 - enemy_hp],
                            (s.getX() * scale.x) - 30, ((s.getY()) * scale.y) + 10);
                }
                else {
                    canvas.draw(allEnemyHeartTextures[0][3 - enemy_hp],
                            (s.getX() * scale.x) - 30, ((s.getY()) * scale.y) + 10);
                }
            }
        }
        displayFont.getData().setScale(1.0f);

        /* SHOW HP */
        int currentHP = avatar.getHP();
        int maxHP = avatar.getCurrentMaxHP();
        int times_improved = (avatar.getCurrentMaxHP() - 5);
        if (currentHP < 0){ currentHP = 0; } //prevent array exception
        canvas.draw(allHeartTextures[times_improved][(maxHP - currentHP)],
                (cameraX - 490), (cameraY + 210));

        // DRAW ACTIVE WEAPON UI
        String wep1FileName = avatar.getWep1().getName();
        String wep2FileName = avatar.getWep2().getName();

        TextureRegion[] wep1Textures = wep_to_bartexture.get(wep1FileName);
        int durability1 = avatar.getWep1().getDurability();
        int maxDurability1 = avatar.getWep1().getMaxDurability();
        if (durability1 < 0){ durability1 = 0; } //fix for negative durability
        canvas.draw(wep1Textures[maxDurability1 - durability1],
                (cameraX - 490), (cameraY + 140));

        TextureRegion[] wep2Textures = wep_to_bartexture.get(wep2FileName);
        int durability2 = avatar.getWep2().getDurability();
        int maxDurability2 = avatar.getWep2().getMaxDurability();
        if (durability2 < 0){ durability2 = 0; } //fix for negative durability
        canvas.draw(wep2Textures[maxDurability2 - durability2],
                (cameraX - 450), (cameraY + 90));

        if (avatar.isAtMopCart()){
            //DRAW MOP CART BACKGROUND
            canvas.draw(mopcartBackgroundTexture, (cameraX + 350), (cameraY + 140));
            //change sy to increase height of black box
            displayFont.getData().setScale(0.5f);

            //RETRIEVE MOP CART WEAPONS
            String[] draw_mopcart = new String[2];
            int unused_indexer = 0;
            for (String wep: mopcart_menu) {
                //if weapon not in use
                draw_mopcart[unused_indexer] = wep;
                unused_indexer += 1;
            }
            //draw unused weapons currently in cart
            Texture unused_wep1 = wep_to_texture.get(draw_mopcart[0]);
            Texture unused_wep2 = wep_to_texture.get(draw_mopcart[1]);
            canvas.draw(unused_wep1, (cameraX + 360), (cameraY + 180));
            canvas.draw(unused_wep2, (cameraX + 435), (cameraY + 180));

            //DRAW MOPCART INDEX
            int current_xlocation = mopcart_index_xlocation[mopcart_index];
            canvas.draw(mopcartIndexTexture, (cameraX + current_xlocation), (cameraY + 145));
        }
        canvas.end();

        super.draw(delta);
    }

    /** Unused ContactListener method */
    public void postSolve(Contact contact, ContactImpulse impulse) {}
    /** Unused ContactListener method */
    public void preSolve(Contact contact, Manifold oldManifold) {}

    public enum StateJoe {STANDING, RUNNINGR, RUNNINGU ,RUNNINGD,
        MOPR, MOPL, MOPU, MOPD,
        LIDR,LIDL,LIDU,LIDD,
        SPRAYR,SPRAYL,SPRAYU,SPRAYD,
        VACUUMR,VACUUML,VACUUMU,VACUUMD,
        DEATH}

    public enum StateMad {STANDING, RUNNINGR, RUNNINGU ,RUNNINGD,
        ATTACKR,ATTACKL,ATTACKU,ATTACKD,DEATH,STUN}

    public enum StateRobot {STANDING, RUNNINGR, RUNNINGU ,RUNNINGD,
        ATTACKR,ATTACKL,ATTACKU,ATTACKD,DEATH,STUN}

    public enum StateSlime {STANDING, RUNNINGR, RUNNINGU ,RUNNINGD,
        ATTACKR,ATTACKL,ATTACKU,ATTACKD,DEATH,STUN}

    public enum StateTurret{STANDING, RUNNINGR, RUNNINGU ,RUNNINGD,
        ATTACKR,ATTACKL,ATTACKU,ATTACKD,DEATH,STUN}

    public enum StateLizard {STANDING, RUNNINGR, RUNNINGU ,RUNNINGD,
        ATTACKR,ATTACKL,ATTACKU,ATTACKD,DEATH,STUN}

    public StateJoe currentState;
    public StateJoe previousState;

    public StateMad currentStateM;
    public StateMad previousStateM;

    public StateRobot currentStateR;
    public StateRobot previousStateR;

    public StateSlime currentStateS;
    public StateSlime previousStateS;

    public StateSlime currentStateT;
    public StateSlime previousStateT;

    public StateLizard currentStateL;
    public StateLizard previousStateL;


    public StateJoe getStateJoe(){

        if (avatar.getHP() <= 0){
            //System.out.println("joedeath");
            if (joeDeathTimer == 0) {
                joeDeathTimer = DEATH_ANIMATION_TIME;
            }
            return StateJoe.DEATH;
        }
        else if ((avatar.isRight() && !avatar.isAtMopCart() && avatar.getWep1().getName() == "mop"
                && !(avatar.getMovementX() < 0)&& avatar.isFacingRight() && avatar.getWep1().durability > 0)||
                ((avatar.isLeft() && !avatar.isAtMopCart() && avatar.getWep1().getName() == "mop")
                        && avatar.getMovementX() < 0 && avatar.getWep1().durability > 0)||
                ((avatar.isLeft() && !avatar.isAtMopCart()&& avatar.getWep1().getName() == "mop")
                        && avatar.getMovementX() == 0 && !avatar.isFacingRight() && avatar.getWep1().durability > 0))
        {
            if (attackTimer == 0) {
                attackTimer = ATTACK_DURATION_MOP;
            }
            return StateJoe.MOPR;
        }
        else if ((avatar.isLeft()&& !avatar.isAtMopCart() && avatar.getWep1().getName() == "mop"
                && avatar.getWep1().durability > 0 ) ||
                ((avatar.isRight() && !avatar.isAtMopCart() && avatar.getWep1().getName() == "mop")
                        && avatar.getMovementX() < 0 && avatar.getWep1().durability > 0)||
                ((avatar.isRight() && !avatar.isAtMopCart()&& avatar.getWep1().getName() == "mop")
                        && avatar.getMovementX() == 0 && !avatar.isFacingRight() && avatar.getWep1().durability > 0)){
            if (attackTimer == 0) {
                attackTimer = ATTACK_DURATION_MOP;
            }
            return StateJoe.MOPL;
        }
        else if (avatar.isUp() && !avatar.isAtMopCart()&& avatar.getWep1().getName() == "mop"
                && avatar.getWep1().durability > 0){
            if (attackTimer == 0) {
                attackTimer = ATTACK_DURATION_MOP;
            }
            return StateJoe.MOPU;
        }
        else if (avatar.isDown()&& !avatar.isAtMopCart()&& avatar.getWep1().getName() == "mop"
                && avatar.getWep1().durability > 0){
            if (attackTimer == 0) {
                attackTimer = ATTACK_DURATION_MOP;
            }
            return StateJoe.MOPD;
        }
        else if ((avatar.isRight() && !avatar.isAtMopCart()&& avatar.getWep1().getName() == "lid"
                && !(avatar.getMovementX() < 0) && avatar.isFacingRight()
                && avatar.getWep1().durability > 0 && avatar.getHasLid())||
                ((avatar.isLeft() && !avatar.isAtMopCart()&& avatar.getWep1().getName() == "lid")
                        && avatar.getMovementX() < 0 && avatar.getWep1().durability > 0 && avatar.getHasLid())||
                ((avatar.isLeft() && !avatar.isAtMopCart()&& avatar.getWep1().getName() == "lid")
                        && avatar.getMovementX() == 0 && !avatar.isFacingRight()
                        && avatar.getWep1().durability > 0 && avatar.getHasLid())){
            if (attackTimer == 0) {
                attackTimer = ATTACK_DURATION_LID;
                avatar.getWep1().decrDurability();
                createBullet(avatar);
                avatar.setHasLid(false);
            }
            return StateJoe.LIDR;
        }
        else if ((avatar.isLeft()&& !avatar.isAtMopCart() && avatar.getWep1().getName() == "lid"
                && avatar.getWep1().durability > 0 && avatar.getHasLid() ) ||
                ((avatar.isRight() && !avatar.isAtMopCart() && avatar.getWep1().getName() == "lid")
                        && avatar.getMovementX() < 0 && avatar.getWep1().durability > 0 && avatar.getHasLid())||
                ((avatar.isRight() && !avatar.isAtMopCart()&& avatar.getWep1().getName() == "lid")
                        && avatar.getMovementX() == 0 && !avatar.isFacingRight() && avatar.getWep1().durability > 0
                        && avatar.getHasLid())){
            if (attackTimer == 0) {
                attackTimer = ATTACK_DURATION_LID;
                avatar.getWep1().decrDurability();
                createBullet(avatar);
                avatar.setHasLid(false);
            }
            return StateJoe.LIDL;
        }
        else if (avatar.isUp()&& !avatar.isAtMopCart() && avatar.getWep1().getName() == "lid"
                && avatar.getWep1().durability > 0 && avatar.getHasLid()){
            if (attackTimer == 0) {
                attackTimer = ATTACK_DURATION_LID;
                avatar.getWep1().decrDurability();
                createBullet(avatar);
                avatar.setHasLid(false);
            }
            return StateJoe.LIDU;
        }
        else if (avatar.isDown()&& !avatar.isAtMopCart()&& avatar.getWep1().getName() == "lid"
                && avatar.getWep1().durability > 0 && avatar.getHasLid()){
            if (attackTimer == 0) {
                attackTimer = ATTACK_DURATION_LID;
                avatar.getWep1().decrDurability();
                createBullet(avatar);
                avatar.setHasLid(false);
            }
            return StateJoe.LIDD;
        }
        else if ((avatar.isRight() && !avatar.isAtMopCart()&& avatar.getWep1().getName() == "spray"
                && !(avatar.getMovementX() < 0) && avatar.isFacingRight()
                && avatar.getWep1().durability > 0 )||
                ((avatar.isLeft() && !avatar.isAtMopCart()&& avatar.getWep1().getName() == "spray")
                        && avatar.getMovementX() < 0 && avatar.getWep1().durability > 0 )||
                ((avatar.isLeft() && !avatar.isAtMopCart()&& avatar.getWep1().getName() == "spray")
                        && avatar.getMovementX() == 0 && !avatar.isFacingRight()
                        && avatar.getWep1().durability > 0)){
            if (attackTimer == 0) {
                attackTimer = ATTACK_DURATION_SPRAY;
                avatar.getWep1().decrDurability();
            }
            return StateJoe.SPRAYR;
        }
        else if ((avatar.isLeft()&& !avatar.isAtMopCart() && avatar.getWep1().getName() == "spray"
                && avatar.getWep1().durability > 0  ) ||
                ((avatar.isRight() && !avatar.isAtMopCart() && avatar.getWep1().getName() == "spray")
                        && avatar.getMovementX() < 0 && avatar.getWep1().durability > 0 )||
                ((avatar.isRight() && !avatar.isAtMopCart()&& avatar.getWep1().getName() == "spray")
                        && avatar.getMovementX() == 0 && !avatar.isFacingRight() && avatar.getWep1().durability > 0
                )){
            if (attackTimer == 0) {
                attackTimer = ATTACK_DURATION_SPRAY;
                avatar.getWep1().decrDurability();
            }
            return StateJoe.SPRAYL;
        }
        else if (avatar.isUp()&& !avatar.isAtMopCart() && avatar.getWep1().getName() == "spray"
                && avatar.getWep1().durability > 0 ){
            if (attackTimer == 0) {
                attackTimer = ATTACK_DURATION_SPRAY;
                avatar.getWep1().decrDurability();
            }
            return StateJoe.SPRAYU;
        }
        else if (avatar.isDown()&& !avatar.isAtMopCart()&& avatar.getWep1().getName() == "spray"
                && avatar.getWep1().durability > 0 ){
            if (attackTimer == 0) {
                attackTimer = ATTACK_DURATION_SPRAY;
                avatar.getWep1().decrDurability();
            }
            return StateJoe.SPRAYD;
        }
        else if ((avatar.isRight() && !avatar.isAtMopCart()&& avatar.getWep1().getName() == "vacuum"
                && !(avatar.getMovementX() < 0) && avatar.isFacingRight()
                && avatar.getWep1().durability > 0 )||
                ((avatar.isLeft() && !avatar.isAtMopCart()&& avatar.getWep1().getName() == "vacuum")
                        && avatar.getMovementX() < 0 && avatar.getWep1().durability > 0 )||
                ((avatar.isLeft() && !avatar.isAtMopCart()&& avatar.getWep1().getName() == "vacuum")
                        && avatar.getMovementX() == 0 && !avatar.isFacingRight()
                        && avatar.getWep1().durability > 0)){
            if (attackTimer == 0) {
                attackTimer = ATTACK_DURATION_VACUUM;
                avatar.getWep1().decrDurability();
            }
            return StateJoe.VACUUMR;
        }
        else if ((avatar.isLeft()&& !avatar.isAtMopCart() && avatar.getWep1().getName() == "vacuum"
                && avatar.getWep1().durability > 0  ) ||
                ((avatar.isRight() && !avatar.isAtMopCart() && avatar.getWep1().getName() == "vacuum")
                        && avatar.getMovementX() < 0 && avatar.getWep1().durability > 0 )||
                ((avatar.isRight() && !avatar.isAtMopCart()&& avatar.getWep1().getName() == "vacuum")
                        && avatar.getMovementX() == 0 && !avatar.isFacingRight() && avatar.getWep1().durability > 0
                )){
            if (attackTimer == 0) {
                attackTimer = ATTACK_DURATION_VACUUM;
                avatar.getWep1().decrDurability();
            }
            return StateJoe.VACUUML;
        }
        else if (avatar.isUp()&& !avatar.isAtMopCart() && avatar.getWep1().getName() == "vacuum"
                && avatar.getWep1().durability > 0 ){
            if (attackTimer == 0) {
                attackTimer = ATTACK_DURATION_VACUUM;
                avatar.getWep1().decrDurability();
            }
            return StateJoe.VACUUMU;
        }
        else if (avatar.isDown()&& !avatar.isAtMopCart()&& avatar.getWep1().getName() == "vacuum"
                && avatar.getWep1().durability > 0 ){
            if (attackTimer == 0) {
                attackTimer = ATTACK_DURATION_VACUUM;
                avatar.getWep1().decrDurability();
            }
            return StateJoe.VACUUMD;
        }
        else if ((avatar.getMovementX()!=0 && avatar.getMovementY()==0 && attackTimer == 0)||(avatar.getMovementX()!=0 && avatar.getMovementY()!=0)&& attackTimer == 0){
            return StateJoe.RUNNINGR;
        }
        else if (avatar.getMovementX()==0 && avatar.getMovementY()>0&& attackTimer == 0) {
            return StateJoe.RUNNINGU;
        }
        else if (avatar.getMovementX()==0 && avatar.getMovementY()<0&& attackTimer == 0) {
            return StateJoe.RUNNINGD;
        }
        else if (avatar.getMovementX()==0 && avatar.getMovementY()== 0 && attackTimer == 0) {
            return StateJoe.STANDING;
        }
        else
            return previousState;
    }

    public TextureRegion getFrameJoe (float dt){
        currentState = getStateJoe();
        TextureRegion region;
        switch (currentState){
            case RUNNINGR:
                region = joeRunR.getKeyFrame(stateTimer,true);
                break;
            case RUNNINGU:
                region = joeRunU.getKeyFrame(stateTimer, true);
                break;
            case RUNNINGD:
                region = joeRunD.getKeyFrame(stateTimer,true);
                break;
            case STANDING:
                region = joeStand.getKeyFrame(stateTimer,true);
                break;
            case MOPR:
                region = joeMopR.getKeyFrame(stateTimer,false);
                break;
            case MOPL:
                region = joeMopL.getKeyFrame(stateTimer,false);
                break;
            case MOPU:
                region = joeMopU.getKeyFrame(stateTimer,false);
                break;
            case MOPD:
                region = joeMopD.getKeyFrame(stateTimer,false);
                break;
            case LIDR:
                region = joeLidR.getKeyFrame(stateTimer,false);
                break;
            case LIDL:
                region = joeLidL.getKeyFrame(stateTimer,false);
                break;
            case LIDU:
                region = joeLidU.getKeyFrame(stateTimer,false);
                break;
            case LIDD:
                region = joeLidD.getKeyFrame(stateTimer,false);
                break;
            case SPRAYR:
                region = joeSprayR.getKeyFrame(stateTimer,false);
                break;
            case SPRAYL:
                region = joeSprayL.getKeyFrame(stateTimer,false);
                break;
            case SPRAYU:
                region = joeSprayU.getKeyFrame(stateTimer,false);
                break;
            case SPRAYD:
                region = joeSprayD.getKeyFrame(stateTimer,false);
                break;
            case VACUUMR:
                region = joeVacuumR.getKeyFrame(stateTimer,false);
                break;
            case VACUUML:
                region = joeVacuumL.getKeyFrame(stateTimer,false);
                break;
            case VACUUMU:
                region = joeVacuumU.getKeyFrame(stateTimer,false);
                break;
            case VACUUMD:
                region = joeVacuumD.getKeyFrame(stateTimer,false);
                break;
            case DEATH:
                region = joeDeath.getKeyFrame(stateTimer,false);
                break;
            default:
                region = joeStand.getKeyFrame(stateTimer,true);
                break;
        }

        if ((currentState == StateJoe.MOPR)||(currentState == StateJoe.MOPL) || (currentState == StateJoe.MOPD)||(currentState == StateJoe.MOPU)||
                (currentState == StateJoe.LIDR)||(currentState == StateJoe.LIDU)||(currentState == StateJoe.LIDD)||(currentState == StateJoe.LIDL)||
                (currentState == StateJoe.SPRAYR)||(currentState == StateJoe.SPRAYU)||(currentState == StateJoe.SPRAYD)||(currentState == StateJoe.SPRAYL) ||
                (currentState == StateJoe.VACUUMR)||(currentState == StateJoe.VACUUMU)||(currentState == StateJoe.VACUUMD)||(currentState == StateJoe.VACUUML)) {

            attack(avatar.getWep1());

            if ((previousState == currentState) && attackTimer > 0) {
                if (dt > attackTimer) {
                    attackTimer = 0;
                }else {
                    attackTimer -= dt;
                }
            }
        }
        else {
            for (EnemyModel s : enemies) {
                s.setAttacked(false);
            }
            }


        stateTimer = currentState == previousState ? stateTimer + dt : 0;
        previousState = currentState;
        return region;
    }
    public StateMad getStateMad(EnemyModel s) {
        double verticalAttackBoundary = 0.5;
        if (s.getHP()<= 0) {
            controls[s.getId()]=null;
            return StateMad.DEATH;
        }
        else if (s.getStunned() == true || s.getStunnedVacuum() == true) {
            return StateMad.STUN;
        }
        else if (s.getAttackAnimationFrame() > 0 && avatar.getY() > s.getY() &&
                Math.abs(avatar.getX() - s.getX()) < verticalAttackBoundary) {
            return StateMad.ATTACKU;
        }
        else if (s.getAttackAnimationFrame() > 0 && avatar.getY() < s.getY() &&
                Math.abs(avatar.getX() - s.getX()) < verticalAttackBoundary) {
            return StateMad.ATTACKD;
        }
        else if (((s.getAttackAnimationFrame() > 0 && avatar.getX() > s.getX())&& scientistMovedLeft == false)||
                (s.getAttackAnimationFrame() > 0 && avatar.getX() < s.getX())&& scientistMovedLeft == true){
            //System.out.println("attack right" + "" + scientistMovedLeft);
            return StateMad.ATTACKR;
        }
        else if (((s.getAttackAnimationFrame() > 0 && avatar.getX() > s.getX())&& scientistMovedLeft == true)||
                (s.getAttackAnimationFrame() > 0 && avatar.getX() < s.getX())&& scientistMovedLeft == false){
            //System.out.println("attack left" + "" + scientistMovedLeft);
            return StateMad.ATTACKL;
        }
        else if (s.getMovementX() > 0) {
            scientistMovedLeft = false;
            return StateMad.RUNNINGR;
        }
        else if (s.getMovementX() < 0) {
            scientistMovedLeft = true;
            return StateMad.RUNNINGR;
        }
        else if (s.getMovementY() > 0) {
            return StateMad.RUNNINGU;
        }
        else if (s.getMovementY() < 0) {
            return StateMad.RUNNINGD;
        }

        else {
            return StateMad.STANDING;
        }

    }
    public TextureRegion getFrameScientist (float dt , EnemyModel s){
        stateTimerS = s.getStateTimer();
        ((ScientistModel)s).state = getStateMad(s);
        TextureRegion region;
        switch (((ScientistModel)s).state){
            case RUNNINGR:
                region = madRunR.getKeyFrame(stateTimerS,true);
                break;
            case RUNNINGU:
                region = madRunU.getKeyFrame(stateTimerS,true);
                break;
            case RUNNINGD:
                region = madRunD.getKeyFrame(stateTimerS,true);
                break;
            case ATTACKR:
                region = madAttackR.getKeyFrame(stateTimerS,false);
                break;
            case ATTACKL:
                region = madAttackL.getKeyFrame(stateTimerS,false);
                break;
            case ATTACKU:
                region = madAttackU.getKeyFrame(stateTimerS,false);
                break;
            case ATTACKD:
                region = madAttackD.getKeyFrame(stateTimerS,false);
                break;
            case DEATH:
                region = madDeath.getKeyFrame(stateTimerS,false);
                break;
            case STUN:
                region = madStun.getKeyFrame(stateTimerS,true);
                break;
            default:
                region = madStand.getKeyFrame(stateTimerS,true);
                break;
        }
        if (((ScientistModel)s).state ==((ScientistModel)s).previousState ){
            s.setStateTimer(s.getStateTimer()+dt);
        }
        else {
            s.setStateTimer(0);
        }
        ((ScientistModel)s).previousState = ((ScientistModel)s).state;
        return region;
    }
    public StateRobot getStateRobot(EnemyModel s) {
        double verticalAttackBoundary = 0.5;
        if (s.getHP()<= 0) {
            controls[s.getId()]=null;
            return StateRobot.DEATH;
        }
        else if (s.getStunned() == true || s.getStunnedVacuum() == true) {
            return StateRobot.STUN;
        }
        else if (s.getAttackAnimationFrame() > 0 && avatar.getY() > s.getY() &&
                Math.abs(avatar.getX() - s.getX()) < verticalAttackBoundary) {
            return StateRobot.ATTACKU;
        }
        else if (s.getAttackAnimationFrame() > 0 && avatar.getY() < s.getY() &&
                Math.abs(avatar.getX() - s.getX()) < verticalAttackBoundary) {
            return StateRobot.ATTACKD;
        }
        else if (((s.getAttackAnimationFrame() > 0 && avatar.getX() > s.getX())&& robotMovedLeft == false)||
                (s.getAttackAnimationFrame() > 0 && avatar.getX() < s.getX())&& robotMovedLeft == true){
            return StateRobot.ATTACKR;
        }
        else if (((s.getAttackAnimationFrame() > 0 && avatar.getX() > s.getX())&& robotMovedLeft == true)||
                (s.getAttackAnimationFrame() > 0 && avatar.getX() < s.getX())&& robotMovedLeft == false){
            return StateRobot.ATTACKR;
        }
        else if (s.getMovementX() > 0) {
            robotMovedLeft = false;
            return StateRobot.RUNNINGR;
        }
        else if (s.getMovementX() < 0) {
            robotMovedLeft = true;
            return StateRobot.RUNNINGR;
        }
        else if (s.getMovementY() > 0) {
            return StateRobot.RUNNINGU;
        } else if (s.getMovementY() < 0) {
            return StateRobot.RUNNINGD;
        }

        else {
            return StateRobot.STANDING;
        }

    }
    public TextureRegion getFrameRobot (float dt , EnemyModel s){
        stateTimerR = s.getStateTimer();
        ((RobotModel)s).state = getStateRobot(s);
        TextureRegion region;
        switch ( ((RobotModel)s).state ){
            case RUNNINGR:
                region = robotRunR.getKeyFrame(stateTimerR,true);
                break;
            case RUNNINGU:
                region = robotRunU.getKeyFrame(stateTimerR,true);
                break;
            case RUNNINGD:
                region = robotRunD.getKeyFrame(stateTimerR,true);
                break;
            case ATTACKL:
                region = robotAttackL.getKeyFrame(stateTimerR,false);
                break;
            case ATTACKR:
                region = robotAttackR.getKeyFrame(stateTimerR,false);
                break;
            case ATTACKU:
                region = robotAttackU.getKeyFrame(stateTimerR,false);
                break;
            case ATTACKD:
                region = robotAttackD.getKeyFrame(stateTimerR,false);
                break;
            case DEATH:
                region = robotDeath.getKeyFrame(stateTimerR,false);
                break;
            case STUN:
                region = robotStun.getKeyFrame(stateTimerR,true);
                break;
            default:
                region = robotStand.getKeyFrame(stateTimerR,true);
                break;
        }
        if (((RobotModel)s).state ==((RobotModel)s).previousState ){
            s.setStateTimer(s.getStateTimer()+dt);
        }
        else {
            s.setStateTimer(0);
        }
        ((RobotModel)s).previousState = ((RobotModel)s).state;
        return region;
    }
    public StateSlime getStateSlime(EnemyModel s) {
        double verticalAttackBoundary = 1;
        if (s.getHP()<= 0) {
            controls[s.getId()]=null;
            return StateSlime.DEATH;
        }
        else if (s.getStunned() == true || s.getStunnedVacuum() == true) {
            return StateSlime.STUN;
        }
        else if (s.getAttackAnimationFrame() > 0 && avatar.getY() > s.getY() &&
                Math.abs(avatar.getX() - s.getX()) < verticalAttackBoundary) {
            return StateSlime.ATTACKU;
        }
        else if (s.getAttackAnimationFrame() > 0 && avatar.getY() < s.getY() &&
                Math.abs(avatar.getX() - s.getX()) < verticalAttackBoundary) {
            return StateSlime.ATTACKD;
        }
        else if (((s.getAttackAnimationFrame() > 0 && avatar.getX() > s.getX())&& slimeMovedLeft == false)||
                (s.getAttackAnimationFrame() > 0 && avatar.getX() < s.getX())&& slimeMovedLeft == true){
            //System.out.println("slime attacking left");
            return StateSlime.ATTACKR;
        }
        else if (((s.getAttackAnimationFrame() > 0 && avatar.getX() > s.getX())&& slimeMovedLeft == true)||
                (s.getAttackAnimationFrame() > 0 && avatar.getX() < s.getX())&& slimeMovedLeft == false){
            //System.out.println("slime attacking right");
            return StateSlime.ATTACKL;
        }
        else if (s.getMovementX() > 0) {
//            System.out.println("Slime run right");
            slimeMovedLeft = false;
            return StateSlime.RUNNINGR;
        }
        else if (s.getMovementX() < 0) {
//            System.out.println("Slime run left");
            slimeMovedLeft = true;
            return StateSlime.RUNNINGR;
        }
        else if (s.getMovementY() > 0) {
            return StateSlime.RUNNINGU;
        } else if (s.getMovementY() < 0) {
            return StateSlime.RUNNINGD;
        }
        else {
            return StateSlime.STANDING;
        }

    }
    public TextureRegion getFrameSlime (float dt , EnemyModel s){
        stateTimerS = s.getStateTimer();
        ((SlimeModel)s).state = getStateSlime(s);
        TextureRegion region;
        switch (((SlimeModel)s).state){
            case RUNNINGR:
                region = slimeRunR.getKeyFrame(stateTimerS,true);
                break;
            case RUNNINGU:
                region = slimeRunU.getKeyFrame(stateTimerS,true);
                break;
            case RUNNINGD:
                region = slimeRunD.getKeyFrame(stateTimerS,true);
                break;
            case ATTACKL:
                region = slimeAttackL.getKeyFrame(stateTimerS,false);
                break;
            case ATTACKR:
                region = slimeAttackR.getKeyFrame(stateTimerS,false);
                break;
            case ATTACKU:
                region = slimeAttackU.getKeyFrame(stateTimerS,false);
                break;
            case ATTACKD:
                region = slimeAttackD.getKeyFrame(stateTimerS,false);
                break;
            case DEATH:
                region = slimeDeath.getKeyFrame(stateTimerS,false);
                break;
            case STUN:
                region = slimeStun.getKeyFrame(stateTimerS,true);
                break;
            default:
                region = slimeStand.getKeyFrame(stateTimerS,true);
                break;
        }
        if (((SlimeModel)s).state ==((SlimeModel)s).previousState ){
            s.setStateTimer(s.getStateTimer()+dt);
        }
        else {
            s.setStateTimer(0);
        }
        ((SlimeModel)s).previousState = ((SlimeModel)s).state;
        return region;
    }

    public StateTurret getStateTurret(EnemyModel s) {
        //USE THE TURRET DIRECTIONS ARRAY LIST TO GET REAL DIRECTIONS IN THE FUTURE

        double verticalAttackBoundary = 1;
        if (s.getHP()<= 0) {
            controls[s.getId()]=null;
            return StateTurret.DEATH;
        }
        else if (s.getStunned() == true || s.getStunnedVacuum() == true) {
            return StateTurret.STUN;
        }
        else if (s.getAttackAnimationFrame() > 0 && avatar.getY() > s.getY() &&
                Math.abs(avatar.getX() - s.getX()) < verticalAttackBoundary){
            return StateTurret.ATTACKU;
        }
        else if (s.getAttackAnimationFrame() > 0 && avatar.getY() < s.getY() &&
                Math.abs(avatar.getX() - s.getX()) < verticalAttackBoundary){
            return StateTurret.ATTACKD;
        }
        else if (((s.getAttackAnimationFrame() > 0 && avatar.getX() > s.getX())&& slimeMovedLeft == false)||
                (s.getAttackAnimationFrame() > 0 && avatar.getX() < s.getX())&& slimeMovedLeft == true){
            //System.out.println("slime attacking left");
            return StateTurret.ATTACKR;
        }
        else if (((s.getAttackAnimationFrame() > 0 && avatar.getX() > s.getX())&& slimeMovedLeft == true)||
                (s.getAttackAnimationFrame() > 0 && avatar.getX() < s.getX())&& slimeMovedLeft == false){
            //System.out.println("slime attacking right");
            return StateTurret.ATTACKL;
        }
        else if (s.getMovementX() > 0) {
            //System.out.println("Slime run right");
            slimeMovedLeft = false;
            return StateTurret.RUNNINGR;
        }
        else if (s.getMovementX() < 0) {
            //System.out.println("Slime run left");
            slimeMovedLeft = true;
            return StateTurret.RUNNINGR;
        }
        else if (s.getMovementY() > 0) {
            return StateTurret.RUNNINGU;
        } else if (s.getMovementY() < 0) {
            return StateTurret.RUNNINGD;
        }
        else {
            return StateTurret.STANDING;
        }

    }
    public TextureRegion getFrameTurret(float dt , EnemyModel s){
        stateTimerT = s.getStateTimer();
        ((TurretModel)s).state = getStateTurret(s);
        TextureRegion region;
        switch (((TurretModel)s).state){
            case RUNNINGR:
                region = turretRunR.getKeyFrame(stateTimerT,true);
                break;
            case RUNNINGU:
                region =  turretRunU.getKeyFrame(stateTimerT,true);
                break;
            case RUNNINGD:
                region =  turretRunD.getKeyFrame(stateTimerT,true);
                break;
            case ATTACKL:
                region =  turretAttackL.getKeyFrame(stateTimerT,false);
                break;
            case ATTACKR:
                region =  turretAttackR.getKeyFrame(stateTimerT,false);
                break;
            case ATTACKU:
                region =  turretAttackU.getKeyFrame(stateTimerT,false);
                break;
            case ATTACKD:
                region = turretAttackD.getKeyFrame(stateTimerT,false);
                break;
            case DEATH:
                region =  turretDeath.getKeyFrame(stateTimerT,false);
                break;
            case STUN:
                region =  turretStun.getKeyFrame(stateTimerT,true);
                break;
            default:
                region =  turretStand.getKeyFrame(stateTimerT,true);
                break;
        }
        if (((TurretModel)s).state ==((TurretModel)s).previousState ){
            s.setStateTimer(s.getStateTimer()+dt);
        }
        else {
            s.setStateTimer(0);
        }
        ((TurretModel)s).previousState = ((TurretModel)s).state;
        return region;
    }
    public StateLizard getStateLizard(EnemyModel s) {
        double verticalAttackBoundary = 0.8;
        if (s.getHP()<= 0) {
            controls[s.getId()]=null;
            return StateLizard.DEATH;
        }
        else if (s.getStunned() == true || s.getStunnedVacuum() == true) {
            return StateLizard.STUN;
        }
        else if (s.getAttackAnimationFrame() > 0 && avatar.getY() > s.getY() &&
                Math.abs(avatar.getX() - s.getX()) < verticalAttackBoundary){
            return StateLizard.ATTACKU;
        }
        else if (s.getAttackAnimationFrame() > 0 && avatar.getY() < s.getY() &&
                Math.abs(avatar.getX() - s.getX()) < verticalAttackBoundary){
            return StateLizard.ATTACKD;
        }
        else if (((s.getAttackAnimationFrame() > 0 && avatar.getX() > s.getX())&& lizardMovedLeft == false)||
                (s.getAttackAnimationFrame() > 0 && avatar.getX() < s.getX())&& lizardMovedLeft == true){
            return StateLizard.ATTACKR;
        }
        else if (((s.getAttackAnimationFrame() > 0 && avatar.getX() > s.getX())&& lizardMovedLeft == true)||
                (s.getAttackAnimationFrame() > 0 && avatar.getX() < s.getX())&& lizardMovedLeft == false){
            return StateLizard.ATTACKL;
        }
        else if (s.getMovementX() > 0) {
            lizardMovedLeft = false;
            return StateLizard.RUNNINGR;
        }
        else if (s.getMovementX() < 0) {
            lizardMovedLeft = true;
            return StateLizard.RUNNINGR;
        }
        else if (s.getMovementY() > 0) {
            return StateLizard.RUNNINGU;
        } else if (s.getMovementY() < 0) {
            return StateLizard.RUNNINGD;
        }

        else {
            return StateLizard.STANDING;
        }

    }
    public TextureRegion getFrameLizard (float dt , EnemyModel s){
        stateTimerS = s.getStateTimer();
        ((LizardModel)s).state = getStateLizard(s);
        TextureRegion region;
        switch (((LizardModel)s).state){
            case RUNNINGR:
                region = lizardRunR.getKeyFrame(stateTimerS,true);
                break;
            case RUNNINGU:
                region = lizardRunU.getKeyFrame(stateTimerS,true);
                break;
            case RUNNINGD:
                region = lizardRunD.getKeyFrame(stateTimerS,true);
                break;
            case ATTACKL:
                region = lizardAttackL.getKeyFrame(stateTimerS,false);
                break;
            case ATTACKR:
                region = lizardAttackR.getKeyFrame(stateTimerS,false);
                break;
            case ATTACKU:
                region = lizardAttackU.getKeyFrame(stateTimerS,false);
                break;
            case ATTACKD:
                region = lizardAttackD.getKeyFrame(stateTimerS,false);
                break;
            case DEATH:
                region = lizardDeath.getKeyFrame(stateTimerS,false);
                break;
            case STUN:
                region = lizardStun.getKeyFrame(stateTimerS,true);
                break;
            default:
                region = lizardStand.getKeyFrame(stateTimerS,true);
                break;
        }
        if (((LizardModel)s).state ==((LizardModel)s).previousState ){
            s.setStateTimer(s.getStateTimer()+dt);
        }
        else {
            s.setStateTimer(0);
        }
        ((LizardModel)s).previousState = ((LizardModel)s).state;
        return region;
    }

}