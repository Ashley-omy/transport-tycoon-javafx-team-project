/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

public class Forest extends Terrain {

    private int treeCount; // 1–4

    public Forest() {
        // initial tree amount 1–4
        this.treeCount = 1 + (int)(Math.random() * 4);
    }

    public int getTreeCount() {
        return treeCount;
    }

    public void grow() {
        if (treeCount < 4) {
            treeCount++;
        }
    }

    public boolean canSpread() {
        return treeCount == 4; // only when trees are full
    }

    @Override
    public boolean isPassable() {
        return true;
    }

    @Override
    public double buildMultiplier() {
        return 1.5;
    }

    @Override
    public boolean isForest() {
        return true;
    }
}
