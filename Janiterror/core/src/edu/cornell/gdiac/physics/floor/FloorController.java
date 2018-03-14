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
    /** The texture file for the spinning barrier */
    private static final String BARRIER_FILE = "floor/barrier.png";
    /** The texture file for the bullet */
    private static final String BULLET_FILE  = "floor/bullet.png";
    /** The texture file for the bridge plank */
    private static final String ROPE_FILE  = "floor/ropebridge.png";
    private static final String BACKGROUND_FILE = "shared/loading.png";
    /** The texture file for the mop icon */
    private static final String MOP_FILE  = "floor/mop.png";
    private static final String HEART_FILE  = "floor/heart.png";

    /** The sound file for a jump */
    private static final String JUMP_FILE = "floor/jump.mp3";
    /** The sound file for a bullet fire */
    private static final String PEW_FILE = "floor/pew.mp3";
    /** The sound file for a bullet collision */
    private static final String POP_FILE = "floor/plop.mp3";

    private int WALL_THICKNESS = 64;
    private int NUM_OF_ENEMIES=1;
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
    /** Texture asset for the bullet */
    private TextureRegion bulletTexture;
    /** Texture asset for the mop cart background */
    private Texture backgroundTexture;
    /** Texture Asset for Mop Icon */
    private Texture mopTexture;
    /** Texture Asset for Mop Icon */
    private Texture heartTexture;

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
        manager.load(BARRIER_FILE, Texture.class);
        assets.add(BARRIER_FILE);
        manager.load(BULLET_FILE, Texture.class);
        assets.add(BULLET_FILE);
        manager.load(ROPE_FILE, Texture.class);
        assets.add(ROPE_FILE);
        manager.load(MOP_FILE, Texture.class);
        assets.add(MOP_FILE);
        manager.load(HEART_FILE, Texture.class);
        assets.add(HEART_FILE);

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
        bulletTexture = createTexture(manager,BULLET_FILE,false);
        backgroundTexture = new Texture(BACKGROUND_FILE);
        mopTexture = new Texture(MOP_FILE);
        heartTexture = new Texture(HEART_FILE);

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
    private static final float  BULLET_OFFSET = 0.2f;
    /** The speed of the bullet after firing */
    private static final float  BULLET_SPEED = 20.0f;
    /** The volume for sound effects */
    private static final float EFFECT_VOLUME = 0.8f;

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
    private ScientistModel[] enemies;
    /** List of all the input AI controllers */
    protected AIController[] controls;

    private Board board;

    /** Reference to the mopCart (for collision detection) */
    private BoxObstacle mopCart;


    private boolean atMopCart;
    private boolean bulletTouch;
    /** Mark set to handle more sophisticated collision callbacks */
    protected ObjectSet<Fixture> sensorFixtures;

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
        setBulletTouch(false);
        world.setContactListener(this);
        sensorFixtures = new ObjectSet<Fixture>();
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
        setBulletTouch(false);
        enemies=new ScientistModel[NUM_OF_ENEMIES];
        controls = new AIController[NUM_OF_ENEMIES];
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

        // Create dude
        dwidth  = avatarTexture.getRegionWidth()/scale.x;
        dheight = avatarTexture.getRegionHeight()/scale.y;
        avatar = new JoeModel(DUDE_POS.x, DUDE_POS.y, dwidth, dheight);
        avatar.setDrawScale(scale);
        avatar.setTexture(avatarTexture);
        //avatar.setWalkingTexture(avatarWalkingTexture); // TODO drawing stuff slows frame rate?
        addObject(avatar);

        for (int ii=0; ii<NUM_OF_ENEMIES; ii++){
            ScientistModel mon =new ScientistModel((float) (BOARD_WIDTH*Math.random()), (float) (BOARD_HEIGHT*Math.random()), dwidth, dheight, ii);
            mon.setDrawScale(scale);
            mon.setTexture(scientistTexture);
            addObject(mon);
            enemies[ii]=mon;
        }
        for (ScientistModel s: enemies){
            controls[s.getId()]=new AIController(s.getId(), board, enemies, avatar);
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
        // Process actions in object model
        avatar.setMovementX(InputController.getInstance().getHorizontal() *avatar.getForce());
        avatar.setMovementY(InputController.getInstance().getVertical() *avatar.getForce());
        //avatar.setJumping(InputController.getInstance().didPrimary());
        //avatar.setShooting(InputController.getInstance().didSecondary());
        avatar.setAttacking1(InputController.getInstance().didPrimary());
        avatar.setAttacking2(InputController.getInstance().didSecondary());
        avatar.setSwapping(InputController.getInstance().didTertiary());
        // Add a bullet if we fire
        if (avatar.isAttacking2() && avatar.getWep2().getDurability() > 0) {
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
        avatar.applyForce();

        for (ScientistModel s : enemies) {
            //this.adjustForDrift(s);
            //this.checkForDeath(s);
            if (this.controls[s.getId()] != null) {
                int action = this.controls[s.getId()].getAction();
                s.update(dt);
                if (action==CONTROL_FIRE && s.canShoot()){
                    s.setMovementX(0);
                    s.setMovementY(0);
                }
                if (action == CONTROL_MOVE_DOWN) {
                    //System.out.println("down");
                    s.setMovementY(-s.getForce());
                }
                if (action == CONTROL_MOVE_LEFT) {
                    //System.out.println("left");
                    s.setMovementX(-s.getForce());
                }
                if (action == CONTROL_MOVE_UP) {
                    //System.out.println("up");
                    s.setMovementY(s.getForce());
                }
                if (action == CONTROL_MOVE_RIGHT) {
                    //System.out.println("right");
                    s.setMovementX(s.getForce());
                }
                s.applyForce();
            }
        }
        SoundController.getInstance().update();
    }

    /**
     * Add a new bullet to the world and send it in the right direction.
     */
    private void createBullet(JoeModel player) {
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
    public void removeBullet2(Obstacle bullet,ScientistModel scientist) {
        bullet.markRemoved(true);
        SoundController.getInstance().play(POP_FILE,POP_FILE,false,EFFECT_VOLUME);
        scientist.decrHP();
        if (scientist.getHP()<= 0) {
            scientist.markRemoved(true);
        }
    }

    public void attack(WeaponModel wep) { /* TODO is it okay to import weaponmodel here */
        if (wep == null) {
            return;
        } else if (wep instanceof MopModel) { // TODO same q for mop model
            MopModel mop = (MopModel) wep;
            if (mop.getDurability() != 0) {
                for (ScientistModel s : enemies) {
                    boolean inRange = Math.abs(board.screenToBoardX(avatar.getX()) - board.screenToBoardX(s.getX())) <= 2
                            && Math.abs(board.screenToBoardY(avatar.getY()) - board.screenToBoardY(s.getY())) <= 2;
                    if (inRange && !s.isRemoved()) {
                        if (s.getHP() == 1) {
                            s.markRemoved(true);
                        } else {
                            s.decrHP();
                        }
                        mop.decrDurability();
                    }
                }
            }
        } else if (wep instanceof SprayModel) {
            SprayModel spray = (SprayModel) wep;
            if (spray.getDurability() != 0) {
                spray.decrDurability();
                for (ScientistModel s : enemies) {
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
            } else if (wep instanceof LidModel) {

            } else if (wep instanceof VacuumModel) {

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
            for (ScientistModel s : enemies){
                if (bd1.getName().equals("bullet") && bd2 == s) {
                    removeBullet2(bd1,s);

                }
                if (bd2.getName().equals("bullet") && bd1 == s) {
                    removeBullet2(bd2,s);

                }
            }
            // Test bullet collision with world
            if (bd1.getName().equals("bullet") && bd2 != avatar) {
                removeBullet(bd1);
            }

            if (bd2.getName().equals("bullet") && bd1 != avatar) {
                removeBullet(bd2);
            }
            if (bd1.getName().equals("bullet") && (bd2 instanceof ScientistModel)) {
                setBulletTouch(true);
                removeBullet(bd1);
            }

            if (bd2.getName().equals("bullet") && (bd1 instanceof ScientistModel)) {
                setBulletTouch(true);
                removeBullet(bd2);
            }

            // See if we have landed on the ground.
//			if ((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
//				(avatar.getSensorName().equals(fd1) && avatar != bd2)) {
//				avatar.setGrounded(true);
//				sensorFixtures.add(avatar == bd1 ? fix2 : fix1); // Could have more than one ground
//			}

            if (bd1 == avatar && (bd2 instanceof ScientistModel)) {
                ((ScientistModel) bd2).setInContact(true);
                String result1 = "in contact/ state: "+controls[((ScientistModel) bd2).getId()].getAction();
                String result2="";
                if (controls[((ScientistModel) bd2).getId()].getAction()==16) {
                    ((JoeModel) bd1).decrHP();
                    result2 ="/ Decrement HP";
                }
                System.out.println(result1+result2);
            }

            if ((bd1 instanceof ScientistModel) && bd2 == avatar) {
                ((ScientistModel) bd1).setInContact(true);
                String result1 = "in contact/ state: "+controls[((ScientistModel) bd1).getId()].getAction();
                String result2="";
                if (controls[((ScientistModel) bd1).getId()].getAction()==16) {
                    ((JoeModel) bd2).decrHP();
                    result2 ="/ Decrement HP";
                }
                System.out.println(result1+result2);
            }

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

        if (bd1 == avatar && (bd2 instanceof ScientistModel)) {
            ((ScientistModel) bd2).setInContact(false);
            System.out.println("out of contact");
        }

        if ((bd1 instanceof ScientistModel) && bd2 == avatar) {
            ((ScientistModel) bd1).setInContact(false);
            System.out.println("out of contact");
        }

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
//        String hpDisplay = "HP: " + avatar.getHP();
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
        canvas.drawText(hpDisplay, displayFont, UI_OFFSET, canvas.getHeight()-UI_OFFSET);
        canvas.drawText(wep1Display, displayFont, UI_OFFSET, canvas.getHeight()-UI_OFFSET - 40);
        canvas.drawText(wep2Display, displayFont, UI_OFFSET, canvas.getHeight()-UI_OFFSET - 60);

        /* Show Multiple HP and Mop Icons */
        int margin = 0;
        int HP = avatar.getHP();
        for (int j = 0; j < HP; j++) {
            canvas.draw(heartTexture, UI_OFFSET + 70 + margin, canvas.getHeight()-UI_OFFSET - 30);
            margin = margin + 35;
        }
        int margin2 = 0;
        int durability = avatar.getWep1().getDurability();
        for (int j = 0; j < durability; j++) {
            canvas.draw(mopTexture, UI_OFFSET + 200 + margin2, canvas.getHeight()-UI_OFFSET - 70);
            margin2 = margin2 + 25;
        }
        int margin3 = 0;
        int durability2 = avatar.getWep2().getDurability();
        for (int j = 0; j < durability2; j++) {
            canvas.draw(mopTexture, UI_OFFSET + 200  + margin3, canvas.getHeight()-UI_OFFSET - 100);
            margin3 = margin3 + 25;
        }


        /* Durability Percent Bars */
        int max_durability = avatar.getWep1().getMaxDurability();
        int max_durability2 = avatar.getWep2().getMaxDurability();
        int min_durability = 0;
        float step = 1 / max_durability;

        /* Online Code Insert */
//        Pixmap pixmap = new Pixmap(100, 20, Pixmap.Format.RGBA8888);
//        pixmap.setColor(Color.RED);
//        pixmap.fill();
//        TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
//        pixmap.dispose();
//        ProgressBar.ProgressBarStyle progressBarStyle = new ProgressBar.ProgressBarStyle();
//        progressBarStyle.background = drawable;
//        pixmap = new Pixmap(0, 20, Pixmap.Format.RGBA8888);
//        pixmap.setColor(Color.GREEN);
//        pixmap.fill();
//        drawable = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
//        pixmap.dispose();
//        progressBarStyle.knob = drawable;
//
//        Pixmap pixmap2 = new Pixmap(100, 20, Pixmap.Format.RGBA8888);
//        pixmap2.setColor(Color.GREEN);
//        pixmap2.fill();
//        drawable = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap2)));
//        pixmap2.dispose();
//        progressBarStyle.knobBefore = drawable;
//
//        Stage stage = new Stage();
//        ProgressBar healthBar = new ProgressBar(0.0f, 1.0f, 0.01f, false, progressBarStyle);
//        healthBar.setValue(0.5f);
//        healthBar.setAnimateDuration(0.25f);
//        healthBar.setBounds(10, 10, 100, 20);
//        stage.addActor(healthBar);
//        stage.draw();
//        stage.act();
//        stage.dispose();


//        ProgressBar.ProgressBarStyle barstyle = new ProgressBar.ProgressBarStyle();
//        ProgressBar(min_durability, max_durability, step, false, ProgressBar.ProgressBarStyle style);

        displayFont.getData().setScale(0.5f);
        for (ScientistModel s : enemies) {
            if (!(s.isRemoved())) {
                canvas.drawText("" + s.getHP(), displayFont, s.getX() * scale.x, (s.getY() + 1) * scale.y);
            }
        }
        displayFont.getData().setScale(1.0f);

        if (atMopCart){
            // itemSwap = new Texture(PLAY_BTN_FILE);
            Color tint1 = Color.BLACK;
//		    Color tint2 = Color.ORANGE;
            canvas.draw(backgroundTexture, tint1, 10.0f, 14.0f,
                    canvas.getWidth()/2, canvas.getHeight()/2, 0, .5f, .5f);
            displayFont.setColor(Color.WHITE);
            canvas.drawText("MOP CART STUFF", displayFont, canvas.getWidth()/2 + 70, 3*canvas.getHeight()/4);
//            canvas.draw(itemSwap, tint2, itemSwap.getWidth()/2, itemSwap.getHeight()/2,
//                    canvas.width/10, canvas.height/2, 0, ITEM_SCALE*2, ITEM_SCALE*2);
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
    public void setBulletTouch(boolean value){
        bulletTouch = value;
    }
    public boolean isAtMopCart( ) {
        return atMopCart;
    }
    public boolean isBullettouch( ){
        return bulletTouch;
    }
}