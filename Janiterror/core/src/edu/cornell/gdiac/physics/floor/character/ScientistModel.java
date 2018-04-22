/*
 * ScientistModel.java
 */
package edu.cornell.gdiac.physics.floor.character;

import edu.cornell.gdiac.physics.floor.FloorController;

/**
 * Model Class for the scientist enemy in the game.
 */
public class ScientistModel extends EnemyModel {

    public FloorController.StateMad state;
    public FloorController.StateMad previousState;
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
    public ScientistModel(float x, float y, float width, float height, int id, int hp, float density, float velocity, int attackRange,
                          FloorController.StateMad state, FloorController.StateMad previousState) {
        super(x,y,width, height, "scientist", hp, density, velocity, 1, id, 3);
        this.previousState = previousState;
        this.state = state;
    }

}
