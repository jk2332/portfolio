package edu.cornell.gdiac.physics.floor.weapon;

public class MopModel extends WeaponModel {
    /*TODO: add to level editor */
    private static final int MOP_DURABILITY = 10;
    private static final float KNOCKBACK_DISTANCE = 5.0f;
    private static final int MOP_COOLDOWN = 40;
    private static final float MOP_RANGE = 40;

    public MopModel() {
        maxDurability = MOP_DURABILITY;
        cooldown = MOP_COOLDOWN;
        range = MOP_RANGE;
        resetDurability();
    }

    public void attack() {

    }
}
