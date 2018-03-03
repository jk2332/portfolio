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
package edu.cornell.gdiac.ailab;

import java.util.*;

/** 
 * InputController corresponding to AI control.
 * 
 * REMEMBER: As an implementation of InputController you will have access to
 * the control code constants in that interface.  You will want to use them.
 */
public class AIController implements InputController {
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
	/** The ship being controlled by this AIController */
	private Ship ship;
	/** The game board; used for pathfinding */
	private Board board;
	/** The other ships; used to find targets */
	private ShipList fleet;
	/** The ship's current state in the FSM */
	private FSMState state;
	/** The target ship (to chase or attack). */
	private Ship target; 
	/** The ship's next action (may include firing). */
	private int move; // A ControlCode
	/** The number of ticks since we started this controller */
	private long ticks;
	
	// Custom fields for AI algorithms
	//#region ADD YOUR CODE:
	LinkedList<TileCoord> q;
	HashMap<Integer, TileCoord> pred;
	TileCoord [][] pred1;
	int prevDirForWanderX;
	int prevDirForWanderY;
	int tempWander;
	//#endregion
	
	/**
	 * Creates an AIController for the ship with the given id.
	 *
	 * @param id The unique ship identifier
	 * @param board The game board (for pathfinding)
	 * @param ships The list of ships (for targetting)
	 */
	public AIController(int id, Board board, ShipList ships) {
		this.ship = ships.get(id);
		this.board = board;
		this.fleet = ships;
		
		state = FSMState.SPAWN;
		move  = CONTROL_NO_ACTION;
		ticks = 0;

		// Select an initial target
		target = null;
		selectTarget();

		prevDirForWanderX = -1;
		prevDirForWanderY = -1;

		pred1 = new TileCoord[board.getWidth()][board.getHeight()];
		tempWander = 0;
	}

	private class TileCoord {
		private int x;
		private int y;
		public TileCoord(int x, int y) {
			this.x = x;
			this.y = y;
		}
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
		if ((ship.getId() + ticks) % 10 == 0) {
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
			/** The ship just spawned, doesn't have target */
		case SPAWN: // Do not pre-empt with FSMState in a case
			// Insert checks and spawning-to-??? transition code here
			//#region PUT YOUR CODE HERE
			//spawn to wander -> if select target gets no target
			//spawn to spawn -> never?
			//spawn to chase ->
			//spawn to attack ->

			if (target == null) {
				state = FSMState.WANDER;
			} else if (isCloseToTarget()) {
				state = FSMState.ATTACK;
			} else {
				state = FSMState.CHASE;
			}
			//#endregion
			break;

			/** The ship is patrolling around without a target */
		case WANDER: // Do not pre-empt with FSMState in a case
			// Insert checks and moving-to-??? transition code here
			//#region PUT YOUR CODE HERE
			//wander to spawn
			//wander to wander
			//wander to chase
			//wander to attack

			if (tempWander != 0) {
				state = FSMState.WANDER;
				tempWander--;
			} else if (target == null) {
				state = FSMState.WANDER;
			} else if (isCloseToTarget()) {
				state = FSMState.ATTACK;
			} else {
				state = FSMState.CHASE;
			}
			//#endregion			
			break;

			/** The ship has a target, but must get closer */
		case CHASE: // Do not pre-empt with FSMState in a case
			// insert checks and chasing-to-??? transition code here
			//#region PUT YOUR CODE HERE
			//chase to spawn
			//chase to wander
			//chase to chase
			//chase to attack
			if (target == null) {
				state = FSMState.WANDER;
			} else if (isCloseToTarget()) {
				state = FSMState.ATTACK;
			} else {
				state = FSMState.CHASE;
			}
			//#endregion			
			break;

			/** The ship has a target and is attacking it */
		case ATTACK: // Do not pre-empt with FSMState in a case
			// insert checks and attacking-to-??? transition code here
			//#region PUT YOUR CODE HERE
			//attack to spawn
			//attack to wander
			//attack to chase
			//attack to attack
			Random rand = new Random();
			int wander = rand.nextInt(300);
			if (target == null) {
				state = FSMState.WANDER;
			} else if (wander > 298) {
				state = FSMState.WANDER;
				tempWander = 15;
			} else if (canShootTarget()) {
				state = FSMState.ATTACK;
			} else {
				state = FSMState.CHASE;
			}
			//#endregion			
			break;

		default:
			// Unknown or unhandled state, should never get here
			assert (false);
			state = FSMState.WANDER; // If debugging is off
			break;
		}
	}

