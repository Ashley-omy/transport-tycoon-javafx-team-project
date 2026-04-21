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
    void buildGarageShouldSucceedOnEmptyTileNextToRoad() {
        World world = new World(25, 25);
        Company company = new Company();
        BuildController buildController = new BuildController(world, company);
        // Do not depend on hard-coded coordinates because world initialization changes often.
        // Instead, find a valid candidate tile from the current map state.
        GridPos garagePos = findEmptyTileNextToRoad(world);

        Money cashBefore = company.getEconomy().getCash();

        ActionResult result = buildController.buildGarage(garagePos);

        assertTrue(result.isSuccess());
        assertEquals("Garage built successfully", result.getMessage());
        assertTrue(world.getMap().getTile(garagePos).hasGarage());
        assertEquals(cashBefore.subtract(World.GARAGE_BUILD_COST), company.getEconomy().getCash());
    }

    @Test
    void buildGarageShouldFailWhenTileIsNotAdjacentToRoad() {
        World world = new World(25, 25);
        Company company = new Company();
        BuildController buildController = new BuildController(world, company);
        // Pick a tile that is buildable in general but intentionally not connected to roads.
        GridPos garagePos = findEmptyTileNotAdjacentToRoad(world);

        ActionResult result = buildController.buildGarage(garagePos);

        assertFalse(result.isSuccess());
        assertNull(world.getMap().getTile(garagePos).getGarage());
    }

    @Test
    void buildGarageShouldFailWhenPlayerCannotAffordIt() {
        World world = new World(25, 25);
        Company company = new Company(Money.of(1_000));
        BuildController buildController = new BuildController(world, company);
        // Reuse the same placement precondition as the success case and isolate only "money" failure.
        GridPos garagePos = findEmptyTileNextToRoad(world);

        ActionResult result = buildController.buildGarage(garagePos);

        assertFalse(result.isSuccess());
        assertEquals("Not enough money", result.getMessage());
        assertNull(world.getMap().getTile(garagePos).getGarage());
        assertEquals(Money.of(1_000), company.getEconomy().getCash());
    }

    @Test
    void buildBridgeShouldSpendMoneyAndPlaceBridgeRoadPiece() {
        World world = new World(25, 25);
        Company company = new Company();
        BuildController buildController = new BuildController(world, company);
        GridPos roadAnchor = new GridPos(2, 0);
        GridPos bridgeTile = new GridPos(3, 0);

        world.buildRoad(roadAnchor);
        world.getMap().setTerrain(bridgeTile, new Water(WaterType.RIVER));

        Money cashBefore = company.getEconomy().getCash();
        Money expectedCost = world.getBridgeSpec(BridgeType.TYPE_A).getCost();

        ActionResult result = buildController.buildBridge(List.of(bridgeTile), BridgeType.TYPE_A);

        assertTrue(result.isSuccess());
        assertEquals("Bridge built successfully", result.getMessage());
        assertEquals(cashBefore.subtract(expectedCost), company.getEconomy().getCash());
        assertEquals(BridgeType.TYPE_A, world.getMap().getTile(bridgeTile).getRoadPiece().getBridgeSpec().getType());
    }

    @Test
    void buildBridgeShouldFailWithoutAdjacentRoadConnection() {
        World world = new World(25, 25);
        Company company = new Company();
        BuildController buildController = new BuildController(world, company);
        GridPos bridgeTile = new GridPos(10, 10);

        world.getMap().setTerrain(bridgeTile, new Water(WaterType.RIVER));

        ActionResult result = buildController.buildBridge(List.of(bridgeTile), BridgeType.TYPE_A);

        assertFalse(result.isSuccess());
        assertEquals("Bridge must connect to an existing road at one end", result.getMessage());
        assertNull(world.getMap().getTile(bridgeTile).getRoadPiece());
    }

    @Test
    void buildStopShouldAttachToAdjacentCity() {
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

        ActionResult result = buildController.buildStop(new GridPos(6, 4));

        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().startsWith("Build stop successfully for City id="));
        assertTrue(result.getMessage().endsWith(" stops=1"));
        Stop stop = world.getMap().getTile(new GridPos(6, 4)).getStop();
        assertEquals(city, stop.getServedPlace());
    }

    private GridPos findEmptyTileNextToRoad(World world) {
        // Find a tile that satisfies garage tile constraints and is road-adjacent.
        // This keeps the test stable even if map layout changes.
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

    private GridPos findEmptyTileNotAdjacentToRoad(World world) {
        // Find a tile that is otherwise valid for garage placement but has no road neighbor.
        // Used to verify the "must be next to a road" validation path.
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
        // Mirrors BuildController#isTileEmptyForGarage conditions so test expectations are explicit.
        var tile = world.getMap().getTile(pos);
        return tile.getTerrain().isPassable()
                && tile.getEntity() == null
                && tile.getRoadPiece() == null
                && tile.getStop() == null
                && tile.getGarage() == null;
    }

    private boolean hasAdjacentRoad(World world, GridPos pos) {
        // 4-neighbor check, same connectivity model used by production build logic.
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
        Tile tile = world.getMap().getTile(pos);
        tile.setTerrain(new Land());
        tile.setEntity(null);
        tile.setRoadPiece(null);
        tile.setStop(null);
        tile.setGarage(null);
    }

    private void placeRoad(World world, GridPos pos) {
        Tile tile = world.getMap().getTile(pos);
        model.RoadPiece road = new model.RoadPiece(model.RoadKind.ROAD, null);
        road.addTile(tile);
        tile.setRoadPiece(road);
    }
}
