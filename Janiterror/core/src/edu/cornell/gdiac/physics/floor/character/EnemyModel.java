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

    private static final int ENEMY_MAX_ATTACK_COOLDOWN = 30;
    //this is affecting slimes and regular enemies

    private int id;
    private long stunTicks;
    private boolean stunned;
    private int knockbackTimer;
    private int attackAnimationFrame;
    private int maxAniFrame;
    private float stateTimer;
    private int attackRange;
    private Board board;


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
        super(x, y, width, height, name, hp, density, velocity, ENEMY_MAX_ATTACK_COOLDOWN);

        this.attackRange = attackRange;
        this.stunTicks=0;
        this.stunned=false;
        this.attackAnimationFrame=0;
        this.maxAniFrame=maxAniFrame;
        this.knockbackTimer = 0;
        this.id = id;
        fixture.filter.groupIndex = -1;
    }

    // TODO figure out what these functions do


    public void resetAttackAniFrame(){
        attackAnimationFrame=0;
    }

    public void incrAttackAniFrame(){
        attackAnimationFrame++;
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

    public void setStateTimer (float timer){
        this.stateTimer = timer;
    }
    public float getStateTimer (){
        return stateTimer;
    }

    /**
     * Draws the enemy to the screen
     *
     * @param canvas    Drawing context
     */
    public void draw(GameCanvas canvas) {
        float effect = isFacingRight() ? 1.0f : -1.0f;
        //if (getAttackAnimationFrame()>=3){
        //    canvas.draw(texture,Color.RED,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect,1.0f);
        //}
        //else {
            canvas.draw(texture,Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect,1.0f);
        //}
    }

    public float getX(){
        return this.getBody().getWorldCenter().x;
    }

    public float getY(){
        return this.getBody().getWorldCenter().y;
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
    public boolean canHitTargetFrom(int x, int y, int tx, int ty, int vertiRange, int leftRange, int rightRange) {
        int dx = tx > x ? tx - x : x - tx;
        int dy = ty > y ? ty - y : y - ty;
        //boolean power = this.board.isPowerTileAt(x, y);
        boolean canhit = tx >= x && dx <= leftRange && dy == 0;
        canhit |= tx==x && dy <= vertiRange;
        canhit |= tx < x && dx <= rightRange && dy == 0;
        //canhit |= power && dx == dy && dx <= 3;
        return canhit;
        /* TODO look over this again to make sure its right*/
    }
    public boolean canHitTargetFrom(float fx, float fy, float ftx, float fty, int x, int y, int tx, int ty) {
        float dx = Math.abs(fx-ftx);
        //boolean power = this.board.isPowerTileAt(x, y);
        boolean canhit = dx <= 1.5 && y==ty;
        canhit |= x==tx && Math.abs(ty-y)<=2;
        //canhit |= power && dx == dy && dx <= 3;
        return canhit;
        /* TODO look over this again to make sure its right*/
    }

}