	private boolean isCloseToTarget() {
		if (target == null) {
			return false;
		} else {
			TileCoord t = getCurrTileOf(target);
			TileCoord s = getCurrTile();
			for (int i = -4; i <= 4; i ++) {
				for (int j = -4; j <= 4; j++) {
					if (t.x + i == s.x && t.y + j == s.y) {
						return true;
					}
				}
			}
		}
		return false;
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
		// if ship has target and target is still active
		// dont do anything (maybe with random probability  change ship target/wander
		// select target - loop through ship list and choose closest that isn't the target
		//#endregion

		if ((target != null && !target.isActive()) || target == null) {
			target = null;
			setNewTarget();
		}
	}

	private void setNewTarget() {
		// choose nearest that isActive and is not ship
		float closestDist = (float) Math.sqrt(board.getWidth()*board.getWidth() + board.getHeight()*board.getHeight());
		Ship closestShip = null;
		TileCoord ct = getCurrTile();
		for (Ship s : fleet) {
			if (s.isActive() && s != ship) {
				TileCoord st = getCurrTileOf(s);
				float dist = dist(ct.x, st.x, ct.y, st.y);
				if (dist <= closestDist) {
					closestDist = dist;
					closestShip = s;
				}
			}
		}
		target = closestShip;
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
		if (target == null) {
			return false;
		} else {
			TileCoord tPos = getCurrTileOf(target);
			int dx = Math.abs(tPos.x - x);
			int dy = Math.abs(tPos.y - y);
			boolean canHitCardinal = false;
			boolean canHitDiagonal = false;
			if (dx <= 4 && dy == 0 || dy <= 4 && dx == 0) {
				canHitCardinal = true;
			}

			if (board.isPowerTileAt(x, y) && dx == dy && dx <= 3) {
				canHitDiagonal = true;
			}
			return canHitCardinal || canHitDiagonal;

		}

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
		TileCoord currTile = getCurrTile();
		return canShootTargetFrom(currTile.x, currTile.y) && ship.canFire();
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
		
		// Add initialization code as necessary
		//#region PUT YOUR CODE HERE

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
			TileCoord g = chooseGoalTileForWander();
			board.setGoal(g.x, g.y);
			setGoal = true;
			//#endregion
			break;

		case CHASE: // Do not pre-empt with FSMState in a case
			// Insert code to mark tiles that will cause us to chase the target;
			// set setGoal to true if we marked any tiles.
			
			//#region PUT YOUR CODE HERE
			selectTarget();
			TileCoord tTile = getCurrTileOf(target);
			board.setGoal(tTile.x, tTile.y);
			setGoal = true;
			//#endregion
			break;

		case ATTACK: // Do not pre-empt with FSMState in a case
			// Insert code here to mark tiles we can attack from, (see
			// canShootTargetFrom); set setGoal to true if we marked any tiles.

			//#region PUT YOUR CODE HERE
			selectTarget();
			if (target != null) {
				setAttackGoals();
				setGoal = true;
			}
			//#endregion
			break;
		}

