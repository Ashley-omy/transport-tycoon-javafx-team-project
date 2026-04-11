package model;

import common.GridPos;
import common.Money;
import common.Id;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class World {
    // Emit supply in chunks instead of every frame.
    private static final double SUPPLY_EMIT_INTERVAL = 5.0;
    // Temporary debug marker for revenue lines in the UI.
    private static final String REVENUE_PREFIX = "[REV] ";
    // Temporary debug marker for maintenance/cost lines in the UI.
    private static final String COST_PREFIX = "[COST] ";

    private GameMap map;
    private RoadNetwork roads;
    private List<BridgeSpec> bridgeCatalog = new ArrayList<>();
    long rngSeed;
    private double supplyEmitTimer = 0.0;
    // Temporary debug buffer. Safe to remove when the debug UI is no longer needed.
    private final List<String> debugMessages = new ArrayList<>();

    // later need to modify (bridge + rngspeed)
    public World(int width, int height){

        this.map = new GameMap(width, height);
        this.roads = new RoadNetwork();

        placeInitialEntities();
    }

    // place city(3*3) / facility(2*2) FOOTPRINT logic
    public void placeEntity(MapEntity entity, GridPos center) {
        int w = entity.getFootprintW();
        int startOffset = -(w / 2);
        int endOffset = startOffset + w - 1;

        for (int dx = startOffset; dx <= endOffset; dx++) {
            for (int dy = startOffset; dy <= endOffset; dy++) {

                GridPos p = new GridPos(center.x + dx, center.y + dy);

                if (!map.inBounds(p)) {
                    throw new IllegalArgumentException("Entity footprint out of bounds: " + p);
                }

                Tile t = map.getTile(p);

                if (t.getEntity() != null) {
                    throw new IllegalStateException("Tile already occupied at: " + p);
                }

                t.setEntity(entity);
                entity.getOccupiedTiles().add(t);
            }
        }
    }

    // for place some cities and facilities for MS2
    private void placeInitialEntities() {
        City c1 = new City(Id.genNew());
        placeEntity(c1, new GridPos(5, 5));

        Facility steelMill = Factory.createSteelMill(Id.genNew());
        placeEntity(steelMill, new GridPos(10, 10));

        Facility ironMine = Mine.createIronMine(Id.genNew());
        placeEntity(ironMine, new GridPos(15, 15));
    }

    public static final Money ROAD_BUILD_COST = Money.of(150);

    public boolean canBuildAt(GridPos pos) {
        if (!map.inBounds(pos)) return false;

        Tile t = map.getTile(pos);

        return t.getRoadPiece() == null &&
                t.getStop() == null &&
                t.getGarage() == null &&
                t.getEntity() == null;
    }
    public GameMap getMap(){ return map;}

    public RoadNetwork getRoadNetwork() { return roads; }

    public List<BridgeSpec> getBridgeCatalog() { return bridgeCatalog; }

    // Temporary debug helper for transport event messages.
    public void pushDebugMessage(String message) {
        if (message == null || message.isBlank()) return;
        debugMessages.add(message);
    }

    // Temporary debug helper for revenue messages.
    public void pushRevenueMessage(String message) {
        if (message == null || message.isBlank()) return;
        debugMessages.add(REVENUE_PREFIX + message);
    }

    // Temporary debug helper for maintenance and other cost messages.
    public void pushCostMessage(String message) {
        if (message == null || message.isBlank()) return;
        debugMessages.add(COST_PREFIX + message);
    }

    // Temporary debug helper consumed by the right-side debug panel.
    public List<String> drainDebugMessages() {
        if (debugMessages.isEmpty()) {
            return List.of();
        }
        List<String> drained = new ArrayList<>(debugMessages);
        debugMessages.clear();
        return Collections.unmodifiableList(drained);
    }

    // Temporary debug helper for coloring revenue lines.
    public boolean isRevenueMessage(String message) {
        return message != null && message.startsWith(REVENUE_PREFIX);
    }

    // Temporary debug helper for coloring maintenance/cost lines.
    public boolean isCostMessage(String message) {
        return message != null && message.startsWith(COST_PREFIX);
    }

    // Temporary debug helper for hiding the internal revenue prefix from the UI text.
    public String stripDebugPrefix(String message) {
        if (isRevenueMessage(message)) {
            return message.substring(REVENUE_PREFIX.length());
        }
        if (isCostMessage(message)) {
            return message.substring(COST_PREFIX.length());
        }
        return message;
    }

    public void buildRoad(GridPos pos) {
        Tile tile = map.getTile(pos);

        RoadPiece piece = new RoadPiece(RoadKind.ROAD, null);
        piece.addTile(tile);
        tile.setRoadPiece(piece);

        roads.rebuild(map);
    }

    public void removeRoad(GridPos pos) {
        if (!map.inBounds(pos)) return;
        Tile tile = map.getTile(pos);
        if (tile != null) {
            tile.setRoadPiece(null);
            roads.rebuild(map);
        }
    }

    public void buildStop(GridPos pos, MapEntity servedPlace) {
        Tile tile = map.getTile(pos);

        Stop stop = new Stop(Id.genNew(), tile, servedPlace);
        tile.setStop(stop);
        servedPlace.attachStop(stop);
    }

    public static final Money GARAGE_BUILD_COST = Money.of(5_000);

    public boolean canBuildGarageAt(GridPos pos) {
        if (!map.inBounds(pos)) return false;
        
        Tile t = map.getTile(pos);
        
        return t.getRoadPiece() == null &&
                t.getStop() == null &&
                t.getGarage() == null &&
                t.getEntity() == null &&
                t.getTerrain().isPassable();
    }

    public void buildGarage(GridPos pos, int capacity, int serviceBayCount) {
        Tile tile = map.getTile(pos);
        
        List<Tile> occupiedTiles = List.of(tile);
        Garage garage = new Garage(Id.genNew(), capacity, serviceBayCount, occupiedTiles);
        tile.setGarage(garage);
    }

    public void tick(double deltaTime) {
        if (Double.isNaN(deltaTime) || Double.isInfinite(deltaTime) || deltaTime <= 0.0) return;

        // Each entity may occupy multiple tiles, so collect unique instances first.
        Set<MapEntity> entities = collectEntities();
        for (MapEntity entity : entities) {
            entity.tick(deltaTime);
        }

        // Stops get their own updates after the entities they serve.
        for (Stop stop : collectStops()) {
            stop.tick(deltaTime);
        }
        
        // Garages also tick (mainly for future maintenance features)
        for (Garage garage : collectGarages()) {
            garage.tick(deltaTime);
        }

        supplyEmitTimer += deltaTime; //using deltaTime to prevent supply from exploding on a fast PC.
        while (supplyEmitTimer >= SUPPLY_EMIT_INTERVAL) {
            supplyEmitTimer -= SUPPLY_EMIT_INTERVAL;
            // Periodically push newly produced cargo/passengers into attached stops.
            for (MapEntity entity : entities) {
                entity.emitSupplyToStops();
            }
        }
    }
    //using LinkedHashSet in order to avoid entity duplication
    private Set<MapEntity> collectEntities() {
        Set<MapEntity> entities = new LinkedHashSet<>();
        for (Tile[] column : map.getTiles()) {
            for (Tile tile : column) {
                if (tile.getEntity() != null) {
                    entities.add(tile.getEntity());
                }
            }
        }
        return entities;
    }

    private List<Stop> collectStops() {
        List<Stop> stops = new ArrayList<>();
        for (Tile[] column : map.getTiles()) {
            for (Tile tile : column) {
                if (tile.getStop() != null) {
                    stops.add(tile.getStop());
                }
            }
        }
        return stops;
    }
    
    private List<Garage> collectGarages() {
        List<Garage> garages = new ArrayList<>();
        for (Tile[] column : map.getTiles()) {
            for (Tile tile : column) {
                if (tile.getGarage() != null) {
                    garages.add(tile.getGarage());
                }
            }
        }
        return garages;
    }
}