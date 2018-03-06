package edu.cornell.gdiac.physics.floor.weapon;

abstract public class WeaponModel {
    public int maxDurability;
    public int durability;
    public int cooldown;
    public float range;

    public void resetDurability() {
        durability = maxDurability;
    }

    public int getDurability() {
        return durability;
    }

    public int getMaxDurability() {
        return maxDurability;
    }

    /*TODO prevent from becoming negative */
    public void decrDurability() {
        durability -= 1;
    }

    public int getCooldown() {
        return cooldown;
    }

    public float getRange() {
        return range;
    }
}
