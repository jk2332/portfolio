package edu.cornell.gdiac.util;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

import java.util.ArrayList;


public class LevelEditorParser {

    private int [][] tiles;

    private ArrayList<Vector2> hazardPos = new ArrayList<Vector2>();
    private ArrayList<Vector2> wallMidPos = new ArrayList<Vector2>();
    private ArrayList<Vector2> wallRightPos = new ArrayList<Vector2>();
    private ArrayList<Vector2> wallLeftPos = new ArrayList<Vector2>();
    private ArrayList<Vector2> wallTRPos = new ArrayList<Vector2>();
    private ArrayList<Vector2> wallTLPos = new ArrayList<Vector2>();
    private ArrayList<Vector2> wallBRPos = new ArrayList<Vector2>();
    private ArrayList<Vector2> wallBLPos = new ArrayList<Vector2>();
    private ArrayList<Vector2> wallSRPos = new ArrayList<Vector2>();
    private ArrayList<Vector2> wallSLPos = new ArrayList<Vector2>();
    private ArrayList<Vector2> wallERPos = new ArrayList<Vector2>();
    private ArrayList<Vector2> wallELPos = new ArrayList<Vector2>();
    private ArrayList<Vector2> scientistPos = new ArrayList<Vector2>();
    private ArrayList<Vector2> robotPos = new ArrayList<Vector2>();
    private ArrayList<Vector2> slimePos = new ArrayList<Vector2>();
    private ArrayList<Vector2> lizardPos = new ArrayList<Vector2>();
    private ArrayList<Vector2> slimeTurretPos = new ArrayList<Vector2>();
    private ArrayList<String> slimeTurretDirections = new ArrayList<String>();
    private ArrayList<Integer> slimeTurretDelays = new ArrayList<Integer>();

    private ArrayList<ArrayList<Vector2>> scientistPatrol = new ArrayList<ArrayList<Vector2>>();
    private ArrayList<ArrayList<Vector2>> robotPatrol = new ArrayList<ArrayList<Vector2>>();
    private ArrayList<ArrayList<Vector2>> slimePatrol = new ArrayList<ArrayList<Vector2>>();
    private ArrayList<ArrayList<Vector2>> lizardPatrol = new ArrayList<ArrayList<Vector2>>();
    private ArrayList<ArrayList<Vector2>> slimeTurretPatrol = new ArrayList<ArrayList<Vector2>>();

    private Vector2 joePos;
    private Vector2 goalDoorPos;
    private ArrayList<Vector2> mopCartPos = new ArrayList<Vector2>();
    private ArrayList<Boolean> mopCartVisitedBefore = new ArrayList<Boolean>();
    private ArrayList<Vector2> specialHealthPos = new ArrayList<Vector2>();
//    private ArrayList<Vector2> specialDurabilityPos = new ArrayList<Vector2>();
//    private ArrayList<Vector2> specialDamagePos = new ArrayList<Vector2>();
//    private ArrayList<Vector2> specialSpeedPos = new ArrayList<Vector2>();

    private ArrayList<Vector2> beakerPos = new ArrayList<Vector2>();
    private ArrayList<Vector2> plantPos = new ArrayList<Vector2>();
    private ArrayList<Vector2> computerPos = new ArrayList<Vector2>();

   /* private int robotAttackRange;
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
    private int sprayStunTimer;*/

    private int wallvgid;
    private int wallhgid;
    private int tilegid;
    private int hazardgid;
    private int beakergid;
    private int computergid;
    private int plantgid;

    private int boardWidth;
    private int boardHeight;

