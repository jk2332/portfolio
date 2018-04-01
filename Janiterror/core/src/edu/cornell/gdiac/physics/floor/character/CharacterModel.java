/*
 * CharacterModel.java
 *
 */
package edu.cornell.gdiac.physics.floor.character;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import edu.cornell.gdiac.physics.*;
import edu.cornell.gdiac.physics.obstacle.*;

/**
 * Model class for all characters in the game
 */
public class CharacterModel extends CapsuleObstacle {

    // TODO see if we need this:
    // This is to fit the image to a tigher hitbox
    /** The amount to shrink the body fixture (vertically) relative to the image */
    private static final float CHARACTER_VSHRINK = 0.95f;
    /** The amount to shrink the body fixture (horizontally) relative to the image */
    private static final float CHARACTER_HSHRINK = 0.7f;
    /** The amount to shrink the sensor fixture (horizontally) relative to the image */
    private static final float CHARACTER_SSHRINK = 0.6f;
    // TODO urgent whats this
    /** Height of the sensor attached to the player's feet */
    private static final float SENSOR_HEIGHT = 0.05f;
    /** Identifier to allow us to track the sensor in ContactListener */
    private static final String SENSOR_NAME = "CharacterGroundSensor";

    /** The amount of friction a character will have */
    /** The dude is a slippery one */
    private static final float CHARACTER_FRICTION = 0.0f;

    /** Cooldown (in animation frames) for attacks */
    private static final int ATTACK_COOLDOWN = 40;

    /*TODO wtf are sensors*/
    /** Ground sensor to represent our feet */
    private Fixture sensorFixture;
    private PolygonShape sensorShape;

    /** The current horizontal movement of the character */
    private float   movementX;
    /** The current veritcal movement of the character */
    private float movementY;
    /** Whether the character is facing right or not */
    private boolean faceRight;
    /** Whether the character is facing up or not */
    private boolean faceUp;
    /** How long until the can attack again */
    private int attackCooldown;
    /** The default velocity that the character moves at */
    private float velocity;

    /* The amount of HP the scientist has left */
    private int hp;

    /**
     * Returns left/right movement of this character.
     *
     * This is the result of input times dude force.
     *
     * @return left/right movement of this character.
     */
    public float getMovementX() { return movementX; }


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
     * Returns the HP of the character
     *
     * @return Returns the HP of the character
     */
    public int getHP() {
        return hp;
    }

    /**
     * Decrements the characters HP by one.
     */
    public void decrHP() {
        hp -= 1; /* TODO dont do if negative */
    }

    /**
     * Returns true if this character is facing right
     *
     * @return true if this character is facing right
     */
    public boolean isFacingRight() {
        return faceRight;
    }

    /**
     * Returns true if this character is facing up
     *
     * @return true if this character is facing up
     */
    public boolean isFacingUp () {
        return faceUp;
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
     * Returns true if this character can attack
     *
     * @return true if this character can attack
     */
    public boolean canAttack() {return attackCooldown <=0;} //TODO URGENT was canShoot

    /**
     * Creates a new character at the given position.
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
    public CharacterModel(float x, float y, float width, float height, String name,
                          int hp, float density, float velocity) {
        super(x,y,width* CHARACTER_HSHRINK,height* CHARACTER_VSHRINK);

        setDensity(density);
        setFriction(CHARACTER_FRICTION);  /// HE WILL STICK TO WALLS IF YOU FORGET
        setFixedRotation(true);
        faceRight = true;
        faceUp = false;
        this.hp=hp;
        attackCooldown = 0;
        setName(name);
        this.velocity=velocity;
    }

    /**
     * Returns the default velocity that the character moves at
     *
     * @return the default velocity that the character moves at
     */
    public float getVelocity(){return velocity;}


    /**
     * Sets the velocity of the character
     *
     * This method should be called after movementX and movementY are set
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
     * Applies an impulse f to the character
     *
     * @param f     Impulse to be applied to the character
     */
    public void applyImpulse(Vector2 f) { //TODO urgent changed from applyForce to applyImpulse
        if (!isActive()) {
            return;
        }
        body.applyLinearImpulse(f,getPosition(),true);
    }

    /**
     * Starts attack cooldown if flag is false and decrements cooldown otherwise (TODO maybe change is kinda confusing)
     *
     * @param flag
     */
    public void coolDown(boolean flag) {
        if (flag && this.attackCooldown > 0) {
            --this.attackCooldown;
        } else if (!flag) {
            this.attackCooldown = ATTACK_COOLDOWN;
        }

    }

    /**
     * Draws the character to the screen
     *
     * @param canvas    Drawing context
     */
    public void draw(GameCanvas canvas) {
        float effect = faceRight ? 1.0f : -1.0f;
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
        canvas.drawPhysics(sensorShape,Color.RED,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
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
        sensorDef.density = getDensity();
        sensorDef.isSensor = true;
        sensorShape = new PolygonShape();
        sensorShape.setAsBox(CHARACTER_SSHRINK *getWidth()/2.0f, SENSOR_HEIGHT, sensorCenter, 0.0f);
        sensorDef.shape = sensorShape;

        sensorFixture = body.createFixture(sensorDef);
        sensorFixture.setUserData(SENSOR_NAME);

        return true;
    }

}