package model;

import common.GridPos;
import common.Money;
import common.Id;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class World implements java.io.Serializable {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    // Emit supply in chunks instead of every frame.
    private static final double SUPPLY_EMIT_INTERVAL = 5.0;
    private static final List<BridgeSpec> DEFAULT_BRIDGE_CATALOG = List.of(
            new BridgeSpec(BridgeType.TYPE_A, 4, Money.of(600), 1.0),
            new BridgeSpec(BridgeType.TYPE_B, 7, Money.of(1_100), 1.5),
            new BridgeSpec(BridgeType.TYPE_C, 10, Money.of(1_800), 2.0)
    );
    private static final String REVENUE_PREFIX = "[REV] ";
    private static final String COST_PREFIX = "[COST] ";

    private final GameMap map;
    private final RoadNetwork roads;
    private final List<BridgeSpec> bridgeCatalog;
    private double supplyEmitTimer = 0.0;
    private final List<String> hudMessages = new ArrayList<>();

    // later need to modify (bridge + rngspeed)
    public World(int width, int height){

        this.map = new GameMap(width, height);
        this.roads = new RoadNetwork();
        this.bridgeCatalog = new ArrayList<>(DEFAULT_BRIDGE_CATALOG);

        WorldInitializer.initialize(this);
    }

    // place city(5*5) / facility(2*2) FOOTPRINT logic
    public void placeEntity(MapEntity entity, GridPos center) {
        int w = entity.getFootprintW();
        int startOffset = -(w / 2);
        int endOffset = startOffset + w - 1;
        List<Tile> tilesToOccupy = new ArrayList<>(w * w);

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
                tilesToOccupy.add(t);
            }
        }

        for (Tile tile : tilesToOccupy) {
            tile.setEntity(entity);
            entity.getOccupiedTiles().add(tile);
        }

        if (entity instanceof City city) {
            initializeCityInternalRoads(city);
        }
    }

    // Initialize fixed city-internal road tiles (center row + center column).
    private void initializeCityInternalRoads(City city) {
        for (Tile tile : city.getOccupiedTiles()) {
            if (!city.hasInternalRoadAt(tile.getPos())) {
                continue;
            }
            if (tile.getRoadPiece() != null) {
                continue;
            }
            RoadPiece internalRoad = new RoadPiece(RoadKind.ROAD, null);
            internalRoad.addTile(tile);
            tile.setRoadPiece(internalRoad);
        }
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

    public List<BridgeSpec> getBridgeCatalog() { return Collections.unmodifiableList(bridgeCatalog); }

    public void pushMessage(String message) {
        if (message == null || message.isBlank()) return;
        hudMessages.add(message);
    }

    public void pushRevenueMessage(String message) {
        if (message == null || message.isBlank()) return;
        hudMessages.add(REVENUE_PREFIX + message);
    }

    public void pushCostMessage(String message) {
        if (message == null || message.isBlank()) return;
        hudMessages.add(COST_PREFIX + message);
    }

    public List<String> drainMessages() {
        if (hudMessages.isEmpty()) {
            return List.of();
        }
        List<String> drained = new ArrayList<>(hudMessages);
        hudMessages.clear();
        return Collections.unmodifiableList(drained);
    }

    public boolean isRevenueMessage(String message) {
        return message != null && message.startsWith(REVENUE_PREFIX);
    }

    public boolean isCostMessage(String message) {
        return message != null && message.startsWith(COST_PREFIX);
    }

    public String stripMessagePrefix(String message) {
        if (isRevenueMessage(message)) {
            return message.substring(REVENUE_PREFIX.length());
        }
        if (isCostMessage(message)) {
            return message.substring(COST_PREFIX.length());
        }
        return message;
    }

    public void buildRoad(GridPos pos) {
        if (!canBuildAt(pos)) {
            throw new IllegalArgumentException("Cannot build road at: " + pos);
        }

        Tile tile = map.getTile(pos);

        RoadPiece piece = new RoadPiece(RoadKind.ROAD, null);
        piece.addTile(tile);
        tile.setRoadPiece(piece);

        roads.rebuild(map);
    }

    public void buildBridge(List<GridPos> line, BridgeType type) {
        BridgeSpec spec = getBridgeSpec(type);
        validateBridgeLine(line, spec);

        RoadPiece bridge = new RoadPiece(RoadKind.BRIDGE, spec);
        for (GridPos pos : line) {
            Tile tile = map.getTile(pos);
            bridge.addTile(tile);
            tile.setRoadPiece(bridge);
        }

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

    public static final Money GARAGE_BUILD_COST = Money.of(5_000);

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
            // Keep per-entity floating UI messages in sync with simulation time.
            entity.tickEventDisplays(deltaTime);
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

        // 1. all trees grow
        for (Tile tile : map.getAllTiles()) {
            Terrain t = tile.getTerrain();
            if (t instanceof Forest forest) {
                forest.grow(deltaTime);
            }
        }

        // 2. record to tick the new spread forest location
        Set<GridPos> newForests = new LinkedHashSet<>();

        for (Tile tile : map.getAllTiles()) {
            Terrain t = tile.getTerrain();

            if (t instanceof Forest forest) {
                int attempts = forest.consumeSpreadAttempts(deltaTime);
                for (int i = 0; i < attempts; i++) {
                    spreadForest(tile.getPos(), newForests);
                }
            }
        }

        // 3. butch update
        for (GridPos pos : newForests) {
            map.setTerrain(pos, new Forest());
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

    // Forest tick helper fn
    private void spreadForest(GridPos origin, Set<GridPos> newForests) {

        int[][] dirs = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}
        };

        for (int[] d : dirs) {
            GridPos np = new GridPos(origin.x + d[0], origin.y + d[1]);

            if (!map.inBounds(np)) continue;

            Tile neighbor = map.getTile(np);

            // Forest can only spread onto empty land tiles.
            if (neighbor.getTerrain().isLand() && isEmptyTile(neighbor)) {

                // the probability of spreading
                if (Math.random() < 0.10) { // 10%
                    newForests.add(np);
                }
            }
        }
    }

    private boolean isEmptyTile(Tile tile) {
        return tile.getRoadPiece() == null &&
                tile.getStop() == null &&
                tile.getGarage() == null &&
                tile.getEntity() == null;
    }

    public BridgeSpec getBridgeSpec(BridgeType type) {
        if (type == null) {
            throw new IllegalArgumentException("Bridge type cannot be null");
        }

        for (BridgeSpec spec : bridgeCatalog) {
            if (spec.getType() == type) {
                return spec;
            }
        }
        throw new IllegalArgumentException("Unknown bridge type: " + type);
    }

    private void validateBridgeLine(List<GridPos> line, BridgeSpec spec) {
        if (line == null || line.isEmpty()) {
            throw new IllegalArgumentException("Bridge line cannot be empty");
        }
        if (line.size() > spec.getMaxSpanTiles()) {
            throw new IllegalArgumentException("Bridge span exceeds maxSpanTiles for " + spec.getType());
        }

        boolean touchesWater = false;
        GridPos previous = null;
        for (GridPos pos : line) {
            if (pos == null || !map.inBounds(pos)) {
                throw new IllegalArgumentException("Bridge tile out of bounds: " + pos);
            }

            Tile tile = map.getTile(pos);
            if (!isEmptyTile(tile)) {
                throw new IllegalArgumentException("Bridge tile is occupied: " + pos);
            }

            if (tile.getTerrain().isWater()) {
                touchesWater = true;
            }

            if (previous != null) {
                int dx = Math.abs(pos.x - previous.x);
                int dy = Math.abs(pos.y - previous.y);
                if (dx + dy != 1) {
                    throw new IllegalArgumentException("Bridge line must be contiguous: " + previous + " -> " + pos);
                }
            }
            previous = pos;
        }

        if (!touchesWater) {
            throw new IllegalArgumentException("Bridge line must cross at least one water tile");
        }
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
        Set<Garage> garages = new LinkedHashSet<>();
        for (Tile[] column : map.getTiles()) {
            for (Tile tile : column) {
                if (tile.getGarage() != null) {
                    garages.add(tile.getGarage());
                }
            }
        }
        return new ArrayList<>(garages);
    }

}
