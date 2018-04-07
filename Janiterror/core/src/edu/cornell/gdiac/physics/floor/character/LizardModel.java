/*
 * LizardModel.java
 */
package edu.cornell.gdiac.physics.floor.character;

/**
 * Model Class for the lizard enemy in the game.
 */
public class LizardModel extends EnemyModel {


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
    public LizardModel(float x, float y, float width, float height, int id, int hp, float density, float velocity, int attackRange) {
        super(x,y,width, height, "lizard", hp, density, velocity, attackRange, id, 3);
    }

}