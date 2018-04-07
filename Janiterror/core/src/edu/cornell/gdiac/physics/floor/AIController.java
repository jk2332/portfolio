package edu.cornell.gdiac.physics.floor;

import edu.cornell.gdiac.physics.Board;
import edu.cornell.gdiac.physics.floor.character.EnemyModel;
import edu.cornell.gdiac.physics.floor.character.JoeModel;
import javafx.scene.layout.Priority;
import javafx.scene.shape.Path;

import java.lang.reflect.Array;
import java.util.*;

public class AIController {
    private static final int CHASE_DIST = 9;
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
    private int patrolSeq;
    private PatrolPath patrolPath;


    public AIController(int id, Board board, EnemyModel[] ships, JoeModel target) {
        this.ship =  (EnemyModel) Array.get(ships, id);
        this.board = board;
        this.fleet = ships;
        this.state = FSMState.SPAWN;
        this.move = 0;
        this.ticks = 0L;
        this.target = target;
        double tmp = Math.random();
        patrolPath = tmp <= 0.4d ? PatrolPath.SQUARE : (tmp <= 0.7d ? PatrolPath.HORIZONTAL : PatrolPath.VERTICAL);
        patrolSeq=0;
    }

    public int getAction() {
        ++this.ticks;
        if (((long)this.ship.getId() + this.ticks) % 20L == 0L) {
            this.changeStateIfApplicable();
            this.markGoalTiles();
            this.move = this.getMoveAlongPathToGoalTile();
        }

        int action = this.move;
        System.out.println(this.ship.getId()+"/state: "+state+"/action: "+action);
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
                    if (target.isRemoved()) {
                        this.state = FSMState.WANDER;
                    } else {
                        tx = this.board.screenToBoardX(this.target.getX());
                        ty = this.board.screenToBoardY(this.target.getY());
                        if (!this.ship.canHitTargetFrom(sx,sy,tx,ty)) {
                            this.state = FSMState.CHASE;
                        } else {
                            this.state = FSMState.ATTACK;
                        }
                    }
                }
                break;
            case WANDER:
                if (!target.isRemoved()) {
                    tx = this.board.screenToBoardX(this.target.getX());
                    ty = this.board.screenToBoardY(this.target.getY());
                    int dist = this.manhattan(sx, sy, tx, ty);
                    if (this.ship.canHitTargetFrom(sx,sy,tx,ty)) {
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
                if (!target.isRemoved()) {
                    tx = this.board.screenToBoardX(this.target.getX());
                    ty = this.board.screenToBoardY(this.target.getY());
                    if (this.ship.canHitTargetFrom(sx,sy,tx,ty)) {
                        this.state =FSMState.ATTACK;
                    }
                } else {
                    this.state = FSMState.WANDER;
                }
                break;
            case ATTACK:
                if (!target.isRemoved()) {
                    tx = this.board.screenToBoardX(this.target.getX());
                    ty = this.board.screenToBoardY(this.target.getY());
                    if (!this.ship.canHitTargetFrom(sx,sy,tx,ty)) {
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

    private boolean canShootTargetFrom(int x, int y) {
        if (!this.board.isSafeAt(x, y)) {
            return false;
        }

        int tx = this.board.screenToBoardX(this.target.getX());
        int ty = this.board.screenToBoardY(this.target.getY());
        return this.ship.canHitTargetFrom(x,y,tx,ty);
    }

    private boolean canShootTarget() {
        int sx = this.board.screenToBoardX(this.ship.getX());
        int sy = this.board.screenToBoardY(this.ship.getY());
        return this.ship.canAttack() && this.canShootTargetFrom(sx, sy);
    }

    // n=0 : left, n=1 : right, n=2 : up, n=3 : down
    private boolean markGoalHelper(int sx, int sy, int n, int dist){
        if (n==0 && board.isSafeAt(sx-dist, sy)) {
          board.setGoal(sx-dist, sy);
          return true;
        }
        if (n==1 && board.isSafeAt(sx+dist, sy)) {
           board.setGoal(sx+dist, sy);
            return true;
        }
        if (n==2 && board.isSafeAt(sx, sy+dist)) {
            board.setGoal(sx, sy+dist);
            return true;
        }
        if (n==3 && board.isSafeAt(sx, sy-dist)) {
            board.setGoal(sx, sy-dist);
            return true;
        }
        return false;
    }

    private void markGoalTiles() {
        this.board.clearMarks();
        boolean setGoal = false;
        int tx;
        int ty;
        int sy;
        int sx;
        boolean b;
        int attackRange = ship.getAttackRange();
        switch(state) {
            case SPAWN:
                setGoal = false;
                break;
            case WANDER:
                sx = this.board.screenToBoardX(ship.getX());
                sy = this.board.screenToBoardY(ship.getY());
                //System.out.println("WANDER");
                if (patrolPath == PatrolPath.HORIZONTAL){
                    if (this.patrolSeq<=4) {
                        b = markGoalHelper(sx, sy, 0, 1);
                        patrolSeq++;
                        if (b) setGoal=true; break;
                    }
                    else {
                        b = markGoalHelper(sx, sy, 1, 1);
                        if (patrolSeq==9) patrolSeq=0;
                        else patrolSeq++;
                        if (b) setGoal=true; break;
                    }
                }
                else if (patrolPath == PatrolPath.VERTICAL) {
                    if (this.patrolSeq<=4){
                        b = markGoalHelper(sx, sy, 2, 1);
                        patrolSeq++;
                        if (b) setGoal=true; break;
                    }
                    else {
                        b = markGoalHelper(sx, sy, 3, 1);
                        if (patrolSeq==9) patrolSeq=0;
                        else patrolSeq++;
                        if (b) setGoal=true; break;
                    }
                }
                else {
                    if (this.patrolSeq < 2 || (patrolSeq>=14 && patrolSeq<=15)){
                        b = markGoalHelper(sx, sy, 2, 1);
                        if (patrolSeq==15) patrolSeq=0;
                        else patrolSeq++;
                        if (b) setGoal=true; break;
                    }
                    else if (patrolSeq <=5) {
                        b = markGoalHelper(sx, sy, 0, 1);
                        patrolSeq++;
                        if (b) setGoal=true; break;
                    }
                    else if (patrolSeq <=9){
                        b = markGoalHelper(sx, sy, 3, 1);
                        patrolSeq++;
                        if (b) setGoal=true; break;
                    }
                    else if (patrolSeq <=13) {
                        b = markGoalHelper(sx, sy, 1, 1);
                        patrolSeq++;
                        if (b) setGoal=true; break;
                    }
                }
                break;
            case CHASE:
                sx = this.board.screenToBoardX(ship.getX());
                sy = this.board.screenToBoardY(ship.getY());
                tx = this.board.screenToBoardX(this.target.getX());
                ty = this.board.screenToBoardY(this.target.getY());
                int manLeft = manhattan(sx, sy, tx-attackRange, ty);
                int manRight = manhattan(sx, sy, tx+attackRange, ty);
                int manUp =  manhattan(sx, sy, tx, ty+attackRange);
                int manDown =  manhattan(sx, sy, tx, ty-attackRange);
                int fin=-1; int temp=Integer.MAX_VALUE;
                if (board.isSafeAt(tx-attackRange-1, ty)){
                    fin = 0; temp=manLeft;
                }
                if (board.isSafeAt(tx+attackRange+1, ty) && manRight<temp) {
                    fin=1; temp=manRight;
                }
                if (board.isSafeAt(tx, ty+attackRange+1) && manUp<temp) {
                    fin=2; temp=manUp;
                }
                if (board.isSafeAt(tx, ty-attackRange-1) && manDown<temp) {
                    fin=3;
                }
                b = markGoalHelper(tx, ty, fin, attackRange);
                if (b) setGoal=true;
                break;
            case ATTACK:
                sx = this.board.screenToBoardX(ship.getX());
                sy = this.board.screenToBoardY(ship.getY());
                tx = this.board.screenToBoardX(this.target.getX());
                ty = this.board.screenToBoardY(this.target.getY());
                int manhLeft = manhattan(sx, sy, tx-attackRange, ty);
                int manhRight = manhattan(sx, sy, tx+attackRange, ty);
                int manhUp =  manhattan(sx, sy, tx, ty+attackRange);
                int manhDown =  manhattan(sx, sy, tx, ty-attackRange);
                int min = -1; int tmp= Integer.MAX_VALUE;
                if (board.isSafeAt(tx-attackRange, ty)){
                    min=0; tmp=manhLeft;
                }
                if (board.isSafeAt(tx+attackRange, ty) && manhRight<tmp) {
                    min=1; tmp=manhRight;
                }
                if (board.isSafeAt(tx, ty+attackRange) && manhUp<tmp) {
                    min=2; tmp=manhUp;
                }
                if (board.isSafeAt(tx, ty-attackRange) && manhDown<tmp) {
                    min=3;
                }
                b = markGoalHelper(tx, ty, min, attackRange);
                if (b) setGoal=true;
                break;
        }
        if (!setGoal) {
            sx = this.board.screenToBoardX(this.ship.getX());
            sy = this.board.screenToBoardY(this.ship.getY());
            if (board.isSafeAt(sx, sy)) board.setGoal(sx, sy);
        }
    }

    private int heuristicCostEstimate(PathNode curr){
        return 10*manhattan(curr.x, curr.y, board.getGoal().x, board.getGoal().y);
    }
    private int distanceBetween(PathNode n0, PathNode n1){
        if (n0.x==n1.x || n1.y==n0.y){
            return 10;
        }
        System.out.println("here");
        return 14;
    }

    private int getMoveAlongPathToGoalTile() {
        if (board.getGoal().x < 0 || board.getGoal().y < 0) return 0;
        PathNode curr = new PathNode(board.screenToBoardX(ship.getX()), board.screenToBoardY(ship.getY()), 0);
        curr.g =0;
        curr.f =  heuristicCostEstimate(curr);
        final Comparator<PathNode> comparator = new Comparator<PathNode>() {
            /**
             * {@inheritDoc}
             */
            @Override
            public int compare(PathNode o1, PathNode o2) {
                if (o1.f < o2.f) return -1;
                if (o2.f < o1.f) return 1;
                return 0;
            }
        };
        PriorityQueue<PathNode> list = new PriorityQueue<PathNode>(50, comparator);
        list.add(curr);

        while (!list.isEmpty()){
            curr = list.poll();
            if (board.isGoal(curr.x, curr.y)) {
                //System.out.println("reached goal tile");
                return curr.act;
            }
            board.setVisited(curr.x, curr.y);

            boolean horiz = this.choosePriority(curr);

            for(int ii = 0; ii < 2; ++ii) {
                for(int jj = 0; jj < 2; ++jj) {
                    PathNode next = new PathNode(curr.x, curr.y, curr.act);
                    if (ii == 0 && horiz || ii == 1 && !horiz) {
                        next.x += jj == 0 ? -1 : 1;
                    } else {
                        next.y += jj == 0 ? -1 : 1;
                    }
                    int tentativeG = curr.g + distanceBetween(curr, next);

                    if (this.board.isSafeAt(next.x, next.y) && !this.board.isVisited(next.x, next.y) &&
                            (next.g==null || tentativeG < next.g)) {          //fix this
                        //System.out.println("here");
                        int dir;
                        if (ii == 0 && horiz || ii == 1 && !horiz) {
                            dir = jj == 0 ? FloorController.CONTROL_MOVE_LEFT : FloorController.CONTROL_MOVE_RIGHT;
                        } else {
                            dir = jj == 0 ? FloorController.CONTROL_MOVE_DOWN : FloorController.CONTROL_MOVE_UP;
                        }
                        next.g = tentativeG;
                        next.f = next.g + heuristicCostEstimate(next);
                        next.act = curr.act == 0 ? dir : curr.act;
                        this.board.setVisited(next.x, next.y);
                        list.add(next);
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
    }

    private static enum PatrolPath{
        SQUARE,
        VERTICAL,
        HORIZONTAL;
    }

    private class PathNode {
        public int x;
        public int y;
        public Integer g;
        public int f;
        public int act;

        public PathNode(int x, int y, int act) {
            this.x = x;
            this.y = y;
            this.act = act;
            this.f = Integer.MAX_VALUE;
            this.g = null;
        }
    }
}