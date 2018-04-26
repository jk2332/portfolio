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

    /** The amount of friction a character will have */
    private static final float CHARACTER_FRICTION = 0.0f;

    /*TODO wtf are sensors*/
    /** Ground sensor to represent our feet */
    private Fixture sensorFixture;
    private PolygonShape sensorShape;

    /** The current horizontal movement of the character */
    private float   movementX;
    /** The current veritcal movement of the character */
    private float movementY;
    /** Whether the character is facing right or not */
    public boolean faceRight;
    /** Whether the character is facing up or not */
    public boolean faceUp;
    /** How long until the can attack again */
    private int attackCooldown;
    /** The maximum cooldown of the character */
    private int maxAttackCooldown;
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
     * Sets the HP of the character
     */
    public void setHP(int new_hp) { hp = new_hp; }

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
     * Returns true if this character can attack
     *
     * @return true if this character can attack
     */
    public boolean canAttack() {return attackCooldown <=0;}

    public void startAttackCooldown() {
        attackCooldown = maxAttackCooldown;
    }

    public void decrAttackCooldown() {
        attackCooldown --;
    }

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
                          int hp, float density, float velocity, int maxAttackCooldown) {
        super(x,y,width* CHARACTER_HSHRINK,height* CHARACTER_VSHRINK);

        setDensity(density);
        setFriction(CHARACTER_FRICTION);  /// HE WILL STICK TO WALLS IF YOU FORGET
        setFixedRotation(true);
        faceRight = true;
        faceUp = false;
        this.hp=hp;
        attackCooldown = 0;
        this.maxAttackCooldown = maxAttackCooldown;
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
    public void applyImpulse(Vector2 f) {
        if (!isActive()) {
            return;
        }
        body.applyLinearImpulse(f,getPosition(),true);
    }

    /**
     * Draws the character to the screen
     *
     * @param canvas    Drawing context
     */
    /**
    public void draw(GameCanvas canvas) {
        float effect = faceRight ? 1.0f : -1.0f;
        if (((JoeModel) this).isBeingAttacked()) {
            System.out.println("here1");
            canvas.draw(texture, Color.RED,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect,1.0f);
        }
        else {
            System.out.println("here2");
            canvas.draw(texture, Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect,1.0f);
        }
    }**/

    /**
     * Draws the outline of the physics body.
     *
     * This method can be helpful for understanding issues with collisions.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
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

        return true;
    }

}