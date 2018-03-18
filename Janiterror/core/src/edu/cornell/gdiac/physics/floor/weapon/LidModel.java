package edu.cornell.gdiac.physics.floor.weapon;

public class LidModel extends WeaponModel {
    /*TODO: add to level editor */
    private static final String LID_NAME = "lid";

    private static final int LID_DURABILITY = 10;

    int maxDurability = LID_DURABILITY;

    public LidModel() {
        name = LID_NAME;
        resetDurability();
    }

    public void attack() {

    }
}
