package edu.cornell.gdiac.physics.floor;

import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.physics.Board;
import edu.cornell.gdiac.physics.floor.character.EnemyModel;
import edu.cornell.gdiac.physics.floor.character.JoeModel;
import edu.cornell.gdiac.util.LevelEditorParser;
import javafx.scene.layout.Priority;
import javafx.scene.shape.Path;

import java.lang.reflect.Array;
import java.util.*;

public class AIController {
    //private static final int CHASE_DIST = 12;
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
    private int chaseDist;
    private int TILE_SIZE = 1;

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
        chaseDist=9;
        if (ship.getName()=="slime") chaseDist=16;
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
        //System.out.println(this.ship.getId()+"/state: "+state+"/action: "+action);
        if (this.state ==FSMState.ATTACK && this.canShootTarget()) {
            action = FloorController.CONTROL_FIRE;
        }

        return action;
    }

    private float getEqY(float x0, float y0, float x1, float y1, float x){
        return (y0-y1)/(x0-x1)*(x-x0)+y0;
    }

    private boolean canSeeJoe(){
        Vector2 shipPos = new Vector2(ship.getX(), ship.getY());
        Vector2 tarPos = new Vector2(target.getX(), target.getY());

        float lineLength = (float) Math.sqrt((tarPos.x-shipPos.x)*(tarPos.x-shipPos.x) + (tarPos.y-shipPos.y)*(tarPos.y-shipPos.y));
        float tilted = (tarPos.y-shipPos.y)/(tarPos.x-shipPos.x);
        float x = Math.min(shipPos.x, tarPos.x);
        float y = shipPos.x > tarPos.x ? tarPos.y : shipPos.y;

        for (int ii=0; ii < (int) lineLength/TILE_SIZE; ii++){
            int tileX = board.screenToBoardX(x);
            int tileY = board.screenToBoardY(y);
            if (board.isBlocked(tileX, tileY)) return false;
            x = TILE_SIZE/(float) Math.sqrt(tilted*tilted+1) + x;
            y = getEqY(tarPos.x, tarPos.y, shipPos.x, shipPos.y, x);
        }
        return true;
    }

    private boolean hasNoWallBetw(int x, int y, int tx, int ty){
        if (y==ty){
            for (int i=Math.min(x, tx); i<=Math.max(x, tx); i++){
                if (board.isBlocked(i, y)) {
                    return false;
                }
            }
        }
        if (tx==x){
            for (int j=Math.min(y, ty); j<=Math.max(y, ty); j++){
                if (board.isBlocked(x, j)) {
                    return false;
                }
            }
        }
        return true;
    }

    //fix this - look at the path?
    private boolean hasNoHazardBetw (int sx, int sy, int tx, int ty) {
        return true;
    }

    private void changeStateIfApplicable() {
        Random rand = new Random();
        int sx = this.board.screenToBoardX(this.ship.getX());
        int sy = this.board.screenToBoardY(this.ship.getY());
        int tx;
        int ty;
        switch(state) {
            case SPAWN:
                if (canSeeJoe()) {
                    tx = this.board.screenToBoardX(this.target.getX());
                    ty = this.board.screenToBoardY(this.target.getY());
                    if (this.ship.canHitTargetFrom(sx,sy,tx,ty) && hasNoHazardBetw(sx, sy, tx, ty)) {
                        this.state = FSMState.ATTACK;
                    } else {
                        this.state = FSMState.CHASE;
                    }
                }
                else {
                    this.state=FSMState.WANDER;
                }
                break;
            case WANDER:
                if (!target.isRemoved() && canSeeJoe()) {
                    tx = this.board.screenToBoardX(this.target.getX());
                    ty = this.board.screenToBoardY(this.target.getY());
                    int dist = this.manhattan(sx, sy, tx, ty);
                    if (this.ship.canHitTargetFrom(sx,sy,tx,ty) && hasNoHazardBetw(sx, sy,tx,ty)) {
                        this.state =FSMState.ATTACK;
                        this.wx = -1;
                    } else if (dist <= chaseDist) {
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
                if (!target.isRemoved() && canSeeJoe()) {
                    tx = this.board.screenToBoardX(this.target.getX());
                    ty = this.board.screenToBoardY(this.target.getY());
                    if (this.ship.canHitTargetFrom(sx,sy,tx,ty) && hasNoHazardBetw(sx, sy, tx, ty)) {
                        this.state =FSMState.ATTACK;
                    }
                } else {
                    this.state = FSMState.WANDER;
                }
                break;
            case ATTACK:
                if (!target.isRemoved() && canSeeJoe()) {
                    tx = this.board.screenToBoardX(this.target.getX());
                    ty = this.board.screenToBoardY(this.target.getY());
                    if (!(this.ship.canHitTargetFrom(sx,sy,tx,ty) && hasNoHazardBetw(sx, sy, tx, ty))) {
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
    private void markGoalHelper(int sx, int sy, int n, int dist){
        if (n==0) {
          board.setGoal(sx-dist, sy);
        }
        if (n==1) {
           board.setGoal(sx+dist, sy);
        }
        if (n==2) {
            board.setGoal(sx, sy+dist);
        }
        if (n==3) {
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
        int dx;
        int dy;
        boolean b;
        int fin =-1;
        int temp = Integer.MAX_VALUE;
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
                    if (this.patrolSeq<=2) {
                        b = board.isSuperSafeAt(sx-1, sy) && !board.isBlocked(sx-1, sy) &&
                        hasNoHazardBetw(sx, sy, sx-1, sy);
                        patrolSeq++;
                        if (b) {markGoalHelper(sx, sy, 0, 1); setGoal=true; break;}
                    }
                    else {
                        b = board.isSuperSafeAt(sx+1, sy) && !board.isBlocked(sx+1, sy) &&
                        hasNoHazardBetw(sx, sy, sx+1, sy);
                        if (patrolSeq==5) patrolSeq=0;
                        else patrolSeq++;
                        if (b) {markGoalHelper(sx, sy, 1, 1); setGoal=true; break;}
                    }
                }
                else if (patrolPath == PatrolPath.VERTICAL) {
                    if (this.patrolSeq<=2){
                        b = board.isSafeAt(sx, sy+1) && !board.isBlocked(sx, sy+1) &&
                        hasNoHazardBetw(sx, sy, sx, sy+1);
                        patrolSeq++;
                        if (b) {markGoalHelper(sx, sy, 2, 1); setGoal=true; break;}
                    }
                    else {
                        b = board.isSuperSafeAt(sx, sy-1) && !board.isBlocked(sx, sy-1) &&
                        hasNoHazardBetw(sx, sy, sx, sy-1);
                        if (patrolSeq==5) patrolSeq=0;
                        else patrolSeq++;
                        if (b) {markGoalHelper(sx, sy, 3, 1); setGoal=true; break;}
                    }
                }
                else {
                    if (this.patrolSeq < 1 || (patrolSeq>=7)){
                        b = board.isSuperSafeAt(sx, sy+1) && !board.isBlocked(sx, sy+1) &&
                        hasNoHazardBetw(sx, sy, sx, sy+1);
                        if (patrolSeq==7) patrolSeq=0;
                        else patrolSeq++;
                        if (b) {markGoalHelper(sx, sy, 2, 1); setGoal=true; break;}
                    }
                    else if (patrolSeq <=2) {
                        b = board.isSuperSafeAt(sx-1, sy) && !board.isBlocked(sx-1, sy) &&
                        hasNoHazardBetw(sx, sy, sx-1, sy);
                        patrolSeq++;
                        if (b) {markGoalHelper(sx, sy, 0, 1); setGoal=true; break;}
                    }
                    else if (patrolSeq <=4){
                        b = board.isSuperSafeAt(sx, sy-1) && !board.isBlocked(sx, sy-1) &&
                        hasNoHazardBetw(sx, sy, sx, sy-1);
                        patrolSeq++;
                        if (b) {markGoalHelper(sx, sy, 3, 1); setGoal=true; break;}
                    }
                    else if (patrolSeq <=6) {
                        b = board.isSuperSafeAt(sx+1, sy) && !board.isBlocked(sx+1, sy) &&
                        hasNoHazardBetw(sx, sy, sx+1, sy);
                        patrolSeq++;
                        if (b) {markGoalHelper(sx, sy, 1, 1); setGoal=true; break;}
                    }
                }
                break;
            case CHASE:
                sx = this.board.screenToBoardX(ship.getX());
                sy = this.board.screenToBoardY(ship.getY());
                tx = this.board.screenToBoardX(this.target.getX());
                ty = this.board.screenToBoardY(this.target.getY());
                if (ship.canHitTargetFrom(sx, sy, tx, ty) && hasNoWallBetw(sx, sy, tx, ty) && hasNoHazardBetw(sx, sy, tx, ty)) {
                    System.out.println("here"); break;
                }

                dx = Math.abs(sx-tx);
                dy = Math.abs(sy-ty);
                attackRange = dx >=2 && dx < attackRange && dy <= dx ? dx : (dy < attackRange && dy >= 2 && dx < dy ? dy : attackRange);

                int manLeft = manhattan(sx, sy, tx-attackRange, ty);
                int manRight = manhattan(sx, sy, tx+attackRange, ty);
                int manUp =  manhattan(sx, sy, tx, ty+attackRange);
                int manDown =  manhattan(sx, sy, tx, ty-attackRange);

                if (board.isSuperSafeAt(tx-attackRange, ty) && hasNoWallBetw(tx-attackRange, ty, tx, ty) &&
                        hasNoHazardBetw(sx, sy, tx-attackRange, ty)){
                    fin = 0; temp=manLeft;
                }
                if (board.isSuperSafeAt(tx+attackRange, ty) && manRight<temp && hasNoWallBetw(tx+attackRange, ty, tx, ty)
                        && hasNoHazardBetw(sx, sy,tx+attackRange, ty)) {
                    fin=1; temp=manRight;
                }
                if (board.isSuperSafeAt(tx , ty+attackRange) && manUp<temp && hasNoWallBetw(tx, ty, tx, ty+attackRange)
                        && hasNoHazardBetw(sx, sy, tx, ty+attackRange)) {
                    fin=2; temp=manUp;
                }
                if (board.isSuperSafeAt(tx , ty-attackRange) && manDown<temp && hasNoWallBetw(tx, ty, tx, ty-attackRange)
                        && hasNoHazardBetw(sx, sy, tx, ty-attackRange)) {
                    fin=3;
                }
                if (fin < 0) break;
                markGoalHelper(tx, ty, fin, attackRange);
                setGoal=true;
                break;
            case ATTACK:
                sx = this.board.screenToBoardX(ship.getX());
                sy = this.board.screenToBoardY(ship.getY());
                tx = this.board.screenToBoardX(this.target.getX());
                ty = this.board.screenToBoardY(this.target.getY());
                if (ship.canHitTargetFrom(sx, sy, tx, ty) && hasNoWallBetw(sx, sy, tx, ty) &&
                        hasNoHazardBetw(sx, sy, tx, ty)) break;

                dx = Math.abs(sx-tx);
                dy = Math.abs(sy-ty);
                attackRange = dx >=2 && dx < attackRange && dy <= dx ? dx : (dy < attackRange && dy >= 2 && dx < dy ? dy : attackRange);

                int manhLeft = manhattan(sx, sy, tx-attackRange, ty);
                int manhRight = manhattan(sx, sy, tx+attackRange, ty);
                int manhUp =  manhattan(sx, sy, tx, ty+attackRange);
                int manhDown =  manhattan(sx, sy, tx, ty-attackRange);

                if (board.isSuperSafeAt(tx-attackRange, ty) && hasNoWallBetw(tx, ty, tx-attackRange, ty) &&
                        hasNoHazardBetw(sx, sy,tx-attackRange, ty)){
                    fin=0; temp=manhLeft;
                }
                if (board.isSuperSafeAt(tx+attackRange, ty) && manhRight<temp && hasNoWallBetw(tx, ty, tx+attackRange, ty)
                        && hasNoHazardBetw(sx, sy,tx+attackRange, ty)) {
                    fin=1; temp=manhRight;
                }
                if (board.isSuperSafeAt(tx, ty+attackRange) && manhUp<temp && hasNoWallBetw(tx, ty, tx, ty+attackRange)
                        && hasNoHazardBetw(sx,sy, tx, ty+attackRange)) {
                    fin=2; temp=manhUp;
                }
                if (board.isSuperSafeAt(tx, ty-attackRange) && manhDown<temp && hasNoWallBetw(tx, ty, tx, ty-attackRange)
                        && hasNoHazardBetw(sx, sy, tx, ty-attackRange)) {
                    fin=3;
                }
                if (fin < 0) break;
                markGoalHelper(tx, ty, fin, attackRange);
                setGoal=true;
                break;
        }
        if (!setGoal) {
            sx = this.board.screenToBoardX(this.ship.getX());
            sy = this.board.screenToBoardY(this.ship.getY());
            board.setGoal(sx, sy);
        }
    }

    private int heuristicCostEstimate(PathNode curr){
        return 10*manhattan(curr.x, curr.y, board.getGoal().x, board.getGoal().y);
    }
    private int distanceBetween(PathNode n0, PathNode n1){
        if (n0.x==n1.x || n1.y==n0.y){
            return 10;
        }
        return 14;
    }

    //need to work on this / should look at corners for walls?
    private boolean isAtCorner(int x, int y){
        return true;
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

                    if (this.board.isSuperSafeAt(next.x, next.y) && !this.board.isVisited(next.x, next.y) &&
                            (next.g==null || tentativeG < next.g)) {          //fix this
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