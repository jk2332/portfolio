package edu.cornell.gdiac.physics.floor.weapon;

public class MopModel extends WeaponModel {
    /*TODO: add to level editor */
    private static final String MOP_NAME = "mop";

    private int knockbackTimer;

    public MopModel(int durability, int range, int knockbackTimer) {
        super(durability, range);
        name = MOP_NAME;
        this.knockbackTimer = knockbackTimer;
        resetDurability();
    }

    public int getKnockbackTimer() {
        return knockbackTimer;
    }
}
