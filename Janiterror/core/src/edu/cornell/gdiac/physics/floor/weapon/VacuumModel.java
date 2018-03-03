package edu.cornell.gdiac.physics.floor.weapon;

public class VacuumModel extends WeaponModel {
    /*TODO: add to level editor */
    private static final int VACUUM_DURABILITY = 10;

    int maxDurability = VACUUM_DURABILITY;

    public VacuumModel() {
        resetDurability();
    }

    public void attack() {

    }
}
