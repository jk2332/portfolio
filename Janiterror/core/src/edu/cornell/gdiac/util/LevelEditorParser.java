package edu.cornell.gdiac.util;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

import java.util.ArrayList;


public class LevelEditorParser {

    private int [][] tiles;
    private ArrayList<Vector2> wallMidPos = new ArrayList<Vector2>();
    private ArrayList<Vector2> wallRightPos = new ArrayList<Vector2>();
    private ArrayList<Vector2> wallLeftPos = new ArrayList<Vector2>();
    private ArrayList<Vector2> wallTRPos = new ArrayList<Vector2>();
    private ArrayList<Vector2> wallTLPos = new ArrayList<Vector2>();
    private ArrayList<Vector2> wallBRPos = new ArrayList<Vector2>();
    private ArrayList<Vector2> wallBLPos = new ArrayList<Vector2>();
    private ArrayList<Vector2> scientistPos = new ArrayList<Vector2>();
    private ArrayList<Vector2> robotPos = new ArrayList<Vector2>();
    private ArrayList<Vector2> slimePos = new ArrayList<Vector2>();
    private ArrayList<Vector2> lizardPos = new ArrayList<Vector2>();
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
                if (characters[i][j] == 21) {
                    scientistPos.add(new Vector2(j, i));
                } else if (characters[i][j] == 19) {
                    robotPos.add(new Vector2(j, i));
                } else if (characters[i][j] == 11) {
                    joePos = new Vector2(j, i);
                } else if (characters[i][j] == 20) {
                    slimePos.add(new Vector2(j, i));
                } else if (characters[i][j] == 22) {
                    lizardPos.add(new Vector2(j, i));
                }
            }
        }

        int[][] horiWalls = layerToList(layers.get(5));
        int[][] vertiWalls = layerToList(layers.get(4));

        for (int i = 0; i < vertiWalls.length; i++) {
            for (int j = 0; j < vertiWalls[0].length; j++) {
                if (vertiWalls[i][j] == 18) {
                    wallRightPos.add(new Vector2(j, i));
                } else if (vertiWalls[i][j] == 10000) { //TODO change later
                    wallLeftPos.add(new Vector2(j, i));
                }
            }
        }

        for (int i = 0; i < horiWalls.length; i++) {
            for (int j = 0; j < horiWalls[0].length; j++) {
                if (horiWalls[i][j] == 13) {
                    wallMidPos.add(new Vector2(j, i ));
                } else if (horiWalls[i][j] == 12) {
                    wallTLPos.add(new Vector2(j, i ));
                } else if (horiWalls[i][j] == 14) {
                    wallTRPos.add(new Vector2(j, i ));
                } else if (horiWalls[i][j] == 15) {
                    wallBLPos.add(new Vector2(j, i ));
                } else if (horiWalls[i][j] == 17) {
                    wallBRPos.add(new Vector2(j, i ));
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
                    return new Vector2(j, i);
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

    public ArrayList<Vector2> getWallBLPos() {
        return wallBLPos;
    }

    public ArrayList<Vector2> getWallTRPos() {
        return wallTRPos;
    }

    public ArrayList<Vector2> getWallBRPos() {
        return wallBRPos;
    }

    public ArrayList<Vector2> getWallTLPos() {
        return wallTLPos;
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
