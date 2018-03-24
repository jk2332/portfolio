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

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import edu.cornell.gdiac.physics.*;
import edu.cornell.gdiac.physics.floor.weapon.*;
import edu.cornell.gdiac.physics.obstacle.*;

/**
 * Player avatar for the plaform game.
 *
 * Note that this class returns to static loading.  That is because there are
 * no other subclasses that we might loop through.
 */
public class JoeModel extends CapsuleObstacle {
    // Physics constants
    /** The density of the character */
    private static final float DUDE_DENSITY = 15.0f;
    /** The factor to multiply by the input */
    private static final float DUDE_FORCE = 5.0f;
    /** The amount to slow the character down */
    private static final float DUDE_DAMPING = 20.0f;
    private static final float SPEED_DAMPNING = 0.75f;
    /** The dude is a slippery one */
    private static final float DUDE_FRICTION = 0.0f;
    /** The maximum character speed */
    private static final float DUDE_MAXSPEED = 5.0f;
    /** The impulse for the character jump */
//	private static final float DUDE_JUMP = 5.5f;
    /** Cooldown (in animation frames) for jumping */
//	private static final int JUMP_COOLDOWN = 30;
    /** Cooldown (in animation frames) for shooting */
    private static final int SHOOT_COOLDOWN = 40;
    /** Cooldown (in animation frames) for shooting */
    private static final int ATTACK_COOLDOWN = 20;
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
    /* Joe's max HP */
    private static final int MAX_HP = 15;
    private static final float EPSILON_CLAMP = 0.01f;

    /** The current horizontal movement of the character */
    private float   movementX;
    private float movementY;
    /** Which direction is the character facing */
    private boolean faceRight;
    private boolean faceUp;
    private boolean faceLeft;
    private boolean faceDown;
    /** How long until we can jump again */
//	private int jumpCooldown;
    /** Whether we are actively jumping */
//	private boolean isJumping;
    /** How long until we can shoot again */
    private int shootCooldown;
    /** How long until we can attack again */
    private int attackCooldown;
    /** Whether our feet are on the ground */
//	private boolean isGrounded;
    /** Whether we are actively attacking */
    private boolean isAttacking1;
    /** Whether we are actively attacking */
    private boolean isAttacking2;
    /** Whether we are actively shooting */
    private boolean isShooting;
    /** Whether we are actively swapping */
    private boolean isSwapping;
    /** Whether we are looking at wep1 in cart */
    private boolean isLeft;
    /** Whether we are looking at wep2 in cart */
    private boolean isRight;
    private boolean isUp;
    private boolean isDown;
    /** Ground sensor to represent our feet */
    private Fixture sensorFixture;
    private PolygonShape sensorShape;
    /** Joe's HP */
    private int hp;
    /* Joe's walking sprite */
    protected TextureRegion textureWalking;
    /* Mop */
    MopModel mop;
    /* Spray */
    SprayModel spray;
    /* Lid */
    LidModel lid;
    /* Vacuum */
    VacuumModel vacuum;
    /** The current weapons Joe is holding */
    WeaponModel wep1;
    WeaponModel wep2;

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
            faceLeft = true;
        } else if (movementX > 0) {
            faceRight = true;
            faceLeft = false;
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
            faceDown = true;
        } else if (movementY > 0) {
            faceUp = true;
            faceDown = false;
        }
    }

    /**
     * Returns true if the dude is actively firing.
     *
     * @return true if the dude is actively firing.
     */
    public boolean isShooting() {
        return isShooting && shootCooldown <= 0;
    }

    /**
     * Sets whether the dude is actively firing.
     *
     * @param value whether the dude is actively firing.
     */
    public void setShooting(boolean value) {
        isShooting = value;
    }

    /**
     * Returns true if the dude is actively firing.
     *
     * @return true if the dude is actively firing.
     */
    public boolean isAttacking1() {
        return isAttacking1 && attackCooldown <= 0;
    }

    public boolean isAttackUp (){
       if (attackCooldown <= 0)
           return true;
       else
           return false;
    }
    /**
     * Sets whether the dude is actively firing.
     *
     * @param value whether the dude is actively firing.
     */
    public void setAttacking1(boolean value) {
        isAttacking1 = value;
    }

    /**
     * Returns true if the dude is actively firing.
     *
     * @return true if the dude is actively firing.
     */
//    public boolean isAttacking2() {
//        return isAttacking2 && attackCooldown <= 0;
//    }
//
//    /**
//     * Sets whether the dude is actively firing.
//     *
//     * @param value whether the dude is actively firing.
//     */
//    public void setAttacking2(boolean value) {
//        isAttacking2 = value;
//    }

    /**
     * Returns true if the dude is actively swapping.
     *
     * @return true if the dude is actively swapping.
     */
    public boolean isSwapping() {
        return isSwapping ;
    }

    /**
     * Sets whether the dude is actively swapping.
     *
     * @param value whether the dude is actively swapping.
     */
    public void setSwapping(boolean value) {
        isSwapping = value;
    }

    /**
     * Sets whether the dude is looking at wep1 in cart.
     *
     * @param value whether the dude is looking at wep1 in cart.
     */
    public boolean isLeft() {
        return isLeft;
    }
    public boolean isRight() {
        return isRight;
    }
    public void setLeft(boolean value) {
        isLeft = value;
    }
    public void setRight(boolean value) {
        isRight = value;
    }

    public boolean isUp() { return isUp; }
    public boolean isDown() {
        return isDown;
    }
    public void setUp(boolean value) {
        isUp = value;
    }
    public void setDown(boolean value) {
        isDown = value;
    }

    /**
     * Returns true if the dude is actively jumping.
     *
     * @return true if the dude is actively jumping.
     */
