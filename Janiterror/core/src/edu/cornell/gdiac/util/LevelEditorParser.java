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

    private int robotAttackRange;
    private float robotDensity;
    private int robotHP;
    private float robotVel;

    private int slimeAttackRange;
    private float slimeDensity;
    private int slimeHP;
    private float slimeVel;
    private float slimeballSpeed;

    private int scientistAttackRange;
    private float scientistDensity;
    private int scientistHP;
    private float scientistVel;

    private int lizardAttackRange;
    private float lizardDensity;
    private int lizardHP;
    private float lizardVel;

    private float joeDensity;
    private int joeHP;
    private float joeVel;

    private int lidAttackRange;
    private int lidDurability;

    private int mopAttackRange;
    private int mopDurability;

    private int sprayAttackRange;
    private int sprayDurability;

    private int vacuumAttackRange;
    private int vacuumDurability;

    private int mopKnockbackTimer;
    private int sprayStunTimer;

    private int wallvgid;
    private int wallhgid;
    private int tilegid;


    public LevelEditorParser(String levelPath) {
        Element level = new XmlReader().parse(Gdx.files.internal(levelPath));
        Array<Element> layers = level.getChildrenByName("layer");
        Array<Element> objects = level.getChildrenByName("objectgroup");

        int boardWidth = level.getIntAttribute("width") * level.getIntAttribute("tilewidth");
        int boardHeight = level.getIntAttribute("height") * level.getIntAttribute("tileheight");

        Array<Element> tilesets = level.getChildrenByName("tileset");
        for (Element ts : tilesets) {
            if (ts.get("source").equals("wallsh.tsx")) {
                wallhgid = ts.getIntAttribute("firstgid");
            } else if (ts.get("source").equals("wallv.tsx")) {
                wallvgid = ts.getIntAttribute("firstgid");
            } else if (ts.get("source").equals("tiles.tsx")) {
                tilegid = ts.getIntAttribute("firstgid");
            }
        }
        tiles = layerToList(layers.get(0));

        Element goalDoorElement = objects.get(1).getChild(0);
        goalDoorPos = new Vector2(goalDoorElement.getFloatAttribute("x"),boardHeight - goalDoorElement.getFloatAttribute("y"));
        Element mopCartElement = objects.get(0).getChild(0);
        mopCartPos = new Vector2(mopCartElement.getFloatAttribute("x"),boardHeight - mopCartElement.getFloatAttribute("y"));
        Array<Element> charactersElement = objects.get(2).getChildrenByName("object");
        for (int i = 0; i < charactersElement.size; i++) {
            Element character = charactersElement.get(i);
            String type = character.get("type");
            float x = character.getFloatAttribute("x");
            float y = boardHeight - character.getFloatAttribute("y");
            if (type.equals("scientist")) {
                if (scientistPos.size() == 0) {
                    Array<Element> ps = character.getChild(0).getChildrenByName("property");
                    for (int j = 0; j < ps.size; j++) {
                        Element p = ps.get(j);
                        String name = p.get("name");
                        if (name.equals("Attack Range")) {
                            scientistAttackRange = p.getIntAttribute("value");
                        } else if (name.equals("Density")) {
                            scientistDensity = p.getFloatAttribute("value");
                        } else if (name.equals("HP")) {
                            scientistHP = p.getIntAttribute("value");
                        } else if (name.equals("Velocity")) {
                            scientistVel = p.getFloatAttribute("value");
                        }
                    }
                }
                scientistPos.add(new Vector2(x, y));
            } else if (type.equals("robot")) {
                if (robotPos.size() == 0) {
                    Array<Element> ps = character.getChild(0).getChildrenByName("property");
                    for (int j = 0; j < ps.size; j++) {
                        Element p = ps.get(j);
                        String name = p.get("name");
                        if (name.equals("Attack Range")) {
                            robotAttackRange = p.getIntAttribute("value");
                        } else if (name.equals("Density")) {
                            robotDensity = p.getFloatAttribute("value");
                        } else if (name.equals("HP")) {
                            robotHP = p.getIntAttribute("value");
                        } else if (name.equals("Velocity")) {
                            robotVel = p.getFloatAttribute("value");
                        }
                    }
                }
                robotPos.add(new Vector2(x, y));
            } else if (type.equals("joe")) {
                if (joePos == null) {
                    Array<Element> ps = character.getChild(0).getChildrenByName("property");
                    for (int j = 0; j < ps.size; j++) {
                        Element p = ps.get(j);
                        String name = p.get("name");
                        if (name.equals("Density")) {
                            joeDensity = p.getFloatAttribute("value");
                        } else if (name.equals("HP")) {
                            joeHP = p.getIntAttribute("value");
                        } else if (name.equals("Velocity")) {
                            joeVel = p.getFloatAttribute("value");
                        } else if (name.equals("Lid Attack Range")) {
                            lidAttackRange = p.getIntAttribute("value");
                        } else if (name.equals("Lid Durability")) {
                            lidDurability = p.getIntAttribute("value");
                        } else if (name.equals("Mop Attack Range")) {
                            mopAttackRange = p.getIntAttribute("value");
                        } else if (name.equals("Mop Durability")) {
                            mopDurability = p.getIntAttribute("value");
                        } else if (name.equals("Spray Attack Range")) {
                            sprayAttackRange = p.getIntAttribute("value");
                        } else if (name.equals("Spray Durability")) {
                            sprayDurability = p.getIntAttribute("value");
                        } else if (name.equals("Vacuum Attack Range")) {
                            vacuumAttackRange = p.getIntAttribute("value");
                        } else if (name.equals("Vacuum Durability")) {
                            vacuumDurability = p.getIntAttribute("value");
                        } else if (name.equals("Mop Knockback Timer")) {
                            mopKnockbackTimer = p.getIntAttribute("value");
                        } else if (name.equals("Spray Stun Timer")) {
                            sprayStunTimer = p.getIntAttribute("value");
                        }
                    }
                }
                joePos = new Vector2(x, y);
            } else if (type.equals("slime")) {
                if (slimePos.size() == 0) {
                    Array<Element> ps = character.getChild(0).getChildrenByName("property");
                    for (int j = 0; j < ps.size; j++) {
                        Element p = ps.get(j);
                        String name = p.get("name");
                        if (name.equals("Attack Range")) {
                            slimeAttackRange = p.getIntAttribute("value");
                        } else if (name.equals("Density")) {
                            slimeDensity = p.getFloatAttribute("value");
                        } else if (name.equals("HP")) {
                            slimeHP = p.getIntAttribute("value");
                        } else if (name.equals("Velocity")) {
                            slimeVel = p.getFloatAttribute("value");
                        } else if (name.equals("Slimeball Speed")) {
                            slimeballSpeed = p.getFloatAttribute("value");
                        }
                    }
                }
                slimePos.add(new Vector2(x, y));
            } else if (type.equals("lizard")) {
                if (lizardPos.size() == 0) {
                    Array<Element> ps = character.getChild(0).getChildrenByName("property");
                    for (int j = 0; j < ps.size; j++) {
                        Element p = ps.get(j);
                        String name = p.get("name");
                        if (name.equals("Attack Range")) {
                            lizardAttackRange = p.getIntAttribute("value");
                        } else if (name.equals("Density")) {
                            lizardDensity = p.getFloatAttribute("value");
                        } else if (name.equals("HP")) {
                            lizardHP = p.getIntAttribute("value");
                        } else if (name.equals("Velocity")) {
                            lizardVel = p.getFloatAttribute("value");
                        }
                    }
                }
                lizardPos.add(new Vector2(x, y));
            }
        }

        int[][] horiWalls = layerToList(layers.get(2));
        int[][] vertiWalls = layerToList(layers.get(1));

        for (int i = 0; i < vertiWalls.length; i++) {
            for (int j = 0; j < vertiWalls[0].length; j++) {
                if (vertiWalls[i][j] == wallvgid) {
                    wallRightPos.add(new Vector2(j, i));
                } else if (vertiWalls[i][j] == 10000) { //TODO change later
                    wallLeftPos.add(new Vector2(j, i));
                }
            }
        }

        for (int i = 0; i < horiWalls.length; i++) {
            for (int j = 0; j < horiWalls[0].length; j++) {
                if (horiWalls[i][j] == wallhgid + 1) {
                    wallMidPos.add(new Vector2(j, i ));
                } else if (horiWalls[i][j] == wallhgid) {
                    wallTLPos.add(new Vector2(j, i ));
                } else if (horiWalls[i][j] == wallhgid + 2) {
                    wallTRPos.add(new Vector2(j, i ));
                } else if (horiWalls[i][j] == wallhgid + 3) {
                    wallBLPos.add(new Vector2(j, i ));
                } else if (horiWalls[i][j] == wallhgid + 5) {
                    wallBRPos.add(new Vector2(j, i ));
                }
            }
        }
    }

    private int[][] layerToList(Element layer) {
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

    public ArrayList<Vector2> getLizardPos() {
        return lizardPos;
    }

    public float getGoalDoorX() {
        return goalDoorPos.x;
    }

    public float getGoalDoorY() {
        return goalDoorPos.y;
    }

    public float getMopCartX() {
        return mopCartPos.x;
    }

    public float getMopCartY() {
        return mopCartPos.y;
    }

    public float getJoePosX() {
        return joePos.x;
    }

    public float getJoePosY() {
        return joePos.y;
    }

    public int getJoeHP() {
        return joeHP;
    }

    public float getJoeVel() {
        return joeVel;
    }

    public float getJoeDensity() {
        return joeDensity;
    }

    public float getLizardDensity() {
        return lizardDensity;
    }

    public int getLizardAttackRange() {
        return lizardAttackRange;
    }

    public float getLizardVel() {
        return lizardVel;
    }

    public int getLizardHP() {
        return lizardHP;
    }

    public float getRobotDensity() {
        return robotDensity;
    }

    public float getRobotVel() {
        return robotVel;
    }

    public int getRobotAttackRange() {
        return robotAttackRange;
    }

    public int getRobotHP() {
        return robotHP;
    }

    public float getScientistDensity() {
        return scientistDensity;
    }

    public float getScientistVel() {
        return scientistVel;
    }

    public int getScientistAttackRange() {
        return scientistAttackRange;
    }

    public int getScientistHP() {
        return scientistHP;
    }

    public float getSlimeballSpeed() {
        return slimeballSpeed;
    }

    public int getSlimeAttackRange() {
        return slimeAttackRange;
    }

    public float getSlimeDensity() {
        return slimeDensity;
    }

    public float getSlimeVel() {
        return slimeVel;
    }

    public int getSlimeHP() {
        return slimeHP;
    }

    public int getSprayAttackRange() {
        return sprayAttackRange;
    }

    public int getSprayDurability() {
        return sprayDurability;
    }

    public int getLidAttackRange() {
        return lidAttackRange;
    }

    public int getLidDurability() {
        return lidDurability;
    }

    public int getMopAttackRange() {
        return mopAttackRange;
    }

    public int getMopDurability() {
        return mopDurability;
    }

    public int getVacuumAttackRange() {
        return vacuumAttackRange;
    }

    public int getVacuumDurability() {
        return vacuumDurability;
    }

    public int getMopKnockbackTimer() {
        return mopKnockbackTimer;
    }

    public int getSprayStunTimer() {
        return sprayStunTimer;
    }
}
