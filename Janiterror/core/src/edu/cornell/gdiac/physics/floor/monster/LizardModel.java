/*
 * JoeModel.java
 *
 * You SHOULD NOT need to modify this file.  However, you may learn valuable lessons
 * for the rest of the lab by looking at it.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.physics.floor.monster;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import edu.cornell.gdiac.physics.*;
import edu.cornell.gdiac.physics.floor.weapon.WeaponModel;
import edu.cornell.gdiac.physics.obstacle.*;

/**
 * Player avatar for the plaform game.
 *
 * Note that this class returns to static loading.  That is because there are
 * no other subclasses that we might loop through.
 */
public class LizardModel extends CapsuleObstacle {
    // Physics constants
    /** The density of the character */
    private static final float DUDE_DENSITY = 1f;
    /** The factor to multiply by the input */
    private static final float DUDE_FORCE = 5;
    /** The dude is a slippery one */
    private static final float DUDE_FRICTION = 0.0f;
    /** The maximum character speed */
    private static final float DUDE_MAXSPEED = 7.0f;
    private static final int SHOOT_COOLDOWN = 40;
    /** Height of the sensor attached to the player's feet */
    private static final float SENSOR_HEIGHT = 0.05f;
    /** Identifier to allow us to track the sensor in ContactListener */
    private static final String SENSOR_NAME = "DudeGroundSensor";

    // This is to fit the image to a tigher hitbox
    /** The amount to shrink the body fixture (vertically) relative to the image */
    private static final float DUDE_VSHRINK = 0.95f;
    /** The amount to shrink the body fixture (horizontally) relative to the image */
    private static final float DUDE_HSHRINK = 0.7f;
    /** The amount to shrink the sensor fixture (horizontally) relative to the image */
    private static final float DUDE_SSHRINK = 0.6f;

    /** The amount of max HP a scientist has */
    private static final int MAX_HP = 2;

    /** The current horizontal movement of the character */
    private float   movementX;
    private float movementY;
    /** Which direction is the character facing */
    private boolean faceRight;
    private boolean faceUp;
    /** How long until we can shoot again */
    private int shootCooldown;
    /** Whether we are actively shooting */
    private boolean isShooting;
    /** Whether we are actively swapping */
    private boolean isSwapping;
    /** Ground sensor to represent our feet */
    private Fixture sensorFixture;
    private PolygonShape sensorShape;
    private int id;
    private int attackAnimationFrame;
    private long ticks;

    /* Whether scientist is in contact with Joe (determines whether scientist is attacking)*/
    private boolean inContact;

    /* The amount of HP the scientist has left */
    private int hp;

    /** The current weapons Joe is holding */
    private WeaponModel[] weps = new WeaponModel[2];

    /** Cache for internal force calculations */
    private Vector2 forceCache = new Vector2();

    /**
     * Returns left/right movement of this character.
     *
     * This is the result of input times dude force.
     *
     * @return left/right movement of this character.
     */
    public float getMovementX() {
        return movementX;
    }

    /**
     * Returns up/down movement of this character.
     *
     * This is the result of input times dude force.
     *
     * @return up/down movement of this character.
     */
    public float getMovementY() {
        return movementY;
    }

    /**
     * Sets left/right movement of this character.
     *
     * This is the result of input times dude force.
     *
     * @param value left/right movement of this character.
     */
    public void setMovementX(float value) {
        movementX = value;
        // Change facing if appropriate
        if (movementX < 0) {
            faceRight = false;
        } else if (movementX > 0) {
            faceRight = true;
        }
    }

    /**
     * Sets up/down movement of this character.
     *
     * This is the result of input times dude force.
     *
     * @param value up/down movement of this character.
     */
    public void setMovementY(float value) {
        movementY = value;
        // Change facing if appropriate
        if (movementY < 0) {
            faceUp = false;
        } else if (movementY > 0) {
            faceUp = true;
        }
    }

    /**
     * Returns how much force to apply to get the dude moving
     *
     * Multiply this by the input to get the movement value.
     *
     * @return how much force to apply to get the dude moving
     */
    public float getForce() {
        return DUDE_FORCE;
    }

    /**
     * Returns ow hard the brakes are applied to get a dude to stop moving
     *
     * @return ow hard the brakes are applied to get a dude to stop moving
     */
    public boolean canShoot() {return shootCooldown<=0;}

    public int getId() {return this.id;}

    /**
     * Returns the name of the ground sensor
     *
     * This is used by ContactListener
     *
     * @return the name of the ground sensor
     */
    public String getSensorName() {
        return SENSOR_NAME;
    }

