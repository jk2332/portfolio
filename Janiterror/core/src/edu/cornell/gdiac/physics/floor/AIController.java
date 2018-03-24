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
    private static final int CHASE_DIST = 9;
    private static final int ATTACK_DIST = 1; //not used - moved to scientistmodel/robotmodel
    private EnemyModel ship;
    private Board board;
    private EnemyModel[] fleet;
    private AIController.FSMState state;
    private JoeModel target;
    private int move;
    private long ticks;
    private int wx = 0;
    private int wy = 0;

    public AIController(int id, Board board, EnemyModel[] ships, JoeModel target) {
        this.ship =  (EnemyModel) Array.get(ships, id);
        this.board = board;
        this.fleet = ships;
        this.state = FSMState.CHASE;
        this.move = 0;
        this.ticks = 0L;
        this.target = target;
        //this.selectTarget();
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
            action = 16;
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
                    if (this.target == null) {
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
                if (this.target != null) {
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
                if (this.target != null && this.target.isActive()) {
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
                dieroll = rand.nextInt(100);
                if (this.target != null && this.target.isActive()) {
                    tx = this.board.screenToBoardX(this.target.getX());
                    ty = this.board.screenToBoardY(this.target.getY());
                    if (!this.ship.canShootTargetFrom(sx,sy,tx,ty)) {
                        this.state =FSMState.CHASE;
                        dieroll = rand.nextInt(2);
                    }
                } else {
                    this.state = FSMState.WANDER;
                }
                break;
            default:
                assert false;
                this.state = FSMState.WANDER;
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

                if (board.inBounds(wx, wy)) this.board.setGoal(this.wx, this.wy);
                setGoal = true;
                break;
            case CHASE:
                tx = this.board.screenToBoardX(this.target.getX());
                ty = this.board.screenToBoardY(this.target.getY());
                int nums=0;
                int rand = (int) (CHASE_DIST*Math.random());
                if (board.inBounds(tx+rand, ty)) {
                    this.board.setGoal(tx+rand, ty); nums++;
                }
                if (board.inBounds(tx-rand, ty)) {this.board.setGoal(tx-rand, ty);  nums++;}
                if (board.inBounds(tx, ty+rand)) {this.board.setGoal(tx, ty+rand); nums++;}
                if (board.inBounds(tx, ty-rand)) {this.board.setGoal(tx, ty-rand); nums++;}
                if (nums!=0) setGoal=true;
            case ATTACK:
                tx = this.board.screenToBoardX(this.target.getX());
                ty = this.board.screenToBoardY(this.target.getY());
                int nums2=0;
                if (board.inBounds(tx+ATTACK_DIST, ty)) {
                    this.board.setGoal(tx+ATTACK_DIST, ty); nums2++;
                }
                if (board.inBounds(tx-ATTACK_DIST, ty)) {this.board.setGoal(tx-ATTACK_DIST, ty);  nums2++;}
                if (board.inBounds(tx, ty+ATTACK_DIST)) {this.board.setGoal(tx, ty+ATTACK_DIST); nums2++;}
                if (board.inBounds(tx, ty-ATTACK_DIST)) {this.board.setGoal(tx, ty-ATTACK_DIST); nums2++;}
                if (nums2!=0) setGoal=true;
        }

        if (!setGoal) {
            int sx = this.board.screenToBoardX(this.ship.getX());
            sy = this.board.screenToBoardY(this.ship.getY());
            this.board.setGoal(sx, sy);
        }
    }

    private int getMoveAlongPathToGoalTile() {
        ArrayDeque<PathNode> queue = new ArrayDeque();
        //System.out.println("ai positionx: "+this.board.screenToBoardX(this.ship.getX())+" /ai positiony: "+this.board.screenToBoardY(this.ship.getY()));
        //System.out.println("ai boardx: "+this.board.screenToBoardX(this.ship.getX())+" /ai positiony: "+this.board.screenToBoardY(this.ship.getY()));
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
