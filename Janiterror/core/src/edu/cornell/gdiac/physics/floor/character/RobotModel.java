/*
 * RobotModel.java
 */
package edu.cornell.gdiac.physics.floor.character;

import com.badlogic.gdx.graphics.Color;
import edu.cornell.gdiac.physics.GameCanvas;

/**
 * Model class for the robot enemy in the game.
 */
public class RobotModel extends EnemyModel {


    /**
     * Creates a new robot at the given position.
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
    public RobotModel(float x, float y, float width, float height, int id, int hp,float density, float velocity, int attackRange) {
        super(x,y,width, height, "robot", hp, density, velocity, 1, id, 3);
    }

}
