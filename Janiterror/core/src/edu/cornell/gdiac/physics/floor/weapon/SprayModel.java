package edu.cornell.gdiac.physics.floor.weapon;

public class SprayModel extends WeaponModel {
    /*TODO: add to level editor */
    private static final String name = "spray";

    private static final int SPRAY_DURABILITY = 10;
    private static final int SPRAY_RANGE = 10;
    private static final int SPRAY_COOLDOWN = 10;
    private static final int SPRAY_KNOCKBACK_DISTANCE= 10;
    private static final int SPRAY_STUNTIME = 10;


    public SprayModel() {
        maxDurability = SPRAY_DURABILITY;
        cooldown = SPRAY_COOLDOWN;
        range = SPRAY_RANGE;
        resetDurability();
    }

    public void attack() {


    }
}
