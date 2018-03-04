//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package edu.cornell.gdiac.physics.floor;

import edu.cornell.gdiac.physics.Board;
import edu.cornell.gdiac.physics.InputController;
import edu.cornell.gdiac.physics.floor.monster.JoeModel;
import edu.cornell.gdiac.physics.floor.monster.ScientistModel;

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Random;

public class AIController extends InputController {
    private static final int CHASE_DIST = 9;
    private static final int ATTACK_DIST = 4;
    private ScientistModel ship;
    private Board board;
    private ScientistModel[] fleet;
    private AIController.FSMState state;
    private JoeModel target;
    private int move;
    private long ticks;
    private int wx = 0;
    private int wy = 0;

    public AIController(int id, Board board, ScientistModel[] ships, JoeModel target) {
        this.ship =  (ScientistModel) Array.get(ships, id);
        this.board = board;
        this.fleet = ships;
        this.state = AIController.FSMState.SPAWN;
        this.move = 0;
        this.ticks = 0L;
        this.target = target;
        //this.selectTarget();
    }

    public int getAction() {
        ++this.ticks;
        if (((long)this.ship.getId() + this.ticks) % 10L == 0L) {
            this.changeStateIfApplicable();
            this.markGoalTiles();
            this.move = this.getMoveAlongPathToGoalTile();
        }

        int action = this.move;
        if (this.state == AIController.FSMState.ATTACK && this.canShootTarget()) {
            action |= 16;
        }

        return action;
    }

    private void changeStateIfApplicable() {
        Random rand = new Random();
        int sx = this.board.screenToBoard(this.ship.getX());
        int sy = this.board.screenToBoard(this.ship.getY());
        int tx;
        int ty;
        int dieroll;
        switch(state) {
            case SPAWN:
                dieroll = rand.nextInt(4);
                if (dieroll != 0) {
                    this.state = AIController.FSMState.WANDER;
                } else {
                    this.selectTarget();
                    if (this.target == null) {
                        this.state = AIController.FSMState.WANDER;
                    } else {
                        tx = this.board.screenToBoard(this.target.getX());
                        ty = this.board.screenToBoard(this.target.getY());
                        if (this.manhattan(sx, sy, tx, ty) > 4) {
                            this.state = AIController.FSMState.CHASE;
                        } else {
                            this.state = AIController.FSMState.ATTACK;
                        }
                    }
                }
                break;
            case WANDER:
                this.selectTarget();
                if (this.target != null) {
                    tx = this.board.screenToBoard(this.target.getX());
                    ty = this.board.screenToBoard(this.target.getY());
                    int dist = this.manhattan(sx, sy, tx, ty);
                    if (dist <= 4) {
                        this.state = AIController.FSMState.ATTACK;
                        this.wx = -1;
                    } else if (dist <= 9) {
                        this.state = AIController.FSMState.CHASE;
                        this.wx = -1;
                    }
                }

                if (this.wx == sx && this.wy == sy) {
                    this.wx = -1;
                    this.wy = -1;
                }
                break;
            case CHASE:
                dieroll = rand.nextInt(20);
                if (dieroll != 0 && this.target != null && this.target.isActive()) {
                    tx = this.board.screenToBoard(this.target.getX());
                    ty = this.board.screenToBoard(this.target.getY());
                    if (this.manhattan(sx, sy, tx, ty) <= 4) {
                        this.state = AIController.FSMState.ATTACK;
                    }
                } else {
                    this.state = AIController.FSMState.WANDER;
                }
                break;
            case ATTACK:
                dieroll = rand.nextInt(100);
                if (this.target != null && this.target.isActive()) {
                    tx = this.board.screenToBoard(this.target.getX());
                    ty = this.board.screenToBoard(this.target.getY());
                    if (this.manhattan(sx, sy, tx, ty) > 4) {
                        this.state = AIController.FSMState.CHASE;
                        dieroll = rand.nextInt(2);
                    }
                } else {
                    this.state = AIController.FSMState.WANDER;
                }

                if (dieroll == 0) {
                    this.state = AIController.FSMState.WANDER;
                }
                break;
            default:
                assert false;

                this.state = AIController.FSMState.WANDER;
        }

    }

    private void selectTarget() {
    }

    private boolean canShootTargetFrom(int x, int y) {
        int tx = this.board.screenToBoard(this.target.getX());
        int ty = this.board.screenToBoard(this.target.getY());
        int dx = tx > x ? tx - x : x - tx;
        int dy = ty > y ? ty - y : y - ty;
        boolean power = this.board.isPowerTileAt(x, y);
        boolean canhit = dx <= 4 && dy == 0;
        canhit |= dx == 0 && dy <= 4;
        canhit |= power && dx == dy && dx <= 3;
        canhit &= this.board.isSafeAt(x, y);
        return canhit;
    }

