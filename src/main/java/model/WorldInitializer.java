package model;

import common.GridPos;
import common.Id;

/**
 * @author asuna
 * Responsible only for initial world population:
 * terrain accents, forests, water, fixed cities/facilities, and starter road graph setup.
 */
final class WorldInitializer {
    private final World world;
    private final GameMap map;
    private final int width;
    private final int height;

    private WorldInitializer(World world) {
        this.world = world;
        this.map = world.getMap();
        this.width = map.getWidth();
        this.height = map.getHeight();
    }

    static void initialize(World world) {
        new WorldInitializer(world).populate();
    }

    static void initializeMapWater(GameMap map) {
        if (map == null) {
            return;
        }

        int width = map.getWidth();
        int height = map.getHeight();

        initializeVerticalCurvedRiver(map, width, height);

        int centerX = clamp(width - 6, 0, width - 1);
        int centerY = clamp(height / 2, 0, height - 1);
        placeEllipseLake(map, centerX, centerY, 2, 4);
    }

    private void populate() {
        placeSecondaryLake();
        placeInitialForests();
        placeInitialCities();
        placeInitialFacilitiesAndMines();
        //placeStarterRoad();
        world.getRoadNetwork().rebuild(map);
    }

    private void placeSecondaryLake() {
        GridPos lakeCenter = new GridPos(
                clamp(width / 4, 3, Math.max(3, width - 4)),
                clamp((height * 3) / 4, 3, Math.max(3, height - 4))
        );
        placeEllipseLake(lakeCenter, 6, 3);
    }

    private void placeInitialForests() {
        placeForestSeedIfLand(new GridPos(clamp(width / 6, 2, width - 3), clamp(height / 2, 2, height - 3)));
        placeForestSeedIfLand(new GridPos(clamp(width / 2, 2, width - 3), clamp((height * 4) / 5, 2, height - 3)));
        placeForestSeedIfLand(new GridPos(clamp((width * 5) / 6, 2, width - 3), clamp(height / 5, 2, height - 3)));
        placeForestSeedIfLand(new GridPos(clamp((width * 2) / 5, 2, width - 3), clamp((height * 2) / 5, 2, height - 3)));
        placeForestSeedIfLand(new GridPos(clamp((width * 4) / 5, 2, width - 3), clamp((height * 3) / 5, 2, height - 3)));
    }

    private void placeInitialCities() {
        world.placeEntity(new City(Id.genNew()), new GridPos(clamp(width / 6, 2, width - 3), clamp(height / 6, 2, height - 3)));
        world.placeEntity(new City(Id.genNew()), new GridPos(clamp((width * 2) / 3, 2, width - 3), clamp(height / 6, 2, height - 3)));
        world.placeEntity(new City(Id.genNew()), new GridPos(clamp((width * 5) / 6, 2, width - 3), clamp(height / 2, 2, height - 3)));
        world.placeEntity(new City(Id.genNew()), new GridPos(clamp(width / 3, 2, width - 3), clamp((height * 5) / 6, 2, height - 3)));
    }

    private void placeInitialFacilitiesAndMines() {
        // Factories
        world.placeEntity(Factory.createSteelMill(Id.genNew()), new GridPos(clamp(width / 5, 1, width - 2), clamp((height * 2) / 3, 1, height - 2)));
        world.placeEntity(Factory.createPaperMill(Id.genNew()), new GridPos(clamp((width * 3) / 5, 1, width - 2), clamp((height * 2) / 3, 1, height - 2)));
        world.placeEntity(Factory.createSteelMill(Id.genNew()), new GridPos(clamp(width / 4, 1, width - 2), clamp(height / 3, 1, height - 2)));
        world.placeEntity(Factory.createPaperMill(Id.genNew()), new GridPos(clamp((width * 7) / 8, 1, width - 2), clamp((height * 3) / 4, 1, height - 2)));
        world.placeEntity(Factory.createSteelMill(Id.genNew()), new GridPos(clamp(2, 1, width - 2), clamp(height / 4, 1, height - 2)));
        world.placeEntity(Factory.createPaperMill(Id.genNew()), new GridPos(clamp(width - 2, 1, width - 2), clamp(height / 5, 1, height - 2)));
        world.placeEntity(Factory.createSteelMill(Id.genNew()), new GridPos(clamp((width / 2) + 2, 1, width - 2), clamp(height - 2, 1, height - 2)));
        world.placeEntity(Factory.createPaperMill(Id.genNew()), new GridPos(clamp(width / 6, 1, width - 2), clamp(height - 2, 1, height - 2)));
        // +2 factories
        world.placeEntity(Factory.createSteelMill(Id.genNew()), new GridPos(clamp(width / 10, 1, width - 2), clamp(height / 8, 1, height - 2)));
        world.placeEntity(Factory.createPaperMill(Id.genNew()), new GridPos(clamp((width * 9) / 10, 1, width - 2), clamp((height * 7) / 8, 1, height - 2)));

        // Mines
        world.placeEntity(Mine.createIronMine(Id.genNew()), new GridPos(clamp((width * 4) / 5, 1, width - 2), clamp(height / 4, 1, height - 2)));
        world.placeEntity(Mine.createWoodMine(Id.genNew()), new GridPos(clamp((width * 3) / 4, 1, width - 2), clamp((height * 4) / 5, 1, height - 2)));
        world.placeEntity(Mine.createIronMine(Id.genNew()), new GridPos(clamp(width / 8, 1, width - 2), clamp((height * 3) / 5, 1, height - 2)));
        world.placeEntity(Mine.createWoodMine(Id.genNew()), new GridPos(clamp((width * 7) / 12, 1, width - 2), clamp(height / 3, 1, height - 2)));
        world.placeEntity(Mine.createIronMine(Id.genNew()), new GridPos(clamp(2, 1, width - 2), clamp((height * 4) / 5, 1, height - 2)));
        world.placeEntity(Mine.createWoodMine(Id.genNew()), new GridPos(clamp(width - 2, 1, width - 2), clamp(height / 2, 1, height - 2)));
        world.placeEntity(Mine.createIronMine(Id.genNew()), new GridPos(clamp((width * 2) / 3, 1, width - 2), clamp(2, 1, height - 2)));
        world.placeEntity(Mine.createWoodMine(Id.genNew()), new GridPos(clamp(width - 2, 1, width - 2), clamp(height - 2, 1, height - 2)));
        // +2 mines
        world.placeEntity(Mine.createIronMine(Id.genNew()), new GridPos(clamp(width / 3, 1, width - 2), clamp(height / 10, 1, height - 2)));
        world.placeEntity(Mine.createWoodMine(Id.genNew()), new GridPos(clamp(width / 10, 1, width - 2), clamp((height * 9) / 10, 1, height - 2)));
    }

//    private void placeStarterRoad() {
//        world.buildRoad(new GridPos(clamp((width / 6) + 2, 0, width - 1), clamp(height / 6, 0, height - 1)));
//    }

