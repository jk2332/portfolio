package edu.cornell.gdiac.physics.floor.weapon;

abstract public class WeaponModel {
    public int maxDurability;
    public int durability;

    public void resetDurability() {
        durability = maxDurability;
    }

    public int getDurability() {
        return durability;
    }

    public int getMaxDurability() {
        return maxDurability;
    }

    abstract public void attack();
}
