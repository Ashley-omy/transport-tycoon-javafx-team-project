/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RoadPiece implements java.io.Serializable {
    @java.io.Serial
    private static final long serialVersionUID = 7639248191131961075L;

    private final List<Tile> occupiedTiles = new ArrayList<>();
    private final RoadKind kind;
    private final BridgeSpec bridgeSpec;

    public RoadPiece(RoadKind kind, BridgeSpec bridgeSpec) {
        this.kind = Objects.requireNonNull(kind, "kind");

        if (kind == RoadKind.ROAD) {
            if (bridgeSpec != null) {
                throw new IllegalArgumentException("ROAD pieces cannot have a bridgeSpec");
            }
            this.bridgeSpec = null;
        } else {
            if (bridgeSpec == null) {
                throw new IllegalArgumentException("BRIDGE pieces require a bridgeSpec");
            }
            this.bridgeSpec = bridgeSpec;
        }
    }

    public List<Tile> getOccupiedTiles() {
        return Collections.unmodifiableList(occupiedTiles);
    }

    public RoadKind getKind() {
        return kind;
    }

    public BridgeSpec getBridgeSpec() {
        return bridgeSpec;
    }

    public boolean isBridge() {
        return kind == RoadKind.BRIDGE;
    }

    public void addTile(Tile tile) {
        if (tile != null && !occupiedTiles.contains(tile)) {
            occupiedTiles.add(tile);
        }
    }
}
