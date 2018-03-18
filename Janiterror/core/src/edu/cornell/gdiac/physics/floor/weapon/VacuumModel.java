package edu.cornell.gdiac.physics.floor.weapon;

public class VacuumModel extends WeaponModel {
    /*TODO: add to level editor */
    private static final String VACUUM_NAME = "vacuum";

    private static final int VACUUM_DURABILITY = 10;

    int maxDurability = VACUUM_DURABILITY;

    public VacuumModel() {
        name = VACUUM_NAME;
        resetDurability();
    }

    public void attack() {

    }
}
