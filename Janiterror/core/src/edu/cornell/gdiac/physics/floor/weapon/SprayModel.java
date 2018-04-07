package edu.cornell.gdiac.physics.floor.weapon;

public class SprayModel extends WeaponModel {
    /*TODO: add to level editor */
    private static final String SPRAY_NAME = "spray";
    private int stunTimer;

    public SprayModel(int durability, int range, int stunTimer) {
        super(durability, range);
        name = SPRAY_NAME;
        this.stunTimer = stunTimer;
        resetDurability();
    }

    public int getStunTimer() {
        return stunTimer;
    }
}
