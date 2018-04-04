package edu.cornell.gdiac.util;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

import java.util.ArrayList;


public class LevelEditorParser {

    private static final int TILE_SCALE = 2;

    private int [][] tiles;
    private ArrayList<Vector2> wallMidPos = new ArrayList<Vector2>();
    private ArrayList<Vector2> wallRightPos = new ArrayList<Vector2>();
    private ArrayList<Vector2> wallLeftPos = new ArrayList<Vector2>();
    private ArrayList<Vector2> scientistPos = new ArrayList<Vector2>();
    private ArrayList<Vector2> robotPos = new ArrayList<Vector2>();
    private ArrayList<Vector2> slimePos = new ArrayList<Vector2>();
    private Vector2 joePos;
    private Vector2 goalDoorPos;
    private Vector2 mopCartPos;

    public LevelEditorParser(String levelPath) {
        Element level = new XmlReader().parse(Gdx.files.internal(levelPath));
        Array<Element> layers = level.getChildrenByName("layer");


        tiles = layerToList(layers.get(0));
        mopCartPos = layerToPos(layers.get(1));
        goalDoorPos = layerToPos(layers.get(2));
        int [][] characters = layerToList(layers.get(3));
        for (int i = 0; i < characters.length; i++) {
            for (int j = 0; j < characters[0].length; j++) {
                if (characters[i][j] == 5) {
                    scientistPos.add(new Vector2(j * TILE_SCALE, i * TILE_SCALE));
                } else if (characters[i][j] == 6) {
                    robotPos.add(new Vector2(j * TILE_SCALE, i * TILE_SCALE));
                } else if (characters[i][j] == 7) {
                    joePos = new Vector2(j * TILE_SCALE, i * TILE_SCALE);
                } else if (characters[i][j] == 8) {
                    slimePos.add(new Vector2(j * TILE_SCALE, i * TILE_SCALE));
                }
            }
        }

        int[][] horiWalls = layerToList(layers.get(4));
        int[][] vertiWalls = layerToList(layers.get(5));

        for (int i = 0; i < vertiWalls.length; i++) {
            for (int j = 0; j < vertiWalls[0].length; j++) {
                if (vertiWalls[i][j] == 2) {
                    wallRightPos.add(new Vector2(j * TILE_SCALE, i * TILE_SCALE));
                } else if (vertiWalls[i][j] == 4) {
                    wallLeftPos.add(new Vector2(j * TILE_SCALE, i * TILE_SCALE));
                }
            }
        }

        for (int i = 0; i < horiWalls.length; i++) {
            for (int j = 0; j < horiWalls[0].length; j++) {
                if (horiWalls[i][j] == 3) {
                    wallMidPos.add(new Vector2(j * TILE_SCALE, i * TILE_SCALE));
                }
            }
        }
    }

    public int[][] layerToList(Element layer) {
        String csv = layer.get("data");
        int w = layer.getIntAttribute("width");
        int h = layer.getIntAttribute("height");
        String[] rows = csv.split("\\r?\\n");
        String[][] grid = new String[h][w];
        int[][] grid2 = new int[h][w];
        for (int i = 0; i < h; i++) {
            grid[h - i - 1] = rows[i].split(",");
        }

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                grid2[i][j] = Integer.parseInt(grid[i][j]);
            }
        }
        return grid2;
    }

    public Vector2 layerToPos(Element layer) {
        int[][] grid = layerToList(layer);
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j] != 0) {
                    return new Vector2(j * TILE_SCALE, i * TILE_SCALE);
                }
            }
        }
        return new Vector2();
    }

    public int[][] getTiles() {
        return tiles;
    }

    public ArrayList<Vector2> getScientistPos() {
        return scientistPos;
    }

    public ArrayList<Vector2> getRobotPos() {
        return robotPos;
    }

    public ArrayList<Vector2> getSlimePos() {
        return slimePos;
    }

    public ArrayList<Vector2> getWallMidPos() {
        return wallMidPos;
    }

    public ArrayList<Vector2> getWallRightPos() {
        return wallRightPos;
    }

    public ArrayList<Vector2> getWallLeftPos() {
        return wallLeftPos;
    }

    public int getGoalDoorX() {
        return (int) goalDoorPos.x;
    }

    public int getGoalDoorY() {
        return (int) goalDoorPos.y;
    }

    public int getMopCartX() {
        return (int) mopCartPos.x;
    }

    public int getMopCartY() {
        return (int) mopCartPos.y;
    }

    public int getJoePosX() {
        return (int) joePos.x;
    }

    public int getJoePosY() {
        return (int) joePos.y;
    }
}