//	public boolean isJumping() {
//		return isJumping && isGrounded && jumpCooldown <= 0;
//	}
//
//	/**
//	 * Sets whether the dude is actively jumping.
//	 *
//	 * @param value whether the dude is actively jumping.
//	 */
//	public void setJumping(boolean value) {
//		isJumping = value;
//	}
//
//	/**
//	 * Returns true if the dude is on the ground.
//	 *
//	 * @return true if the dude is on the ground.
//	 */
//	public boolean isGrounded() {
//		return isGrounded;
//	}
//
//	/**
//	 * Sets whether the dude is on the ground.
//	 *
//	 * @param value whether the dude is on the ground.
//	 */
//	public void setGrounded(boolean value) {
//		isGrounded = value;
//	}

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
    public float getDamping() {
        return DUDE_DAMPING;
    }

    /**
     * Returns the upper limit on dude left-right movement.
     *
     * This does NOT apply to vertical movement.
     *
     * @return the upper limit on dude left-right movement.
     */
    public float getMaxSpeed() {
        return DUDE_MAXSPEED;
    }

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

    public boolean isFacingLeft() {
        return faceLeft;
    }

    public boolean isFacingDown () {
        return faceDown;
    }

    /**
     * Creates a new dude at the origin.
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param width		The object width in physics units
     * @param height	The object width in physics units
     */
    public JoeModel(float width, float height) {
        this(0,0,width,height);
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
    public JoeModel(float x, float y, float width, float height) {
        super(x,y,width*DUDE_HSHRINK,height*DUDE_VSHRINK);
        setDensity(DUDE_DENSITY);
        setFriction(DUDE_FRICTION);  /// HE WILL STICK TO WALLS IF YOU FORGET
        setFixedRotation(true);

        // Gameplay attributes
//		isGrounded = false;
        isShooting = false;
        isSwapping = false;
        isAttacking1 = false;
        isUp = false;
        isDown = false;
        isRight = false;
        isLeft = false;
//		isJumping = false;
        faceRight = true;
        faceUp = false;

        hp = MAX_HP;
        mop = new MopModel();
        spray = new SprayModel();
        lid = new LidModel();
        vacuum = new VacuumModel();

        wep1 = mop;
        wep2 = spray;

        shootCooldown = 0;
        attackCooldown = 0;
//		jumpCooldown = 0;
        setName("dude");
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
    public void setVelocity() {
        if (!isActive()) {
            return;
        }

        if (getMovementX()==0f && getMovementY()==0f) {
            body.setLinearVelocity(0, 0);
        }
        body.setLinearVelocity(getMovementX(), getMovementY());
    }

    /**
     * Updates the object's physics state (NOT GAME LOGIC).
     *
     * We use this method to reset cooldowns.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void update(float dt) {
        // Apply cooldowns
//		if (isJumping()) {
//			jumpCooldown = JUMP_COOLDOWN;
//		} else {
//			jumpCooldown = Math.max(0, jumpCooldown - 1);
//		}

        if (isShooting()) {
            shootCooldown = SHOOT_COOLDOWN;
        } else {
            shootCooldown = Math.max(0, shootCooldown - 1);
        }

        if (isUp()||isLeft()||isDown()||isRight()) {
            attackCooldown = ATTACK_COOLDOWN;
        } else {
            attackCooldown = Math.max(0, attackCooldown - 1);
        }

        super.update(dt);
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        float effect = faceRight ? 1.0f : -1.0f;
        /*if (movementX == 0 && movementY == 0) {
            canvas.draw(texture,Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect,1.0f);
        } else {
            canvas.draw(textureWalking,Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect,1.0f);
        }*/
        canvas.draw(texture,Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect,1.0f);
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
        canvas.drawPhysics(sensorShape, Color.RED, getX(), getY(), getAngle(), drawScale.x, drawScale.y);
    }

    /**
     * Sets the object texture for drawing purposes.
     *
     * In order for drawing to work properly, you MUST set the drawScale.
     * The drawScale converts the physics units to pixels.
     *
     * @param value  the object texture for drawing purposes.
     */
    public void setWalkingTexture(TextureRegion value) {
        textureWalking = value;
        origin.set(textureWalking.getRegionWidth()/2.0f, textureWalking.getRegionHeight()/2.0f);
    }

    public int getHP() {
        return hp;
    }
    public void decrHP() {
        hp -= 1; /* TODO dont do if negative */
    }

//    Weapon Getters and Setters
    public WeaponModel getWep1() {
        return wep1;
    }
    public WeaponModel getWep2() {
        return wep2;
    }
    public void setWep1(WeaponModel new_weapon) {
        this.wep1 = new_weapon;
    }
    public void setWep2(WeaponModel new_weapon) {
        this.wep2 = new_weapon;
    }
}