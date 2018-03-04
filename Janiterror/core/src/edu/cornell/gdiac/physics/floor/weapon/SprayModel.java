package edu.cornell.gdiac.physics.floor.weapon;

public class SprayModel extends WeaponModel {
    /*TODO: add to level editor */
    private static final int SPRAY_DURABILITY = 10;
    private static final int SPRAY_RANGE = 10;
    private static final int SPRAY_COOLDOWN = 10;
    private static final int SPRAY_KNOCKBACK_DISTANCE= 10;
    private static final int SPRAY_STUNTIME = 10;



    int maxDurability = SPRAY_DURABILITY;

    public SprayModel() {
        resetDurability();
    }

    public void attack() {


    }
}
