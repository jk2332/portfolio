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

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new Board.TileState[width * height];

        for (int ii=0; ii<width; ii++){
            for (int jj=0; jj<height; jj++){
                tiles[jj*width+ii]=new TileState(ii, jj, width, height);
            }
        }
        this.resetTiles();
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

    public boolean isSafeAt(int x, int y) {
        return inBounds(x, y) && !this.getTileState(x, y).falling;
    }

    public void clearMarks() {
        for(int x = 0; x < this.width; ++x) {
            for(int y = 0; y < this.height; ++y) {
                Board.TileState state = this.getTileState(x, y);
                state.visited = false;
                state.goal = false;
            }
        }
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < this.width && y < this.height;
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


}
