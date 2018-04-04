//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package edu.cornell.gdiac.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import edu.cornell.gdiac.mesh.TexturedMesh;
import edu.cornell.gdiac.mesh.MeshLoader.MeshParameter;
import edu.cornell.gdiac.mesh.MeshLoader;
import edu.cornell.gdiac.mesh.TexturedMesh;

public class Board {
    private int width;
    private int height;
    private TileState[] tiles;
    private float BOARD_WIDTH=32;
    private float BOARD_HEIGHT=18;
    private int TILE_SCALE = 2;

    /** Texture+Mesh for tile. Only need one, since all have same geometry */
    private Texture tileTexture;

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new Board.TileState[width * height];

        for (int ii=1; ii<width-1; ii++){
            for (int jj=1; jj<height-1; jj++){
                tiles[jj*width+ii]=new TileState(ii, jj, width, height);
            }
        }
        this.resetTiles();
    }
    public void resetTiles() {
        for(int x = 1; x < this.width-1; ++x) {
            for(int y = 1; y < this.height-1; ++y) {
                Board.TileState tile = this.getTileState(x, y);
                tile.goal = false;
                tile.visited = false;
            }
        }
    }

    public void setGoal(int x, int y) {
        if (!this.inBounds(x, y)) {
            Gdx.app.error("Board", "Illegal tile " + x + "," + y, new IndexOutOfBoundsException());
        } else {
            this.getTileState(x, y).goal = true;
        }
    }

    public boolean isVisited(int x, int y) {
        return !this.inBounds(x, y) ? false : this.getTileState(x, y).visited;
    }

    public void setVisited(int x, int y) {
        if (!this.inBounds(x, y)) {
            Gdx.app.error("Board", "Illegal tile " + x + "," + y, new IndexOutOfBoundsException());
        } else {
            this.getTileState(x, y).visited = true;
        }
    }

    public boolean isGoal(int x, int y) {
        return !this.inBounds(x, y) ? false : this.getTileState(x, y).goal;
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
        return (float) (n + 0.5f * TILE_SCALE) * (BOARD_WIDTH/width);
    }

    public float boardToScreenY(int n) {
        return (float) (n + 0.5f * TILE_SCALE) * (BOARD_HEIGHT/height);
    }

    public boolean isSafeAt(int x, int y) {
        return inBounds(x, y) && !this.getTileState(x, y).falling;
    }

    public void clearMarks() {
        for(int x = 1; x < this.width-1; ++x) {
            for(int y = 1; y < this.height-1; ++y) {
                Board.TileState state = this.getTileState(x, y);
                state.visited = false;
                state.goal = false;
            }
        }
    }

    public boolean inBounds(int x, int y) {
        return x >= 1 && y >= 1 && x < this.width-1 && y < this.height-1;
    }

    private TileState getTileState(int x, int y) {
        return !this.inBounds(x, y) ? null : this.tiles[width*y+x];
    }

    private static class TileState {
        public boolean goal;
        public boolean visited;
        public boolean falling;
        public int tilex;
        public int tiley;

        private TileState(int x, int y, int width, int height) {
            tilex=x;
            tiley=y;
            this.goal = false;
            //this.visited = false;
            //if (x==0 || y==0 || x==width-1 || y==height-1) {falling=true; visited=true;} else {falling=false; visited=false;}
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
    public void draw(GameCanvas canvas) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                drawTile(x, y, canvas);
            }
        }
    }

    // Drawing information
    /**
     * Returns the textured mesh for each tile.
     *
     * We only need one mesh, as all tiles look (mostly) the same.
     *
     * @return the textured mesh for each tile.
     */
    public Texture getTileTexture() {
        return tileTexture;
    }

    /**
     * Sets the textured mesh for each tile.
     *
     * We only need one mesh, as all tiles look (mostly) the same.
     *
     * @param mesh the textured mesh for each tile.
     */
    public void setTileTexture(Texture t) {
        tileTexture = t;
    }

    /**
     * Draws the individual tile at position (x,y).
     *
     * Fallen tiles are not drawn.
     *
     * @param x The x index for the Tile cell
     * @param y The y index for the Tile cell
     */
    private void drawTile(int x, int y, GameCanvas canvas) {
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
        canvas.draw(tileTexture, Color.WHITE, tileTexture.getWidth()/2, tileTexture.getHeight()/2,
                1024/width * x, 576/height * y, 0, 1.0f, 1.0f);
    }


}
