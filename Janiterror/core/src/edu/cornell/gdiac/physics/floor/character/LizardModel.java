/*
 * LizardModel.java
 */
package edu.cornell.gdiac.physics.floor.character;

/**
 * Model Class for the lizard enemy in the game.
 */
public class LizardModel extends EnemyModel {
    // Physics constants
    /** The density of the lizard */
    private static final float LIZARD_DENSITY = 0.1f;
    /** The velocity of the lizard */
    private static final float LIZARD_VELOCITY = 6.0f;

    private static final int LIZARD_ATTACK_RANGE = 1;

    /** The amount of max HP a lizard has */
    private static final int LIZARD_MAX_HP = 5;


    /**
     * Creates a new lizard at the given position.
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
    public LizardModel(float x, float y, float width, float height, int id) {
        super(x,y,width, height, "lizard", LIZARD_MAX_HP, LIZARD_DENSITY, LIZARD_VELOCITY, LIZARD_ATTACK_RANGE, id, 3);
    }

}