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
package edu.cornell.gdiac.physics.floor.monster;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import edu.cornell.gdiac.physics.*;
import edu.cornell.gdiac.physics.floor.weapon.WeaponModel;
import edu.cornell.gdiac.physics.obstacle.*;

/**
 * Player avatar for the plaform game.
 *
 * Note that this class returns to static loading.  That is because there are
 * no other subclasses that we might loop through.
 */
public class RobotModel extends EnemyModel {
    // Physics constants
    /** The density of the character */
    private static final float DUDE_DENSITY = 30f;
    /** The factor to multiply by the input */
<<<<<<< HEAD
    private static final float DUDE_VEL = 2.5f;
=======
    private static final float DUDE_FORCE = 2.5f;
>>>>>>> 5e222e955b273a1ae09025d8607d5f8f05e92776
    /** The dude is a slippery one */
    private static final float DUDE_FRICTION = 0.0f;
    /** Height of the sensor attached to the player's feet */
    private static final float SENSOR_HEIGHT = 0.05f;
    /** Identifier to allow us to track the sensor in ContactListener */
    private static final String SENSOR_NAME = "DudeGroundSensor";

    /** The amount to shrink the sensor fixture (horizontally) relative to the image */
    private static final float DUDE_SSHRINK = 0.6f;

    /** The amount of max HP a scientist has */
    private static final int MAX_HP = 5;

    /** Ground sensor to represent our feet */
    private Fixture sensorFixture;
    private PolygonShape sensorShape;


    /**
     * Returns how much force to apply to get the dude moving
     *
     * Multiply this by the input to get the movement value.
     *
     * @return how much force to apply to get the dude moving
     */
    public float getForce() {
        return DUDE_VEL;
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
    public RobotModel(float x, float y, float width, float height, int id) {
        super(x,y,width, height, id, DUDE_FRICTION, MAX_HP, "scientist", DUDE_VEL, 3);
        setDensity(DUDE_DENSITY);
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        float effect = isFacingRight() ? 1.0f : -1.0f;
        if (getAttackAnimationFrame()==1){
            System.out.println("robot: frame1");

            canvas.draw(texture,Color.PURPLE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect,1.0f);
        }
        if (getAttackAnimationFrame()==2){
            System.out.println("robot: frame2");

            canvas.draw(texture,Color.GREEN,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect,1.0f);
        }
        if (getAttackAnimationFrame()==3){
            System.out.println("robot: frame3");

            canvas.draw(texture,Color.RED,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect,1.0f);
        }
        else {
            canvas.draw(texture,Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect,1.0f);
        }
    }

    /**
     * Draws the outline of the physics body.
     *
     * This method can be helpful for understanding issues with collisions.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        canvas.drawPhysics(sensorShape,Color.RED,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
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

        Vector2 sensorCenter = new Vector2(0, -getHeight() / 2);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = DUDE_DENSITY;
        sensorDef.isSensor = true;
        sensorShape = new PolygonShape();
        sensorShape.setAsBox(DUDE_SSHRINK*getWidth()/2.0f, SENSOR_HEIGHT, sensorCenter, 0.0f);
        sensorDef.shape = sensorShape;

        sensorFixture = body.createFixture(sensorDef);
        sensorFixture.setUserData(SENSOR_NAME);

        return true;
    }

}
