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
    private static final String LEVEL = "level-editor.tmx";

    /** The texture files for characters/attacks */
    private static final String WALL_LEFT_FILE = "floor/wall4.png";
    private static final String WALL_RIGHT_FILE = "floor/wall2.png";
    private static final String WALL_MID_FILE = "floor/wall3.png";
    private static final String JANITOR_FILE  = "floor/joe.png";
    private static final String JANITOR_WALKR_FILE  = "floor/janitor-walk-R.png";
    private static final String JANITOR_IDLE_FILE  = "floor/janitor-idle.png";
    private static final String JANITOR_WALKU_FILE  = "floor/janitor-walk-U.png";
    private static final String JANITOR_WALKD_FILE  = "floor/janitor-walk-D.png";
    private static final String SCIENTIST_FILE  = "floor/scientist.png";
    private static final String SLIME_FILE  = "floor/slime.png";
    private static final String ROBOT_FILE = "floor/robot.png";
    private static final String BULLET_FILE  = "floor/lid.png";
    private static final String SLIMEBALL_FILE = "floor/slimeball.png";

    /** The texture files for the UI icons */
    private static final String MOP_FILE  = "floor/ui-mop.png";
    private static final String SPRAY_FILE  = "floor/ui-spray.png";
    private static final String VACUUM_FILE  = "floor/ui-vacuum.png";
    private static final String LID_FILE  = "floor/ui-lid.png";
    private static final String MOP_FILE_SMALL  = "floor/ui-mop-small.png";
    private static final String SPRAY_FILE_SMALL  = "floor/ui-spray-small.png";
    private static final String VACUUM_FILE_SMALL  = "floor/ui-vacuum-small.png";
    private static final String LID_FILE_SMALL  = "floor/ui-lid-small.png";
    private static final String HEART_FILE  = "floor/sponge.png";

    private static final String BACKGROUND_TRACK_FILE = "floor/background-track.mp3";

    /*TODO check if these textures are necessary*/
    private static final String SPRAY_TEMP_FILE  = "floor/spray.png";
    private static final String BARRIER_FILE = "floor/barrier.png";
    private static final String ROPE_FILE  = "floor/ropebridge.png";
    private static final String BACKGROUND_FILE = "shared/loading.png";
    private static final String TILE_FILE = "shared/basic-tile.png";

    /** The sound file for a jump */
    private static final String JUMP_FILE = "floor/jump.mp3";
    /** The sound file for a bullet fire */
    private static final String PEW_FILE = "floor/pew.mp3";
    /** The sound file for a bullet collision */
    private static final String POP_FILE = "floor/plop.mp3";

    private static final int TILE_WIDTH = 32;
    private static final int TILE_SCALE = 2;

    private static final int BOARD_WIDTH=1024/TILE_WIDTH;
    private static final int BOARD_HEIGHT=576/TILE_WIDTH;

    private static final float WALL_THICKNESS_SCALE = 0.33f;

    /** Offset for the UI on the screen */
    private static final float UI_OFFSET   = 5.0f;

    public static int CONTROL_NO_ACTION = 0;
    public static int CONTROL_MOVE_LEFT = 1;
    public static int CONTROL_MOVE_RIGHT = 2;
    public static int CONTROL_MOVE_UP = 4;
    public static int CONTROL_MOVE_DOWN = 8;
    public static int CONTROL_FIRE = 16;

    /** Texture assets for characters/attacks */
    private TextureRegion wallRightTexture;
    private TextureRegion wallLeftTexture;
    private TextureRegion wallMidTexture;
    private TextureRegion avatarTexture;
    private TextureRegion avatarIdleTexture;
    private TextureRegion avatarWalkRTexture;
    private TextureRegion avatarWalkUTexture;
    private TextureRegion avatarWalkDTexture;
    private TextureRegion scientistTexture;
    private TextureRegion slimeTexture;
    private TextureRegion robotTexture;
    private TextureRegion bulletTexture;
    private TextureRegion slimeballTexture;

    /** Texture assets for UI Icons */
    private Texture mopTexture;
    private Texture sprayTexture;
    private Texture vacuumTexture;
    private Texture lidTexture;
    private Texture mopTextureSmall;
    private Texture sprayTextureSmall;
    private Texture vacuumTextureSmall;
    private Texture lidTextureSmall;
    private Texture heartTexture;

    /** Texture Asset for tiles */
    private Texture tileTexture;
    /** Texture asset for the mop cart background */
    private Texture backgroundTexture;

    /** Weapon Name -> Texture Dictionary*/
    /*TODO maybe move info to weapons class */
    private HashMap<String, Texture> wep_to_texture = new HashMap<String, Texture>();
    private HashMap<String, Texture> wep_to_small_texture = new HashMap<String, Texture>();
    private HashMap<String, WeaponModel> wep_to_model = new HashMap<String, WeaponModel>();
    private HashMap<String, Boolean> wep_in_use = new HashMap<String, Boolean>();
    private String[] list_of_weapons = new String[4];
    private String[] mopcart = new String[2];
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
        manager.load(WALL_LEFT_FILE, Texture.class);
        assets.add(WALL_LEFT_FILE);
        manager.load(WALL_RIGHT_FILE, Texture.class);
        assets.add(WALL_RIGHT_FILE);
        manager.load(WALL_MID_FILE, Texture.class);
        assets.add(WALL_MID_FILE);
        manager.load(JANITOR_FILE, Texture.class);
        assets.add(JANITOR_FILE);
        manager.load(JANITOR_WALKR_FILE, Texture.class);
        assets.add(JANITOR_WALKR_FILE);
        manager.load(JANITOR_WALKU_FILE, Texture.class);
        assets.add(JANITOR_WALKU_FILE);
        manager.load(JANITOR_WALKD_FILE, Texture.class);
        assets.add(JANITOR_WALKD_FILE);
        manager.load(JANITOR_IDLE_FILE, Texture.class);
        assets.add(JANITOR_IDLE_FILE);
        manager.load(SCIENTIST_FILE, Texture.class);
        assets.add(SCIENTIST_FILE);
        manager.load(SLIME_FILE, Texture.class);
        assets.add(SLIME_FILE);
        manager.load(ROBOT_FILE, Texture.class);
        assets.add(ROBOT_FILE);
        manager.load(BARRIER_FILE, Texture.class);
        assets.add(BARRIER_FILE);
        manager.load(BULLET_FILE, Texture.class);
        assets.add(BULLET_FILE);
        manager.load(SLIMEBALL_FILE, Texture.class);
        assets.add(SLIMEBALL_FILE);
        manager.load(SPRAY_TEMP_FILE, Texture.class);
        assets.add(SPRAY_TEMP_FILE);
        manager.load(ROPE_FILE, Texture.class);
        assets.add(ROPE_FILE);

        //UI Icons
        manager.load(MOP_FILE, Texture.class);
        assets.add(MOP_FILE);
        manager.load(SPRAY_FILE, Texture.class);
        assets.add(SPRAY_FILE);
        manager.load(VACUUM_FILE, Texture.class);
        assets.add(VACUUM_FILE);
        manager.load(LID_FILE, Texture.class);
        assets.add(LID_FILE);
        manager.load(MOP_FILE_SMALL, Texture.class);
        assets.add(MOP_FILE_SMALL);
        manager.load(SPRAY_FILE_SMALL, Texture.class);
        assets.add(SPRAY_FILE_SMALL);
        manager.load(VACUUM_FILE_SMALL, Texture.class);
        assets.add(VACUUM_FILE_SMALL);
        manager.load(LID_FILE_SMALL, Texture.class);
        assets.add(LID_FILE_SMALL);
        manager.load(HEART_FILE, Texture.class);
        assets.add(HEART_FILE);
        manager.load(TILE_FILE, Texture.class);
        assets.add(TILE_FILE);

        manager.load(BACKGROUND_TRACK_FILE, Sound.class);
        assets.add(BACKGROUND_TRACK_FILE);
        manager.load(JUMP_FILE, Sound.class);
        assets.add(JUMP_FILE);
        manager.load(PEW_FILE, Sound.class);
        assets.add(PEW_FILE);
        manager.load(POP_FILE, Sound.class);
        assets.add(POP_FILE);

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
        wallRightTexture = createTexture(manager,WALL_RIGHT_FILE,false);
        wallLeftTexture = createTexture(manager,WALL_LEFT_FILE,false);
        wallMidTexture = createTexture(manager,WALL_MID_FILE,false);
        avatarTexture = createTexture(manager,JANITOR_FILE,false);
        avatarWalkRTexture = createTexture(manager,JANITOR_WALKR_FILE,false);
        avatarWalkUTexture = createTexture(manager,JANITOR_WALKU_FILE,false);
        avatarWalkDTexture = createTexture(manager,JANITOR_WALKD_FILE,false);
        avatarIdleTexture = createTexture(manager,JANITOR_IDLE_FILE,false);
        scientistTexture = createTexture(manager,SCIENTIST_FILE,false);
        robotTexture = createTexture(manager,ROBOT_FILE,false);
        slimeTexture = createTexture(manager,SLIME_FILE, false);
        bulletTexture = createTexture(manager,BULLET_FILE,false);
        slimeballTexture = createTexture(manager,SLIMEBALL_FILE,false);
        backgroundTexture = new Texture(BACKGROUND_FILE);

        //UI Icons
        mopTexture = new Texture(MOP_FILE);
        sprayTexture = new Texture(SPRAY_FILE);
        vacuumTexture = new Texture(VACUUM_FILE);
        lidTexture = new Texture(LID_FILE);
        mopTextureSmall = new Texture(MOP_FILE_SMALL);
        sprayTextureSmall = new Texture(SPRAY_FILE_SMALL);
        vacuumTextureSmall = new Texture(VACUUM_FILE_SMALL);
        lidTextureSmall = new Texture(LID_FILE_SMALL);
        heartTexture = new Texture(HEART_FILE);
        tileTexture = new Texture(TILE_FILE);

        SoundController sounds = SoundController.getInstance();
        sounds.allocate(manager, BACKGROUND_TRACK_FILE);
        sounds.allocate(manager, JUMP_FILE);
        sounds.allocate(manager, PEW_FILE);
        sounds.allocate(manager, POP_FILE);
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
    private float stateTimer;
    private boolean isRunningRight;

    // TODO reform weapon class and move to mop class
    /** Disables setVelocity until knockback is finished */
    private static final int KNOCKBACK_TIMER = 15;

    LevelEditorParser level;

    ArrayList<Vector2> scientistPos;
    ArrayList<Vector2> slimePos;
    ArrayList<Vector2> robotPos;
    ArrayList<Vector2> wallRightPos;
    ArrayList<Vector2> wallLeftPos;
    ArrayList<Vector2> wallMidPos;

    // Physics objects for the game
    /** Reference to the character avatar */
    private JoeModel avatar;
    /** Reference to the goalDoor (for collision detection) */
    private Animation <TextureRegion> joeRunR;
    private Animation <TextureRegion> joeRunU;
    private Animation <TextureRegion> joeRunD;
    private Animation <TextureRegion> joeStand;
    private BoxObstacle goalDoor;
    /** Reference to the monsters */
    private EnemyModel[] enemies;
    /** List of all the input AI controllers */
    protected AIController[] controls;
    /** Game sectioned off into tiles for AI purposes */
    private Board board;

    /** Reference to the mopCart (for collision detection) */
    private BoxObstacle mopCart;

    /** Mark set to handle more sophisticated collision callbacks */
    protected ObjectSet<Fixture> sensorFixtures;

    /** For mop knockback force calculations*/
    private Vector2 knockbackForce = new Vector2();



    /**
     * Creates and initialize a new instance of the platformer game
     *
     * The game has default gravity and other settings
     */
    public FloorController() {
        super(DEFAULT_WIDTH,DEFAULT_HEIGHT,DEFAULT_GRAVITY);
        currentState = State.STANDING;
        previousState = State.STANDING;
        stateTimer = 0;
        isRunningRight = true;
        setDebug(false);
        setComplete(false);
        setFailure(false);
        world.setContactListener(this);
        sensorFixtures = new ObjectSet<Fixture>();
        level = new LevelEditorParser(LEVEL);
        scientistPos = level.getScientistPos();
        slimePos = level.getSlimePos();
        robotPos = level.getRobotPos();
        wallLeftPos = level.getWallLeftPos();
        wallRightPos = level.getWallRightPos();
        wallMidPos = level.getWallMidPos();

        //scientistContactTicks=0;
    }

    /**
     * Resets the status of the game so that we can play again.
     *
     * This method disposes of the world and creates a new one.
     */
    public void reset() {
        SoundController.getInstance().play(BACKGROUND_TRACK_FILE, BACKGROUND_TRACK_FILE, true, 0.4f);

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

        enemies=new EnemyModel[scientistPos.size() + robotPos.size() + slimePos.size()];
        controls = new AIController[scientistPos.size() + robotPos.size() + slimePos.size()];
        board = new Board(BOARD_WIDTH, BOARD_HEIGHT);
        populateLevel();
    }

    /**
     * Lays out the game geography.
     */
    private void populateLevel() {
        // Add level goal
        float dwidth  = goalTile.getRegionWidth()/scale.x;
        float dheight = goalTile.getRegionHeight()/scale.y;
        goalDoor = new BoxObstacle(board.boardToScreenX(level.getGoalDoorX()),board.boardToScreenY(level.getGoalDoorY()),dwidth,dheight);
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
        mopCart = new BoxObstacle(board.boardToScreenX(level.getMopCartX()), board.boardToScreenY(level.getMopCartY()),mopwidth,mopheight);
        mopCart.setBodyType(BodyDef.BodyType.StaticBody);
        mopCart.setDensity(0.0f);
        mopCart.setFriction(0.0f);
        mopCart.setRestitution(0.0f);
        mopCart.setSensor(true);
        mopCart.setDrawScale(scale);
        mopCart.setTexture(mopTile);
        mopCart.setName("mopCart");
        addObject(mopCart);

        mopcart_index_xlocation[0] = 890;
        mopcart_index_xlocation[1] = 960;

        /** Add names to list of weapons */
        list_of_weapons[0] = "mop";
        list_of_weapons[1] = "spray";
        list_of_weapons[2] = "vacuum";
        list_of_weapons[3] = "lid";
        mopcart[0] = "vacuum";
        mopcart[1] = "lid";

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
        wep_to_model.put("mop", new MopModel());
        wep_to_model.put("spray", new SprayModel());
        wep_to_model.put("vacuum", new VacuumModel());
        wep_to_model.put("lid", new LidModel());
        /** Load name -> in use dictionary */
        wep_in_use.put("mop", true);
        wep_in_use.put("spray", true);
        wep_in_use.put("vacuum", false);
        wep_in_use.put("lid", false);

        board.setTileTexture(tileTexture);

        // Create dude
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

        dwidth  = avatarTexture.getRegionWidth()/scale.x;
        dheight = avatarTexture.getRegionHeight()/scale.y;
        avatar = new JoeModel(board.boardToScreenX(level.getJoePosX()), board.boardToScreenY(level.getJoePosY()), dwidth, dheight);
        avatar.setDrawScale(scale);
        avatar.setTexture(avatarTexture);
        avatar.setName("joe");
        addObject(avatar);

        for (int ii=0; ii<scientistPos.size(); ii++){
            EnemyModel mon =new ScientistModel(board.boardToScreenX((int) scientistPos.get(ii).x), board.boardToScreenX((int) scientistPos.get(ii).y),
                    dwidth, dheight, ii);
            mon.setDrawScale(scale);
            mon.setTexture(scientistTexture);
            addObject(mon);
            enemies[ii]=mon;
        }
        for (int ii=0; ii<robotPos.size(); ii++){
            EnemyModel mon =new RobotModel(board.boardToScreenX((int) robotPos.get(ii).x), board.boardToScreenX((int) robotPos.get(ii).y),
                    dwidth, dheight, scientistPos.size()+ii);
            mon.setDrawScale(scale);
            mon.setTexture(robotTexture);
            addObject(mon);
            enemies[scientistPos.size()+ii]=mon;
        }
        for (int ii=0; ii<slimePos.size(); ii++){
            EnemyModel mon =new SlimeModel(board.boardToScreenX((int) slimePos.get(ii).x), board.boardToScreenX((int) slimePos.get(ii).y),
                    dwidth, dheight, scientistPos.size()+robotPos.size()+ii);
            mon.setDrawScale(scale);
            mon.setTexture(slimeTexture);
            addObject(mon);
            enemies[scientistPos.size()+robotPos.size()+ii]=mon;
        }
        for (EnemyModel s: enemies){
            if (s!=null) {controls[s.getId()]=new AIController(s.getId(), board, enemies, avatar);}
        }

        String pname = "wall";
        dwidth  = wallMidTexture.getRegionWidth()/scale.x;
        dheight = wallMidTexture.getRegionHeight()/scale.y;
        float offset;

        offset = -(TILE_WIDTH * TILE_SCALE*(1 - WALL_THICKNESS_SCALE))/2;
        for (int ii = 0; ii < wallMidPos.size(); ii++) {
            BoxObstacle obj;
            float x = board.boardToScreenX((int) wallMidPos.get(ii).x);
            float y = board.boardToScreenY((int) wallMidPos.get(ii).y) + offset/32;
            obj = new BoxObstacle(x, y, dwidth, dheight * WALL_THICKNESS_SCALE);
            obj.setBodyType(BodyDef.BodyType.StaticBody);
            obj.setDensity(BASIC_DENSITY);
            obj.setFriction(BASIC_FRICTION);
            obj.setRestitution(BASIC_RESTITUTION);
            obj.setDrawScale(scale);
            obj.setTexture(wallMidTexture, 0, offset);
            obj.setName(pname+ii);
            addObject(obj);
        }

        for (int ii = 0; ii < wallLeftPos.size(); ii++) {
            BoxObstacle obj;
            float x = board.boardToScreenX((int) wallLeftPos.get(ii).x) + offset/32;
            float y = board.boardToScreenY((int) wallLeftPos.get(ii).y);
            obj = new BoxObstacle(x, y, dwidth * WALL_THICKNESS_SCALE, dheight);
            obj.setBodyType(BodyDef.BodyType.StaticBody);
            obj.setDensity(BASIC_DENSITY);
            obj.setFriction(BASIC_FRICTION);
            obj.setRestitution(BASIC_RESTITUTION);
            obj.setDrawScale(scale);
            obj.setTexture(wallLeftTexture, offset, 0);
            obj.setName(pname+ii);
            addObject(obj);
        }

        offset = -offset;
        for (int ii = 0; ii < wallRightPos.size(); ii++) {
            BoxObstacle obj;
            float x = board.boardToScreenX((int) wallRightPos.get(ii).x) + offset/32;
            float y = board.boardToScreenY((int) wallRightPos.get(ii).y);
            obj = new BoxObstacle(x, y, dwidth * WALL_THICKNESS_SCALE, dheight);
            obj.setBodyType(BodyDef.BodyType.StaticBody);
            obj.setDensity(BASIC_DENSITY);
            obj.setFriction(BASIC_FRICTION);
            obj.setRestitution(BASIC_RESTITUTION);
            obj.setDrawScale(scale);
            obj.setTexture(wallRightTexture, offset, 0);
            obj.setName(pname+ii);
            addObject(obj);
        }
    }

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
     * @param delta Number of seconds since last animation frame
     */
    public void update(float dt) {
        if(avatar.getHP()<=0) {
            avatar.setAlive(false);
            avatar.markRemoved(true);
            setFailure(true);
        } else {
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

            avatar.setVelocity();
            avatar.setTexture(getFrame(dt));
        }

        enemyUpdate();

        SoundController.getInstance().update();
    }

    /**
     * Update function for Joe when he at the mop cart
     */
    private void joeAtMopCartUpdate() {
        //recharge durability of weapons
        avatar.getWep1().durability = avatar.getWep1().getMaxDurability();
        avatar.getWep2().durability = avatar.getWep2().getMaxDurability();

        //move mop cart index
        if (avatar.isLeft()) {
            System.out.println("Move mop index left");
            if (mopcart_index == 1) { mopcart_index = 0; }
            else if (mopcart_index == 0) { mopcart_index = 1; }
        } else if (avatar.isRight()) {
            System.out.println("Move mop index right");
            if (mopcart_index == 0) { mopcart_index = 1; }
            else if (mopcart_index == 1) { mopcart_index = 0; }
        }
        // swapping weapon
        if (avatar.isSwapping()) {
            //get weapon at index
            String swapping_weapon_name = mopcart[mopcart_index];
            System.out.print(swapping_weapon_name);
            WeaponModel swapping_weapon = wep_to_model.get(swapping_weapon_name);

            //set all new weapons
            WeaponModel old_primary = avatar.getWep1();
            WeaponModel old_secondary = avatar.getWep2();
            avatar.setWep1(swapping_weapon);
            avatar.setWep2(old_primary);
            mopcart[mopcart_index] = old_secondary.name;
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
                    if (s.getStunTicks()<=150) {action=CONTROL_NO_ACTION; s.setMovementY(0); s.setMovementX(0);}
                    else {s.resetStunTicks(); s.setStunned(false);}
                }

                performAction(s, action);
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
            s.resetAttackAniFrame();
        }
        if (action == CONTROL_MOVE_LEFT) {
            s.setMovementX(-s.getVelocity());
            s.resetAttackAniFrame();
        }
        if (action == CONTROL_MOVE_UP) {
            s.setMovementY(s.getVelocity());
            s.resetAttackAniFrame();
        }
        if (action == CONTROL_MOVE_RIGHT) {
            s.setMovementX(s.getVelocity());
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
                s.resetAttackAniFrame();
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
        }
        else
            bullet.setVY(speedy);
        addQueuedObject(bullet);

        SoundController.getInstance().play(PEW_FILE, PEW_FILE, false, EFFECT_VOLUME);
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
        addQueuedObject(bullet);

        SoundController.getInstance().play(PEW_FILE, PEW_FILE, false, EFFECT_VOLUME);
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
     * Only called when the lid collids with an enemy.
     *
     * @param lid the lid to be removed
     * @param enemy the enemy that has been hit
     */
    public void removeLid(Obstacle lid,EnemyModel enemy) {
        float knockbackx = 10f;
        float knockbackx2 = (lid.getX() > enemy.getX() ? -knockbackx : knockbackx);
        knockbackForce.set(knockbackx2,0f);
        lid.markRemoved(true);
        avatar.setHasLid(true);
        SoundController.getInstance().play(POP_FILE,POP_FILE,false,EFFECT_VOLUME);
        enemy.decrHP();
        enemy.setKnockbackTimer(KNOCKBACK_TIMER);
        enemy.applyImpulse(knockbackForce);
        if (enemy.getHP() <= 0) {
            controls[enemy.getId()]=null;
            enemy.markRemoved(true);
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
    }

    public void attack(WeaponModel wep) {
        if (wep == null) {
            return;
        } else if (wep instanceof MopModel) {
            MopModel mop = (MopModel) wep;
            if (mop.getDurability() != 0) {
                SoundController.getInstance().play(PEW_FILE, PEW_FILE, false, EFFECT_VOLUME);
                for (EnemyModel s : enemies) {
                    int horiGap = board.screenToBoardX(avatar.getX()) - board.screenToBoardX(s.getX());
                    int vertiGap = board.screenToBoardY(avatar.getY()) - board.screenToBoardY(s.getY());
                    boolean case1 = Math.abs(horiGap)<=2 && horiGap>=0 && avatar.isLeft() && Math.abs(vertiGap)<= 1;
                    boolean case2 = Math.abs(horiGap)<=2 && horiGap<=0 && avatar.isRight() && Math.abs(vertiGap)<= 1;
                    boolean case3 = Math.abs(vertiGap)<=2 && vertiGap>=0 && avatar.isDown() && Math.abs(horiGap)<= 1;
                    boolean case4 = Math.abs(vertiGap)<=2 && vertiGap<=0 && avatar.isUp() && Math.abs(horiGap)<= 1;

                    if ((case1 || case2 || case3 || case4)) {
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
                        mop.decrDurability();
                    }
                }
            }
        } else if (wep instanceof SprayModel) {
            SoundController.getInstance().play(PEW_FILE, PEW_FILE, false, EFFECT_VOLUME);
            SprayModel spray = (SprayModel) wep;
            if (spray.getDurability() != 0) {
                spray.decrDurability();
                for (EnemyModel s : enemies) {
                    int horiGap = board.screenToBoardX(avatar.getX()) - board.screenToBoardX(s.getX());
                    int vertiGap = board.screenToBoardY(avatar.getY()) - board.screenToBoardY(s.getY());
                    boolean case1 = Math.abs(horiGap) <= 4 && horiGap >= 0 && avatar.isLeft() && Math.abs(vertiGap)<= 1;
                    boolean case2 = Math.abs(horiGap) <= 4 && horiGap <= 0 && avatar.isRight() && Math.abs(vertiGap)<= 1;
                    boolean case3 = Math.abs(vertiGap) <= 4 && vertiGap >= 0 && avatar.isDown() && Math.abs(horiGap)<= 1;
                    boolean case4 = Math.abs(vertiGap) <= 4 && vertiGap <= 0 && avatar.isUp() && Math.abs(horiGap)<= 1;

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
        } else if (wep instanceof LidModel) {
            LidModel lid = (LidModel) wep;
            if (lid.getDurability() != 0 && avatar.getHasLid()) {
                createBullet(avatar);
                avatar.setHasLid(false);
                lid.decrDurability();
            }
        } else if (wep instanceof VacuumModel) {
                VacuumModel vacuum = (VacuumModel) wep;
                if (vacuum.getDurability() != 0){
                    System.out.println("vacuum");
                    vacuum.decrDurability();
                    for (EnemyModel s : enemies){
                        int horiGap = board.screenToBoardX(avatar.getX()) - board.screenToBoardX(s.getX());
                        int vertiGap = board.screenToBoardY(avatar.getY()) - board.screenToBoardY(s.getY());
                        boolean case1 = Math.abs(horiGap) <= 10 && horiGap >= 0 && avatar.isLeft() && Math.abs(vertiGap) <=1;
                        boolean case2 = Math.abs(horiGap) <= 10 && horiGap <= 0 && avatar.isRight() && Math.abs(vertiGap) <=1;
                        boolean case3 = Math.abs(vertiGap) <= 10 && vertiGap >= 0 && avatar.isDown() && Math.abs(horiGap) <=1;
                        boolean case4 = Math.abs(vertiGap) <= 10 && vertiGap <= 0 && avatar.isUp() && Math.abs(horiGap) <=1;
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
            }
            if (bd2.getName().equals("lid") && (bd1 == avatar) ) {
                removeBullet(bd2);
                avatar.setHasLid(true);
            }

            if (bd1.getName().equals("slimeball") && bd2 == avatar) {
                removeBullet(bd1);
                avatar.decrHP();
            } else if (bd1.getName().equals("slimeball") && !(bd2 instanceof EnemyModel)) {
                removeBullet(bd1);
            }

            if (bd2.getName().equals("slimeball") && bd1 == avatar) {
                removeBullet(bd2);
                avatar.decrHP();
            } else if(bd2.getName().equals("slimeball") && !(bd1 instanceof EnemyModel)) {
                removeBullet(bd2);
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

        Object bd1 = body1.getUserData();
        Object bd2 = body2.getUserData();

        if ((bd1 == avatar   && bd2 == mopCart) ||
                (bd1 == mopCart && bd2 == avatar)) {
            avatar.setAtMopCart(false);
        }

        if ((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
                (avatar.getSensorName().equals(fd1) && avatar != bd2)) {
            sensorFixtures.remove(avatar == bd1 ? fix2 : fix1);
        }
    }


    public void draw(float delta) {
        GameCanvas canvas = super.getCanvas();

        canvas.clear();
        //LEVEL SCROLLING CODE COPIED FROM WALKER
        //might not be in the right place (?)
//        Affine2 oTran = new Affine2();
//        oTran.setToTranslation(object.getPosition());
//        Affine2 wTran = new Affine2();
//        Vector2 wPos = viewWindow.getPosition();
//        wTran.setToTranslation(-wPos.x,-wPos.y);
//        oTran.mul(wTran);
        canvas.begin();

        board.draw(canvas);

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
                    canvas.getWidth()/2 + 340, canvas.getHeight()/2 + 180, 0, .18f, .34f);
            displayFont.getData().setScale(0.5f);
            canvas.drawText("Mop Cart", displayFont,
                    canvas.getWidth()/2 + 375, canvas.getHeight()/2 + 280);
            displayFont.getData().setScale(1.0f);

            //RETRIEVE MOP CART WEAPONS
            String[] draw_mopcart = new String[2];
            int unused_indexer = 0;
            for (String wep: mopcart) {
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
            canvas.draw(heartTexture, current_xlocation, canvas.getHeight()/2 + 170);
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

    public enum State {STANDING, RUNNINGR,RUNNINGU,RUNNINGD}
    public State currentState;
    public State previousState;

    public State getState(){
        if ((avatar.getMovementX()!=0 && avatar.getMovementY()==0)||(avatar.getMovementX()!=0 && avatar.getMovementY()!=0)){
            return State.RUNNINGR;
        }
        else if (avatar.getMovementX()==0 && avatar.getMovementY()>0) {
            return State.RUNNINGU;
        }
        else if (avatar.getMovementX()==0 && avatar.getMovementY()<0) {
            return State.RUNNINGD;
        }
        else return State.STANDING;
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
            default:
                region = joeStand.getKeyFrame(stateTimer,true);
                break;
        }
        if (((avatar.getMovementX() < 0 && !isRunningRight))&& !region.isFlipX()){
            System.out.println(avatar.getMovementX());
            region.flip(true,false);
            isRunningRight = false;
        }
        else if (((avatar.getMovementX() > 0 && isRunningRight))&& region.isFlipX()){
            region.flip(true,false);
            System.out.print(region.isFlipX());
            isRunningRight = true;
        }
        stateTimer = currentState == previousState ? stateTimer + dt : 0;
        previousState = currentState;
        return region;
    }

}
