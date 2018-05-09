package edu.cornell.gdiac.physics.floor;

import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.physics.Board;
import edu.cornell.gdiac.physics.floor.character.EnemyModel;
import edu.cornell.gdiac.physics.floor.character.JoeModel;
import edu.cornell.gdiac.physics.floor.character.TurretModel;
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
    private AIController.FSMState state;
    private JoeModel target;
    private int move;
    private long ticks;
    private int wx = 0;
    private int wy = 0;
    private int patrolSeq;
    private PatrolPath patrolPath;
    private int TILE_SIZE = 1;
    //int horiRange;
    int vertiRange;
    int leftRange;
    int rightRange;
    int indexTile;
    int indexPatrol;
    EnemyModel[] fleet;
    //int indexArray;
    //ArrayList<Vector2> patrol;
    int chaseDist;

    LevelEditorParser level;

    public AIController(int id, Board board, EnemyModel[] ships, JoeModel target) {
        this.ship =  (EnemyModel) Array.get(ships, id);
        this.board = board;
        this.state = FSMState.SPAWN;
        this.fleet=ships;
        this.move = 0;
        this.ticks = 0L;
        this.target = target;
        double tmp = Math.random();
        patrolPath = tmp <= 0.4d ? PatrolPath.SQUARE : (tmp <= 0.7d ? PatrolPath.HORIZONTAL : PatrolPath.VERTICAL);
        patrolSeq=0;
        leftRange = rightRange = ship.getAttackRange();
        vertiRange = ship.getAttackRange()+1;
        if (ship.getName()=="slime" || ship.getName()=="turret") vertiRange = ship.getAttackRange();
        indexTile =0;
        indexPatrol=0;
        chaseDist = 12;
        if (ship.getName()=="slime") chaseDist = 12;
    }

    private void rangeReset(){
        vertiRange = ship.getAttackRange()+1;
        leftRange = rightRange= ship.getAttackRange();
    }

    public int getAction() {
        ++this.ticks;
        rangeReset();
        if (this.ship.getName()=="turret") {
            return FloorController.CONTROL_FIRE;
        }
        if (((long)this.ship.getId() + this.ticks) % 20L == 0L) {
            if (board.screenToBoardX(target.getX()-0.3f) == board.screenToBoardX(target.getX())-1){
                leftRange++;
            }
            else if (board.screenToBoardX(target.getX()+0.3f) == board.screenToBoardX(target.getX())+1){
                rightRange++;
            }
            this.changeStateIfApplicable();
            for (EnemyModel s: fleet){
                if (!s.isRemoved()) {
                    int sx = board.screenToBoardX(s.getX());
                    int sy = board.screenToBoardY(s.getY());
                    if (s != ship) board.setStanding(sx, sy);
                }
            }
            board.setStanding(board.screenToBoardX(target.getX()), board.screenToBoardY(target.getY()));
            this.markGoalTiles();
            board.resetStanding();
            this.move = this.getMoveAlongPathToGoalTile();
        }
        int action = this.move;
        if (this.state ==FSMState.ATTACK && this.canShootTarget()) {
            action = FloorController.CONTROL_FIRE;
        }
        leftRange = rightRange =ship.getAttackRange();
//        System.out.println(ship.getName()+"/state:"+state+"/action:"+action);
        return action;
    }

    private float getEqY(float x0, float y0, float x1, float y1, float x){
        return (y0-y1)/(x0-x1)*(x-x0)+y0;
    }

    public boolean canSeeJoe(){
        Vector2 shipPos = new Vector2(ship.getX(), ship.getY());
        Vector2 tarPos = new Vector2(target.getX(), target.getY());

        float lineLength = (float) Math.sqrt((tarPos.x-shipPos.x)*(tarPos.x-shipPos.x) + (tarPos.y-shipPos.y)*(tarPos.y-shipPos.y));
        float tilted = (tarPos.y-shipPos.y)/(tarPos.x-shipPos.x);
        float x = Math.min(shipPos.x, tarPos.x);
        float y = shipPos.x > tarPos.x ? tarPos.y : shipPos.y;

        for (int ii=0; ii <= (int) lineLength/TILE_SIZE; ii++){
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
            for (int i=Math.min(x, tx)+1; i<Math.max(x, tx); i++){
                if (board.isBlocked(i, y) || board.isStanding(i, y)) {
                    return false;
                }
            }
        }
        if (tx==x){
            for (int j=Math.min(y, ty)+1; j<Math.max(y, ty); j++){
                if (board.isBlocked(x, j) || board.isStanding(x, j)) {
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
        int sx = this.board.screenToBoardX(this.ship.getX());
        int sy = this.board.screenToBoardY(this.ship.getY());
        int tx;
        int ty;
        int dist;
        switch(state) {
            case SPAWN:
                if (canSeeJoe() && !target.isRemoved()) {
                    tx = this.board.screenToBoardX(this.target.getX());
                    ty = this.board.screenToBoardY(this.target.getY());
                    if (this.ship.canHitTargetFrom(sx,sy,tx,ty, vertiRange, leftRange, rightRange) && hasNoHazardBetw(sx, sy, tx, ty)) {
                        this.state = FSMState.ATTACK;
                    } else if (manhattan(sx, sy, tx, ty) <= chaseDist){
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
                    if (this.ship.canHitTargetFrom(sx,sy,tx,ty, vertiRange, leftRange, rightRange) && hasNoHazardBetw(sx, sy,tx,ty)) {
                        this.state =FSMState.ATTACK;
                        //this.wx = -1;
                    } else if (manhattan(sx, sy, tx, ty) <= chaseDist){
                        this.state = FSMState.CHASE;
                        //this.wx = -1;
                    }
                }
                /**
                if (this.wx == sx && this.wy == sy) {
                    this.wx = -1;
                    this.wy = -1;
                }**/
                break;
            case CHASE:
                tx = this.board.screenToBoardX(this.target.getX());
                ty = this.board.screenToBoardY(this.target.getY());
                if (!target.isRemoved()) {
                    if (this.ship.canHitTargetFrom(sx,sy,tx,ty, vertiRange, leftRange, rightRange) && hasNoHazardBetw(sx, sy, tx, ty)
                            && canSeeJoe()) {
                        this.state =FSMState.ATTACK;
                    }
                    else if (manhattan(sx, sy, tx, ty) > chaseDist) {
                        this.state=FSMState.WANDER;
                    }
                } else {
                    this.state = FSMState.WANDER;
                }
                break;
            case ATTACK:
                if (!target.isRemoved() && canSeeJoe()) {
                    tx = this.board.screenToBoardX(this.target.getX());
                    ty = this.board.screenToBoardY(this.target.getY());
                    if (!(this.ship.canHitTargetFrom(sx,sy,tx,ty, vertiRange, leftRange, rightRange) && hasNoHazardBetw(sx, sy, tx, ty))
                            && manhattan(sx, sy, tx, ty) <= chaseDist) {
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
        return this.ship.canHitTargetFrom(x,y,tx,ty, vertiRange, leftRange, rightRange);
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
        int sx = this.board.screenToBoardX(ship.getX());
        int sy = this.board.screenToBoardY(ship.getY());
        int tx ;
        int ty ;
        int dx;
        int dy;
        int manLeft;
        int manDown;
        int manUp;
        int manRight;
        //int leftRange = ship.getAttackRange();
        //int rightRange = ship.getAttackRange();
        boolean b;
        int fin =-1;
        int temp = Integer.MAX_VALUE;
        int attackRange = ship.getAttackRange();
        switch(state) {
            case SPAWN:
                setGoal = false;
                break;
            case WANDER:
                /**
                if (ship.getPatrol()==null) break;
                 Vector2 tile = ship.getPatrol().get(indexPatrol);
                 indexPatrol++;
                 if (indexPatrol==ship.getPatrol().size()) indexPatrol=0;
                 board.setGoal((int) tile.x, (int) tile.y);**/
                if (patrolPath == PatrolPath.HORIZONTAL){
                    if (this.patrolSeq<=2) {
                        b = board.isSuperSafeAt(sx-1, sy);
                        patrolSeq++;
                        if (b) {markGoalHelper(sx, sy, 0, 1); setGoal=true; break;}
                    }
                    else {
                        b = board.isSuperSafeAt(sx+1, sy);
                        if (patrolSeq==5) patrolSeq=0;
                        else patrolSeq++;
                        if (b) {markGoalHelper(sx, sy, 1, 1); setGoal=true; break;}
                    }
                }
                else if (patrolPath == PatrolPath.VERTICAL) {
                    if (this.patrolSeq<=2){
                        b = board.isSuperSafeAt(sx, sy+1);
                        patrolSeq++;
                        if (b) {markGoalHelper(sx, sy, 2, 1); setGoal=true; break;}
                    }
                    else {
                        b = board.isSuperSafeAt(sx, sy-1);
                        if (patrolSeq==5) patrolSeq=0;
                        else patrolSeq++;
                        if (b) {markGoalHelper(sx, sy, 3, 1); setGoal=true; break;}
                    }
                }
                else {
                    if (this.patrolSeq < 1 || (patrolSeq>=7)){
                        b = board.isSuperSafeAt(sx, sy+1);
                        if (patrolSeq==7) patrolSeq=0;
                        else patrolSeq++;
                        if (b) {markGoalHelper(sx, sy, 2, 1); setGoal=true; break;}
                    }
                    else if (patrolSeq <=2) {
                        b = board.isSuperSafeAt(sx-1, sy);
                        patrolSeq++;
                        if (b) {markGoalHelper(sx, sy, 0, 1); setGoal=true; break;}
                    }
                    else if (patrolSeq <=4){
                        b = board.isSuperSafeAt(sx, sy-1);
                        patrolSeq++;
                        if (b) {markGoalHelper(sx, sy, 3, 1); setGoal=true; break;}
                    }
                    else if (patrolSeq <=6) {
                        b = board.isSuperSafeAt(sx+1, sy);
                        patrolSeq++;
                        if (b) {markGoalHelper(sx, sy, 1, 1); setGoal=true; break;}
                    }
                }
                 setGoal=true;
                break;
            case CHASE:
                tx = this.board.screenToBoardX(this.target.getX());
                ty = this.board.screenToBoardY(this.target.getY());
                if (ship.canHitTargetFrom(sx, sy, tx, ty, vertiRange, leftRange, rightRange) && hasNoWallBetw(sx, sy, tx, ty) && hasNoHazardBetw(sx, sy, tx, ty)) {
                    state=FSMState.ATTACK;
                    break;
                }

                dx = Math.abs(sx-tx);
                dy = Math.abs(sy-ty);
                attackRange = dx >=1 && dx < attackRange && dy <= dx ? dx : (dy < attackRange && dy >= 1 && dx < dy ? dy : attackRange);

                manLeft = manhattan(sx, sy, tx-leftRange, ty);
                manRight = manhattan(sx, sy, tx+rightRange, ty);
                manUp =  manhattan(sx, sy, tx, ty+vertiRange);
                manDown =  manhattan(sx, sy, tx, ty-vertiRange);

                if (board.isSuperSafeAt(tx-leftRange, ty) && hasNoWallBetw(tx-leftRange, ty, tx, ty) &&
                        hasNoHazardBetw(sx, sy, tx-attackRange, ty)){
                    fin = 0; temp=manLeft;
                }
                if (board.isSuperSafeAt(tx+rightRange, ty) && manRight<temp && hasNoWallBetw(tx+rightRange, ty, tx, ty)
                        && hasNoHazardBetw(sx, sy,tx+attackRange, ty)) {
                    fin=1; temp=manRight;
                }
                if (board.isSuperSafeAt(tx , ty+vertiRange) && manUp<temp && hasNoWallBetw(tx, ty, tx, ty+vertiRange)
                        && hasNoHazardBetw(sx, sy, tx, ty+attackRange)) {
                    fin=2; temp=manUp;
                }
                if (board.isSuperSafeAt(tx , ty-vertiRange) && manDown<temp && hasNoWallBetw(tx, ty, tx, ty-vertiRange)
                        && hasNoHazardBetw(sx, sy, tx, ty-attackRange)) {
                    fin=3;
                }
                if (fin < 0) break;
                if (fin==0) markGoalHelper(tx, ty, fin, leftRange);
                else if (fin==1) markGoalHelper(tx, ty, fin, rightRange);
                else markGoalHelper(tx, ty, fin, vertiRange);
                setGoal=true;
                break;
            case ATTACK:
                tx = this.board.screenToBoardX(this.target.getX());
                ty = this.board.screenToBoardY(this.target.getY());
                if (ship.canHitTargetFrom(sx, sy, tx, ty, vertiRange, leftRange, rightRange) && hasNoWallBetw(sx, sy, tx, ty) && hasNoHazardBetw(sx, sy, tx, ty)) {
                    break;
                }

                dx = Math.abs(sx-tx);
                dy = Math.abs(sy-ty);
                attackRange = dx >=1 && dx < attackRange && dy <= dx ? dx : (dy < attackRange && dy >= 1 && dx < dy ? dy : attackRange);

                manLeft = manhattan(sx, sy, tx-leftRange, ty);
                manRight = manhattan(sx, sy, tx+rightRange, ty);
                manUp =  manhattan(sx, sy, tx, ty+vertiRange);
                manDown =  manhattan(sx, sy, tx, ty-vertiRange);

                if (board.isSuperSafeAt(tx-leftRange, ty) && hasNoWallBetw(tx-leftRange, ty, tx, ty) &&
                        hasNoHazardBetw(sx, sy, tx-attackRange, ty)){
                    fin = 0; temp=manLeft;
                }
                if (board.isSuperSafeAt(tx+rightRange, ty) && manRight<temp && hasNoWallBetw(tx+rightRange, ty, tx, ty)
                        && hasNoHazardBetw(sx, sy,tx+attackRange, ty)) {
                    fin=1; temp=manRight;
                }
                if (board.isSuperSafeAt(tx , ty+vertiRange) && manUp<temp && hasNoWallBetw(tx, ty, tx, ty+vertiRange)
                        && hasNoHazardBetw(sx, sy, tx, ty+attackRange)) {
                    fin=2; temp=manUp;
                }
                if (board.isSuperSafeAt(tx , ty-vertiRange) && manDown<temp && hasNoWallBetw(tx, ty, tx, ty-vertiRange)
                        && hasNoHazardBetw(sx, sy, tx, ty-attackRange)) {
                    fin=3;
                }
                if (fin < 0) break;
                if (fin==0) markGoalHelper(tx, ty, fin, leftRange);
                else if (fin==1) markGoalHelper(tx, ty, fin, rightRange);
                else markGoalHelper(tx, ty, fin, vertiRange);
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

    public FSMState getState() {
        return state;
    }

    public int getVertiRange(){return vertiRange;}
    public int getLeftRange() {return leftRange;}
    public int getRightRange() {return rightRange;}

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

    public static enum FSMState {
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