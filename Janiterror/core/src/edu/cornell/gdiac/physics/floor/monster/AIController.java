/*
 * AIController.java
 *
 * This class is an inplementation of InputController that uses AI and pathfinding
 * algorithms to determine the choice of input.
 *
 * NOTE: This is the file that you need to modify.  You should not need to
 * modify any other files (though you may need to read Board.java heavily).
 *
 * Author: Walker M. White, Cristian Zaloj
 * Based on original AI Game Lab by Yi Xu and Don Holden, 2007
 * LibGDX version, 1/24/2015
 */
package edu.cornell.gdiac.physics.floor.monster;

import java.lang.reflect.Array;
import java.util.*;
import com.badlogic.gdx.math.*;
import edu.cornell.gdiac.physics.InputController;
import edu.cornell.gdiac.physics.Board;
import edu.cornell.gdiac.physics.floor.*;

/**
 * InputController corresponding to AI control.
 *
 * REMEMBER: As an implementation of InputController you will have access to
 * the control code constants in that interface.  You will want to use them.
 */
public class AIController extends InputController {
    /**
     * Enumeration to encode the finite state machine.
     */
    private static enum FSMState {
        /** The ship just spawned */
        SPAWN,
        /** The ship is patrolling around without a target */
        WANDER,
        /** The ship has a target, but must get closer */
        CHASE,
        /** The ship has a target and is attacking it */
        ATTACK
    }

    // Constants for chase algorithms
    /** How close a target must be for us to chase it */
    private static final int CHASE_DIST  = 9;
    /** How close a target must be for us to attack it */
    private static final int ATTACK_DIST = 4;

    // Instance Attributes
    /** the ai controlled by this AIController */
    private ScientistModel ai;
    /** The game board; used for pathfinding */
    private Board board;
    /** The other ships; used to find targets */
    private ScientistModel[] fleet;
    /** The ship's current state in the FSM */
    private FSMState state;
    /** The target ship (to chase or attack). */
    private JoeModel target;
    /** The ship's next action (may include firing). */
    private int move; // A ControlCode
    /** The number of ticks since we started this controller */
    private long ticks;

    // Custom fields for AI algorithms
    //#region ADD YOUR CODE:

    //#endregion

    /**
     * Creates an AIController for the ship with the given id.
     *
     * @param id The unique ship identifier
     * @param board The game board (for pathfinding)
     * @param scientists The list of ships (for targetting)
     */
    public AIController(int id, Board board, ScientistModel[] scientists) {
        this.ai = (ScientistModel) Array.get(scientists, id);
        this.board = board;
        this.fleet = scientists;

        state = FSMState.SPAWN;
        move  = CONTROL_NO_ACTION;
        ticks = 0;

        // Select an initial target
        target = null;
        selectTarget();
    }

    /**
     * Returns the action selected by this InputController
     *
     * The returned int is a bit-vector of more than one possible input
     * option. This is why we do not use an enumeration of Control Codes;
     * Java does not (nicely) provide bitwise operation support for enums.
     *
     * This function tests the environment and uses the FSM to chose the next
     * action of the ship. This function SHOULD NOT need to be modified.  It
     * just contains code that drives the functions that you need to implement.
     *
     * @return the action selected by this InputController
     */
    public int getAction() {
        // Increment the number of ticks.
        ticks++;

        // Do not need to rework ourselves every frame. Just every 10 ticks.
        if ((ai.getid() + ticks) % 10 == 0) {
            // Process the FSM
            changeStateIfApplicable();

            // Pathfinding
            markGoalTiles();
            move = getMoveAlongPathToGoalTile();
        }

        int action = move;

        // If we're attacking someone and we can shoot him now, then do so.
        if (state == FSMState.ATTACK && canShootTarget()) {
            action |= CONTROL_FIRE;
        }

        return action;
    }

    // FSM Code for Targeting (MODIFY ALL THE FOLLOWING METHODS)

