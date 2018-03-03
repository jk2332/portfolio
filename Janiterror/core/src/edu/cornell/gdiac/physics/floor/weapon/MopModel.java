package edu.cornell.gdiac.physics.floor.weapon;

public class MopModel extends WeaponModel {
    /*TODO: add to level editor */
    private static final int MOP_DURABILITY = 10;
    private static final float KNOCKBACK_DISTANCE = 5.0f;

    private int maxDurability = MOP_DURABILITY;

    public MopModel() {
        resetDurability();
    }

    public void attack() {

    }
}
