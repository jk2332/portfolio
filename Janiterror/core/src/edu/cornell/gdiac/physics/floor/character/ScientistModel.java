/*
 * ScientistModel.java
 */
package edu.cornell.gdiac.physics.floor.character;

/**
 * Model Class for the scientist enemy in the game.
 */
public class ScientistModel extends EnemyModel {
    // Physics constants
    /** The density of the scientist */
    private static final float SCIENTIST_DENSITY = 0.1f;
    /** The velocity of the scientist */
    private static final float SCIENTIST_VELOCITY = 2.5f;

    private static final int SCIENTIST_ATTACK_RANGE = 1;

    /** The amount of max HP a scientist has */
    private static final int SCIENTIST_MAX_HP = 5;


    /**
     * Creates a new scientist at the given position.
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
    public ScientistModel(float x, float y, float width, float height, int id) {
        super(x,y,width, height, "scientist", SCIENTIST_MAX_HP, SCIENTIST_DENSITY, SCIENTIST_VELOCITY, SCIENTIST_ATTACK_RANGE, id, 3);
    }

}