    private boolean canShootTarget() {
        int sx = this.board.screenToBoard(this.ship.getX());
        int sy = this.board.screenToBoard(this.ship.getY());
        return this.ship.canShoot() && this.canShootTargetFrom(sx, sy);
    }

    private void markGoalTiles() {
        this.board.clearMarks();
        boolean setGoal = false;
        int tx;
        int ty;
        int sy;
        switch(state) {
            case SPAWN:
                setGoal = false;
                break;
            case WANDER:
                Random random = new Random();
                if (this.wx == -1) {
                    this.wx = random.nextInt(this.board.getWidth() - 1) + 1;
                    this.wy = random.nextInt(this.board.getHeight() - 1) + 1;
                }

                this.board.setGoal(this.wx, this.wy);
                setGoal = true;
                break;
            case CHASE:
                tx = this.board.screenToBoard(this.target.getX());
                ty = this.board.screenToBoard(this.target.getY());
                this.board.setGoal(tx, ty);
                setGoal = true;
                break;
            case ATTACK:
                tx = this.board.screenToBoard(this.target.getX());
                ty = this.board.screenToBoard(this.target.getY());
                sy = tx < 4 ? 0 : tx - 4;
                int maxx = tx >= this.board.getWidth() - 4 ? this.board.getWidth() - 1 : tx + 4;
                int miny = ty < 4 ? 0 : ty - 4;
                int maxy = ty >= this.board.getHeight() - 4 ? this.board.getHeight() - 1 : ty + 4;

                for(int ii = sy; ii <= maxx; ++ii) {
                    for(int jj = miny; jj <= maxy; ++jj) {
                        if (this.canShootTargetFrom(ii, jj)) {
                            this.board.setGoal(ii, jj);
                            setGoal = true;
                        }
                    }
                }

                if (!setGoal) {
                    tx = this.board.screenToBoard(this.target.getX());
                    ty = this.board.screenToBoard(this.target.getY());
                    this.board.setGoal(tx, ty);
                    setGoal = true;
                }
        }

        if (!setGoal) {
            int sx = this.board.screenToBoard(this.ship.getX());
            sy = this.board.screenToBoard(this.ship.getY());
            this.board.setGoal(sx, sy);
        }

    }

    private int getMoveAlongPathToGoalTile() {
        ArrayDeque<AIController.PathNode> queue = new ArrayDeque();
        AIController.PathNode curr = new AIController.PathNode(this.board.screenToBoard(this.ship.getX()), this.board.screenToBoard(this.ship.getY()), 0);
        queue.add(curr);
        if (board.inBounds(curr.x, curr.y)) this.board.setVisited(curr.x, curr.y);

        while(queue.size() != 0) {
            curr = (AIController.PathNode)queue.pollFirst();
            if (this.board.isGoal(curr.x, curr.y)) {
                return curr.act;
            }

            boolean horiz = this.choosePriority(curr);

            for(int ii = 0; ii < 2; ++ii) {
                for(int jj = 0; jj < 2; ++jj) {
                    AIController.PathNode next = new AIController.PathNode(curr.x, curr.y, curr.act);
                    if (ii == 0 && horiz || ii == 1 && !horiz) {
                        next.x += jj == 0 ? -1 : 1;
                    } else {
                        next.y += jj == 0 ? -1 : 1;
                    }

                    if (!this.board.isVisited(next.x, next.y) && this.board.isSafeAt(next.x, next.y)) {
                        int dir;
                        if (ii == 0 && horiz || ii == 1 && !horiz) {
                            dir = jj == 0 ? 1 : 2;
                        } else {
                            dir = jj == 0 ? 4 : 8;
                        }

                        next.act = curr.act == 0 ? dir : curr.act;
                        this.board.setVisited(next.x, next.y);
                        queue.add(next);
                    }
                }
            }
        }

        return 0;
    }

    private boolean choosePriority(AIController.PathNode curr) {
        boolean horiz = true;
        if (this.state == AIController.FSMState.CHASE) {
            int dx = Math.abs(curr.x - this.board.screenToBoard(this.target.getX()));
            int dy = Math.abs(curr.y - this.board.screenToBoard(this.target.getY()));
            if (dy > dx) {
                horiz = false;
            }
        } else if (this.state == AIController.FSMState.ATTACK) {
            float a = this.ship.getAngle();
            horiz = 0.0F <= a && a < 45.0F || 135.0F < a && a < 225.0F || 335.0F < a && a <= 360.0F;
        }

        return horiz;
    }

    private int manhattan(int x0, int y0, int x1, int y1) {
        return Math.abs(x1 - x0) + Math.abs(y1 - y0);
    }

    private static enum FSMState {
        SPAWN,
        WANDER,
        CHASE,
        ATTACK;

        private FSMState() {
        }
    }

    private static class PathNode {
        public int x;
        public int y;
        public int act;

        public PathNode(int x, int y, int act) {
            this.x = x;
            this.y = y;
            this.act = act;
        }
    }
}