    /**
     * Change the state of the ship.
     *
     * A Finite State Machine (FSM) is just a collection of rules that,
     * given a current state, and given certain observations about the
     * environment, chooses a new state. For example, if we are currently
     * in the ATTACK state, we may want to switch to the CHASE state if the
     * target gets out of range.
     */
    private void changeStateIfApplicable() {
        // Add initialization code as necessary
        //#region PUT YOUR CODE HERE
        //#endregion
        // Next state depends on current state.
        switch (state) {
            case SPAWN: // Do not pre-empt with FSMState in a case
                // Insert checks and spawning-to-??? transition code here
                //#region PUT YOUR CODE HERE
                if (target==null) {state=FSMState.WANDER;}
                if (Math.random()<=0.5) {state=FSMState.CHASE;}
                else if (ticks%3==0) {state=FSMState.ATTACK;}
                //#en8dregion
                break;

            case WANDER: // Do not pre-empt with FSMState in a case
                // Insert checks and moving-to-??? transition code here
                //#region PUT YOUR CODE HERE
                if (Math.random()<=0.7f && target!=null) {state=FSMState.CHASE;}
                //#endregion
                break;

            case CHASE: // Do not pre-empt with FSMState in a case
                // insert checks and chasing-to-??? transition code here
                //#region PUT YOUR CODE HERE
                if (target==null) {state=FSMState.WANDER;}
                if (Math.random()<=0.8) {state=FSMState.ATTACK;}
                //#endregion
                break;

            case ATTACK: // Do not pre-empt with FSMState in a case
                // insert checks and attacking-to-??? transition code here
                //#region PUT YOUR CODE HERE
                if (target==null) {state=FSMState.WANDER;}
                if (!canShootTarget()) {state=FSMState.CHASE;}
                if (Math.random()<=0.2f) {state=FSMState.CHASE;}
                //#endregion
                break;

            default:
                // Unknown or unhandled state, should never get here
                assert (false);
                state = FSMState.WANDER; // If debugging is off
                break;
        }
    }

    /**
     * Acquire a target to attack (and put it in field target).
     *
     * Insert your checking and target selection code here. Note that this
     * code does not need to reassign <c>target</c> every single time it is
     * called. Like all other methods, make sure it works with any number
     * of players (between 0 and 32 players will be checked). Also, it is a
     * good idea to make sure the ship does not target itself or an
     * already-fallen (e.g. inactive) ship.
     */
    private void selectTarget() {
        //#region PUT YOUR CODE HERE
        //#endregion
    }

    /**
     * Returns true if we can hit a target from here.
     *
     * Insert code to return true if a shot fired from the given (x,y) would
     * be likely to hit the target. We can hit a target if it is in a straight
     * line from this tile and within attack range. The implementation must take
     * into consideration whether or not the source tile is a Power Tile.
     *
     * @param x The x-index of the source tile
     * @param y The y-index of the source tile
     *
     * @return true if we can hit a target from here.
     */
    private boolean canShootTargetFrom(int x, int y) {
        //#region PUT YOUR CODE HERE
        if (target==null) {return false;}
        int targetx = board.screenToBoard(target.getPosition().x);
        int targety = board.screenToBoard(target.getPosition().y);
        if (targetx==x) {int distance = Math.abs(targety-y); if (distance<=5) {return true;}}
        if (targety==y) {int distance = Math.abs(targetx-x); if (distance<=5) {return true;}}
        if (board.isPowerTileAt(x, y) && Math.abs(targetx-x)==Math.abs(targety-y)) {return true;}
        return false;
        //#endregion
    }

    /**
     * Returns true if we can both fire and hit our target
     *
     * If we can fire now, and we could hit the target from where we are,
     * we should hit the target now.
     *
     * @return true if we can both fire and hit our target
     */
    private boolean canShootTarget() {
        //#region PUT YOUR CODE HERE
        if (target==null) {return false;}
        int x = board.screenToBoard(ai.getPosition().x);
        int y = board.screenToBoard(ai.getPosition().y);
        if (ai.canShoot() && canShootTargetFrom(x,y)) {return true;}
        return false;
        //#endregion
    }

    // Pathfinding Code (MODIFY ALL THE FOLLOWING METHODS)

