package edu.cornell.gdiac.physics.floor.weapon;

public class VacuumModel extends WeaponModel {
    /*TODO: add to level editor */
    private static final String VACUUM_NAME = "vacuum";

    private static final int VACUUM_DURABILITY = 10;
    private static final int VACUUM_COOLDOWN = 10;
    private static final int VACUUM_RANGE= 10;
    private static final int VACUUM_STUNTIME = 10;


    public VacuumModel() {
        maxDurability = VACUUM_DURABILITY;
        cooldown = VACUUM_COOLDOWN;
        range = VACUUM_RANGE;
        name = VACUUM_NAME;
        resetDurability();
    }

    public void attack() {

    }
}
