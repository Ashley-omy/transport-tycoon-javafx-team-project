package controller;

import common.GridPos;
import common.Money;
import model.BridgeType;
import model.City;
import model.Company;
import model.Land;
import model.Stop;
import model.Tile;
import model.World;
import model.Water;
import model.WaterType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuildControllerTest {

    @Test
    // requirement coverage:
    // happy path: player can build road on one valid empty tile and gets charged for it
    void buildRoadShouldSucceedOnValidEmptyTile() {
        // step 1: make one valid empty tile that is next to an existing road
        World world = new World(25, 25);
        Company company = new Company();
        BuildController buildController = new BuildController(world, company);
        GridPos roadPos = findBuildableRoadTile(world);

        Money cashBefore = company.getEconomy().getCash();

        // step 2: build road on that tile
        ActionResult result = buildController.buildRoad(roadPos);

        // step 3: road should be placed and company should pay standard road cost
        assertTrue(result.isSuccess());
        assertEquals("Build road successfully", result.getMessage());
        assertTrue(world.getMap().getTile(roadPos).hasRoad());
        assertEquals(cashBefore.subtract(World.ROAD_BUILD_COST), company.getEconomy().getCash());
    }

    @Test
    // requirement coverage:
    // error path: player with no money should not be able to build road
    void buildRoadShouldFailWhenPlayerHasNoMoney() {
        // step 1: prepare one valid road position but give company zero starting cash
        World world = new World(25, 25);
        Company company = new Company(Money.ZERO);
        BuildController buildController = new BuildController(world, company);
        GridPos roadPos = findBuildableRoadTile(world);

        // step 2: try to build road anyway
        ActionResult result = buildController.buildRoad(roadPos);

        // step 3: build should fail and cash should stay unchanged
        assertFalse(result.isSuccess());
        assertEquals("Not enough money", result.getMessage());
        assertNull(world.getMap().getTile(roadPos).getRoadPiece());
        assertEquals(Money.ZERO, company.getEconomy().getCash());
    }

    @Test
    // requirement coverage:
    // error path: blocked tile such as water or entity tile should reject road building
    void buildRoadShouldFailOnBlockedTile() {
        // step 1: find one blocked tile from the initialized world
        World world = new World(25, 25);
        Company company = new Company();
        BuildController buildController = new BuildController(world, company);
        GridPos blockedPos = findBlockedRoadTile(world);

        // step 2: try to build road on blocked tile
        ActionResult result = buildController.buildRoad(blockedPos);

        // step 3: controller should reject build and tile should stay unchanged
        assertFalse(result.isSuccess());
        assertEquals("Cannot place road here", result.getMessage());
        assertNull(world.getMap().getTile(blockedPos).getRoadPiece());
    }

    @Test
    // error path: road building must not overwrite an existing garage tile
    void buildRoadShouldFailOnGarageTileWithoutSpendingMoney() {
        World world = new World(25, 25);
        Company company = new Company();
        BuildController buildController = new BuildController(world, company);
        GridPos garagePos = findEmptyTileNextToRoad(world);

        ActionResult garageResult = buildController.buildGarage(garagePos);
        Money cashAfterGarage = company.getEconomy().getCash();

        ActionResult roadResult = buildController.buildRoad(garagePos);

        assertTrue(garageResult.isSuccess());
        assertFalse(roadResult.isSuccess());
        assertEquals("Cannot place road here", roadResult.getMessage());
        assertNull(world.getMap().getTile(garagePos).getRoadPiece());
        assertTrue(world.getMap().getTile(garagePos).hasGarage());
        assertEquals(cashAfterGarage, company.getEconomy().getCash());
    }

    @Test
    // happy path: player can build garage on one empty tile next to road
    void buildGarageShouldSucceedOnEmptyTileNextToRoad() {
        // step 1: find one valid empty tile next to existing road
        World world = new World(25, 25);
        Company company = new Company();
        BuildController buildController = new BuildController(world, company);
        GridPos garagePos = findEmptyTileNextToRoad(world);

        Money cashBefore = company.getEconomy().getCash();

        // step 2: build garage on that tile
        ActionResult result = buildController.buildGarage(garagePos);

        // step 3: garage should be placed and company should pay garage cost
        assertTrue(result.isSuccess());
        assertEquals("Garage built successfully", result.getMessage());
        assertTrue(world.getMap().getTile(garagePos).hasGarage());
        assertEquals(cashBefore.subtract(World.GARAGE_BUILD_COST), company.getEconomy().getCash());
    }

    @Test
    // error path: garage cannot be built if tile is not connected to road
    void buildGarageShouldFailWhenTileIsNotAdjacentToRoad() {
        // step 1: find one empty tile that is not next to any road
        World world = new World(25, 25);
        Company company = new Company();
        BuildController buildController = new BuildController(world, company);
        GridPos garagePos = findEmptyTileNotAdjacentToRoad(world);

        // step 2: try to build garage anyway
        ActionResult result = buildController.buildGarage(garagePos);

        // step 3: build should fail and no garage should appear on tile
        assertFalse(result.isSuccess());
        assertNull(world.getMap().getTile(garagePos).getGarage());
    }

    @Test
    // error path: player without enough money cannot build garage
    void buildGarageShouldFailWhenPlayerCannotAffordIt() {
        // step 1: use same valid garage tile but give company too little money
        World world = new World(25, 25);
        Company company = new Company(Money.of(1_000));
        BuildController buildController = new BuildController(world, company);
        GridPos garagePos = findEmptyTileNextToRoad(world);

        // step 2: try to build garage
        ActionResult result = buildController.buildGarage(garagePos);

        // step 3: build should fail and cash should not change
        assertFalse(result.isSuccess());
        assertEquals("Not enough money", result.getMessage());
        assertNull(world.getMap().getTile(garagePos).getGarage());
        assertEquals(Money.of(1_000), company.getEconomy().getCash());
    }

    @Test
    // happy path: bridge should spend money and place one bridge road piece
    void buildBridgeShouldSpendMoneyAndPlaceBridgeRoadPiece() {
        // step 1: prepare one road anchor and one water tile for bridge
        World world = new World(25, 25);
        Company company = new Company();
        BuildController buildController = new BuildController(world, company);
        GridPos roadAnchor = new GridPos(2, 0);
        GridPos bridgeTile = new GridPos(3, 0);

        world.buildRoad(roadAnchor);
        world.getMap().setTerrain(bridgeTile, new Water(WaterType.RIVER));

        Money cashBefore = company.getEconomy().getCash();
        Money expectedCost = world.getBridgeSpec(BridgeType.TYPE_A).getCost();

        // step 2: build bridge on selected bridge tile
        ActionResult result = buildController.buildBridge(List.of(bridgeTile), BridgeType.TYPE_A);

        // step 3: bridge should be placed and company should pay bridge cost
        assertTrue(result.isSuccess());
        assertEquals("Bridge built successfully", result.getMessage());
        assertEquals(cashBefore.subtract(expectedCost), company.getEconomy().getCash());
        assertEquals(BridgeType.TYPE_A, world.getMap().getTile(bridgeTile).getRoadPiece().getBridgeSpec().getType());
    }

    @Test
    // error path: bridge must connect to existing road from at least one end
    void buildBridgeShouldFailWithoutAdjacentRoadConnection() {
        // step 1: prepare one water tile that has no road anchor
        World world = new World(25, 25);
        Company company = new Company();
        BuildController buildController = new BuildController(world, company);
        GridPos bridgeTile = new GridPos(10, 10);

        world.getMap().setTerrain(bridgeTile, new Water(WaterType.RIVER));

        // step 2: try to build bridge without road connection
        ActionResult result = buildController.buildBridge(List.of(bridgeTile), BridgeType.TYPE_A);

        // step 3: build should fail and no bridge road should appear
        assertFalse(result.isSuccess());
        assertEquals("Bridge must connect to an existing road at one end", result.getMessage());
        assertNull(world.getMap().getTile(bridgeTile).getRoadPiece());
    }

    @Test
    // happy path: stop should attach to adjacent city when tile is valid
    void buildStopShouldAttachToAdjacentCity() {
        // step 1: prepare one empty stop tile, one road tile, and one city tile
        World world = new World(25, 25);
        Company company = new Company();
        BuildController buildController = new BuildController(world, company);

        prepareOpenTile(world, new GridPos(5, 4));
        prepareOpenTile(world, new GridPos(6, 4));
        prepareOpenTile(world, new GridPos(6, 5));

        placeRoad(world, new GridPos(5, 4));
        world.getRoadNetwork().rebuild(world.getMap());

        City city = new City(common.Id.genNew());
        Tile cityTile = world.getMap().getTile(new GridPos(6, 5));
        cityTile.setEntity(city);
        city.getOccupiedTiles().add(cityTile);

        // step 2: build stop on tile next to road and city
        ActionResult result = buildController.buildStop(new GridPos(6, 4));

        // step 3: stop should be attached to that city
        assertTrue(result.isSuccess());
        assertEquals("Build stop successfully", result.getMessage());
        Stop stop = world.getMap().getTile(new GridPos(6, 4)).getStop();
        assertEquals(city, stop.getServedPlace());
    }

    private GridPos findEmptyTileNextToRoad(World world) {
        // helper for finding one empty tile next to road
        for (int x = 0; x < world.getMap().getWidth(); x++) {
            for (int y = 0; y < world.getMap().getHeight(); y++) {
                GridPos pos = new GridPos(x, y);
                if (isEmptyGarageCandidate(world, pos) && hasAdjacentRoad(world, pos)) {
                    return pos;
                }
            }
        }
        throw new AssertionError("No empty tile adjacent to road found in test world");
    }

    private GridPos findBuildableRoadTile(World world) {
        // helper for finding one valid road tile
        for (int x = 0; x < world.getMap().getWidth(); x++) {
            for (int y = 0; y < world.getMap().getHeight(); y++) {
                GridPos pos = new GridPos(x, y);
                var tile = world.getMap().getTile(pos);
                if (tile.getTerrain().isPassable()
                        && tile.getEntity() == null
                        && tile.getRoadPiece() == null
                        && tile.getStop() == null
                        && hasAdjacentRoad(world, pos)) {
                    return pos;
                }
            }
        }
        throw new AssertionError("No valid empty tile found for road building");
    }

    private GridPos findBlockedRoadTile(World world) {
        // helper for finding one blocked road tile
        for (int x = 0; x < world.getMap().getWidth(); x++) {
            for (int y = 0; y < world.getMap().getHeight(); y++) {
                GridPos pos = new GridPos(x, y);
                Tile tile = world.getMap().getTile(pos);
                if (!tile.getTerrain().isPassable() || tile.getEntity() != null) {
                    return pos;
                }
            }
        }
        throw new AssertionError("No blocked tile found for road building");
    }

    private GridPos findEmptyTileNotAdjacentToRoad(World world) {
        // helper for finding one empty tile away from roads
        for (int x = 0; x < world.getMap().getWidth(); x++) {
            for (int y = 0; y < world.getMap().getHeight(); y++) {
                GridPos pos = new GridPos(x, y);
                if (isEmptyGarageCandidate(world, pos) && !hasAdjacentRoad(world, pos)) {
                    return pos;
                }
            }
        }
        throw new AssertionError("No empty tile away from roads found in test world");
    }

    private boolean isEmptyGarageCandidate(World world, GridPos pos) {
        // helper for checking if tile matches garage build conditions
        var tile = world.getMap().getTile(pos);
        return tile.getTerrain().isPassable()
                && tile.getEntity() == null
                && tile.getRoadPiece() == null
                && tile.getStop() == null
                && tile.getGarage() == null;
    }

    private boolean hasAdjacentRoad(World world, GridPos pos) {
        // helper for checking 4-neighbor road connection
        GridPos[] neighbors = {
                new GridPos(pos.x + 1, pos.y),
                new GridPos(pos.x - 1, pos.y),
                new GridPos(pos.x, pos.y + 1),
                new GridPos(pos.x, pos.y - 1)
        };
        for (GridPos neighbor : neighbors) {
            if (!world.getMap().inBounds(neighbor)) {
                continue;
            }
            if (world.getMap().getTile(neighbor).getRoadPiece() != null) {
                return true;
            }
        }
        return false;
    }

    private void prepareOpenTile(World world, GridPos pos) {
        // helper for making one tile empty first
        Tile tile = world.getMap().getTile(pos);
        tile.setTerrain(new Land());
        tile.setEntity(null);
        tile.setRoadPiece(null);
        tile.setStop(null);
        tile.setGarage(null);
    }

    private void placeRoad(World world, GridPos pos) {
        // helper for placing one simple road tile
        Tile tile = world.getMap().getTile(pos);
        model.RoadPiece road = new model.RoadPiece(model.RoadKind.ROAD, null);
        road.addTile(tile);
        tile.setRoadPiece(road);
    }
}
