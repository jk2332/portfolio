package edu.cornell.gdiac.physics.floor.weapon;

public class LidModel extends WeaponModel {
    /*TODO: add to level editor */
    private static final String LID_NAME = "lid";

    public LidModel(int durability, int range) {
        super(durability,range);
        name = LID_NAME;
        resetDurability();
    }
}
