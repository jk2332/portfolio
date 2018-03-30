//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package edu.cornell.gdiac.physics.floor;

import edu.cornell.gdiac.physics.Board;
import edu.cornell.gdiac.physics.floor.monster.EnemyModel;
import edu.cornell.gdiac.physics.floor.monster.JoeModel;
import edu.cornell.gdiac.physics.floor.monster.ScientistModel;

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Random;

public class AIController {
    private static final int CHASE_DIST = 12;
    //private static final int ATTACK_DIST = 1; //not used - moved to scientistmodel/robotmodel
    private EnemyModel ship;
    private Board board;
    private EnemyModel[] fleet;
    private AIController.FSMState state;
    private JoeModel target;
    private int move;
    private long ticks;
    private int wx = 0;
    private int wy = 0;
    private int patrolState;

    public AIController(int id, Board board, EnemyModel[] ships, JoeModel target) {
        this.ship =  (EnemyModel) Array.get(ships, id);
        this.board = board;
        this.fleet = ships;
        this.state = FSMState.WANDER;
        this.move = 0;
        this.ticks = 0L;
        this.target = target;
        this.patrolState=0;
    }

    public int getAction() {
        ++this.ticks;
        if (((long)this.ship.getId() + this.ticks) % 10L == 0L) {
            this.changeStateIfApplicable();
            this.markGoalTiles(); //System.out.println("id: "+ship.getId());
            this.move = this.getMoveAlongPathToGoalTile();
        }

        int action = this.move;
        if (this.state ==FSMState.ATTACK && this.canShootTarget()) {
            action = FloorController.CONTROL_FIRE;
        }

        return action;
    }

