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
    private static final String MODEL_FILE = "models/Tile.obj";
    private static final String TEXTURE_FILE = "models/Tile.png";
    private static Mesh tileMesh;
    private static Texture tileTexture;
    private static final float FALL_RATE = 0.5F;
    private static final float MIN_FALL_AMOUNT = 1.0F;
    private static final float MAX_FALL_AMOUNT = 200.0F;
    private static final float TILE_SPACE = 12.0F;
    private static final int TILE_WIDTH = 64;
    private static final int POWER_SPACE = 4;
    private static final Color BASIC_COLOR = new Color(0.25F, 0.25F, 0.25F, 0.5F);
    private static final Color POWER_COLOR = new Color(0.0F, 1.0F, 1.0F, 0.5F);
    private int width;
    private int height;
    private Board.TileState[] tiles;
    private TexturedMesh tileModel;

    public static void PreLoadContent(AssetManager manager) {
        MeshLoader.MeshParameter parameter = new MeshLoader.MeshParameter();
        parameter.attribs = new VertexAttribute[2];
        parameter.attribs[0] = new VertexAttribute(1, 3, "vPosition");
        parameter.attribs[1] = new VertexAttribute(16, 2, "vUV");
        manager.load("models/Tile.obj", Mesh.class, parameter);
        manager.load("models/Tile.png", Texture.class);
    }

    public static void LoadContent(AssetManager manager) {
        if (manager.isLoaded("models/Tile.obj")) {
            tileMesh = (Mesh)manager.get("models/Tile.obj", Mesh.class);
        } else {
            tileMesh = null;
        }

        if (manager.isLoaded("models/Tile.png")) {
            tileTexture = (Texture)manager.get("models/Tile.png", Texture.class);
            tileTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        } else {
            tileTexture = null;
        }

    }

    public static void UnloadContent(AssetManager manager) {
        if (tileMesh != null) {
            tileMesh = null;
            manager.unload("models/Tile.obj");
        }

        if (tileTexture != null) {
            tileTexture = null;
            manager.unload("models/Tile.png");
        }

    }

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new Board.TileState[width * height];

        for(int ii = 0; ii < this.tiles.length; ++ii) {
            this.tiles[ii] = new Board.TileState();
        }

        this.resetTiles();
        this.tileModel = new TexturedMesh(tileMesh, tileTexture);
    }

    public void resetTiles() {
        for(int x = 0; x < this.width; ++x) {
            for(int y = 0; y < this.height; ++y) {
                Board.TileState tile = this.getTileState(x, y);
                tile.power = x % 4 == 0 || y % 4 == 0;
                tile.goal = false;
                tile.visited = false;
                tile.fallAmount = 0.0F;
                tile.falling = false;
            }
        }

    }

    private Board.TileState getTileState(int x, int y) {
        return !this.inBounds(x, y) ? null : this.tiles[x * this.height + y];
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getTileSize() {
        return 64;
    }

    public float getTileSpacing() {
        return 12.0F;
    }

    public boolean isSafeAtScreen(float x, float y) {
        int bx = this.screenToBoard(x);
        int by = this.screenToBoard(y);
        return x >= 0.0F && y >= 0.0F && x < (float)this.width * ((float)this.getTileSize() ) && y < (float)this.height * ((float)this.getTileSize() ) && !this.getTileState(bx, by).falling;
    }

    public boolean isSafeAt(int x, int y) {
        return x >= 0 && y >= 0 && x < this.width && y < this.height && !this.getTileState(x, y).falling;
    }

    public void destroyTileAt(int x, int y) {
        if (this.inBounds(x, y)) {
            this.getTileState(x, y).falling = false;
        }
    }

    public boolean isDestroyedAt(int x, int y) {
        if (!this.inBounds(x, y)) {
            return true;
        } else {
            return this.getTileState(x, y).fallAmount >= 1.0F;
        }
    }

    public boolean isPowerTileAtScreen(float x, float y) {
        int tx = this.screenToBoard(x);
        int ty = this.screenToBoard(y);
        return !this.inBounds(tx, ty) ? false : this.getTileState(tx, ty).power;
    }

    public boolean isPowerTileAt(int x, int y) {
        return !this.inBounds(x, y) ? false : this.getTileState(x, y).power;
    }

    public void update() {
        for(int x = 0; x < this.width; ++x) {
            for(int y = 0; y < this.height; ++y) {
                Board.TileState tile = this.getTileState(x, y);
                if (tile.falling && tile.fallAmount <= 200.0F) {
                    tile.fallAmount += 0.5F;
                }
            }
        }

    }

    /**
    public void draw(GameCanvas canvas) {
        for(int x = 0; x < this.width; ++x) {
            for(int y = 0; y < this.height; ++y) {
                this.drawTile(x, y, canvas);
            }
        }

    }

    private void drawTile(int x, int y, GameCanvas canvas) {
        Board.TileState tile = this.getTileState(x, y);
        if (tile.fallAmount < 190.0F) {
            float sx = this.boardToScreen(x);
            float sy = this.boardToScreen(y);
            float sz = tile.fallAmount;
            float a = 0.1F * tile.fallAmount;
            this.tileModel.setColor(BASIC_COLOR);
            if (tile.power) {
                this.tileModel.setColor(POWER_COLOR);
            }

            canvas.drawTile(this.tileModel, sx, sy, sz, a);
        }
    }
    **/

    public int screenToBoard(float f) {
        return (int)(f / ((float)this.getTileSize() ));
    }

    public float boardToScreen(int n) {
        return ((float)n + 0.5F) * ((float)this.getTileSize() );
    }

    public float centerOffset(float f) {
        float paddedTileSize = (float)this.getTileSize() ;
        int cell = this.screenToBoard(f);
        float nearestCenter = ((float)cell + 0.5F) * paddedTileSize;
        return f - nearestCenter;
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < this.width && y < this.height;
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

    public void setGoal(int x, int y) {
        if (!this.inBounds(x, y)) {
            Gdx.app.error("Board", "Illegal tile " + x + "," + y, new IndexOutOfBoundsException());
        } else {
            this.getTileState(x, y).goal = true;
        }
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

    private static class TileState {
        public boolean power;
        public boolean goal;
        public boolean visited;
        public boolean falling;
        public float fallAmount;

        private TileState() {
            this.power = false;
            this.goal = false;
            this.visited = false;
            this.falling = false;
            this.fallAmount = 0.0F;
        }
    }
}
