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
    // Physics constants
    /** The density of the robot */
    private static final float ROBOT_DENSITY = 30f;
    /** The velocity of the robot */
    private static final float ROBOT_VELOCITY = 2.5f;

    private static final int ROBOT_ATTACK_RANGE = 2;

    /** The amount of max HP a robot has */
    private static final int ROBOT_MAX_HP = 5;


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
    public RobotModel(float x, float y, float width, float height, int id) {
        super(x,y,width, height, "robot", ROBOT_MAX_HP, ROBOT_DENSITY, ROBOT_VELOCITY, ROBOT_ATTACK_RANGE, id, 3);
    }

}
