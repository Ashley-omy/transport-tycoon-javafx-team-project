/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.util.ArrayList;
import java.util.List;

public class RoadPiece {
    private final List<Tile> occupiedTiles = new ArrayList<>();
    private final RoadKind kind;
    private final BridgeSpec bridgeSpec;

    public RoadPiece(RoadKind kind, BridgeSpec bridgeSpec) {
        this.kind = kind;

        if (kind == RoadKind.ROAD) {
            this.bridgeSpec = null;
        } else {
            // later will implement Bridge!!
            this.bridgeSpec = bridgeSpec;
        }
    }

    public List<Tile> getOccupiedTiles() {
        return occupiedTiles;
    }

    public RoadKind getKind() {
        return kind;
    }

    public BridgeSpec getBridgeSpec() {
        return bridgeSpec;
    }

    public void addTile(Tile tile) {
        occupiedTiles.add(tile);
    }
}
