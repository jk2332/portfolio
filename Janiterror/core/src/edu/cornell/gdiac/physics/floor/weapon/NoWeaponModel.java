package edu.cornell.gdiac.physics.floor.weapon;

public class NoWeaponModel extends WeaponModel {
    private static final String NO_WEAPON_NAME = "none";


    public NoWeaponModel(int durability, int range) {
        super(durability, range);
        name = NO_WEAPON_NAME;
        resetDurability();
    }
}
