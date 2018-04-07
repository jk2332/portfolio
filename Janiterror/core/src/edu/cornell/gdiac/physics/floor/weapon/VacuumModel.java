package edu.cornell.gdiac.physics.floor.weapon;

public class VacuumModel extends WeaponModel {
    private static final String VACUUM_NAME = "vacuum";


    public VacuumModel(int durability, int range) {
        super(durability, range);
        name = VACUUM_NAME;
        resetDurability();
    }
}