    public LevelEditorParser(String levelPath) {
        System.out.println(levelPath);
        Element level = new XmlReader().parse(Gdx.files.internal(levelPath));

        Array<Element> layers = level.getChildrenByName("layer");
        Array<Element> objects = level.getChildrenByName("objectgroup");

        int bh = level.getIntAttribute("height") * level.getIntAttribute("tileheight");

        Array<Element> tilesets = level.getChildrenByName("tileset");
        for (Element ts : tilesets) {
            if (ts.get("source").equals("wallsh.tsx")) {
                wallhgid = ts.getIntAttribute("firstgid");
            } else if (ts.get("source").equals("wallsv.tsx")) {
                wallvgid = ts.getIntAttribute("firstgid");

            } else if (ts.get("source").equals("tiles.tsx")) {
                tilegid = ts.getIntAttribute("firstgid");
            } else if (ts.get("source").equals("hazard.tsx")) {
                hazardgid = ts.getIntAttribute("firstgid");
            } else if (ts.get("source").equals("beaker-table.tsx")) {
                beakergid = ts.getIntAttribute("firstgid");
            } else if (ts.get("source").equals("computer.tsx")) {
                computergid = ts.getIntAttribute("firstgid");
            } else if (ts.get("source").equals("plant.tsx")) {
                plantgid = ts.getIntAttribute("firstgid");
            }
            if (hazardgid == 0) {
                hazardgid = 1000;
            }
            if (tilegid == 0) {
                tilegid = 1000;
            }
            if (wallvgid == 0) {
                wallvgid = 1000;
            }
            if (wallhgid == 0) {
                wallhgid = 1000;
            }
            if (computergid == 0) {
                computergid = 1000;
            }
            if (plantgid == 0) {
                plantgid = 1000;
            }
            if (beakergid == 0) {
                beakergid = 1000;
            }
        }
        tiles = layerToList(layers.get(0));

        boardHeight= tiles.length;
        boardWidth= tiles[0].length;

        for (int i = 0; i < boardHeight; i++) {
            for (int j = 0; j < boardWidth; j++) {
                tiles[i][j] = tiles[i][j] - tilegid + 1;
            }
        }
        Array<Element> mopCartElements = objects.get(0).getChildrenByName("object");
        for (int i = 0; i < mopCartElements.size; i++) {
            Element mopCartElement = mopCartElements.get(i);
            Vector2 mopCartVector = new Vector2(mopCartElement.getFloatAttribute("x"),
                    bh - mopCartElement.getFloatAttribute("y"));
            mopCartPos.add(mopCartVector);
            boolean reloaded = Boolean.parseBoolean(mopCartElement.getChild(0).getChildrenByName("property").get(0).getAttribute("value"));
            mopCartVisitedBefore.add(reloaded);
        }

        Element goalDoorElement = objects.get(1).getChild(0);
        goalDoorPos = new Vector2(goalDoorElement.getFloatAttribute("x"),bh - goalDoorElement.getFloatAttribute("y"));

        Array<Element> charactersElement = objects.get(2).getChildrenByName("object");
        for (int i = 0; i < charactersElement.size; i++) {
            Element character = charactersElement.get(i);
            String type = character.get("type");
            float x = character.getFloatAttribute("x");
            float y = bh - character.getFloatAttribute("y");
            if (type.equals("scientist")) {
                /*Array<Element> ps = character.getChild(0).getChildrenByName("property");
                for (int j = 0; j < ps.size; j++) {
                    Element p = ps.get(j);
                    String name = p.get("name");
                    if (name.equals("Attack Range") && scientistPos.size() == 0) {
                        scientistAttackRange = p.getIntAttribute("value");
                    } else if (name.equals("Density") && scientistPos.size() == 0) {
                        scientistDensity = p.getFloatAttribute("value");
                    } else if (name.equals("HP") && scientistPos.size() == 0) {
                        scientistHP = p.getIntAttribute("value");
                    } else if (name.equals("Velocity") && scientistPos.size() == 0) {
                        scientistVel = p.getFloatAttribute("value");
                    } else if (name.equals("Patrol Path")) {
                        scientistPatrol.add(patrolStrToArr(p.getAttribute("value")));
                    }
                }*/
                Element p = character.getChild(0).getChildrenByName("property").get(0);
                scientistPatrol.add(patrolStrToArr(p.getAttribute("value")));
                scientistPos.add(new Vector2(x, y));
            } else if (type.equals("robot")) {
                /*Array<Element> ps = character.getChild(0).getChildrenByName("property");
                for (int j = 0; j < ps.size; j++) {
                    Element p = ps.get(j);
                    String name = p.get("name");
                    if (name.equals("Attack Range") && robotPos.size() == 0) {
                        robotAttackRange = p.getIntAttribute("value");
                    } else if (name.equals("Density") && robotPos.size() == 0) {
                        robotDensity = p.getFloatAttribute("value");
                    } else if (name.equals("HP") && robotPos.size() == 0) {
                        robotHP = p.getIntAttribute("value");
                    } else if (name.equals("Velocity") && robotPos.size() == 0) {
                        robotVel = p.getFloatAttribute("value");
                    } else if (name.equals("Patrol Path")) {
                        robotPatrol.add(patrolStrToArr(p.getAttribute("value")));
                    }
                }*/
                Element p = character.getChild(0).getChildrenByName("property").get(0);
                robotPatrol.add(patrolStrToArr(p.getAttribute("value")));
                robotPos.add(new Vector2(x, y));
            } else if (type.equals("joe")) {
                /*if (joePos == null) {
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
                }*/
                joePos = new Vector2(x, y);
            } else if (type.equals("slime")) {
                /*Array<Element> ps = character.getChild(0).getChildrenByName("property");
                for (int j = 0; j < ps.size; j++) {
                    Element p = ps.get(j);
                    String name = p.get("name");
                    if (name.equals("Attack Range") && slimePos.size() == 0) {
                        slimeAttackRange = p.getIntAttribute("value");
                    } else if (name.equals("Density") && slimePos.size() == 0) {
                        slimeDensity = p.getFloatAttribute("value");
                    } else if (name.equals("HP") && slimePos.size() == 0) {
                        slimeHP = p.getIntAttribute("value");
                    } else if (name.equals("Velocity") && slimePos.size() == 0) {
                        slimeVel = p.getFloatAttribute("value");
                    } else if (name.equals("Slimeball Speed") && slimePos.size() == 0) {
                        slimeballSpeed = p.getFloatAttribute("value");
                    } else if (name.equals("Patrol Path")) {
                        slimePatrol.add(patrolStrToArr(p.getAttribute("value")));
                    }
                }*/
                Element p = character.getChild(0).getChildrenByName("property").get(0);
                slimePatrol.add(patrolStrToArr(p.getAttribute("value")));
                slimePos.add(new Vector2(x, y));
            } else if (type.equals("lizard")) {
                /*Array<Element> ps = character.getChild(0).getChildrenByName("property");
                for (int j = 0; j < ps.size; j++) {
                    Element p = ps.get(j);
                    String name = p.get("name");
                    if (name.equals("Attack Range") && lizardPos.size() == 0) {
                        lizardAttackRange = p.getIntAttribute("value");
                    } else if (name.equals("Density") && lizardPos.size() == 0) {
                        lizardDensity = p.getFloatAttribute("value");
                    } else if (name.equals("HP") && lizardPos.size() == 0) {
                        lizardHP = p.getIntAttribute("value");
                    } else if (name.equals("Velocity") && lizardPos.size() == 0) {
                        lizardVel = p.getFloatAttribute("value");
                    } else if (name.equals("Patrol Path")) {
                        lizardPatrol.add(patrolStrToArr(p.getAttribute("value")));
                    }
                }*/
                Element p = character.getChild(0).getChildrenByName("property").get(0);
                lizardPatrol.add(patrolStrToArr(p.getAttribute("value")));
                lizardPos.add(new Vector2(x, y));
            } else if (type.equals("turret-slime")) {
                Array<Element> ps = character.getChild(0).getChildrenByName("property");
                slimeTurretPatrol.add(patrolStrToArr(ps.get(0).getAttribute("value")));
                slimeTurretDelays.add(Integer.parseInt(ps.get(1).getAttribute("value")));
                slimeTurretDirections.add(ps.get(2).getAttribute("value"));
                slimeTurretPos.add(new Vector2(x, y));
            }
        }

        Array<Element> specialElement = objects.get(3).getChildrenByName("object");
        //get raw locations of special elements (powerups)
        for (int i = 0; i < specialElement.size; i++) {
            Element special = specialElement.get(i);
            String type = special.get("type");
            float x = special.getFloatAttribute("x");
            float y = bh - special.getFloatAttribute("y");
            if (type.equals("health")) {
                specialHealthPos.add(new Vector2(x, y));
            } else if (type.equals("mop")) {
                //make new position vector for mop powerups
            }
        }

        int[][] horiWalls = layerToList(layers.get(1));
        int[][] vertiWalls = layerToList(layers.get(3));
        int [][] hazardTiles = layerToList(layers.get(4));

        int [][] extraTiles = layerToList(layers.get(2));

        for (int i = 0; i < extraTiles.length; i++) {
            for (int j = 0; j < extraTiles[0].length; j++) {
                if (extraTiles[i][j] == computergid) {
                    computerPos.add(new Vector2(j, i));
                } else if (extraTiles[i][j] == beakergid) {
                    beakerPos.add(new Vector2(j, i));
                } else if (extraTiles[i][j] == plantgid) {
                    plantPos.add(new Vector2(j, i));
                }
            }
        }


        for (int i = 0; i < vertiWalls.length; i++) {
            for (int j = 0; j < vertiWalls[0].length; j++) {
                if (vertiWalls[i][j] == wallvgid) {
                    wallLeftPos.add(new Vector2(j, i));
                } else if (vertiWalls[i][j] == wallvgid + 1) { //TODO change later
                    wallRightPos.add(new Vector2(j, i));
                }
            }
        }
        for (int i = 0; i < horiWalls.length; i++) {
            for (int j = 0; j < horiWalls[0].length; j++) {
                if (horiWalls[i][j] == wallhgid + 1) {
                    wallMidPos.add(new Vector2(j, i ));
                } else if (horiWalls[i][j] == wallhgid) {
                    wallTLPos.add(new Vector2(j, i ));
                } else if (horiWalls[i][j] == wallhgid + 3) {
                    wallTRPos.add(new Vector2(j, i ));
                } else if (horiWalls[i][j] == wallhgid + 5) {
                    wallBLPos.add(new Vector2(j, i ));
                } else if (horiWalls[i][j] == wallhgid + 8) {
                    wallBRPos.add(new Vector2(j, i ));
                } else if (horiWalls[i][j] == wallhgid + 7) {
                    wallSLPos.add(new Vector2(j, i));
                } else if (horiWalls[i][j] == wallhgid + 6) {
                    wallSRPos.add(new Vector2(j, i));
                } else if (horiWalls[i][j] == wallhgid + 9) {
                    wallERPos.add(new Vector2(j, i));
                } else if (horiWalls[i][j] == wallhgid + 4) {
                    wallELPos.add(new Vector2(j, i));
                }
            }
        }
        for (int i = 0; i < hazardTiles.length; i++) {
            for (int j = 0; j < hazardTiles[0].length; j++) {
                if (hazardTiles[i][j] == hazardgid) {
                    //if this tile is a hazard tile
                    //why add it backwards?
                    hazardPos.add(new Vector2(j, i));
                }
            }
        }
    }

