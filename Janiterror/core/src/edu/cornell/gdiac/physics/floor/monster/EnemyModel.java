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

public class EnemyModel extends CapsuleObstacle {

    // This is to fit the image to a tigher hitbox
    /** The amount to shrink the body fixture (vertically) relative to the image */
    private static final float DUDE_VSHRINK = 0.95f;
    /** The amount to shrink the body fixture (horizontally) relative to the image */
    private static final float DUDE_HSHRINK = 0.7f;
    /** The amount to shrink the sensor fixture (horizontally) relative to the image */
    private static final float DUDE_SSHRINK = 0.6f;
    /** Height of the sensor attached to the player's feet */
    private static final float SENSOR_HEIGHT = 0.05f;
    private static final int SHOOT_COOLDOWN = 40;


    /** The current horizontal movement of the character */
    private float   movementX;
    private float movementY;
    /** Which direction is the character facing */
    private boolean faceRight;
    private boolean faceUp;
    /** How long until we can shoot again */
    private int shootCooldown;
    /** Ground sensor to represent our feet */
    private Fixture sensorFixture;
    private PolygonShape sensorShape;
    private int id;
    private long stunTicks;
    private boolean stunned;
    private float density;
    private float force;
    private int attackAnimationFrame;
    private int maxAniFrame;


    /* The amount of HP the scientist has left */
    private int hp;

    /**
     * Returns left/right movement of this character.
     *
     * This is the result of input times dude force.
     *
     * @return left/right movement of this character.
     */
    public float getMovementX() {
        return movementX;
    }


    /**
     * Returns up/down movement of this character.
     *
     * This is the result of input times dude force.
     *
     * @return up/down movement of this character.
     */
    public float getMovementY() {
        return movementY;
    }

    /**
     * Sets left/right movement of this character.
     *
     * This is the result of input times dude force.
     *
     * @param value left/right movement of this character.
     */
    public void setMovementX(float value) {
        movementX = value;
        // Change facing if appropriate
        if (movementX < 0) {
            faceRight = false;
        } else if (movementX > 0) {
            faceRight = true;
        }
    }

    /**
     * Sets up/down movement of this character.
     *
     * This is the result of input times dude force.
     *
     * @param value up/down movement of this character.
     */
    public void setMovementY(float value) {
        movementY = value;
        // Change facing if appropriate
        if (movementY < 0) {
            faceUp = false;
        } else if (movementY > 0) {
            faceUp = true;
        }
    }


    /**
     * Returns ow hard the brakes are applied to get a dude to stop moving
     *
     * @return ow hard the brakes are applied to get a dude to stop moving
     */
    public boolean canShoot() {return shootCooldown<=0;}

    public int getId() {return this.id;}


    /**
     * Returns true if this character is facing right
     *
     * @return true if this character is facing right
     */
    public boolean isFacingRight() {
        return faceRight;
    }

    public boolean isFacingUp () {
        return faceUp;
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
    public EnemyModel(float x, float y, float width, float height, int id, float friction, int hp,
                      String name, float force, int maxAniFrame) {
        super(x,y,width*DUDE_HSHRINK,height*DUDE_VSHRINK);
        setDensity(density);
        setFriction(friction);  /// HE WILL STICK TO WALLS IF YOU FORGET
        setFixedRotation(true);

        faceRight = true;
        faceUp = true;
        this.id = id;
        this.hp=hp;

        shootCooldown = 0;
        setName(name);
        this.stunTicks=0;
        this.stunned=false;
        this.force=force;
        this.attackAnimationFrame=0;
        this.maxAniFrame=maxAniFrame;
    }

    public boolean endOfAttack(){
        return attackAnimationFrame==maxAniFrame;
    }


    public void resetAttackAniFrame(){
        attackAnimationFrame=0;
    }

    public void incrAttackAniFrame(){
        if (attackAnimationFrame<3) {attackAnimationFrame++;} else {attackAnimationFrame=0;}
    }


    public float getForce(){return force;}
    /**
     * Applies the force to the body of this dude
     *
     * This method should be called after the force attribute is set.
     */
    public void applyForce() {
        if (!isActive()) {
            return;
        }

        if (getMovementX()==0f && getMovementY()==0f) {
            body.setLinearVelocity(0, 0);
        }
        body.setLinearVelocity(getMovementX(), getMovementY());
    }

    public void coolDown(boolean flag) {
        if (flag && this.shootCooldown > 0) {
            --this.shootCooldown;
        } else if (!flag) {
            this.shootCooldown = SHOOT_COOLDOWN;
        }

    }

    public boolean getStunned(){
        return this.stunned;
    }

    public void resetStunTicks(){
        stunTicks=0;
    }

    public void setStunned(boolean b){
        this.stunned=b;
    }

    public void incrStunTicks(){
        if (this.stunned) stunTicks++;
    }

    public long getStunTicks(){
        return this.stunTicks;
    }

    public int getAttackAnimationFrame(){return attackAnimationFrame;}

    public void draw(GameCanvas canvas) {
        float effect = faceRight ? 1.0f : -1.0f;
        if (attackAnimationFrame==1){
            System.out.println("frame1");

            canvas.draw(texture,Color.PURPLE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect,1.0f);
        }
        if (attackAnimationFrame==2){
            System.out.println("frame2");

            canvas.draw(texture,Color.GREEN,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect,1.0f);
        }
        if (attackAnimationFrame==3){
            System.out.println("frame3");

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

    public int getHP() {
        return hp;
    }
    public void decrHP() {
        hp -= 1; /* TODO dont do if negative */
    }

}