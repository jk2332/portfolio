package edu.cornell.gdiac.physics.floor.weapon;

public class WeaponModel {
    public String name;
    public int maxDurability;
    public int durability;
    //public int cooldown;
    public int range;

    public WeaponModel(int maxDurability, int range) {
        this.maxDurability = maxDurability;
        this.range = range;
    }

    public void resetDurability() {
        durability = maxDurability;
    }

    public int getDurability() {
        return durability;
    }

    public int getMaxDurability() {
        return maxDurability;
    }

    public String getName() {return name;}

    /*TODO prevent from becoming negative */
    public void decrDurability() {
        durability -= 1;
    }

    //public int getCooldown() {return cooldown;}

    public int getRange() {
        return range;
    }
}
