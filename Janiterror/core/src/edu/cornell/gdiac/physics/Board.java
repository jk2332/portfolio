//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package edu.cornell.gdiac.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class Board {
    private int width;
    private int height;
    private TileState[] tiles;
    private float BOARD_WIDTH=32;
    private float BOARD_HEIGHT=18;
    private int goalX;
    private int goalY;

    /** Texture+Mesh for tile. Only need one, since all have same geometry */
    private Texture[] tileTextures;

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new Board.TileState[width * height];

        for (int ii=0; ii<width; ii++){
            for (int jj=0; jj<height; jj++){
                tiles[jj*width+ii]=new TileState(ii, jj);
            }
        }
        this.resetTiles();
    }

    public TileState getGoal(){
        return new TileState(goalX, goalY);
    }

    public void resetTiles() {
        for(int x = 0; x < this.width; ++x) {
            for(int y = 0; y < this.height; ++y) {
                Board.TileState tile = this.getTileState(x, y);
                tile.goal = false;
                tile.visited = false;
            }
        }
    }

    public void setGoal(int x, int y) {
        if (!this.isSafeAt(x, y)) {
            //Gdx.app.error("Board", "Illegal tile " + x + "," + y, new IndexOutOfBoundsException());
        } else {
            goalX=x; goalY=y;
        }
    }

    public boolean isVisited(int x, int y) {
        return !this.isSafeAt(x, y) ? false : this.getTileState(x, y).visited;
    }

    public void setVisited(int x, int y) {
        if (!this.isSafeAt(x, y)) {
            //Gdx.app.error("Board", "Illegal tile " + x + "," + y, new IndexOutOfBoundsException());
        } else {
            this.getTileState(x, y).visited = true;
        }
    }

    public boolean isGoal(int x, int y) {
        return !this.isSafeAt(x, y) ? false : (goalX==x && goalY==y);
    }

    public int getWidth() {return width;}

    public int getHeight() {return height;}

    public int screenToBoardX(float f){
        return (int) (f/(BOARD_WIDTH/width));
    }

    public int screenToBoardY(float f){
        return (int) (f/(BOARD_HEIGHT/height));
    }

    /**
     * Returns the screen position coordinate for a board cell index.
     *
     * While all positions are 2-dimensional, the dimensions to
     * the board are symmetric. This allows us to use the same
     * method to convert an x coordinate or a y coordinate to
     * a cell index.
     *
     * @param n Tile cell index
     *
     * @return the screen position coordinate for a board cell index.
     */
    public float boardToScreenX(int n) {
        return (float) (n + 0.5f) * (BOARD_WIDTH/width);
    }

    public float boardToScreenY(int n) {
        return (float) (n + 0.5f) * (BOARD_HEIGHT/height);
    }

    public boolean isSafeAt(int x, int y) {
        return inBounds(x, y) && !this.getTileState(x, y).blocked;
    }

    public void setBlocked(int x, int y){
        if (inBounds(x, y)) getTileState(x,y).blocked=true;
    }

    public void clearMarks() {
        goalX=-1; goalY=-1;
        for(int x = 0; x < this.width; ++x) {
            for(int y = 0; y < this.height; ++y) {
                Board.TileState state = this.getTileState(x, y);
                state.visited = false;
            }
        }
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < this.width && y < this.height;
    }

    private TileState getTileState(int x, int y) {
        return !this.inBounds(x, y) ? null : this.tiles[width*y+x];
    }

    public void setHazard (int x, int y) {
        if (this.isSafeAt(x, y)) getTileState(x, y).isHazard=true;
    }

    public boolean isHazard(int x, int y){
        if (!isSafeAt(x, y)) return false;
        return getTileState(x, y).isHazard;
    }

    public class TileState {
        public boolean goal;
        public boolean visited;
        public boolean blocked;
        public boolean isHazard;
        public int x;
        public int y;

        private TileState(int x, int y) {
            this.x=x;
            this.y=y;
            this.goal = false;
            this.visited = false;
            this.blocked=false;
            this.isHazard=false;
        }
    }

    /**
     * Draws the board to the given canvas.
     *
     * This method draws all of the tiles in this board. It should be the first drawing
     * pass in the GameEngine.
     *
     * @param canvas the drawing context
     */
    public void draw(GameCanvas canvas, int[][] tiles) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                drawTile(x, y, canvas, tileTextures[tiles[y][x]]);
            }
        }
    }

    /**
     * Sets the textured mesh for each tile.
     *
     * We only need one mesh, as all tiles look (mostly) the same.
     *
     * @param mesh the textured mesh for each tile.
     */
    public void setTileTextures(Texture[] t) {
        tileTextures = t;
    }

    public boolean isBlocked(int x, int y){
        if (!inBounds(x, y)) return false;
        return getTileState(x, y).blocked;
    }

    /**
     * Draws the individual tile at position (x,y).
     *
     * Fallen tiles are not drawn.
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     */
    private void drawTile(int x, int y, GameCanvas canvas, Texture tileTexture) {
//        System.out.println(tileTexture);
        TileState tile = getTileState(x, y);

        // Compute drawing coordinates
        float sx = boardToScreenX(x);
        float sy = boardToScreenY(y);

        /*tileMesh.setColor(BASIC_COLOR);
        if (tile.power) {
            tileMesh.setColor(POWER_COLOR);
        }*/


        // Draw
        //canvas.drawTile(tileMesh, sx, sy, 0, 0);
        if (x==goalX && y==goalY){
            canvas.draw(tileTexture, Color.RED, tileTexture.getWidth()/2, tileTexture.getHeight()/2,
                    1024/width * (x + 0.5f), 576/height * (y + 0.5f), 0, 1.0f, 1.0f);
        }
        //else if (getTileState(x,y).blocked){
        //    canvas.draw(tileTexture, Color.BLUE, tileTexture.getWidth()/2, tileTexture.getHeight()/2,
        //            1024/width * (x + 0.5f), 576/height * (y + 0.5f), 0, 1.0f, 1.0f);
        //}
        else if (getTileState(x,y).isHazard){
            canvas.draw(tileTexture, Color.BLUE, tileTexture.getWidth()/2, tileTexture.getHeight()/2,
                    1024/width * (x + 0.5f), 576/height * (y + 0.5f), 0, 1.0f, 1.0f);
        }
        else {
            canvas.draw(tileTexture, Color.WHITE, tileTexture.getWidth()/2, tileTexture.getHeight()/2,
                    1024/width * (x + 0.5f), 576/height * (y + 0.5f), 0, 1.0f, 1.0f);
        }
    }

}