/*
 * EnemyModel.java
 */
package edu.cornell.gdiac.physics.floor.character;
import com.badlogic.gdx.graphics.*;
import edu.cornell.gdiac.physics.*;

/**
 * Model class for all enemies in the game
 */
public class EnemyModel extends CharacterModel {
    private int id;
    private long stunTicks;
    private boolean stunned;
    private int knockbackTimer;
    private int attackAnimationFrame;
    private int maxAniFrame;

    private int attackRange;


    /**
     * Returns the attack range of this enemy
     *
     * @return attack range of this enemy
     */
    public int getAttackRange() {return attackRange;}

    /**
     * Returns the id of the enemy for AIController purposes
     *
     * @return id of the enemy
     */
    public int getId() {return this.id;}

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
    public EnemyModel(float x, float y, float width, float height, String name, int hp,
                      float density, float velocity, int attackRange, int id, int maxAniFrame) {
        super(x, y, width, height, name, hp, density, velocity);

        this.attackRange = attackRange;
        this.stunTicks=0;
        this.stunned=false;
        this.attackAnimationFrame=0;
        this.maxAniFrame=maxAniFrame;
        this.knockbackTimer = 0;
        this.id = id;
    }

    // TODO figure out what these functions do

    public boolean endOfAttack(){
        return attackAnimationFrame==maxAniFrame;
    }


    public void resetAttackAniFrame(){
        attackAnimationFrame=0;
    }

    public void incrAttackAniFrame(){
        if (attackAnimationFrame<maxAniFrame) {attackAnimationFrame++;} else {attackAnimationFrame=0;}
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

    public void setKnockbackTimer(int knockbackTimer) {
        this.knockbackTimer = knockbackTimer;
    }

    public int getKnockbackTimer() {
        return knockbackTimer;
    }

    public void decrKnockbackTimer() {
        knockbackTimer --;
    }

    public int getAttackAnimationFrame(){return attackAnimationFrame;}

    /**
     * Draws the enemy to the screen
     *
     * @param canvas    Drawing context
     */
    public void draw(GameCanvas canvas) {
        float effect = isFacingRight() ? 1.0f : -1.0f;
        if (attackAnimationFrame==1){
            canvas.draw(texture,Color.PURPLE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect,1.0f);
        }
        if (attackAnimationFrame==2){
            canvas.draw(texture,Color.GREEN,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect,1.0f);
        }
        if (attackAnimationFrame==3){
            canvas.draw(texture,Color.RED,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect,1.0f);
        }
        else {
            canvas.draw(texture,Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect,1.0f);
        }
    }

    /** Returns true if enemy can hit the target from a position.
     *
     * Default canHitTargetFrom function for melee ranged enemies
     *
     * @param x     Enemies x coordinate in tiles
     * @param y     Enemies x coordinate in tiles
     * @param tx    Targets x coordinate in tiles
     * @param ty    Targets y coordinate in tiles
     * @return true if enemy can hit the target from a position and false otherwise
     */
    public boolean canHitTargetFrom(int x, int y, int tx, int ty) {
        int dx = tx > x ? tx - x : x - tx;
        int dy = ty > y ? ty - y : y - ty;
        //boolean power = this.board.isPowerTileAt(x, y);
        boolean canhit = dx <= 1 && dy == 0;
        canhit |= dx == 0 && dy <= 1;
        //canhit |= power && dx == dy && dx <= 3;
        return canhit;
        /*TODO override if character has a different range */
        /* TODO look over this again to make sure its right*/
    }

}