/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import common.*;
import model.*;
import java.util.ArrayList;
import java.util.List;

public class BuildController {
    private final World world;
    private final Company company;

    public BuildController(World world, Company company) {
        this.world = world;
        this.company = company;
    }

    //--------- road build rules --------------------

    // for if user is allowed to build road
    // step 1: we wanna get the neighbors of the tile
    private List<Tile> getNeighbors(Tile tile) {
        List<Tile> list = new ArrayList<>();
        int x = tile.getPos().x;
        int y = tile.getPos().y;

        GridPos right = new GridPos(x+1, y);
        GridPos left = new GridPos(x-1, y);
        GridPos up = new GridPos(x, y+1);
        GridPos down = new GridPos(x, y-1);

        if (world.getMap().inBounds(right)) list.add(world.getMap().getTile(right));
        if (world.getMap().inBounds(left)) list.add(world.getMap().getTile(left));
        if (world.getMap().inBounds(up)) list.add(world.getMap().getTile(up));
        if (world.getMap().inBounds(down)) list.add(world.getMap().getTile(down));

        return list;
    }

    // step 2: do we aleady have existing roads on map?
    // if no, its fine to build at random place
    // if yes, we make sure new one connect to existing one to later road network easier to maintain
    private boolean hasAnyRoad() {
        for (int x = 0; x < world.getMap().getWidth(); ++x) {
            for (int y = 0; y < world.getMap().getHeight(); ++y) {
                Tile t = world.getMap().getTile(new GridPos(x, y));
                if (t.getRoadPiece() != null) return true;
            }
        }
        return false;
    }

    // step 3: can we build the road?
    // limitations: not on existing road/ infrastructure/ terrain limit(not on water) / first road is ok
    private boolean canPlaceRoad(Tile tile) {
        if (tile == null) return false;
        if (tile.getRoadPiece()!=null) return false;
        if(tile.getEntity()!=null) return false;
        if (!tile.getTerrain().isPassable()) return false;
        if(!hasAnyRoad()) return true;

        // If roads already exist, new road must connect to nearby infrastructure:
        // existing road OR stop tile.
        return hasAdjacentRoadOrStop(tile);
    }

    private boolean hasAdjacentRoadOrStop(Tile tile) {
        for (Tile n : getNeighbors(tile)) {
            if (n.getRoadPiece() != null || n.getStop() != null) {
                return true;
            }
        }
        return false;
    }

    // step 4: now its ok to build the road
    public ActionResult buildRoad(GridPos pos) {
        Tile tile = world.getMap().getTile(pos);

        if (tile == null) {
            return ActionResult.fail("Tile out of bounds");
        }

        if (!canPlaceRoad(tile))
            return ActionResult.fail("Cannot place road here");

        Money roadCost = getRoadBuildCost(tile);
        if (!company.getEconomy().spend(
                roadCost,
                TransactionType.ROAD_CONSTRUCTION,
                "Built road at " + pos))
            return ActionResult.fail("Not enough money");

        world.buildRoad(pos);
        return ActionResult.success("Build road successfully");
    }

    public ActionResult buildBridge(List<GridPos> line, BridgeType type) {
        if (line == null || line.isEmpty()) {
            return ActionResult.fail("Bridge line cannot be empty");
        }

        BridgeSpec spec;
        try {
            spec = world.getBridgeSpec(type);
        } catch (IllegalArgumentException ex) {
            return ActionResult.fail(ex.getMessage());
        }

        if (!hasRoadConnectionAtEitherEnd(line)) {
            return ActionResult.fail("Bridge must connect to an existing road at one end");
        }

        if (!company.getEconomy().spend(
                spec.getCost(),
                TransactionType.INFRASTRUCTURE,
                "Built bridge " + type)) {
            return ActionResult.fail("Not enough money");
        }

        try {
            world.buildBridge(line, type);
        } catch (IllegalArgumentException ex) {
            company.getEconomy().earn(
                    spec.getCost(),
                    TransactionType.INFRASTRUCTURE,
                    "Refund for failed bridge build"
            );
            return ActionResult.fail(ex.getMessage());
        }

        return ActionResult.success("Bridge built successfully");
    }

