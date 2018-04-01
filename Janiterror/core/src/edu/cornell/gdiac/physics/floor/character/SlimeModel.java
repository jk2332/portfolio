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
package edu.cornell.gdiac.physics.floor.character;

/**
 * Player avatar for the plaform game.
 *
 * Note that this class returns to static loading.  That is because there are
 * no other subclasses that we might loop through.
 */
public class SlimeModel extends EnemyModel {
    // Physics constants
    /** The density of the character */
    private static final float SLIME_DENSITY = 1f;
    /** The factor to multiply by the input */
    private static final float SLIME_VELOCITY = 1.5f;

    /** The amount of max HP a scientist has */
    private static final int SLIME_MAX_HP = 5;

    private static final int SLIME_ATTACK_RANGE = 8;


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
    public SlimeModel(float x, float y, float width, float height, int id) {
        super(x,y,width, height, "slime", SLIME_MAX_HP, SLIME_DENSITY, SLIME_VELOCITY, SLIME_ATTACK_RANGE, id,3);
    }

    public boolean canHitTargetFrom(int x, int y, int tx, int ty) {
        return tx == x && Math.abs(ty - y) <= SLIME_ATTACK_RANGE || ty == y && Math.abs(tx - x) <= SLIME_ATTACK_RANGE;
    }

}