    /**
     * Mark all desirable tiles to move to.
     *
     * This method implements pathfinding through the use of goal tiles.
     * It searches for all desirable tiles to move to (there may be more than
     * one), and marks each one as a goal. Then, the pathfinding method
     * getMoveAlongPathToGoalTile() moves the ship towards the closest one.
     *
     * POSTCONDITION: There is guaranteed to be at least one goal tile
     * when completed.
     */
    private void markGoalTiles() {
        // Clear out previous pathfinding data.
        board.clearMarks();
        boolean setGoal = false; // Until we find a goal
        int nums=0;
        // Add initialization code as necessary
        //#region PUT YOUR CODE HERE
        int currx = board.screenToBoard(ai.getPosition().x);
        int curry = board.screenToBoard(ai.getPosition().y);
        //#endregion

        switch (state) {
            case SPAWN: // Do not pre-empt with FSMState in a case
                // insert code here to mark tiles (if any) that spawning ships
                // want to go to, and set setGoal to true if we marked any.
                // Ships in the spawning state will immediately move to another
                // state, so there is no need for goal tiles here.
                //#region PUT YOUR CODE HERE
                //#endregion
                break;

            case WANDER: // Do not pre-empt with FSMState in a case
                // Insert code to mark tiles that will cause us to move around;
                // set setGoal to true if we marked any tiles.
                // NOTE: this case must work even if the ship has no target
                // (and changeStateIfApplicable should make sure we are never
                // in a state that won't work at the time)
                //#region PUT YOUR CODE HERE
                for (int i=0; i<50; i++) {
                    int x = (int) Math.random()*board.getWidth();
                    int y = (int) Math.random()*board.getHeight();
                    if (!(currx==x&&curry==y)) {board.setGoal(x,y); nums++;}
                }
                if (nums!=0) {setGoal=true;}
                //#endregion
                break;

            case CHASE: // Do not pre-empt with FSMState in a case
                // Insert code to mark tiles that will cause us to chase the target;
                // set setGoal to true if we marked any tiles.
                //#region PUT YOUR CODE HERE
                int targetx = board.screenToBoard(target.getPosition().x);
                int targety = board.screenToBoard(target.getPosition().y);
                for (int i=0; i<50; i++) {
                    int temp = (int) (Math.random()*board.getWidth());
                    if (Math.abs(currx-temp)<=CHASE_DIST) {board.setGoal(temp, targety); nums++;}
                }
                for (int i=0; i<50; i++) {
                    int temp = (int) (Math.random()*board.getHeight());
                    if (Math.abs(currx-temp)<=CHASE_DIST) {board.setGoal(targetx, temp); nums++;}
                }
                if (nums!=0) {setGoal=true;}
                //#endregion
                break;

            case ATTACK: // Do not pre-empt with FSMState in a case
                // Insert code here to mark tiles we can attack from, (see
                // canShootTargetFrom); set setGoal to true if we marked any tiles.
                //#region PUT YOUR CODE HERE
                int tarx = board.screenToBoard(target.getPosition().x);
                int tary = board.screenToBoard(target.getPosition().y);
                for (int i=0; i<100; i++) {
                    int temp = (int) (Math.random()*board.getWidth());
                    if (Math.abs(currx-temp)<=ATTACK_DIST) {board.setGoal(temp, tary); nums++;}
                }
                for (int i=0; i<100; i++) {
                    int temp = (int) (Math.random()*board.getHeight());
                    if (Math.abs(curry-temp)<=ATTACK_DIST) {board.setGoal(tarx, temp); nums++;}
                }
                if (nums!=0) {setGoal=true;}
                //#endregion
                break;
        }

        // If we have no goals, mark current position as a goal
        // so we do not spend time looking for nothing:
        if (!setGoal) {
            int sx = board.screenToBoard(ai.getX());
            int sy = board.screenToBoard(ai.getY());
            board.setGoal(sx, sy);
        }
    }

    /**
     * Returns a movement direction that moves towards a goal tile.
     *
     * This is one of the longest parts of the assignment. Implement
     * breadth-first search (from 2110) to find the best goal tile
     * to move to. However, just return the movement direction for
     * the next step, not the entire path.
     *
     * The value returned should be a control code.  See PlayerController
     * for more information on how to use control codes.
     *
     * @return a movement direction that moves towards a goal tile.
     */
    private int getMoveAlongPathToGoalTile() {
        //#region PUT YOUR CODE HERE
        int shipx = board.screenToBoard(ai.getPosition().x);
        int shipy = board.screenToBoard(ai.getPosition().y);
        Vector2 desTile = bfs(new Vector2(shipx, shipy));
        if(desTile!=null) {
            //BFSTile nextTile = getPath(sourceTile, getDes(sourceTile)).get(1);
            if (desTile.x > shipx ) {
                return CONTROL_MOVE_RIGHT;
            }
            if (desTile.x < shipx ) {
                return CONTROL_MOVE_LEFT;
            }
            if (desTile.y > shipy ) {
                return CONTROL_MOVE_UP;
            }
            if (desTile.y < shipy ) {
                return CONTROL_MOVE_DOWN;
            }
        }
        return CONTROL_NO_ACTION;
        //#endregion
    }

    // Add any auxiliary methods or data structures here
    //#region PUT YOUR CODE HERE
    private Vector2 bfs(Vector2 sourceTile) {
        Vector2 currTile = sourceTile;
        Queue<Vector2> queue = new LinkedList<Vector2>();
        queue.add(currTile);
        board.setVisited((int) currTile.x, (int) currTile.y);
        while (!queue.isEmpty()) {
            currTile = queue.remove();
            if (board.isGoal((int) currTile.x, (int) currTile.y)) {
                break;
            } else {
                bfs_helper(new Vector2(currTile.x + 1, currTile.y), queue);
                bfs_helper(new Vector2(currTile.x, currTile.y + 1), queue);
                bfs_helper(new Vector2(currTile.x, currTile.y - 1), queue);
                bfs_helper(new Vector2(currTile.x - 1, currTile.y), queue);
            }
        }
        if (!board.isGoal((int) currTile.x, (int) currTile.y)) {
            return null;
        }
        return currTile;
    }

    private void bfs_helper (Vector2 nextTile, Queue<Vector2> q){
        int x = (int) nextTile.x; int y = (int) nextTile.y;
        if (board.inBounds(x,y) && (!board.isVisited(x,y)) ) {
            q.add(nextTile); board.setVisited(x,y);
        }
    }

    //#endregion
}
