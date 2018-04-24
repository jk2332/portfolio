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

import edu.cornell.gdiac.physics.floor.FloorController;

/**
 * Player avatar for the plaform game.
 *
 * Note that this class returns to static loading.  That is because there are
 * no other subclasses that we might loop through.
 */
public class SlimeModel extends EnemyModel {

    float slimeballSpeed;
    public FloorController.StateSlime state;
    public FloorController.StateSlime previousState;
    private boolean turret;
    private int direction; //direction: -1-not a turret (no need for direction), 0-left, 1-right, 2-up, 3-down


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
    public SlimeModel(float x, float y, float width, float height, int id, int hp, float density, float velocity, int attackRange, float slimeballSpeed,
                      FloorController.StateSlime state, FloorController.StateSlime previousState) {
        super(x,y,width, height, "slime", hp, density, velocity, attackRange, id,3);
        this.slimeballSpeed = slimeballSpeed;
        this.state = state;
        this.previousState = previousState;
        turret=false;

    }

    public void setTurret (boolean b) {turret=b;}
    public boolean getTurret(){return turret;}
    public int getDirection() {return direction;}

    public boolean canHitTargetFrom(int x, int y, int tx, int ty) {
        return tx == x && Math.abs(ty - y) <= getAttackRange() || ty == y && Math.abs(tx - x) <= getAttackRange();
    }

    public float getSlimeballSpeed() {
        return slimeballSpeed;
    }

}