    public ActionResult removeRoad(GridPos pos) {
        Tile tile = world.getMap().getTile(pos);
        if (tile == null) {
            return ActionResult.fail("Tile out of bounds");
        }
        if (tile.getRoadPiece() == null) {
            return ActionResult.fail("No road to remove");
        }
        if (tile.getEntity() instanceof City city && city.hasInternalRoadAt(pos)) {
            return ActionResult.fail("City internal roads cannot be removed");
        }

        for (Vehicle v : company.getFleet()) {
            if (v.isUsingTile(pos)) {
                return ActionResult.fail("Cannot demolish: Road used in active route!");
            }
        }

        Money refund = getRoadBuildCost(tile);
        world.removeRoad(pos);
        company.getEconomy().earn(
                refund,
                TransactionType.ROAD_CONSTRUCTION,
                "Refund for deconstructed road at " + pos);
                
        return ActionResult.success("Deconstructed road successfully");
    }

    private Money getRoadBuildCost(Tile tile) {
        if (tile == null) {
            throw new IllegalArgumentException("tile cannot be null");
        }
        return World.ROAD_BUILD_COST.multiply(tile.getTerrain().buildMultiplier());
    }

    private boolean hasRoadConnectionAtEitherEnd(List<GridPos> line) {
        return hasAdjacentRoadOutsideLine(line.get(0), line)
                || hasAdjacentRoadOutsideLine(line.get(line.size() - 1), line);
    }

    private boolean hasAdjacentRoadOutsideLine(GridPos pos, List<GridPos> line) {
        Tile tile = world.getMap().getTile(pos);
        if (tile == null) {
            return false;
        }

        for (Tile neighbor : getNeighbors(tile)) {
            if (neighbor.getRoadPiece() == null) {
                continue;
            }
            if (!line.contains(neighbor.getPos())) {
                return true;
            }
        }
        return false;
    }

    //--------- Stop placement rules ----------------

    // step 1: is the tile empty?
    private boolean isTileEmptyForStop(Tile tile) {
        if (tile == null) return false;
        if (!tile.getTerrain().isPassable()) return false;
        if (tile.getEntity() != null) return false;
        if (tile.getRoadPiece() != null) return false;
        if (tile.getStop() != null) return false;
        if (tile.getGarage() != null) return false;
        return true;
    }

    // step 2: Stop should along road
    private boolean hasAdjacentRoad(Tile tile) {
        for (Tile n : getNeighbors(tile)) {
            if (n.getRoadPiece() != null) return true;
        }
        return false;
    }

    // step 3: Stop must be near a City or Facility (servedPlace)
    private MapEntity findNearbyServedPlace(Tile tile) {
        for (Tile n : getNeighbors(tile)) {
            MapEntity e = n.getEntity();
            if (e instanceof City || e instanceof Facility) {
                return e;
            }
        }
        return null;
    }


    // step 4: place the stop
    public ActionResult buildStop(GridPos pos) {
        Tile tile = world.getMap().getTile(pos);

        if (tile == null) {
            return ActionResult.fail("Tile out of bounds");
        }

        if (!isTileEmptyForStop(tile))
            return ActionResult.fail("Tile not empty");

        if (!hasAdjacentRoad(tile))
            return ActionResult.fail("Stop must be next to a road");

        MapEntity served = findNearbyServedPlace(tile);
        if (served == null)
            return ActionResult.fail("Stop must serve a city or facility");

        // Stop built logic in controller is cuz we need servedPlace
        Stop stop = new Stop(Id.genNew(), tile, served);
        tile.setStop(stop);
        served.attachStop(stop);

        return ActionResult.success("Build stop sucessfully");
    }

    // Garage building
    private boolean isTileEmptyForGarage(Tile tile) {
        if (tile == null) return false;
        return tile.getTerrain().isPassable()
                && tile.getEntity() == null
                && tile.getRoadPiece() == null
                && tile.getStop() == null
                && tile.getGarage() == null;
    }

    public ActionResult buildGarage(GridPos pos) {
        Tile tile = world.getMap().getTile(pos);

        if (tile == null) {
            return ActionResult.fail("Tile out of bounds");
        }

        if (!isTileEmptyForGarage(tile))
            return ActionResult.fail("Cannot place garage here");

        if (!hasAdjacentRoad(tile))
            return ActionResult.fail("Garage must be next to a road");

        if (!company.getEconomy().spend(
                World.GARAGE_BUILD_COST,
                TransactionType.INFRASTRUCTURE,
                "Built garage at " + pos))
            return ActionResult.fail("Not enough money");

        world.buildGarage(pos, 10, 2);  // capacity: 10 vehicles, 2 service bays
        return ActionResult.success("Garage built successfully");
    }

    /*
    // stop removal
    public void removeStop(Tile tile) {
        Stop s = tile.getStop();
        if (s == null) return;

        s.getServedPlace().detachStop(s);
        tile.setStop(null);
    }
    */
}
