package model;

import common.GridPos;
import common.Id;
import common.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SaveServiceTest {
    @TempDir
    Path saveDirectory;

    @Test
    void savedMovingVehicleShouldLoadAtSameProgressAndContinueMoving() throws Exception {
        World world = new World(25, 25);
        Company company = new Company();
        company.setWorld(world);

        List<GridPos> path = List.of(
                new GridPos(2, 4),
                new GridPos(3, 4),
                new GridPos(4, 4)
        );
        for (GridPos pos : path) {
            prepareOpenTile(world, pos);
            placeRoad(world, pos);
        }
        world.getRoadNetwork().rebuild(world.getMap());

        City cityA = new City(Id.genNew());
        City cityB = new City(Id.genNew());
        Stop stopA = new Stop(Id.genNew(), world.getMap().getTile(new GridPos(2, 4)), cityA);
        Stop stopB = new Stop(Id.genNew(), world.getMap().getTile(new GridPos(4, 4)), cityB);
        cityA.attachStop(stopA);
        cityB.attachStop(stopB);
        Route route = new Route(Id.genNew(), List.of(stopA, stopB));

        Truck truck = new Truck(new Id("truck-save-load"), 10, Money.of(100), Money.of(1), 1.0);
        truck.setOwner(company);
        truck.setWorld(world);
        truck.assignRoute(route);
        truck.setState(VehicleState.ON_ROUTE);
        setVehicleField(truck, "currentStopIndex", 0);
        setVehicleField(truck, "targetStopIndex", 1);
        setVehicleField(truck, "currentPath", path);
        setVehicleField(truck, "currentPathIndex", 0);
        setVehicleField(truck, "segmentProgress", 0.25);
        setVehicleField(truck, "tilePos", new GridPos(2, 4));
        company.getFleet().add(truck);

        SaveService saveService = new SaveService(saveDirectory);
        saveService.save(new Game(world, company), "moving vehicle");

        Game loadedGame = saveService.load("moving vehicle");
        Vehicle loadedTruck = loadedGame.getCompany().getFleet().get(0);

        assertEquals(VehicleState.ON_ROUTE, loadedTruck.getState());
        assertEquals(new GridPos(2, 4), loadedTruck.getTilePos());
        assertEquals(new GridPos(2, 4), loadedTruck.getCurrentPathTile());
        assertEquals(new GridPos(3, 4), loadedTruck.getNextPathTile());
        assertEquals(0.25, loadedTruck.getSegmentProgress(), 1e-9);

        loadedGame.update(0.25);

        assertEquals(new GridPos(2, 4), loadedTruck.getTilePos());
        assertTrue(loadedTruck.getSegmentProgress() > 0.25);
    }

    @Test
    void listSavesShouldReturnMultipleSavedGameNames() throws Exception {
        SaveService saveService = new SaveService(saveDirectory);

        saveService.save(new Game(new World(25, 25), new Company()), "first save");
        saveService.save(new Game(new World(25, 25), new Company()), "second-save");

        Files.setLastModifiedTime(saveDirectory.resolve("first_save.sav"), FileTime.fromMillis(1_000));
        Files.setLastModifiedTime(saveDirectory.resolve("second-save.sav"), FileTime.fromMillis(2_000));

        assertEquals(List.of("second-save", "first_save"), saveService.listSaves());
    }

    @Test
    void savedGameShouldLoadWithElapsedTime() throws Exception {
        Game game = new Game(new World(25, 25), new Company());
        game.update(65.0);

        SaveService saveService = new SaveService(saveDirectory);
        saveService.save(game, "time");

        Game loadedGame = saveService.load("time");

        assertEquals(65.0, loadedGame.getElapsedTimeSeconds(), 1e-9);
        assertEquals("00:01:05", loadedGame.getFormattedTime());
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
        RoadPiece road = new RoadPiece(RoadKind.ROAD, null);
        road.addTile(tile);
        tile.setRoadPiece(road);
    }

    private void setVehicleField(Vehicle vehicle, String fieldName, Object value) throws Exception {
        Field field = Vehicle.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(vehicle, value);
    }
}