		// If we have no goals, mark current position as a goal
		// so we do not spend time looking for nothing:
		if (!setGoal) {
			int sx = board.screenToBoard(ship.getX());
			int sy = board.screenToBoard(ship.getY());
			board.setGoal(sx, sy);
		}
	}

	private void setAttackGoals() {
		TileCoord t = getCurrTileOf(target);
		for (int i = -2; i <= 2; i ++) {
			for (int j = -2; j <= 2; j++) {
				if (board.isSafeAt(t.x + i, t.y + j)){
					board.setGoal(t.x + i, t.y + j);
				}
			}
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
		TileCoord currTile = getCurrTile();
		TileCoord goal = bfs(currTile);
		return getFirstMove(goal.x, goal.y, currTile.x, currTile.y);
		//#endregion
	}

	private TileCoord bfs(TileCoord currTile) {
		q= new LinkedList<TileCoord>();
		board.setVisited(currTile.x, currTile.y);
		q.add(currTile);
		//pred = new HashMap<Integer, TileCoord>();
		pred1 = new TileCoord[board.getWidth()][board.getHeight()];
		while (q.peek() != null) {
			TileCoord t = q.poll();
			if (board.isGoal(t.x,t.y)) {
				return t;
			}
			if (board.isSafeAt(t.x + 1, t.y) && !(board.isVisited(t.x + 1, t.y))) {
				addTileToQueue(t, t.x + 1, t.y);
			}
			if (board.isSafeAt(t.x - 1, t.y) && !(board.isVisited(t.x - 1, t.y))) {
				addTileToQueue(t, t.x - 1, t.y);
			}
			if (board.isSafeAt(t.x, t.y + 1) && !(board.isVisited(t.x, t.y + 1))) {
				addTileToQueue(t, t.x, t.y + 1);
			}
			if (board.isSafeAt(t.x, t.y - 1) && !(board.isVisited(t.x, t.y - 1))) {
				addTileToQueue(t, t.x, t.y - 1);
			}
		}
		return currTile;
	}

	private int getFirstMove(int goalX, int goalY, int startX, int startY) {
		int tileX = goalX;
		int tileY = goalY;
		int nextX = goalX;
		int nextY = goalY;

		while (!(tileX ==startX && tileY == startY)) {
			//TileCoord prevTile = pred.get(tileX * board.getHeight() + tileY);
			TileCoord prevTile = pred1[tileX][tileY];
			nextX = tileX;
			nextY = tileY;
			tileX = prevTile.x;
			tileY = prevTile.y;
		}
		int dx = nextX - tileX;
		int dy = nextY - tileY;
		if (dx > 0 && dy == 0) {
			return CONTROL_MOVE_RIGHT;
		} else if (dx < 0 && dy == 0) {
			return CONTROL_MOVE_LEFT;
		} else if (dx == 0 && dy > 0) {
			return CONTROL_MOVE_DOWN;
		} else if (dx == 0 && dy < 0) {
			return CONTROL_MOVE_UP;
		} else {
			return CONTROL_NO_ACTION;
		}
	}

	// Add any auxiliary methods or data structures here
	//#region PUT YOUR CODE HERE
	public void addTileToQueue(TileCoord t, int x, int y) {
		if (board.inBounds(x, y)) {
			TileCoord newTile = new TileCoord(x, y);
			board.setVisited(newTile.x, newTile.y);
			//pred.put(x * board.getHeight() + y, t);
			pred1[x][y] = t;
			q.add(newTile);
		}

	}

	//set goal tile to be a tile next to the ship
	private TileCoord chooseGoalTileForWander() {
		Random rand = new Random();
		int changeDir = rand.nextInt(10);
		TileCoord currTile = getCurrTile();
		int gx = currTile.x + prevDirForWanderX;
		int gy = currTile.y + prevDirForWanderY;
		if (!(prevDirForWanderY == -1 && prevDirForWanderX == -1) && changeDir < 8 && board.isSafeAt(gx, gy)) {
			return new TileCoord(gx, gy);
		}
		ArrayList<TileCoord> safeTiles= findAdjacentSafeTiles(currTile);
		int numAdjacentSafeTiles = safeTiles.size();
		if (numAdjacentSafeTiles != 0) {
			TileCoord g = safeTiles.get(rand.nextInt(numAdjacentSafeTiles));
			prevDirForWanderX = g.x - currTile.x;
			prevDirForWanderY = g.y - currTile.y;
			return g;
		}
		return currTile;
	}

	private ArrayList<TileCoord> findAdjacentSafeTiles(TileCoord currTile) {
		ArrayList<TileCoord> safeTiles= new ArrayList();
		if (board.isSafeAt(currTile.x + 1, currTile.y)) {
			safeTiles.add(new TileCoord(currTile.x + 1, currTile.y));
		}
		if (board.isSafeAt(currTile.x - 1, currTile.y)) {
			safeTiles.add(new TileCoord(currTile.x  - 1, currTile.y));
		}
		if (board.isSafeAt(currTile.x, currTile.y + 1)) {
			safeTiles.add(new TileCoord(currTile.x, currTile.y + 1));
		}
		if (board.isSafeAt(currTile.x, currTile.y - 1)) {
			safeTiles.add(new TileCoord(currTile.x, currTile.y - 1));
		}
		return safeTiles;
	}

	private TileCoord getCurrTileOf(Ship s) {
		return new TileCoord(board.screenToBoard(s.getX()),board.screenToBoard(s.getY()));
	}
	private TileCoord getCurrTile() {
		return getCurrTileOf(ship);
	}

	private float dist(int x1, int x2, int y1, int y2) {
		return (float) Math.sqrt((x1 - x2)*(x1 - x2) + (y1 - y2)*(y1 - y2));
	}
	//#endregion
}