    private ArrayList<Vector2> patrolStrToArr(String s) {
        String[] coordList = s.split(",");
        ArrayList<Vector2> path = new ArrayList<Vector2>();
        for (String coord : coordList) {
            String[] cList = coord.split(" ");
            path.add(new Vector2(Integer.parseInt(cList[0]), Integer.parseInt(cList[1])));
        }
        return path;
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

    public int[][] getTiles() { return tiles; }

    public ArrayList<Vector2> getHazardPos() {
        return hazardPos;
    }

    public ArrayList<Vector2> getSpecialHealthPos() {
        return specialHealthPos;
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

    public ArrayList<Vector2> getWallSLPos() {
        return wallSLPos;
    }

    public ArrayList<Vector2> getWallSRPos() {
        return wallSRPos;
    }

    public ArrayList<Vector2> getWallELPos() {
        return wallELPos;
    }

    public ArrayList<Vector2> getWallERPos() {
        return wallERPos;
    }

    public ArrayList<Vector2> getMopCartPos() {
        return mopCartPos;
    }
    public ArrayList<Boolean> getMopCartVisitedBefore() {
        return mopCartVisitedBefore;
    }

    public float getGoalDoorX() {
        return goalDoorPos.x;
    }

    public float getGoalDoorY() {
        return goalDoorPos.y;
    }

    public float getJoePosX() {
        return joePos.x;
    }

    public float getJoePosY() {
        return joePos.y;
    }

    /*public int getJoeHP() {
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
    }*/

    public int getBoardHeight() {
        return boardHeight;
    }

    public int getBoardWidth() {
        return boardWidth;
    }

    public ArrayList<ArrayList<Vector2>> getLizardPatrol() {
        return lizardPatrol;
    }

    public ArrayList<ArrayList<Vector2>> getRobotPatrol() {
        return robotPatrol;
    }

    public ArrayList<ArrayList<Vector2>> getScientistPatrol() {
        return scientistPatrol;
    }

    public ArrayList<ArrayList<Vector2>> getSlimePatrol() {
        return slimePatrol;
    }

    public ArrayList<Vector2> getSlimeTurretPos() {
        return slimeTurretPos;
    }

    public ArrayList<String> getSlimeTurretDirections() {
        return slimeTurretDirections;
    }
    public ArrayList<Integer> getSlimeTurretDelays() {
        return slimeTurretDelays;
    }
    public ArrayList<ArrayList<Vector2>> getSlimeTurretPatrol() {
        return slimeTurretPatrol;
    }

    public ArrayList<Vector2> getBeakerPos() {
        return beakerPos;
    }

    public ArrayList<Vector2> getComputerPos() {
        return computerPos;
    }

    public ArrayList<Vector2> getPlantPos() {
        return plantPos;
    }
}