    /**
     * Returns true if this character is facing right
     *
     * @return true if this character is facing right
     */
    public boolean isFacingRight() {
        return faceRight;
    }

    public boolean isFacingUp () {
        return faceUp;
    }

    /**
     * Creates a new dude avatar at the given position.
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param x  		Initial x position of the avatar center
     * @param y  		Initial y position of the avatar center
     * @param width		The object width in physics units
     * @param height	The object width in physics units
     */
    public LizardModel (float x, float y, float width, float height, int id) {
        super(x,y,width*DUDE_HSHRINK,height*DUDE_VSHRINK);
        setDensity(DUDE_DENSITY);
        setFriction(DUDE_FRICTION);  /// HE WILL STICK TO WALLS IF YOU FORGET
        setFixedRotation(true);

        // Gameplay attributes
        isShooting = false;
        isSwapping = false;
        faceRight = true;
        faceUp = true;
        this.id = id;
        hp = MAX_HP;

        shootCooldown = 0;
        setName("dude");
        attackAnimationFrame=0;
        this.ticks = 0L;
    }

    /**
     * Creates the physics Body(s) for this object, adding them to the world.
     *
     * This method overrides the base method to keep your ship from spinning.
     *
     * @param world Box2D world to store body
     *
     * @return true if object allocation succeeded
     */
    public boolean activatePhysics(World world) {
        // create the box from our superclass
        if (!super.activatePhysics(world)) {
            return false;
        }

        // Ground Sensor
        // -------------
        // We only allow the dude to jump when he's on the ground.
        // Double jumping is not allowed.
        //
        // To determine whether or not the dude is on the ground,
        // we create a thin sensor under his feet, which reports
        // collisions with the world but has no collision response.
        Vector2 sensorCenter = new Vector2(0, -getHeight() / 2);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = DUDE_DENSITY;
        sensorDef.isSensor = true;
        sensorShape = new PolygonShape();
        sensorShape.setAsBox(DUDE_SSHRINK*getWidth()/2.0f, SENSOR_HEIGHT, sensorCenter, 0.0f);
        sensorDef.shape = sensorShape;

        sensorFixture = body.createFixture(sensorDef);
        sensorFixture.setUserData(getSensorName());

        return true;
    }


    /**
     * Applies the force to the body of this dude
     *
     * This method should be called after the force attribute is set.
     */
    public void applyForce() {
        if (!isActive()) {
            return;
        }

        if (getMovementX()==0f && getMovementY()==0f) {
            body.setLinearVelocity(0, 0);
            /**
             body.setLinearVelocity(getVX()*SPEED_DAMPNING, getVY()*SPEED_DAMPNING);
             if (Math.abs(getVX()) < EPSILON_CLAMP) {
             body.setLinearVelocity(0, getVY());
             }
             if (Math.abs(getVY()) < EPSILON_CLAMP) {
             body.setLinearVelocity(getVX(), 0);
             }**/
        }
        body.setLinearVelocity(getMovementX(), getMovementY());
    }

    public void coolDown(boolean flag) {
        if (flag && this.shootCooldown > 0) {
            --this.shootCooldown;
        } else if (!flag) {
            this.shootCooldown = SHOOT_COOLDOWN;
        }

    }

    public int getAttackAniFrame(){
        return attackAnimationFrame;
    }

    public void incrAttackAniFrame(){
        if (attackAnimationFrame<3) {attackAnimationFrame++;} else {attackAnimationFrame=0;}
    }
    public void resetAttackAniFrame(){
        attackAnimationFrame=0;
    }
    public boolean endOfAttack(){
        return attackAnimationFrame==3;
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        float effect = faceRight ? 1.0f : -1.0f;
        if (attackAnimationFrame==1){
            System.out.println("frame: 1");
            canvas.draw(texture,Color.PURPLE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect,1.0f);
        }
        if (attackAnimationFrame==2){
            System.out.println("frame: 2");
            canvas.draw(texture,Color.GREEN,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect,1.0f);
        }
        if (attackAnimationFrame==3){
            System.out.println("frame: 3");
            canvas.draw(texture,Color.RED,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect,1.0f);
        }
        else {
            canvas.draw(texture,Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect,1.0f);
        }
    }

    /**
     * Draws the outline of the physics body.
     *
     * This method can be helpful for understanding issues with collisions.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        canvas.drawPhysics(sensorShape,Color.RED,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
    }

    public int getHP() {
        return hp;
    }
    public void decrHP() {
        hp -= 1; /* TODO dont do if negative */
    }

}