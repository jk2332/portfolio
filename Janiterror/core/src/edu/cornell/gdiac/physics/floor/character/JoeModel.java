/*
 * JoeModel.java
 *
 */
package edu.cornell.gdiac.physics.floor.character;

import edu.cornell.gdiac.physics.floor.weapon.*;

/**
 * Model class for Joe, the main character of the game
 */
public class JoeModel extends CharacterModel {
    // Physics constants

    /** The density of Joe */
    private static final float JOE_DENSITY = 15.0f;
    /** The velocity of Joe */
    private static final float JOE_VELOCITY = 5.0f;
    /** Cooldown (in animation frames) for attacking */
    private static final int JOE_MAX_ATTACK_COOLDOWN = 20;

    /** Joe's max HP */
    private static final int JOE_MAX_HP = 15;

    /** Whether we are actively swapping */
    private boolean isSwapping;

    //TODO rename?
    /** Whether we are looking at wep1 in cart */
    private boolean isLeft;
    /** Whether we are looking at wep2 in cart */
    private boolean isRight;
    private boolean isUp;
    private boolean isDown;
    private boolean alive;

    /** The current weapons Joe is holding */
    WeaponModel wep1;
    WeaponModel wep2;

    private int attackCooldown;

    /** Whether Joe is at the mop cart or not */
    private boolean atMopCart;

    /** Whether Joe is holding the lid weapon or not */
    private boolean hasLid;


    public boolean canAttack(){
        return attackCooldown <= 0;
    }

    /**
     * Returns weapon in slot 1
     * @return weapon in slot 1
     */
    public WeaponModel getWep1() {
        return wep1;
    }

    /**
     * Returns weapon in slot 2
     * @return weapon in slot 2
     */
    public WeaponModel getWep2() {
        return wep2;
    }

    /** Sets new weapon in slot 1
     *
     * @param new_weapon new weapon to be put in slot 1
     */
    public void setWep1(WeaponModel new_weapon) {
        this.wep1 = new_weapon;
    }

    /** Sets new weapon in slot 2
     *
     * @param new_weapon new weapon to be put in slot 2
     */
    public void setWep2(WeaponModel new_weapon) {
        this.wep2 = new_weapon;
    }

    /**
     * Returns true if Joe is actively swapping.
     *
     * @return true if Joe is actively swapping.
     */
    public boolean isSwapping() {
        return isSwapping ;
    }

    /**
     * Sets whether the Joe is actively swapping.
     *
     * @param value whether the dude is actively swapping.
     */
    public void setSwapping(boolean value) {
        isSwapping = value;
    }

    /**
     * Returns whether or not the left arrow key is pressed
     *
     * @return whether the left arrow key is pressed
     */
    public boolean isLeft() {
        return isLeft;
    }

    /**
     * Returns whether or not the right arrow key is pressed
     *
     * @return whether the right arrow key is pressed
     */
    public boolean isRight() {
        return isRight;
    }

    /**
     * Returns whether or not the up arrow key is pressed
     *
     * @return whether the up arrow key is pressed
     */
    public boolean isUp() {
        return isUp;
    }

    /**
     * Returns whether or not the down arrow key is pressed
     *
     * @return whether the down arrow key is pressed
     */
    public boolean isDown() {
        return isDown;
    }

    /**
     * Sets whether the left arrow key is pressed
     *
     * @param value true if left arrow key is pressed, false otherwise
     */
    public void setLeft(boolean value) {
        isLeft = value;
    }

    /**
     * Sets whether the right arrow key is pressed
     *
     * @param value true if right arrow key is pressed, false otherwise
     */
    public void setRight(boolean value) {
        isRight = value;
    }

    /**
     * Sets whether the up arrow key is pressed
     *
     * @param value true if up arrow key is pressed, false otherwise
     */
    public void setUp(boolean value) {
        isUp = value;
    }

    /**
     * Sets whether the down arrow key is pressed
     *
     * @param value true if down arrow key is pressed, false otherwise
     */
    public void setDown(boolean value) {
        isDown = value;
    }

    /**
     * Sets whether Joe is at the mop cart
     * @param value whether or not Joe is at the mop cart
     */
    public void setAtMopCart(boolean value) {
        atMopCart = value;
    }

    /**
     * Returns whether Joe is at the mop cart
     * @return whether Joe is at the mop cart
     */
    public boolean isAtMopCart( ) {
        return atMopCart;
    }

    /**
     * Sets whether Joe is holding the lid weapon
     * @param value whether Joe is holding the lid weapon
     */
    public void setHasLid(boolean value) {
        hasLid = value;
    }

    /**
     * Returns whether Joe is holding the lid weapon
     * @return whether Joe is holding the lid weapon
     */
    public boolean getHasLid() {
        return hasLid;
    }

    public void setAlive (boolean b){
        alive=b;
    }

    public boolean isAlive(){
        return alive;
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
    public JoeModel(float x, float y, float width, float height, int hp, float density, float velocity) {
        super(x,y,width,height,"joe", hp,density,velocity,JOE_MAX_ATTACK_COOLDOWN);

        isSwapping = false;
        isUp = false;
        isDown = false;
        isRight = false;
        isLeft = false;
        attackCooldown = 0;
        atMopCart = false;
        hasLid = false;
        alive = true;
    }

    /**
     * Updates the object's physics state (NOT GAME LOGIC).
     *
     * We use this method to reset cooldowns.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void update(float dt) {
//TODO reconcile with other attack cooldown and deleted shoot cooldown
        if (isUp()||isLeft()||isDown()||isRight()) {
            attackCooldown = JOE_MAX_ATTACK_COOLDOWN;
        } else {
            attackCooldown = Math.max(0, attackCooldown - 1);
        }

        super.update(dt);
    }

}