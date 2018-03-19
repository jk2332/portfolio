/*
 * PlatformController.java
 *
 * This is one of the files that you are expected to modify. Please limit changes to
 * the regions that say INSERT CODE HERE.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.physics.floor;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSorter;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
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
import edu.cornell.gdiac.physics.floor.monster.*;

import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

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
    /** The texture file for the character avatar (no animation) */
    private static final String DUDE_FILE  = "floor/joe.png";
    /** The texture file for the character avatar walking */
    private static final String DUDE_WALKING_FILE  = "floor/janitor-walk-1.gif";
    /** The texture file for the character avatar walking */
    private static final String SCIENTIST_FILE  = "floor/scientist.png";
    private static final String ROBOT_FILE = "floor/robot.png";
    /** The texture file for the spinning barrier */
    private static final String BARRIER_FILE = "floor/barrier.png";
    /** The texture file for the bullet */
    private static final String BULLET_FILE  = "floor/lid.png";
    private static final String SPRAY_TEMP_FILE  = "floor/spray.png";
    /** The texture file for the bridge plank */
    private static final String ROPE_FILE  = "floor/ropebridge.png";
    private static final String BACKGROUND_FILE = "shared/loading.png";
    private static final String TILE_FILE = "shared/basic-tile.png";

    /** The texture files for the UI icons */
    private static final String MOP_FILE  = "floor/ui-mop.png";
    private static final String SPRAY_FILE  = "floor/ui-spray.png";
    private static final String VACUUM_FILE  = "floor/ui-vacuum.png";
    private static final String LID_FILE  = "floor/ui-lid.png";
    private static final String HEART_FILE  = "floor/sponge.png";

    /** The sound file for a jump */
    private static final String JUMP_FILE = "floor/jump.mp3";
    /** The sound file for a bullet fire */
    private static final String PEW_FILE = "floor/pew.mp3";
    /** The sound file for a bullet collision */
    private static final String POP_FILE = "floor/plop.mp3";

    private int WALL_THICKNESS = 64;
    private int NUM_OF_SCIENTISTS = 4;
    private int NUM_OF_ROBOTS = 2;
    private int BOARD_WIDTH=1024/WALL_THICKNESS;
    private int BOARD_HEIGHT=576/WALL_THICKNESS;
    /** Offset for the UI on the screen */
    private static final float UI_OFFSET   = 5.0f;

    public static int CONTROL_NO_ACTION = 0;
    public static int CONTROL_MOVE_LEFT = 1;
    public static int CONTROL_MOVE_RIGHT = 2;
    public static int CONTROL_MOVE_UP = 4;
    public static int CONTROL_MOVE_DOWN = 8;
    public static int CONTROL_FIRE = 16;

    /** Texture asset for character avatar */
    private TextureRegion avatarTexture;
    /** Texture asset for character avatar */
    private TextureRegion avatarWalkingTexture;
    /** Texture asset for character avatar */
    private TextureRegion scientistTexture;
    private TextureRegion robotTexture;
    /** Texture asset for the bullet */
    private TextureRegion bulletTexture;
    /** Texture asset for the mop cart background */
    private Texture backgroundTexture;

    /** Texture Assets for UI Icons */
    private Texture mopTexture;
    private Texture sprayTexture;
    private Texture vacuumTexture;
    private Texture lidTexture;
    private Texture heartTexture;
    /** Texture Asset for tiles */
    private Texture tileTexture;
    /** Weapon Name -> Texture Dictionary*/
    HashMap<String, Texture> wep_to_texture = new HashMap<String, Texture>();
    HashMap<String, WeaponModel> wep_to_model = new HashMap<String, WeaponModel>();
    HashMap<String, Boolean> wep_in_use = new HashMap<String, Boolean>();
    String[] list_of_weapons = new String[4];

    private long scientistContactTicks;
    private long stunTicks;

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
        manager.load(DUDE_FILE, Texture.class);
        assets.add(DUDE_FILE);
        // manager.load(DUDE_WALKING_FILE, Texture.class); TODO
        // assets.add(DUDE_WALKING_FILE);
        manager.load(SCIENTIST_FILE, Texture.class);
        assets.add(SCIENTIST_FILE);
        manager.load(ROBOT_FILE, Texture.class);
        assets.add(ROBOT_FILE);
        manager.load(BARRIER_FILE, Texture.class);
        assets.add(BARRIER_FILE);
        manager.load(BULLET_FILE, Texture.class);
        assets.add(BULLET_FILE);
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
        assets.add(MOP_FILE);
        manager.load(HEART_FILE, Texture.class);
        assets.add(HEART_FILE);
        manager.load(TILE_FILE, Texture.class);
        assets.add(TILE_FILE);

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

        avatarTexture = createTexture(manager,DUDE_FILE,false);
        // avatarWalkingTexture = createTexture(manager,DUDE_WALKING_FILE,false); TODO
        scientistTexture = createTexture(manager,SCIENTIST_FILE,false);
        robotTexture = createTexture(manager,ROBOT_FILE,false);
        bulletTexture = createTexture(manager,BULLET_FILE,false);
        backgroundTexture = new Texture(BACKGROUND_FILE);

        //UI Icons
        mopTexture = new Texture(MOP_FILE);
        sprayTexture = new Texture(SPRAY_FILE);
        vacuumTexture = new Texture(VACUUM_FILE);
        lidTexture = new Texture(LID_FILE);
        heartTexture = new Texture(HEART_FILE);
        tileTexture = new Texture(TILE_FILE);

        SoundController sounds = SoundController.getInstance();
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
    /** The volume for sound effects */
    private static final float EFFECT_VOLUME = 0.8f;

    /** disables setVelocity until knockback is finished */
    private static final int KNOCKBACK_TIMER = 15;

    // Since these appear only once, we do not care about the magic numbers.
    // In an actual game, this information would go in a data file.
    // Wall vertices

    private static final float[][] WALLS = {

            {16.0f, 18.0f, 16.0f, 17.0f,  1.0f, 17.0f,
                    1.0f,  0.0f,  0.0f,  0.0f,  0.0f, 18.0f},
            {32.0f, 18.0f, 32.0f,  0.0f, 31.0f,  0.0f,
                    31.0f, 17.0f, 16.0f, 17.0f, 16.0f, 18.0f},
            {1.0f, 0.0f, 1.0f,  1.0f, 31.0f,  1.0f,
                    31.0f, 0.0f}

    };

    /** The outlines of all of the platforms */
    private static final float[][] PLATFORMS = {
            /**
            { 1.0f, 3.0f, 6.0f, 3.0f, 6.0f, 2.5f, 1.0f, 2.5f},
            { 6.0f, 4.0f, 9.0f, 4.0f, 9.0f, 2.5f, 6.0f, 2.5f},
            {23.0f, 4.0f,31.0f, 4.0f,31.0f, 2.5f,23.0f, 2.5f},
            {26.0f, 5.5f,28.0f, 5.5f,28.0f, 5.0f,26.0f, 5.0f},
            {29.0f, 7.0f,31.0f, 7.0f,31.0f, 6.5f,29.0f, 6.5f},
            {24.0f, 8.5f,27.0f, 8.5f,27.0f, 8.0f,24.0f, 8.0f},
            {29.0f,10.0f,31.0f,10.0f,31.0f, 9.5f,29.0f, 9.5f},
            {23.0f,11.5f,27.0f,11.5f,27.0f,11.0f,23.0f,11.0f},
            {19.0f,12.5f,23.0f,12.5f,23.0f,12.0f,19.0f,12.0f},
            { 1.0f,12.5f, 7.0f,12.5f, 7.0f,12.0f, 1.0f,12.0f}
             **/
    };

    // Other game objects
    /** The goal door position */
    private static Vector2 GOAL_POS = new Vector2(29.0f,15.0f);
    /** The mop cart  position */
    private static Vector2 MopCart_POS = new Vector2(10.0f,14.0f);
    /** The position of the spinning barrier */
    private static Vector2 SPIN_POS = new Vector2(13.0f,12.5f);
    /** The initial position of the dude */
    private static Vector2 DUDE_POS = new Vector2(2.5f, 5.0f);
    /** The position of the rope bridge */
    private static Vector2 BRIDGE_POS  = new Vector2(9.0f, 3.8f);

    // Physics objects for the game
    /** Reference to the character avatar */
    private JoeModel avatar;
    /** Reference to the goalDoor (for collision detection) */
    private BoxObstacle goalDoor;
    /** Reference to the monsters */
    private EnemyModel[] enemies;
    /** List of all the input AI controllers */
    protected AIController[] controls;

    private Board board;

    /** Reference to the mopCart (for collision detection) */
    private BoxObstacle mopCart;


    private boolean atMopCart;
    private boolean isLidHand;
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
        setDebug(false);
        setComplete(false);
        setAtMopCart(false);
        setFailure(false);
        setLid(true);
        world.setContactListener(this);
        sensorFixtures = new ObjectSet<Fixture>();
        scientistContactTicks=0;
    }

    /**
     * Resets the status of the game so that we can play again.
     *
     * This method disposes of the world and creates a new one.
     */
    public void reset() {
        Vector2 gravity = new Vector2(world.getGravity() );

        for(Obstacle obj : objects) {
            obj.deactivatePhysics(world);
        }
        objects.clear();
        addQueue.clear();
        world.dispose();

        world = new World(gravity,false);
        world.setContactListener(this);
        setAtMopCart(false);
        setComplete(false);
        setFailure(false);

        enemies=new EnemyModel[NUM_OF_SCIENTISTS+NUM_OF_ROBOTS];
        controls = new AIController[NUM_OF_SCIENTISTS+NUM_OF_ROBOTS];
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
        goalDoor = new BoxObstacle(GOAL_POS.x,GOAL_POS.y,dwidth,dheight);
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
        mopCart = new BoxObstacle(MopCart_POS.x,MopCart_POS.y,mopwidth,mopheight);
        mopCart.setBodyType(BodyDef.BodyType.StaticBody);
        mopCart.setDensity(0.0f);
        mopCart.setFriction(0.0f);
        mopCart.setRestitution(0.0f);
        mopCart.setSensor(true);
        mopCart.setDrawScale(scale);
        mopCart.setTexture(mopTile);
        mopCart.setName("mopCart");
        addObject(mopCart);

        /** Add names to list of weapons */
        list_of_weapons[0] = "mop";
        list_of_weapons[1] = "spray";
        list_of_weapons[2] = "vacuum";
        list_of_weapons[3] = "lid";
        /** Load name -> texture dictionary */
        wep_to_texture.put("mop", mopTexture);
        wep_to_texture.put("spray", sprayTexture);
        wep_to_texture.put("vacuum", vacuumTexture);
        wep_to_texture.put("lid", lidTexture);
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

        String wname = "wall";
        for (int ii = 0; ii < WALLS.length; ii++) {
            PolygonObstacle obj;
            obj = new PolygonObstacle(WALLS[ii], 0, 0);
            obj.setBodyType(BodyDef.BodyType.StaticBody);
            obj.setDensity(BASIC_DENSITY);
            obj.setFriction(BASIC_FRICTION);
            obj.setRestitution(BASIC_RESTITUTION);
            obj.setDrawScale(scale);
            obj.setTexture(earthTile);
            obj.setName(wname+ii);
            addObject(obj);
        }

        String pname = "platform";
        for (int ii = 0; ii < PLATFORMS.length; ii++) {
            PolygonObstacle obj;
            obj = new PolygonObstacle(PLATFORMS[ii], 0, 0);
            obj.setBodyType(BodyDef.BodyType.StaticBody);
            obj.setDensity(BASIC_DENSITY);
            obj.setFriction(BASIC_FRICTION);
            obj.setRestitution(BASIC_RESTITUTION);
            obj.setDrawScale(scale);
            obj.setTexture(earthTile);
            obj.setName(pname+ii);
            addObject(obj);
        }

        board.setTileTexture(tileTexture);

        // Create dude
        dwidth  = avatarTexture.getRegionWidth()/scale.x;
        dheight = avatarTexture.getRegionHeight()/scale.y;
        avatar = new JoeModel(DUDE_POS.x, DUDE_POS.y, dwidth, dheight);
        avatar.setDrawScale(scale);
        avatar.setTexture(avatarTexture);
        avatar.setName("joe");
        //avatar.setWalkingTexture(avatarWalkingTexture); // TODO drawing stuff slows frame rate?
        addObject(avatar);

        for (int ii=0; ii<NUM_OF_SCIENTISTS; ii++){
            EnemyModel mon =new ScientistModel((float) ((BOARD_WIDTH-1)*Math.random()+1), (float) ((BOARD_HEIGHT-1)*Math.random()+1),
                    dwidth, dheight, ii);
            mon.setDrawScale(scale);
            mon.setTexture(scientistTexture);
            mon.setName("scientist");
            addObject(mon);
            enemies[ii]=mon;
        }
        for (int ii=0; ii<NUM_OF_ROBOTS; ii++){
            EnemyModel mon =new RobotModel((float) ((BOARD_WIDTH-1)*Math.random()+1), (float) ((BOARD_HEIGHT-1)*Math.random()+1),
                    dwidth, dheight, NUM_OF_SCIENTISTS+ii);
            mon.setDrawScale(scale);
            mon.setTexture(robotTexture);
            mon.setName("robot");
            addObject(mon);
            enemies[NUM_OF_SCIENTISTS+ii]=mon;
        }
        for (EnemyModel s: enemies){
            if (s!=null) {controls[s.getId()]=new AIController(s.getId(), board, enemies, avatar);}
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
            avatar.markRemoved(true);
            setFailure(true);
        }

        // Process actions in object model
        avatar.setMovementX(InputController.getInstance().getHorizontal() *avatar.getForce());
        avatar.setMovementY(InputController.getInstance().getVertical() *avatar.getForce());
        //avatar.setJumping(InputController.getInstance().didPrimary());
        //avatar.setShooting(InputController.getInstance().didSecondary());
        avatar.setAttacking1(InputController.getInstance().didPrimary());
        avatar.setAttacking2(InputController.getInstance().didSecondary());
        avatar.setSwapping(InputController.getInstance().didTertiary());
        // Add a bullet if we fire
        if (avatar.isAttacking2() && avatar.getWep2().getDurability() > 0 && isLidHand && avatar.getWep2().getName() == "lid") {
            createBullet(avatar);
        }
        if (isAtMopCart()) {
            //recharge durability of weapon 1
            avatar.getWep1().durability = avatar.getWep1().getMaxDurability();
            avatar.getWep2().durability = avatar.getWep2().getMaxDurability();
        }
        if (avatar.isSwapping() && isAtMopCart()) {
            System.out.println("You are swapping weapons");
        }
        if (avatar.isAttacking1()) {
            attack(avatar.getWep1());
        } else if (avatar.isAttacking2()) {
            attack(avatar.getWep2());
        }
        avatar.setVelocity();
        boolean winning = true;
        for (EnemyModel s : enemies) {
            //this.adjustForDrift(s);
            //this.checkForDeath(s);
            if (this.controls[s.getId()] != null) {
                winning=false;
                int action = this.controls[s.getId()].getAction();
                if (s.getStunned()) {
                    System.out.println("stunned");
                    s.incrStunTicks();
                    if (s.getStunTicks()<=500) {action=CONTROL_NO_ACTION;}
                    else {s.resetStunTicks(); s.setStunned(false);}
                }
                if (action==CONTROL_NO_ACTION){
                    s.setMovementY(0); s.setMovementX(0);
                }
                if (action == CONTROL_MOVE_DOWN) {
                    //System.out.println("down");
                    s.setMovementY(-s.getForce());
                    s.resetAttackAniFrame();
                    scientistContactTicks=0;
                }
                if (action == CONTROL_MOVE_LEFT) {
                    //System.out.println("left");
                    s.setMovementX(-s.getForce());
                    s.resetAttackAniFrame();
                    scientistContactTicks=0;
                }
                if (action == CONTROL_MOVE_UP) {
                    //System.out.println("up");
                    s.setMovementY(s.getForce());
                    s.resetAttackAniFrame();
                    scientistContactTicks=0;
                }
                if (action == CONTROL_MOVE_RIGHT) {
                    //System.out.println("right");
                    s.setMovementX(s.getForce());
                    s.resetAttackAniFrame();
                    scientistContactTicks=0;

                }
                if (action==CONTROL_FIRE){
                    scientistContactTicks++;
                    s.setMovementX(0);
                    s.setMovementY(0);
                    s.coolDown(false);
                    if (scientistContactTicks%2==0) {
                        s.incrAttackAniFrame();
                        boolean hori = Math.abs(board.screenToBoardX(s.getPosition().x)-board.screenToBoardX(avatar.getPosition().x))<=1
                                && board.screenToBoardY(s.getPosition().y)==board.screenToBoardY(avatar.getPosition().y);
                        boolean verti = Math.abs(board.screenToBoardY(s.getPosition().y)-board.screenToBoardY(avatar.getPosition().y))<=1
                                && board.screenToBoardX(s.getPosition().x)==board.screenToBoardX(avatar.getPosition().x);
                        if((hori || verti) && s.endOfAttack()){
                            if (s.endOfAttack()) {
                                avatar.decrHP();
                                scientistContactTicks=0;
                            }
                        }
                    }
                }
                else {
                    s.coolDown(true);
                }
                if (s.getKnockbackTimer() == 0) {
                    s.setVelocity();
                } else {
                    s.decrKnockbackTimer();
                }
            }
        }
        if (winning) {setComplete(true);}
        SoundController.getInstance().update();
    }

    /**
     * Add a new bullet to the world and send it in the right direction.
     */
    private void createBullet(JoeModel player) {
        float offset = (player.isFacingRight() ? BULLET_OFFSET : -BULLET_OFFSET);
        float radius = bulletTexture.getRegionWidth()/(2.0f*scale.x);
        WheelObstacle bullet = new WheelObstacle(player.getX()+offset, player.getY(), radius);
        bullet.setName("lid");
        bullet.setDensity(HEAVY_DENSITY);
        bullet.setDrawScale(scale);
        bullet.setTexture(bulletTexture);
        bullet.setBullet(true);
        bullet.setGravityScale(0);

        // Compute position and velocity
        float speed  = (player.isFacingRight() ? BULLET_SPEED : -BULLET_SPEED);
        bullet.setVX(speed);
        addQueuedObject(bullet);

        SoundController.getInstance().play(PEW_FILE, PEW_FILE, false, EFFECT_VOLUME);
    }

    /**
     * Add a new bullet to the world and send it in the right direction.
     */
    private void createBullet(ScientistModel player) {
        float offset = (player.isFacingRight() ? BULLET_OFFSET : -BULLET_OFFSET);
        float radius = bulletTexture.getRegionWidth()/(2.0f*scale.x);
        WheelObstacle bullet = new WheelObstacle(player.getX()+offset, player.getY(), radius);

        bullet.setName("bullet");
        bullet.setDensity(HEAVY_DENSITY);
        bullet.setDrawScale(scale);
        bullet.setTexture(bulletTexture);
        bullet.setBullet(true);
        bullet.setGravityScale(0);

        // Compute position and velocity
        float speed  = (player.isFacingRight() ? BULLET_SPEED : -BULLET_SPEED);
        bullet.setVX(speed);
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
    public void removeBullet2(Obstacle bullet,EnemyModel scientist) {
        bullet.markRemoved(true);
        setLid(true);
        SoundController.getInstance().play(POP_FILE,POP_FILE,false,EFFECT_VOLUME);
        scientist.decrHP();
        if (scientist.getHP()<= 0) {
            controls[scientist.getId()]=null;
            scientist.markRemoved(true);
        }
    }
    public void removeBullet3(Obstacle bullet) {
        bullet.setVX(0.0f);
        bullet.setVY(0.0f);
        SoundController.getInstance().play(POP_FILE,POP_FILE,false,EFFECT_VOLUME);
    }

    public void attack(WeaponModel wep) { /* TODO is it okay to import weaponmodel here */
        if (wep == null) {
            return;
        } else if (wep instanceof MopModel) { // TODO same q for mop model
            MopModel mop = (MopModel) wep;
            if (mop.getDurability() != 0) {
                SoundController.getInstance().play(PEW_FILE, PEW_FILE, false, EFFECT_VOLUME);
                for (EnemyModel s : enemies) {
                    /*if (xDist <= 3 && yDist <= 3) {
                        System.out.println("x");
                        System.out.println(xDist);
                        System.out.println("y");
                        System.out.println(yDist);
                        System.out.println("");
                    }*/

                    /*boolean inRangeR = avatar.isFacingRight() && (board.screenToBoardX(avatar.getX())-board.screenToBoardX(s.getX())) >=-2
                            && (board.screenToBoardX(avatar.getX())-board.screenToBoardX(s.getX())) <=0
                            && Math.abs(board.screenToBoardY(avatar.getY()) - board.screenToBoardY(s.getY())) <= 2;
                    boolean inRangeL = !avatar.isFacingRight() && (board.screenToBoardX(avatar.getX())-board.screenToBoardX(s.getX())) <=2
                            && (board.screenToBoardX(avatar.getX())-board.screenToBoardX(s.getX())) >=0
                            && Math.abs(board.screenToBoardY(avatar.getY()) - board.screenToBoardY(s.getY())) <= 2;
                    boolean inRangeU = avatar.isFacingUp() && (board.screenToBoardY(avatar.getY())-board.screenToBoardY(s.getY())) >=-2
                            && (board.screenToBoardY(avatar.getY())-board.screenToBoardY(s.getY())) <=0
                            && Math.abs(board.screenToBoardX(avatar.getX())-board.screenToBoardX(s.getX())) <= 2;
                    boolean inRangeD = !avatar.isFacingUp() && (board.screenToBoardY(avatar.getY())-board.screenToBoardY(s.getY())) <=2
                            && (board.screenToBoardY(avatar.getY())-board.screenToBoardY(s.getY())) >=0
                            && Math.abs(board.screenToBoardX(avatar.getX())-board.screenToBoardX(s.getX())) <= 2;

                    inRange = inRangeR || inRangeL || inRangeU || inRangeD;*/

                    int horiGap = board.screenToBoardX(avatar.getX()) - board.screenToBoardX(s.getX());
                    int vertiGap = board.screenToBoardY(avatar.getY()) - board.screenToBoardY(s.getY());
                    boolean case1 = Math.abs(horiGap)<=1 && horiGap>0 && !avatar.isFacingRight() && vertiGap==0;
                    boolean case2 = Math.abs(horiGap)<=1 && horiGap<0 && avatar.isFacingRight() && vertiGap==0;
                    boolean case3 = Math.abs(vertiGap)<=1 && vertiGap>0 && !avatar.isFacingUp() && horiGap==0;
                    boolean case4 = Math.abs(vertiGap)<=1 && vertiGap<0 && avatar.isFacingUp() && horiGap==0;

                    if (!s.isRemoved() && (case1 || case2 || case3 || case4)) {
                        if (s.getHP() == 1) {
                            s.markRemoved(true);
                            controls[s.getId()]=null;
                        } else {
                            s.decrHP();
                        }
                        knockbackForce.set(horiGap * -30f, vertiGap * -30f);
                        //knockbackForce.nor();

                        s.applyForce(knockbackForce);
                        s.setKnockbackTimer(KNOCKBACK_TIMER);
                        System.out.println(knockbackForce);
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
//                    for (Obstacle obj : objects) {
//                        if (obj.isBullet()) {
                    int horiGap = board.screenToBoardX(avatar.getX()) - board.screenToBoardX(s.getX());
                    int vertiGap = board.screenToBoardY(avatar.getY()) - board.screenToBoardY(s.getY());
                    boolean case1 = Math.abs(horiGap) <= 2 && horiGap > 0 && !avatar.isFacingRight() && vertiGap == 0;
                    boolean case2 = Math.abs(horiGap) <= 2 && horiGap < 0 && avatar.isFacingRight() && vertiGap == 0;
                    boolean case3 = Math.abs(vertiGap) <= 2 && vertiGap > 0 && !avatar.isFacingUp() && horiGap == 0;
                    boolean case4 = Math.abs(vertiGap) <= 2 && vertiGap < 0 && avatar.isFacingUp() && horiGap == 0;

//                            boolean inRangeB = Math.abs(board.screenToBoardX(obj.getX()) - board.screenToBoardX(s.getX())) <= 2
//                                    && Math.abs(board.screenToBoardY(obj.getY()) - board.screenToBoardY(s.getY())) <= 2;
                    if (!s.isRemoved() && (case1 || case2 || case3 || case4)) {
                        if (s.getHP() == 1 ) {
                            s.markRemoved(true);
                        } else {
                            s.setStunned(true);
                            s.decrHP();
                        }

//                            }
//
//                        }
                    }
                }
            }
        } else if (wep instanceof LidModel) {
            LidModel lid = (LidModel) wep;
            if (lid.getDurability() != 0 && isLidHand) {
                lid.decrDurability();
                setLid(false);
                for (EnemyModel s : enemies) {
                    for (Obstacle obj : objects) {
                        if (obj.isBullet()) {
                            boolean inRangeB = Math.abs(board.screenToBoardX(obj.getX()) - board.screenToBoardX(s.getX())) <= 2
                                    && Math.abs(board.screenToBoardY(obj.getY()) - board.screenToBoardY(s.getY())) <= 2;

                            if (inRangeB && !s.isRemoved()) {
                                if (s.getHP() == 1) {
                                    s.markRemoved(true);
                                } else {
                                    s.decrHP();

                                }

                            }

                        }
                    }
                }
            }
        }else if (wep instanceof VacuumModel) {
                VacuumModel vacuum = (VacuumModel) wep;
                if (vacuum.getDurability() != 0){
                    System.out.println("vacuum");
                    vacuum.decrDurability();
                    for (EnemyModel s : enemies){
                        int horiGap = board.screenToBoardX(avatar.getX()) - board.screenToBoardX(s.getX());
                        int vertiGap = board.screenToBoardY(avatar.getY()) - board.screenToBoardY(s.getY());
                        boolean case1 = Math.abs(horiGap) <= 30 && horiGap > 0 && !avatar.isFacingRight() && Math.abs(vertiGap) < 1;
                        boolean case2 = Math.abs(horiGap) <= 30 && horiGap < 0 && avatar.isFacingRight() && Math.abs(vertiGap) < 1;
                        boolean case3 = Math.abs(vertiGap) <= 30 && vertiGap > 0 && !avatar.isFacingUp() && Math.abs(horiGap) < 1;
                        boolean case4 = Math.abs(vertiGap) <= 30 && vertiGap < 0 && avatar.isFacingUp() && Math.abs(horiGap) < 1;
                        if ((case1)) {
                            System.out.println("case1");
                            s.setMovementX(100);
                            s.setMovementY(0);
                            s.setVelocity();
                        }
                        if ((case2)) {
                            System.out.println("case2");
                            s.setMovementX(-100);
                            s.setMovementY(0);
                            s.setVelocity();
                        }
                        if ((case3)) {
                            System.out.println("case3");
                            s.setMovementX(0);
                            s.setMovementY(100);
                            s.setVelocity();
                        }
                        if ((case4)) {
                            System.out.println("case4");
                            s.setMovementX(0);
                            s.setMovementY(-100);
                            s.setVelocity();
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
                    removeBullet2(bd1,s);

                }
                if (bd2.getName().equals("lid") && bd1 == s) {
                    removeBullet2(bd2,s);

                }
                if (bd1.getName().equals("lid") && (bd2 != s) && (bd2 != avatar) ) {
                    removeBullet3(bd1);
                }

                if (bd2.getName().equals("lid") && ((bd1 != s)) && (bd1 != avatar) ) {
                    removeBullet3(bd2);
                }
            }
            if (bd1.getName().equals("lid") && (bd2 == avatar) ) {
                removeBullet(bd2);
                setLid(true);
            }
            if (bd2.getName().equals("lid") && (bd1 == avatar) ) {
                removeBullet(bd2);
                setLid(true);
            }

            // See if we have landed on the ground.
//			if ((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
//				(avatar.getSensorName().equals(fd1) && avatar != bd2)) {
//				avatar.setGrounded(true);
//				sensorFixtures.add(avatar == bd1 ? fix2 : fix1); // Could have more than one ground
//			}

            /**
            if (bd1 == avatar && (bd2 instanceof ScientistModel)) {
                scientistContactTicks++;
                System.out.println("in contact");
                ((ScientistModel) bd2).setInContact(true);
                if (((ScientistModel) bd2).isShooting()) {
                    System.out.println(scientistContactTicks);
                    if (scientistContactTicks%2==0L) {
                        ((ScientistModel) bd2).incrAttackAniFrame();
                        System.out.println("frame: "+ ((ScientistModel) bd2).getAttackAniFrame());
                        if(((ScientistModel) bd2).endOfAttack()){
                            ((JoeModel) bd1).decrHP();
                        }
                    }
                }
            }

            if ((bd1 instanceof ScientistModel) && bd2 == avatar) {
                System.out.println("in contact");
                scientistContactTicks++;
                ((ScientistModel) bd1).setInContact(true);
                if (((ScientistModel) bd1).isShooting()) {
                    System.out.println(scientistContactTicks);
                    if (scientistContactTicks%2==0L) {
                        ((ScientistModel) bd1).incrAttackAniFrame();
                        System.out.println("frame: "+ ((ScientistModel) bd1).getAttackAniFrame());
                        if(((ScientistModel) bd1).endOfAttack()){
                            ((JoeModel) bd2).decrHP();
                            System.out.println("decrHP");
                        }
                    }
                }
            }
             **/

            // Check for win condition
            if ((bd1 == avatar   && bd2 == goalDoor) ||
                    (bd1 == goalDoor && bd2 == avatar)) {
                setComplete(true);
            }
            if ((bd1 == avatar   && bd2 == mopCart) ||
                    (bd1 == mopCart && bd2 == avatar)) {
                setAtMopCart(true);
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
            setAtMopCart(false);
        }

        /**
        if (bd1 == avatar && (bd2 instanceof ScientistModel)) {
            ((ScientistModel) bd2).setInContact(false);
            ((ScientistModel) bd2).resetAttackAniFrame();
        }

        if ((bd1 instanceof ScientistModel) && bd2 == avatar) {
            ((ScientistModel) bd1).setInContact(false);
            ((ScientistModel) bd1).resetAttackAniFrame();
        }
        **/

        if ((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
                (avatar.getSensorName().equals(fd1) && avatar != bd2)) {
            sensorFixtures.remove(avatar == bd1 ? fix2 : fix1);
            if (sensorFixtures.size == 0) {
//				avatar.setGrounded(false);
            }
        }
    }


    public void draw(float delta) {
        super.draw(delta);
        GameCanvas canvas = super.getCanvas();

        canvas.begin();

        board.draw(canvas);


        for(Obstacle obj : objects) {
            obj.draw(canvas);
        }
//        String hpDisplay = "HP: " + avatar.getHP();
//        if (avatar.isAttacking2() && avatar.getWep2().getDurability() > 0 && avatar.getWep2().getName() == "spray"){
//            canvas.draw(sprayTexture);
//        }
        String hpDisplay = "HP:";
        String wep1Display;
        if (avatar.getWep1() != null) {
            wep1Display = "Weapon 1: ";
        } else {
            wep1Display = "";
        }
        String wep2Display;
        if (avatar.getWep2() != null) {
            wep2Display = "Weapon 2: ";
        } else {
            wep2Display = "";
        }

        displayFont.setColor(Color.WHITE);
//        canvas.drawText(hpDisplay, displayFont, UI_OFFSET, canvas.getHeight()-UI_OFFSET);
//        canvas.drawText(wep1Display, displayFont, UI_OFFSET, canvas.getHeight()-UI_OFFSET - 40);
//        canvas.drawText(wep2Display, displayFont, UI_OFFSET, canvas.getHeight()-UI_OFFSET - 60);

        /* Show Multiple HP and Mop Icons */
        int margin = 0;
        int HP = avatar.getHP();
        for (int j = 0; j < HP; j++) {
            canvas.draw(heartTexture, UI_OFFSET + 30 + margin, canvas.getHeight()-UI_OFFSET - 27);
            margin = margin + 25;
        }

        //get textures via hash map from weapons names
        String wep1FileName = avatar.getWep1().getName();
        String wep2FileName = avatar.getWep2().getName();
        Texture wep1Texture = wep_to_texture.get(wep1FileName);
        Texture wep2Texture = wep_to_texture.get(wep2FileName);
        //draw retrieved textures
        canvas.draw(wep1Texture, UI_OFFSET + 50, canvas.getHeight()-UI_OFFSET - 100);
        canvas.draw(wep2Texture, UI_OFFSET + 170, canvas.getHeight()-UI_OFFSET - 100);

        //draw weapon UI durability bars (currently temporary)
        String durability1 = Integer.toString(avatar.getWep1().getDurability());
        String maxDurability1 = Integer.toString(avatar.getWep1().getMaxDurability());
        String durability2 = Integer.toString(avatar.getWep2().getDurability());
        String maxDurability2 = Integer.toString(avatar.getWep2().getMaxDurability());
        canvas.drawText(durability1 + "/" + maxDurability1,
                displayFont, UI_OFFSET + 30, canvas.getHeight()-UI_OFFSET - 110);
        canvas.drawText(durability2 + "/" + maxDurability2,
                displayFont, UI_OFFSET + 150, canvas.getHeight()-UI_OFFSET - 110);

        displayFont.getData().setScale(0.5f);
        for (EnemyModel s : enemies) {
            if (!(s.isRemoved())) {
                canvas.drawText("" + s.getHP(), displayFont, s.getX() * scale.x, (s.getY() + 1) * scale.y);
            }
        }
        displayFont.getData().setScale(1.0f);

        if (atMopCart){
            // itemSwap = new Texture(PLAY_BTN_FILE);
            Color tint1 = Color.BLACK;
            canvas.draw(backgroundTexture, tint1, 10.0f, 14.0f,
                    canvas.getWidth()/2 + 340, canvas.getHeight()/2 + 200, 0, .18f, .28f);
            displayFont.getData().setScale(0.5f);
            canvas.drawText("Mop Cart", displayFont,
                    canvas.getWidth()/2 + 375, canvas.getHeight()/2 + 280);
            displayFont.getData().setScale(1.0f);

            //retrieve unused weapons
            String[] unused = new String[2];
            int unused_indexer = 0;
            for (String wep: list_of_weapons) {
                if (!wep_in_use.get(wep)) {
                    //if weapon not in use
                    unused[unused_indexer] = wep;
                    unused_indexer += 1;
                }
            }
            //draw unused weapons currently in cart
            Texture unused_wep1 = wep_to_texture.get(unused[0]);
            Texture unused_wep2 = wep_to_texture.get(unused[1]);
            canvas.draw(unused_wep1, canvas.getWidth()/2 + 360, canvas.getHeight()/2 + 200);
            canvas.draw(unused_wep2, canvas.getWidth()/2 + 430, canvas.getHeight()/2 + 200);

            //IF YOU SWAP
            if (avatar.isSwapping()) {
                //update what weapons are in use
                for (String wep: list_of_weapons) {
                    if (!wep_in_use.get(wep)) { wep_in_use.put(wep, true); }
                    else { wep_in_use.put(wep, false); }
                }
                //get the new weapons from hashmap and create
                WeaponModel new_wep1 = wep_to_model.get(unused[0]);
                WeaponModel new_wep2 = wep_to_model.get(unused[1]);
                //set the new weapons
                avatar.setWep1(new_wep1);
                avatar.setWep2(new_wep2);
            }
        }

        canvas.end();
    }

    /** Unused ContactListener method */
    public void postSolve(Contact contact, ContactImpulse impulse) {}
    /** Unused ContactListener method */
    public void preSolve(Contact contact, Manifold oldManifold) {}

    public void setAtMopCart(boolean value) {
        if (value) {
            System.out.println("Press O to Swap Weapons");
        }
        atMopCart = value;

        //get player durability and change it
    }

    public boolean isAtMopCart( ) {
        return atMopCart;
    }
    public void setLid(boolean value) {

       isLidHand = value;

        //get player durability and change it
    }

    public boolean isLid( ) {
        return isLidHand;
    }
}