    //Preventing entity's position from being placed on edges
    private static int clamp(int value, int min, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    // Vertical river: progresses along Y and shifts left/right on X to look natural.
    private static void initializeVerticalCurvedRiver(GameMap map, int width, int height) {
        int baseX = Math.max(2, width / 2);
        int amplitude = Math.max(1, width / 20);
        int period = Math.max(14, height / 3);
        int previousX = baseX;

        // Draw river across the full Y range so both map ends are covered.
        for (int y = 0; y < height; y++) {
            double angle = (2.0 * Math.PI * y) / period;
            int x = clamp((int) Math.round(baseX + (Math.sin(angle) * amplitude)), 1, width - 2);

            // Fill horizontal steps between rows so the river stays connected.
            int minX = Math.min(previousX, x);
            int maxX = Math.max(previousX, x);
            for (int xx = minX; xx <= maxX; xx++) {
                setTerrainIfInBounds(map, new GridPos(xx, y), new Water(WaterType.RIVER));
                setTerrainIfInBounds(map, new GridPos(xx, clamp(y + 1, 0, height - 1)), new Water(WaterType.RIVER));
            }
            previousX = x;
        }
    }

    // Fill lake tiles inside an ellipse: ((x-cx)^2/rx^2) + ((y-cy)^2/ry^2) <= 1.
    private static void placeEllipseLake(GameMap map, int centerX, int centerY, int radiusX, int radiusY) {
        if (radiusX <= 0 || radiusY <= 0) {
            return;
        }
        for (int x = centerX - radiusX; x <= centerX + radiusX; x++) {
            for (int y = centerY - radiusY; y <= centerY + radiusY; y++) {
                double nx = (double) (x - centerX) / radiusX;
                double ny = (double) (y - centerY) / radiusY;
                if ((nx * nx) + (ny * ny) <= 1.0) {
                    setTerrainIfInBounds(map, new GridPos(x, y), new Water(WaterType.LAKE));
                }
            }
        }
    }

    private static void setTerrainIfInBounds(GameMap map, GridPos pos, Terrain terrain) {
        if (map.inBounds(pos)) {
            map.setTerrain(pos, terrain);
        }
    }

    private void placeEllipseLake(GridPos center, int radiusX, int radiusY) {
        if (center == null || radiusX <= 0 || radiusY <= 0) {
            return;
        }
        for (int x = center.x - radiusX; x <= center.x + radiusX; x++) {
            for (int y = center.y - radiusY; y <= center.y + radiusY; y++) {
                double nx = (double) (x - center.x) / radiusX;
                double ny = (double) (y - center.y) / radiusY;
                if ((nx * nx) + (ny * ny) > 1.0) {
                    continue;
                }
                GridPos p = new GridPos(x, y);
                if (map.inBounds(p)) {
                    map.setTerrain(p, new Water(WaterType.LAKE));
                }
            }
        }
    }

    private void placeForestSeedIfLand(GridPos pos) {
        if (pos == null || !map.inBounds(pos)) {
            return;
        }
        Tile tile = map.getTile(pos);
        if (tile == null || !tile.getTerrain().isLand()) {
            return;
        }
        map.setTerrain(pos, new Forest());
    }
}
