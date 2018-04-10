/*
 * FloorController.java
 *
 */
package edu.cornell.gdiac.physics.floor;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;
import edu.cornell.gdiac.physics.floor.weapon.*;
import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.physics.*;
import edu.cornell.gdiac.physics.obstacle.*;
import edu.cornell.gdiac.physics.floor.character.*;

import java.util.ArrayList;
import java.util.HashMap;

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
    private static final String LEVEL = "level-basic.tmx";
//    private static final String LEVEL = "level-advanced.tmx";

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
    /** The sound file for a reload */
    private static final String OUCH_FILE = "floor/ouch.mp3";

    private static final int TILE_SIZE = 32;

    private static final int BOARD_WIDTH=1024;
    private static final int BOARD_HEIGHT=576;

    private static final int NUM_OF_TILES_X = BOARD_WIDTH/TILE_SIZE;
    private static final int NUM_OF_TILES_Y = BOARD_HEIGHT/TILE_SIZE;

    private static final float WALL_THICKNESS_SCALE = 0.33f;

    private static final float OBJ_OFFSET_X = 1f;
    private static final float OBJ_OFFSET_Y = 1f;

    /** Offset for the UI on the screen */
    private static final float UI_OFFSET   = 5.0f;

    public static int CONTROL_NO_ACTION = 0;
    public static int CONTROL_MOVE_LEFT = 1;
    public static int CONTROL_MOVE_RIGHT = 2;
    public static int CONTROL_MOVE_UP = 4;
    public static int CONTROL_MOVE_DOWN = 8;
    public static int CONTROL_FIRE = 16;

    /** Weapon Name -> Texture Dictionary*/
    /*TODO maybe move info to weapons class */
    private HashMap<String, Texture> wep_to_texture = new HashMap<String, Texture>();
    private HashMap<String, Texture> wep_to_small_texture = new HashMap<String, Texture>();
    private HashMap<String, WeaponModel> wep_to_model = new HashMap<String, WeaponModel>();
    private HashMap<String, Boolean> wep_in_use = new HashMap<String, Boolean>();
    private String[] list_of_weapons = new String[4];
    private String[] mopcart_menu = new String[2];
    private int mopcart_index = 0;
    private int[] mopcart_index_xlocation = new int[2];
    private boolean mop_cart_reloaded_before = false;

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
        manager.load(OUCH_FILE, Sound.class);
        assets.add(OUCH_FILE);

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
        sounds.allocate(manager, OUCH_FILE);

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
    private static final float  BULLET_OFFSET = 2.0f;
    /** The speed of the bullet after firing */
    private static final float  BULLET_SPEED = 20.0f;
    /** The speed of the slimeball after firing */
    private static final float  SLIMEBALL_SPEED = 10.0f;
    /** The volume for sound effects */
    private static final float EFFECT_VOLUME = 0.8f;
    /** Attack total time frames*timerperframe for mop */
    private static final float ATTACK_DURATION_MOP = 0.4f;
    /** Attack total time frames*timerperframe for lid */
    private static final float ATTACK_DURATION_LID = 0.2f;
    /** Attack total time frames*timerperframe for spray */
    private static final float ATTACK_DURATION_SPRAY= 0.8f;
    /** Attack total time frames*timerperframe for vacuum */
    private static final float ATTACK_DURATION_VACUUM= 0.5f;
    /** The timer for animations*/
    private float stateTimer;
    /** The cooldown for attack animations*/
    private float attackTimer;
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
    ArrayList<Vector2> slimePos;
    ArrayList<Vector2> robotPos;
    ArrayList<Vector2> lizardPos;
    ArrayList<Vector2> wallRightPos;
    ArrayList<Vector2> wallLeftPos;
    ArrayList<Vector2> wallMidPos;
    ArrayList<Vector2> wallTLPos;
    ArrayList<Vector2> wallTRPos;
    ArrayList<Vector2> wallBLPos;
    ArrayList<Vector2> wallBRPos;
    ArrayList<Vector2> wallSLPos;
    ArrayList<Vector2> wallSRPos;
    ArrayList<Vector2> hazardPos;

    int [][] tiles;

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
    private BoxObstacle mopCart;

    /** Mark set to handle more sophisticated collision callbacks */
    protected ObjectSet<Fixture> sensorFixtures;

    /** For mop knockback force calculations*/
    private Vector2 knockbackForce = new Vector2();

    /** Saved lid velocity before colliding with slimeball */
    private Vector2 lidVel = new Vector2();



    /**
     * Creates and initialize a new instance of the platformer game
     *
     * The game has default gravity and other settings
     */
    public FloorController() {
        super(DEFAULT_WIDTH,DEFAULT_HEIGHT,DEFAULT_GRAVITY);
        currentState = State.STANDING;
        previousState = State.STANDING;
        stateTimer = 0.0f;
        attackTimer = 0.0f;
        lidTimer = LID_RANGE;
        lidGround = false;
        setDebug(false);
        setComplete(false);
        setFailure(false);
        world.setContactListener(this);
        sensorFixtures = new ObjectSet<Fixture>();
        level = new LevelEditorParser(LEVEL);
        scientistPos = level.getScientistPos();
        slimePos = level.getSlimePos();
        robotPos = level.getRobotPos();
        lizardPos = level.getLizardPos();

//        robotPos = new ArrayList<Vector2>();
//        lizardPos = new ArrayList<Vector2>();
//        slimePos = new ArrayList<Vector2>();
        //scientistPos=new ArrayList<Vector2>();
        //scientistPos=level.getLizardPos();

        wallLeftPos = level.getWallLeftPos();
        wallRightPos = level.getWallRightPos();
        wallMidPos = level.getWallMidPos();
        wallTLPos = level.getWallTLPos();
        wallTRPos = level.getWallTRPos();
        wallBLPos = level.getWallBLPos();
        wallBRPos = level.getWallBRPos();
        wallSLPos = level.getWallSLPos();
        wallSRPos = level.getWallSRPos();

        hazardPos = level.getHazardPos();

        tiles = level.getTiles();
    }

    /**
     * Resets the status of the game so that we can play again.
     *
     * This method disposes of the world and creates a new one.
     */
    public void reset() {
        SoundController.getInstance().play(BACKGROUND_TRACK_FILE, BACKGROUND_TRACK_FILE, true, 0.4f);
        mop_cart_reloaded_before = false;

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
        stateTimer = 0.0f;
        attackTimer = 0.0f;
        lidTimer = LID_RANGE;
        lidGround = false;

        enemies=new EnemyModel[scientistPos.size() + robotPos.size() + slimePos.size() + lizardPos.size()];
        controls = new AIController[scientistPos.size() + robotPos.size() + slimePos.size() + lizardPos.size()];
        board = new Board(NUM_OF_TILES_X, NUM_OF_TILES_Y);
        populateLevel();
    }

    /**
     * Lays out the game geography.
     */
    private void populateLevel() {
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
        mopCart = new BoxObstacle(level.getMopCartX()/32+OBJ_OFFSET_X, level.getMopCartY()/32+OBJ_OFFSET_X,mopwidth,mopheight);
        mopCart.setBodyType(BodyDef.BodyType.StaticBody);
        mopCart.setDensity(0.0f);
        mopCart.setFriction(0.0f);
        mopCart.setRestitution(0.0f);
        mopCart.setSensor(true);
        mopCart.setDrawScale(scale);
        mopCart.setTexture(mopTile);
        mopCart.setName("mopCart");
        addObject(mopCart);

        Texture[] tileTextures = {null, tileTexture, broken1TileTexture,
                broken2tileTexture, broken3tileTexture, grateTileTexture,
                broken4tileTexture,underTileTexture,stairsTileTexture};

        board.setTileTextures(tileTextures);
        board.setHazardTileTexture(hazardTileTexture);
        setHazardTiles();
        addUIInfo();
        addWalls();
        addCharacters();
        //does this using hazardpos
    }

    private void addUIInfo() {
        /** Pixel Locations of Weapon Icons in Mop Cart*/
        mopcart_index_xlocation[0] = 890;
        mopcart_index_xlocation[1] = 960;
        /** Add names to list of weapons */
        list_of_weapons[0] = "mop";
        list_of_weapons[1] = "spray";
        list_of_weapons[2] = "vacuum";
        list_of_weapons[3] = "lid";
        mopcart_menu[0] = "vacuum";
        mopcart_menu[1] = "lid";
        /** Load name -> texture dictionary */
        wep_to_texture.put("mop", mopTexture);
        wep_to_texture.put("spray", sprayTexture);
        wep_to_texture.put("vacuum", vacuumTexture);
        wep_to_texture.put("lid", lidTexture);
        /** Load name -> small texture dictionary */
        wep_to_small_texture.put("mop", mopTextureSmall);
        wep_to_small_texture.put("spray", sprayTextureSmall);
        wep_to_small_texture.put("vacuum", vacuumTextureSmall);
        wep_to_small_texture.put("lid", lidTextureSmall);
        /** Load name -> model dictionary */
        wep_to_model.put("mop", new MopModel(level.getMopDurability(), level.getMopAttackRange(), level.getMopKnockbackTimer()));
        wep_to_model.put("spray", new SprayModel(level.getSprayDurability(), level.getSprayAttackRange(), level.getSprayStunTimer()));
        wep_to_model.put("vacuum", new VacuumModel(level.getVacuumDurability(), level.getVacuumAttackRange()));
        wep_to_model.put("lid", new LidModel(level.getLidDurability(), level.getLidAttackRange()));
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
        joeMopR = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();
        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(avatarMopLTexture,i*192,0,192,64));
        }
        joeMopL = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();
        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(avatarMopUTexture,i*64,0,64,192));
        }
        joeMopU = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();
        for (int i=0; i <= 3; i++){
            frames.add (new TextureRegion(avatarMopDTexture,i*64,0,64,192));
        }
        joeMopD = new Animation<TextureRegion>(0.1f, frames);
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
        joeSprayR = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();
        for (int i=0; i <= 7; i++){
            frames.add (new TextureRegion(avatarSprayLTexture,i*320,0,320,64));
        }
        joeSprayL = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();
        for (int i=0; i <= 7; i++){
            frames.add (new TextureRegion(avatarSprayUTexture,i*64,0,64,320));
        }
        joeSprayU = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();
        for (int i=0; i <= 7; i++){
            frames.add (new TextureRegion(avatarSprayDTexture,i*64,0,64,320));
        }
        joeSprayD = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();
        for (int i=0; i <= 0; i++){
            frames.add (new TextureRegion(avatarVacuumRTexture,i*64,0,64,64));
        }
        joeVacuumR = new Animation<TextureRegion>(0.5f, frames);
        frames.clear();
        for (int i=0; i <= 0; i++){
            frames.add (new TextureRegion(avatarVacuumLTexture,i*64,0,64,64));
        }
        joeVacuumL = new Animation<TextureRegion>(0.5f, frames);
        frames.clear();
        for (int i=0; i <= 0; i++){
            frames.add (new TextureRegion(avatarVacuumUTexture,i*64,0,64,64));
        }
        joeVacuumU = new Animation<TextureRegion>(0.5f, frames);
        frames.clear();
        for (int i=0; i <= 0; i++){
            frames.add (new TextureRegion(avatarVacuumDTexture,i*64,0,64,64));
        }
        joeVacuumD = new Animation<TextureRegion>(0.5f, frames);
        frames.clear();

        float dwidth  = 64/scale.x;
        float dheight = 64/scale.y;
        avatar = new JoeModel(level.getJoePosX()/32+OBJ_OFFSET_X, level.getJoePosY()/32+OBJ_OFFSET_Y, dwidth, dheight,
                level.getJoeHP(), level.getJoeDensity(), level.getJoeVel());
        avatar.setWep1(wep_to_model.get("mop"));
        avatar.setWep2(wep_to_model.get("spray"));
        avatar.setDrawScale(scale);
        avatar.setTexture(avatarIdleTexture);
        avatar.setName("joe");
        addObject(avatar);

        for (int ii=0; ii<scientistPos.size(); ii++) {
            EnemyModel mon =new ScientistModel(scientistPos.get(ii).x/32+OBJ_OFFSET_X, scientistPos.get(ii).y/32+OBJ_OFFSET_Y,
                    dwidth, dheight, ii, level.getScientistHP(), level.getScientistDensity(), level.getScientistVel(), level.getScientistAttackRange());
            mon.setDrawScale(scale);
            mon.setTexture(scientistTexture);
            mon.setName("scientist");
            addObject(mon);
            enemies[ii]=mon;
        }

        for (int ii=0; ii<robotPos.size(); ii++) {
            EnemyModel mon =new RobotModel(robotPos.get(ii).x/32+OBJ_OFFSET_X, robotPos.get(ii).y/32+OBJ_OFFSET_Y,
                    dwidth, dheight, scientistPos.size()+ii, level.getRobotHP(), level.getRobotDensity(), level.getRobotVel(), level.getRobotAttackRange());
            mon.setDrawScale(scale);
            mon.setTexture(robotTexture);
            mon.setName("robot");
            addObject(mon);
            enemies[scientistPos.size()+ii]=mon;
        }
        for (int ii=0; ii<slimePos.size(); ii++){
            EnemyModel mon =new SlimeModel(slimePos.get(ii).x/32+OBJ_OFFSET_X, slimePos.get(ii).y/32+OBJ_OFFSET_Y,
                    dwidth, dheight, scientistPos.size()+robotPos.size()+ii, level.getSlimeHP(), level.getSlimeDensity(), level.getSlimeVel(), level.getSlimeAttackRange(), level.getSlimeballSpeed());
            mon.setDrawScale(scale);
            mon.setTexture(slimeTexture);
            mon.setName("slime");
            addObject(mon);
            enemies[scientistPos.size()+robotPos.size()+ii]=mon;
        }
        for (int ii=0; ii<lizardPos.size(); ii++){
            EnemyModel mon =new LizardModel(lizardPos.get(ii).x/32+OBJ_OFFSET_X, lizardPos.get(ii).y/32+OBJ_OFFSET_Y,
                    dwidth, dheight, scientistPos.size()+robotPos.size()+slimePos.size()+ii, level.getLizardHP(), level.getLizardDensity(), level.getLizardVel(), level.getLizardAttackRange());
            mon.setDrawScale(scale);
            mon.setTexture(lizardTexture);
            mon.setName("lizard");
            addObject(mon);
            enemies[scientistPos.size()+robotPos.size()+slimePos.size()+ii]=mon;
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

        offset = (TILE_SIZE * 2*(1 - WALL_THICKNESS_SCALE))/2;
        BoxObstacle obj;
        float x;
        float y;
        for (int ii = 0; ii < wallMidPos.size(); ii++) {
            x = board.boardToScreenX((int) wallMidPos.get(ii).x);
            y = board.boardToScreenY((int) wallMidPos.get(ii).y) + offset/32 + 0.5f; //added 0.5f for offset due to wall dimensions
            board.setBlocked((int) wallMidPos.get(ii).x, (int) wallMidPos.get(ii).y);
            board.setBlocked((int) wallMidPos.get(ii).x, (int) wallMidPos.get(ii).y+1);

            obj = new BoxObstacle(x, y, dwidth, dheight * WALL_THICKNESS_SCALE / 2);
            obj.setTexture(wallMidTexture, 0, offset);
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
        }

        for (int ii = 0; ii < wallBRPos.size(); ii++) {
            x = board.boardToScreenX((int) wallBRPos.get(ii).x);
            y = board.boardToScreenY((int) wallBRPos.get(ii).y) + offset/32 + 0.5f; //added 0.5f for offset due to wall dimensions
            board.setBlocked((int) wallBRPos.get(ii).x, (int) wallBRPos.get(ii).y);
            board.setBlocked((int) wallBRPos.get(ii).x, (int) wallBRPos.get(ii).y+1);

            obj = new BoxObstacle(x, y, dwidth, dheight * WALL_THICKNESS_SCALE / 2);
            obj.setTexture(wallBRTexture, 0, offset);
            obj.setName(pname+ii);
            addWallObject(obj);
        }

        for (int ii = 0; ii < wallBLPos.size(); ii++) {
            x = board.boardToScreenX((int) wallBLPos.get(ii).x);
            y = board.boardToScreenY((int) wallBLPos.get(ii).y) + offset/32 + 0.5f; //added 0.5f for offset due to wall dimensions
            board.setBlocked((int) wallBLPos.get(ii).x, (int) wallBLPos.get(ii).y);
            board.setBlocked((int) wallBLPos.get(ii).x, (int) wallBLPos.get(ii).y+1);

            obj = new BoxObstacle(x, y, dwidth, dheight * WALL_THICKNESS_SCALE / 2);
            obj.setTexture(wallBLTexture, 0, offset);
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
        }

        offset = -offset;
        for (int ii = 0; ii < wallSRPos.size(); ii++) {
            x = board.boardToScreenX((int) wallSRPos.get(ii).x) + offset/32;
            y = board.boardToScreenY((int) wallSRPos.get(ii).y) + offsetY/32 + 0.5f; //added 0.5f for offset due to wall dimensions
            board.setBlocked((int) wallSRPos.get(ii).x, (int) wallSRPos.get(ii).y);
            board.setBlocked((int) wallSRPos.get(ii).x, (int) wallBLPos.get(ii).y+1);

            obj = new BoxObstacle(x, y, dwidth * WALL_THICKNESS_SCALE, dheight * WALL_THICKNESS_SCALE / 2);
            obj.setTexture(wallSRTexture, offset, offsetY);
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
        ticks ++;
        if(avatar.getHP()<=0) {
            avatar.setAlive(false);
            avatar.markRemoved(true);
            setFailure(true);
        }
        else if (board.isHazard(board.screenToBoardX(avatar.getX()), board.screenToBoardY(avatar.getY())) &&
                ticks % 30==0L){ //adjust this later
            //System.out.println("You're on a hazard tile");
            avatar.decrHP();
//            avatar.drawAttacked(canvas);
            SoundController.getInstance().play(OUCH_FILE, OUCH_FILE,false,EFFECT_VOLUME);
        }
        else {
            // Process actions in object model
            avatar.setMovementX(InputController.getInstance().getHorizontal() *avatar.getVelocity());
            avatar.setMovementY(InputController.getInstance().getVertical() *avatar.getVelocity());
            avatar.setSwapping(InputController.getInstance().didTertiary());

            avatar.setLeft(InputController.getInstance().didLeftArrow());
            avatar.setRight(InputController.getInstance().didRightArrow());
            avatar.setUp(InputController.getInstance().didUpArrow());
            avatar.setDown(InputController.getInstance().didDownArrow());

            if (avatar.isAtMopCart()) {
                joeAtMopCartUpdate();
            } else {
                joeNotAtMopCartUpdate();
            }
            if (attackTimer == 0) {
                avatar.setVelocity();
        }
            else {
                avatar.setMovementX(0.0f);
                avatar.setMovementY(0.0f);
                avatar.setVelocity();
            }
            avatar.setTexture(getFrame(dt));
        }
        lidRange(dt);
        enemyUpdate();

        SoundController.getInstance().update();
    }

    /**
     * Update function for Joe when he at the mop cart
     */
    private void joeAtMopCartUpdate() {
        if (!mop_cart_reloaded_before
                && (avatar.getWep1().durability != avatar.getWep1().getMaxDurability()
                || avatar.getWep2().durability != avatar.getWep2().getMaxDurability())) {
            SoundController.getInstance().play(RELOAD_FILE, RELOAD_FILE,false,EFFECT_VOLUME);
            mop_cart_reloaded_before = true;
            //recharge durability of weapons
            avatar.getWep1().durability = avatar.getWep1().getMaxDurability();
            avatar.getWep2().durability = avatar.getWep2().getMaxDurability();
            //recharge to max health
            avatar.setHP(avatar.getMaxHP());
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
            else if (mopcart_index == 0) { mopcart_index = 1; }
        } else if (avatar.isRight()) {
            if (mopcart_index == 0) { mopcart_index = 1; }
            else if (mopcart_index == 1) { mopcart_index = 0; }
        }
        // swapping weapon
        if (avatar.isSwapping()) {
            //get weapon at index
            String swapping_weapon_name = mopcart_menu[mopcart_index];
//            System.out.print(swapping_weapon_name);
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
                && avatar.canAttack()) {
            attack(avatar.getWep1());
        }
    }

    /**
     * Update function for enemies
     */
    private void enemyUpdate() {
        for (EnemyModel s : enemies) {
            if (this.controls[s.getId()] != null) {

                int action = this.controls[s.getId()].getAction();
                if (s.getStunned()) {
                    System.out.println("stunned");
                    s.incrStunTicks();
                    if (s.getStunTicks()<=level.getSprayStunTimer()) {action=CONTROL_NO_ACTION; s.setMovementY(0); s.setMovementX(0);} //TODO change to get from sprayModel
                    else {s.resetStunTicks(); s.setStunned(false);}
                }

                performAction(s, action);
            }

            if (board.isHazard(board.screenToBoardX(s.getX()), board.screenToBoardY(s.getY()))
                    && !(s instanceof RobotModel) && ticks % 30==0L ){ //adjust this later
                //System.out.println("Enemy is on a hazard tile");
                s.decrHP();
                if (s.getHP() <= 0) {
                    controls[s.getId()]=null;
                    s.markRemoved(true);
                }
            }
        }
    }

    /**
     * Perform enemy s's action
     * @param s enemy that is performing the action
     * @param action action to be performed
     */
    private void performAction(EnemyModel s, int action) {
        if (action == CONTROL_MOVE_DOWN) {
            s.setMovementY(-s.getVelocity());
            s.setMovementX(0);
            s.resetAttackAniFrame();
        }
        if (action == CONTROL_MOVE_LEFT) {
            s.setMovementX(-s.getVelocity());
            s.setMovementY(0);
            s.resetAttackAniFrame();
        }
        if (action == CONTROL_MOVE_UP) {
            s.setMovementY(s.getVelocity());
            s.setMovementX(0);
            s.resetAttackAniFrame();
        }
        if (action == CONTROL_MOVE_RIGHT) {
            s.setMovementX(s.getVelocity());
            s.setMovementY(0);
            s.resetAttackAniFrame();

        }
        if (s.canAttack()) {
            if (action==CONTROL_FIRE){
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
                avatar.decrHP();
//                avatar.drawAttacked(canvas);
                s.resetAttackAniFrame();
                SoundController.getInstance().play(OUCH_FILE, OUCH_FILE,false,EFFECT_VOLUME);
            }
        } else if (s instanceof SlimeModel) {
            //System.out.println("shoot1");
            createBullet((SlimeModel) s);
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
        bullet.setFixtureGroupIndex((short) -1);
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
                    int horiGap = board.screenToBoardX(avatar.getX()) - board.screenToBoardX(s.getX());
                    int vertiGap = board.screenToBoardY(avatar.getY()) - board.screenToBoardY(s.getY());
                    boolean case1 = Math.abs(horiGap)<= mop.getRange() && horiGap>=0 && avatar.isLeft() && Math.abs(vertiGap)<= 1;
                    boolean case2 = Math.abs(horiGap)<= mop.getRange() && horiGap<=0 && avatar.isRight() && Math.abs(vertiGap)<= 1;
                    boolean case3 = Math.abs(vertiGap)<= mop.getRange() && vertiGap>=0 && avatar.isDown() && Math.abs(horiGap)<= 1;
                    boolean case4 = Math.abs(vertiGap)<= mop.getRange() && vertiGap<=0 && avatar.isUp() && Math.abs(horiGap)<= 1;

                    if ((case1 || case2 || case3 || case4)) {
                        enemy_hit = true;
                        if (s.getHP() == 1) {
                            s.markRemoved(true);
                            controls[s.getId()]=null;
                        } else {
                            s.decrHP();
                        }
                        knockbackForce.set(horiGap * -7.5f, vertiGap * -7.5f);
                        //knockbackForce.nor();

                        s.applyImpulse(knockbackForce);
                        s.setKnockbackTimer(KNOCKBACK_TIMER);
                        //System.out.println(knockbackForce);
                    }
                }
                if (enemy_hit) {
                    mop.decrDurability();
                    if (mop.durability < 0) { mop.durability = 0; } //fix negative durability UI displays;
                }
            }
        } else if (wep instanceof SprayModel) {
            SoundController.getInstance().play(PEW_FILE, PEW_FILE, false, 0.5f);
            SprayModel spray = (SprayModel) wep;
            if (spray.getDurability() != 0) {
                spray.decrDurability();
                for (EnemyModel s : enemies) {
                    int horiGap = board.screenToBoardX(avatar.getX()) - board.screenToBoardX(s.getX());
                    int vertiGap = board.screenToBoardY(avatar.getY()) - board.screenToBoardY(s.getY());
                    boolean case1 = Math.abs(horiGap) <= spray.getRange() && horiGap >= 0 && avatar.isLeft() && Math.abs(vertiGap)<= 1;
                    boolean case2 = Math.abs(horiGap) <= spray.getRange() && horiGap <= 0 && avatar.isRight() && Math.abs(vertiGap)<= 1;
                    boolean case3 = Math.abs(vertiGap) <= spray.getRange() && vertiGap >= 0 && avatar.isDown() && Math.abs(horiGap)<= 1;
                    boolean case4 = Math.abs(vertiGap) <= spray.getRange() && vertiGap <= 0 && avatar.isUp() && Math.abs(horiGap)<= 1;

                    if (!s.isRemoved() && (case1 || case2 || case3 || case4)) {
                        if (s.getHP() == 1 ) {
                            controls[s.getId()]=null;
                            s.markRemoved(true);
                        } else if (s instanceof RobotModel){
                            s.setStunned(true);
                            s.decrHP();
                        } else {
                            s.setStunned(true);
                        }

                    }
                }
            }
        } else if (wep instanceof VacuumModel) {
                VacuumModel vacuum = (VacuumModel) wep;
                if (vacuum.getDurability() != 0){
                    System.out.println("vacuum");
                    vacuum.decrDurability();
                    for (EnemyModel s : enemies){
                        int horiGap = board.screenToBoardX(avatar.getX()) - board.screenToBoardX(s.getX());
                        int vertiGap = board.screenToBoardY(avatar.getY()) - board.screenToBoardY(s.getY());
                        boolean case1 = Math.abs(horiGap) <= vacuum.getRange() && horiGap >= 0 && avatar.isLeft() && Math.abs(vertiGap) <=1;
                        boolean case2 = Math.abs(horiGap) <= vacuum.getRange() && horiGap <= 0 && avatar.isRight() && Math.abs(vertiGap) <=1;
                        boolean case3 = Math.abs(vertiGap) <= vacuum.getRange() && vertiGap >= 0 && avatar.isDown() && Math.abs(horiGap) <=1;
                        boolean case4 = Math.abs(vertiGap) <= vacuum.getRange() && vertiGap <= 0 && avatar.isUp() && Math.abs(horiGap) <=1;
                        if ((case1)) {
                            knockbackForce.set(30f,0f);
                            s.applyImpulse(knockbackForce);
                            s.setKnockbackTimer(KNOCKBACK_TIMER);
                            s.setStunned(true);

                        }
                        if ((case2)) {
                            knockbackForce.set(-30f,0f);
                            s.applyImpulse(knockbackForce);
                            s.setKnockbackTimer(KNOCKBACK_TIMER);
                            s.setStunned(true);
                        }
                        if ((case3)) {
                            knockbackForce.set(0f,30f);
                            s.applyImpulse(knockbackForce);
                            s.setKnockbackTimer(KNOCKBACK_TIMER);
                            s.setStunned(true);
                        }
                        if ((case4)) {
                            knockbackForce.set(0f,-30f);
                            s.applyImpulse(knockbackForce);
                            s.setKnockbackTimer(KNOCKBACK_TIMER);
                            s.setStunned(true);
                        }

                    }
                }
        } else if (wep instanceof LidModel) {
            LidModel lid = (LidModel) wep;
            if (lid.getDurability() != 0 && avatar.getHasLid()) {
//                avatar.setHasLid(false);
                createBullet(avatar);
                lid.decrDurability();
            }
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
                if (bd1.getName().equals("lid") && (bd2 != s) && (bd2 != avatar) ) {
                    dropLid(bd1);
                }

                if (bd2.getName().equals("lid") && ((bd1 != s)) && (bd1 != avatar) ) {
                    dropLid(bd2);
                }
            }

            if (bd1.getName().equals("lid") && (bd2 == avatar) ) {
                removeBullet(bd2);
                avatar.setHasLid(true);
                lidGround = false;
                lidTimer = LID_RANGE;

            }
            if (bd2.getName().equals("lid") && (bd1 == avatar) ) {
                removeBullet(bd2);
                avatar.setHasLid(true);
                lidGround = false;
                lidTimer = LID_RANGE;
            }

            if (bd1.getName().equals("slimeball") && bd2 == avatar) {
                if (!bd1.isRemoved()) {
                    avatar.decrHP();
//                    avatar.drawAttacked(canvas);
                    removeBullet(bd1);
                    SoundController.getInstance().play(OUCH_FILE, OUCH_FILE,false,EFFECT_VOLUME);

                }
            } else if (bd1.getName().equals("slimeball") && !(bd2 instanceof EnemyModel)) {
                removeBullet(bd1);
            }

            if (bd2.getName().equals("slimeball") && bd1 == avatar) {
                if (!bd2.isRemoved()) {
                    removeBullet(bd2);
                    avatar.decrHP();
//                    avatar.drawAttacked(canvas);
                    SoundController.getInstance().play(OUCH_FILE, OUCH_FILE,false,EFFECT_VOLUME);
                }
            } else if(bd2.getName().equals("slimeball") && !(bd1 instanceof EnemyModel)) {
                removeBullet(bd2);
            }

            if (bd2.getName().equals("slimeball") && bd1.getName().equals("lid")) {
                removeBullet(bd2);
            }

            if (bd1.getName().equals("slimeball") && bd2.getName().equals("lid")) {
                removeBullet(bd1);
            }

            // Check for win condition
            if ((bd1 == avatar   && bd2 == goalDoor) ||
                    (bd1 == goalDoor && bd2 == avatar)) {
                setComplete(true);
            }
            if ((bd1 == avatar   && bd2 == mopCart) ||
                    (bd1 == mopCart && bd2 == avatar)) {
                avatar.setAtMopCart(true);
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

        if ((bd1 == avatar   && bd2 == mopCart) ||
                (bd1 == mopCart && bd2 == avatar)) {
            avatar.setAtMopCart(false);
        }

        if (bd2.getName().equals("slimeball") && bd1.getName().equals("lid")) {
            bd1.setVX(lidVel.x);
            bd1.setVY(lidVel.y);
            System.out.println("set lidvel");
            System.out.println(bd1.getVX());
            System.out.println(bd1.getVY());
            System.out.println("");

        }

        if (bd1.getName().equals("slimeball") && bd2.getName().equals("lid")) {
            bd2.setVX(lidVel.x);
            bd2.setVY(lidVel.y);
            System.out.println("set lidvel");
            System.out.println(bd2.getVX());
            System.out.println(bd2.getVY());
            System.out.println("");
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

        displayFont.setColor(Color.WHITE);

        /* Show Multiple HP and Mop Icons */
        int margin = 0;
        int HP = avatar.getHP();
        for (int j = 0; j < HP; j++) {
            canvas.draw(heartTexture, UI_OFFSET + 30 + margin, canvas.getHeight()-UI_OFFSET - 27);
            margin = margin + 25;
        }

        // DISPLAY ACTIVE WEAPON UI
        //get textures via hash map from weapons names
        String wep1FileName = avatar.getWep1().getName();
        String wep2FileName = avatar.getWep2().getName();
        Texture wep1Texture = wep_to_texture.get(wep1FileName);
        Texture wep2Texture = wep_to_small_texture.get(wep2FileName);
        //draw retrieved textures
        canvas.draw(wep1Texture, UI_OFFSET + 50, canvas.getHeight()-UI_OFFSET - 100);
        canvas.draw(wep2Texture, UI_OFFSET + 65, canvas.getHeight()-UI_OFFSET - 180);
        //draw weapon UI durability bars (currently temporary)
        int durability1 = avatar.getWep1().getDurability();
        String maxDurability1 = Integer.toString(avatar.getWep1().getMaxDurability());
        int durability2 = avatar.getWep2().getDurability();
        String maxDurability2 = Integer.toString(avatar.getWep2().getMaxDurability());
        displayFont.getData().setScale(0.8f);
        if (durability1 <= 3 && durability2 <= 3) {
            displayFont.setColor(Color.RED);
            canvas.drawText(Integer.toString(durability1) + "/" + maxDurability1,
                    displayFont, UI_OFFSET + 39, canvas.getHeight()-UI_OFFSET - 110);
            canvas.drawText(Integer.toString(durability2) + "/" + maxDurability2,
                    displayFont, UI_OFFSET + 43, canvas.getHeight()-UI_OFFSET - 190);
            displayFont.setColor(Color.WHITE);
        }
        else if (durability1 <= 3) {
            displayFont.setColor(Color.RED);
            canvas.drawText(Integer.toString(durability1) + "/" + maxDurability1,
                    displayFont, UI_OFFSET + 39, canvas.getHeight()-UI_OFFSET - 110);
            displayFont.setColor(Color.WHITE);
            canvas.drawText(Integer.toString(durability2) + "/" + maxDurability2,
                    displayFont, UI_OFFSET + 43, canvas.getHeight()-UI_OFFSET - 190);
        }
        else if (durability2 <= 3) {
            canvas.drawText(Integer.toString(durability1) + "/" + maxDurability1,
                    displayFont, UI_OFFSET + 39, canvas.getHeight()-UI_OFFSET - 110);
            displayFont.setColor(Color.RED);
            canvas.drawText(Integer.toString(durability2) + "/" + maxDurability2,
                    displayFont, UI_OFFSET + 43, canvas.getHeight()-UI_OFFSET - 190);
            displayFont.setColor(Color.WHITE);
        }
        else {
            canvas.drawText(Integer.toString(durability1) + "/" + maxDurability1,
                    displayFont, UI_OFFSET + 39, canvas.getHeight()-UI_OFFSET - 110);
            canvas.drawText(Integer.toString(durability2) + "/" + maxDurability2,
                    displayFont, UI_OFFSET + 43, canvas.getHeight()-UI_OFFSET - 190);
        }
        displayFont.getData().setScale(1f);

        if (avatar.isAtMopCart()){
            //DRAW MOP CART TEXT AND BACKGROUND
            Color tint1 = Color.BLACK;
            canvas.draw(backgroundTexture, tint1, 10.0f, 14.0f,
                    canvas.getWidth()/2 + 340, canvas.getHeight()/2 + 180, 0, .18f, .8f);
                //change sy to increase height of black box
            displayFont.getData().setScale(0.5f);
            canvas.drawText("Mop Cart", displayFont,
                    canvas.getWidth()/2 + 375, canvas.getHeight()/2 + 280);
            displayFont.getData().setScale(1.0f);

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
            canvas.draw(unused_wep1, canvas.getWidth()/2 + 360, canvas.getHeight()/2 + 200);
            canvas.draw(unused_wep2, canvas.getWidth()/2 + 430, canvas.getHeight()/2 + 200);

            //DRAW MOPCART INDEX
            int current_xlocation = mopcart_index_xlocation[mopcart_index];
            canvas.draw(mopcartIndexTexture, current_xlocation, canvas.getHeight()/2 + 170);
        }

        //Draw Enemy Health
        displayFont.getData().setScale(0.5f);
        for (EnemyModel s : enemies) {
            if (!(s.isRemoved())) {
                canvas.drawText("" + s.getHP(), displayFont, s.getX() * scale.x, (s.getY() + 1) * scale.y);
            }
        }
        displayFont.getData().setScale(1.0f);

        canvas.end();

        super.draw(delta);
    }

    /** Unused ContactListener method */
    public void postSolve(Contact contact, ContactImpulse impulse) {}
    /** Unused ContactListener method */
    public void preSolve(Contact contact, Manifold oldManifold) {}

    public enum State {STANDING, RUNNINGR, RUNNINGU ,RUNNINGD,
        MOPR, MOPL, MOPU, MOPD,
        LIDR,LIDL,LIDU,LIDD,
        SPRAYR,SPRAYL,SPRAYU,SPRAYD,
        VACUUMR,VACUUML,VACUUMU,VACUUMD}
    public State currentState;
    public State previousState;

    public State getState(){
        System.out.println(avatar.getHasLid());
        if ((avatar.isRight() && !avatar.isAtMopCart() && avatar.getWep1().getName() == "mop"
                && !(avatar.getMovementX() < 0)&& avatar.isFacingRight() && avatar.getWep1().durability > 0)||
                ((avatar.isLeft() && !avatar.isAtMopCart() && avatar.getWep1().getName() == "mop")
                        && avatar.getMovementX() < 0 && avatar.getWep1().durability > 0)||
                ((avatar.isLeft() && !avatar.isAtMopCart()&& avatar.getWep1().getName() == "mop")
                        && avatar.getMovementX() == 0 && !avatar.isFacingRight() && avatar.getWep1().durability > 0)){
           attackTimer = ATTACK_DURATION_MOP;
            return State.MOPR;
        }
        else if ((avatar.isLeft()&& !avatar.isAtMopCart() && avatar.getWep1().getName() == "mop"
                        && avatar.getWep1().durability > 0 ) ||
                ((avatar.isRight() && !avatar.isAtMopCart() && avatar.getWep1().getName() == "mop")
                        && avatar.getMovementX() < 0 && avatar.getWep1().durability > 0)||
                ((avatar.isRight() && !avatar.isAtMopCart()&& avatar.getWep1().getName() == "mop")
                        && avatar.getMovementX() == 0 && !avatar.isFacingRight() && avatar.getWep1().durability > 0)){
            attackTimer = ATTACK_DURATION_MOP;
            return State.MOPL;
        }
        else if (avatar.isUp() && !avatar.isAtMopCart()&& avatar.getWep1().getName() == "mop"
                && avatar.getWep1().durability > 0){
           attackTimer = ATTACK_DURATION_MOP;
            return State.MOPU;
        }
        else if (avatar.isDown()&& !avatar.isAtMopCart()&& avatar.getWep1().getName() == "mop"
                && avatar.getWep1().durability > 0){
           attackTimer = ATTACK_DURATION_MOP;
            return State.MOPD;
        }
        else if ((avatar.isRight() && !avatar.isAtMopCart()&& avatar.getWep1().getName() == "lid"
                        && !(avatar.getMovementX() < 0) && avatar.isFacingRight()
                        && avatar.getWep1().durability > 0 && avatar.getHasLid())||
                ((avatar.isLeft() && !avatar.isAtMopCart()&& avatar.getWep1().getName() == "lid")
                        && avatar.getMovementX() < 0 && avatar.getWep1().durability > 0 && avatar.getHasLid())||
                ((avatar.isLeft() && !avatar.isAtMopCart()&& avatar.getWep1().getName() == "lid")
                        && avatar.getMovementX() == 0 && !avatar.isFacingRight()
                        && avatar.getWep1().durability > 0 && avatar.getHasLid())){
            attackTimer = ATTACK_DURATION_LID;
            avatar.setHasLid(false);
            return State.LIDR;
        }
        else if ((avatar.isLeft()&& !avatar.isAtMopCart() && avatar.getWep1().getName() == "lid"
                && avatar.getWep1().durability > 0 && avatar.getHasLid() ) ||
                ((avatar.isRight() && !avatar.isAtMopCart() && avatar.getWep1().getName() == "lid")
                        && avatar.getMovementX() < 0 && avatar.getWep1().durability > 0 && avatar.getHasLid())||
                ((avatar.isRight() && !avatar.isAtMopCart()&& avatar.getWep1().getName() == "lid")
                        && avatar.getMovementX() == 0 && !avatar.isFacingRight() && avatar.getWep1().durability > 0
                        && avatar.getHasLid())){
            attackTimer = ATTACK_DURATION_LID;
            avatar.setHasLid(false);
            return State.LIDL;
        }
        else if (avatar.isUp()&& !avatar.isAtMopCart() && avatar.getWep1().getName() == "lid"
                && avatar.getWep1().durability > 0 && avatar.getHasLid()){
            attackTimer = ATTACK_DURATION_LID;
            avatar.setHasLid(false);
            return State.LIDU;
        }
        else if (avatar.isDown()&& !avatar.isAtMopCart()&& avatar.getWep1().getName() == "lid"
                && avatar.getWep1().durability > 0 && avatar.getHasLid()){
            attackTimer = ATTACK_DURATION_LID;
            avatar.setHasLid(false);
            return State.LIDD;
        }
        else if ((avatar.isRight() && !avatar.isAtMopCart()&& avatar.getWep1().getName() == "spray"
                && !(avatar.getMovementX() < 0) && avatar.isFacingRight()
                && avatar.getWep1().durability > 0 )||
                ((avatar.isLeft() && !avatar.isAtMopCart()&& avatar.getWep1().getName() == "spray")
                        && avatar.getMovementX() < 0 && avatar.getWep1().durability > 0 )||
                ((avatar.isLeft() && !avatar.isAtMopCart()&& avatar.getWep1().getName() == "spray")
                        && avatar.getMovementX() == 0 && !avatar.isFacingRight()
                        && avatar.getWep1().durability > 0)){
            attackTimer = ATTACK_DURATION_SPRAY;
            return State.SPRAYR;
        }
        else if ((avatar.isLeft()&& !avatar.isAtMopCart() && avatar.getWep1().getName() == "spray"
                && avatar.getWep1().durability > 0  ) ||
                ((avatar.isRight() && !avatar.isAtMopCart() && avatar.getWep1().getName() == "spray")
                        && avatar.getMovementX() < 0 && avatar.getWep1().durability > 0 )||
                ((avatar.isRight() && !avatar.isAtMopCart()&& avatar.getWep1().getName() == "spray")
                        && avatar.getMovementX() == 0 && !avatar.isFacingRight() && avatar.getWep1().durability > 0
                        )){
            attackTimer = ATTACK_DURATION_SPRAY;
            return State.SPRAYL;
        }
        else if (avatar.isUp()&& !avatar.isAtMopCart() && avatar.getWep1().getName() == "spray"
                && avatar.getWep1().durability > 0 ){
            attackTimer = ATTACK_DURATION_SPRAY;
            return State.SPRAYU;
        }
        else if (avatar.isDown()&& !avatar.isAtMopCart()&& avatar.getWep1().getName() == "spray"
                && avatar.getWep1().durability > 0 ){
            attackTimer = ATTACK_DURATION_SPRAY;
            return State.SPRAYD;
        }
        else if ((avatar.isRight() && !avatar.isAtMopCart()&& avatar.getWep1().getName() == "vacuum"
                && !(avatar.getMovementX() < 0) && avatar.isFacingRight()
                && avatar.getWep1().durability > 0 )||
                ((avatar.isLeft() && !avatar.isAtMopCart()&& avatar.getWep1().getName() == "vacuum")
                        && avatar.getMovementX() < 0 && avatar.getWep1().durability > 0 )||
                ((avatar.isLeft() && !avatar.isAtMopCart()&& avatar.getWep1().getName() == "vacuum")
                        && avatar.getMovementX() == 0 && !avatar.isFacingRight()
                        && avatar.getWep1().durability > 0)){
            attackTimer = ATTACK_DURATION_VACUUM;
            return State.VACUUMR;
        }
        else if ((avatar.isLeft()&& !avatar.isAtMopCart() && avatar.getWep1().getName() == "vacuum"
                && avatar.getWep1().durability > 0  ) ||
                ((avatar.isRight() && !avatar.isAtMopCart() && avatar.getWep1().getName() == "vacuum")
                        && avatar.getMovementX() < 0 && avatar.getWep1().durability > 0 )||
                ((avatar.isRight() && !avatar.isAtMopCart()&& avatar.getWep1().getName() == "vacuum")
                        && avatar.getMovementX() == 0 && !avatar.isFacingRight() && avatar.getWep1().durability > 0
                )){
            attackTimer = ATTACK_DURATION_VACUUM;
            return State.VACUUML;
        }
        else if (avatar.isUp()&& !avatar.isAtMopCart() && avatar.getWep1().getName() == "vacuum"
                && avatar.getWep1().durability > 0 ){
            attackTimer = ATTACK_DURATION_VACUUM;
            return State.VACUUMU;
        }
        else if (avatar.isDown()&& !avatar.isAtMopCart()&& avatar.getWep1().getName() == "vacuum"
                && avatar.getWep1().durability > 0 ){
            attackTimer = ATTACK_DURATION_VACUUM;
            return State.VACUUMD;
        }
        else if ((avatar.getMovementX()!=0 && avatar.getMovementY()==0 && attackTimer == 0)||(avatar.getMovementX()!=0 && avatar.getMovementY()!=0)&& attackTimer == 0){
            return State.RUNNINGR;
        }
        else if (avatar.getMovementX()==0 && avatar.getMovementY()>0&& attackTimer == 0) {
            return State.RUNNINGU;
        }
        else if (avatar.getMovementX()==0 && avatar.getMovementY()<0&& attackTimer == 0) {
            return State.RUNNINGD;
        }
        else if (avatar.getMovementX()==0 && avatar.getMovementY()== 0 && attackTimer == 0) {
           return State.STANDING;
       }
       else
       return previousState;
    }

    public TextureRegion getFrame (float dt){
        currentState = getState();
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
            default:
                region = joeStand.getKeyFrame(stateTimer,true);
                break;
        }

        if ((currentState == State.MOPR)||(currentState == State.MOPL) || (currentState == State.MOPD)||(currentState == State.MOPU)||
                (currentState == State.LIDR)||(currentState == State.LIDU)||(currentState == State.LIDD)||(currentState == State.LIDL)||
        (currentState == State.SPRAYR)||(currentState == State.SPRAYU)||(currentState == State.SPRAYD)||(currentState == State.SPRAYL) ||
                (currentState == State.VACUUMR)||(currentState == State.VACUUMU)||(currentState == State.VACUUMD)||(currentState == State.VACUUML)){

            if ((previousState == currentState) &&attackTimer > 0) {
                attackTimer -= dt;
            }else if((previousState == currentState) && attackTimer <= 0) {
                attackTimer = 0;
            }
        }
        stateTimer = currentState == previousState ? stateTimer + dt : 0;
        previousState = currentState;
        return region;
    }

}
