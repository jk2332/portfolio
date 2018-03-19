package edu.cornell.gdiac.physics.floor.weapon;

public class LidModel extends WeaponModel {
    /*TODO: add to level editor */
    private static final String LID_NAME = "lid";

    private static final int LID_DURABILITY = 10;
    private static final int LID_RANGE = 10;
    private static final int LID_COOLDOWN = 10;
    private static final int LID_KNOCKBACK_DISTANCE= 10;
    private static final int LID_STUNTIME = 10;



    public LidModel() {
        maxDurability = LID_DURABILITY;
        cooldown = LID_COOLDOWN;
        range = LID_RANGE;
        name = LID_NAME;
        resetDurability();
    }

    public void attack() {

    }
}