    private void changeStateIfApplicable() {
        Random rand = new Random();
        int sx = this.board.screenToBoardX(this.ship.getX());
        int sy = this.board.screenToBoardY(this.ship.getY());
        int tx;
        int ty;
        int dieroll;
        switch(state) {
            case SPAWN:
                dieroll = rand.nextInt(4);
                if (dieroll != 0) {
                    this.state = FSMState.WANDER;
                } else {
                    this.selectTarget();
                    if (!target.isAlive()) {
                        this.state = FSMState.WANDER;
                    } else {
                        tx = this.board.screenToBoardX(this.target.getX());
                        ty = this.board.screenToBoardY(this.target.getY());
                        if (!this.ship.canShootTargetFrom(sx,sy,tx,ty)) {
                            this.state = FSMState.CHASE;
                        } else {
                            this.state = FSMState.ATTACK;
                        }
                    }
                }
                break;
            case WANDER:
                this.selectTarget();
                if (!target.isAlive()) {
                    tx = this.board.screenToBoardX(this.target.getX());
                    ty = this.board.screenToBoardY(this.target.getY());
                    int dist = this.manhattan(sx, sy, tx, ty);
                    if (this.ship.canShootTargetFrom(sx,sy,tx,ty)) {
                        this.state =FSMState.ATTACK;
                        this.wx = -1;
                    } else if (dist <= CHASE_DIST) {
                        this.state = FSMState.CHASE;
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
                if (!target.isAlive()) {
                    tx = this.board.screenToBoardX(this.target.getX());
                    ty = this.board.screenToBoardY(this.target.getY());
                    if (this.ship.canShootTargetFrom(sx,sy,tx,ty)) {
                        this.state =FSMState.ATTACK;
                    }
                } else {
                    this.state = FSMState.WANDER;
                }
                break;
            case ATTACK:
                if (!target.isAlive()) {
                    tx = this.board.screenToBoardX(this.target.getX());
                    ty = this.board.screenToBoardY(this.target.getY());
                    if (!this.ship.canShootTargetFrom(sx,sy,tx,ty)) {
                        this.state =FSMState.CHASE;
                    }
                } else {
                    this.state = FSMState.WANDER;
                }
                break;
            default:
                assert false;
        }

    }

    private void selectTarget() {
    }

    private boolean canShootTargetFrom(int x, int y) {
        if (!this.board.inBounds(x, y)) {
            return false;
        }

        int tx = this.board.screenToBoardX(this.target.getX());
        int ty = this.board.screenToBoardY(this.target.getY());
        return this.ship.canShootTargetFrom(x,y,tx,ty);
    }

    private boolean canShootTarget() {
        int sx = this.board.screenToBoardX(this.ship.getX());
        int sy = this.board.screenToBoardY(this.ship.getY());
        return this.ship.canShoot() && this.canShootTargetFrom(sx, sy);
    }

    // n=0 : left, n=1 : right, n=2 : up, n=3 : down
    private void markGoalHelper(int n, int dist){
        int sx = board.screenToBoardX(ship.getX());
        int sy = board.screenToBoardY(ship.getY());
        if (n==0 && board.inBounds(sx-dist, sy)) {
            board.setGoal(sx-dist, sy);
        }
        if (n==1 && board.inBounds(sx+dist, sy)) {
            board.setGoal(sx+dist, sy);
        }if (n==2 && board.inBounds(sx, sy+dist)) {
            board.setGoal(sx, sy+dist);
        }if (n==3 && board.inBounds(sx, sy-dist)) {
            board.setGoal(sx, sy-dist);
        }
    }

    private void markGoalTiles() {
        this.board.clearMarks();
        boolean setGoal = false;
        int tx;
        int ty;
        int sy;
        int sx;
        switch(state) {
            case SPAWN:
                setGoal = false;
                break;
            case WANDER:
                //System.out.println("WANDER");
                if (this.patrolState==0) { markGoalHelper(2, 1); setGoal=true; patrolState++; break; }
                if (this.patrolState==1) { markGoalHelper(0, 2); setGoal=true; patrolState++; break; }
                if (this.patrolState==2) { markGoalHelper(0, 2); setGoal=true; patrolState++; break; }
                if (this.patrolState==3) { markGoalHelper(3, 2); setGoal=true; patrolState++; break; }
                if (this.patrolState==4) { markGoalHelper(3, 2); setGoal=true; patrolState++; break; }
                if (this.patrolState==5) { markGoalHelper(1, 2); setGoal=true; patrolState++; break; }
                if (this.patrolState==6) { markGoalHelper(1, 2); setGoal=true; patrolState++; break; }
                if (this.patrolState==7) { markGoalHelper(2, 1); setGoal=true; patrolState=0; break; }

                break;
            case CHASE:
                //System.out.println("CHASE");
                tx = this.board.screenToBoardX(this.target.getX());
                ty = this.board.screenToBoardY(this.target.getY());
                int nums=0;
                int rand = (int) ((CHASE_DIST-2)*Math.random())+2;
                if (board.inBounds(tx+rand, ty)) {
                    this.board.setGoal(tx+rand, ty); nums++;
                }
                if (board.inBounds(tx-rand, ty)) {this.board.setGoal(tx-rand, ty);  nums++;}
                if (board.inBounds(tx, ty+rand)) {this.board.setGoal(tx, ty+rand); nums++;}
                if (board.inBounds(tx, ty-rand)) {this.board.setGoal(tx, ty-rand); nums++;}
                if (nums!=0) setGoal=true;
            case ATTACK:
                //System.out.println("ATTACK");
                tx = this.board.screenToBoardX(this.target.getX());
                ty = this.board.screenToBoardY(this.target.getY());
                int nums2=0;
                int attackRange = this.ship.getAttackRange();
                for (int i = -attackRange; i <= attackRange; i++) {
                    if (!(i == 0) && board.inBounds(tx + attackRange,ty)) {
                        this.board.setGoal(tx + attackRange, ty); nums2++;
                    }
                    if (!(i == 0) && board.inBounds(tx,ty + attackRange)) {
                        this.board.setGoal(tx, ty + attackRange); nums2++;
                    }
                }
                if (board.inBounds(tx+attackRange, ty)) {
                    this.board.setGoal(tx+attackRange, ty); nums2++;
                }
                if (board.inBounds(tx-attackRange, ty)) {this.board.setGoal(tx-attackRange, ty);  nums2++;}
                if (board.inBounds(tx, ty+attackRange)) {this.board.setGoal(tx, ty+attackRange); nums2++;}
                if (board.inBounds(tx, ty-attackRange)) {this.board.setGoal(tx, ty-attackRange); nums2++;}
                if (nums2!=0) setGoal=true;
        }

        if (!setGoal) {
            sx = this.board.screenToBoardX(this.ship.getX());
            sy = this.board.screenToBoardY(this.ship.getY());
            this.board.setGoal(sx, sy);
        }
    }

    private int getMoveAlongPathToGoalTile() {
        ArrayDeque<PathNode> queue = new ArrayDeque();
        PathNode curr = new PathNode(this.board.screenToBoardX(this.ship.getX()), this.board.screenToBoardY(this.ship.getY()), 0);
        queue.add(curr);
        if (board.isSafeAt(curr.x, curr.y)) this.board.setVisited(curr.x, curr.y);

        while(queue.size() != 0) {
            curr = (PathNode)queue.pollFirst();
            if (this.board.isGoal(curr.x, curr.y)) {
                return curr.act;
            }

            boolean horiz = this.choosePriority(curr);

            for(int ii = 0; ii < 2; ++ii) {
                for(int jj = 0; jj < 2; ++jj) {
                    PathNode next = new PathNode(curr.x, curr.y, curr.act);
                    if (ii == 0 && horiz || ii == 1 && !horiz) {
                        next.x += jj == 0 ? -1 : 1;
                    } else {
                        next.y += jj == 0 ? -1 : 1;
                    }

                    if (!this.board.isVisited(next.x, next.y) && this.board.isSafeAt(next.x, next.y)) {
                        int dir;
                        if (ii == 0 && horiz || ii == 1 && !horiz) {
                            dir = jj == 0 ? FloorController.CONTROL_MOVE_LEFT : FloorController.CONTROL_MOVE_RIGHT;
                        } else {
                            dir = jj == 0 ? FloorController.CONTROL_MOVE_DOWN : FloorController.CONTROL_MOVE_UP;
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

    private boolean choosePriority(PathNode curr) {
        boolean horiz = true;
        if (this.state == FSMState.CHASE) {
            int dx = Math.abs(curr.x - this.board.screenToBoardX(this.target.getX()));
            int dy = Math.abs(curr.y - this.board.screenToBoardY(this.target.getY()));
            if (dy > dx) {
                horiz = false;
            }
        } else if (this.state == FSMState.ATTACK) {
            //float a = this.ship.getAngle();
            //horiz = 0.0F <= a && a < 45.0F || 135.0F < a && a < 225.0F || 335.0F < a && a <= 360.0F;
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
