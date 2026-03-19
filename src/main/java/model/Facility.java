/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import common.Id;

public abstract class Facility extends MapEntity {
    public Facility(Id id, GoodsType input, GoodsType output) {
        super(id);
    }
}
