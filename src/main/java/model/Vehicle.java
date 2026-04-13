package model;

import common.GridPos;
import common.Id;
import common.Money;
import common.Vec2;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public abstract class Vehicle {
    private static final double STOP_DURATION_SECONDS = 1.5;
    private static final double EPSILON = 1e-9;
    private static final double BASE_MAINTENANCE_INTERVAL = 300.0; // 5 minutes for new vehicles
    private static final double MAINTENANCE_DURATION = 5.0; // 5 seconds in garage
    private static final double MIN_MAINTENANCE_INTERVAL = 120.0; // 2 minutes minimum
    private static final double AGE_FOR_MIN_INTERVAL = 1200.0; // 20 minutes to reach minimum

    protected final Id id;
    protected final int capacityUnits;
    protected final Money purchaseCost;
    protected final Money maintenanceCost;
    protected final double speed;
    protected Shipment cargo;
    protected Company owner;
    protected VehicleState state;
    protected Route assignedRoute;
    protected World world;
    private GridPos tilePos;
    private Vec2 worldPos;
    private int currentStopIndex = -1;
    private int targetStopIndex = -1;
    private double stopTimerRemaining = 0.0;
    private List<GridPos> currentPath = List.of();
    private int currentPathIndex = 0;

    // Maintenance tracking
    private double age = 0.0; // Total time vehicle has existed
    private double timeSinceLastMaintenance = 0.0;
    private double maintenanceTimer = 0.0; // Time spent in garage
    private Garage homeGarage = null;
    private boolean returningToGarage = false;
    private VehicleState savedStateBeforeMaintenance = VehicleState.IDLE;
    private int savedCurrentStopIndex = -1;
    private int savedTargetStopIndex = -1;
    private double savedStopTimerRemaining = 0.0;
    private List<GridPos> savedCurrentPath = List.of();
    private int savedCurrentPathIndex = 0;
    private GridPos savedTilePos;
    private Vec2 savedWorldPos;

    protected Vehicle(Id id, int capacityUnits, Money purchaseCost, Money maintenanceCost, double speed) {
        if (id == null) throw new IllegalArgumentException("id cannot be null");
        if (capacityUnits <= 0) throw new IllegalArgumentException("capacityUnits must be > 0");
        if (purchaseCost == null) throw new IllegalArgumentException("purchaseCost cannot be null");
        if (maintenanceCost == null) throw new IllegalArgumentException("maintenanceCost cannot be null");
        if (speed <= 0) throw new IllegalArgumentException("speed must be > 0");
        this.id = id;
        this.capacityUnits = capacityUnits;
        this.purchaseCost = purchaseCost;
        this.maintenanceCost = maintenanceCost;
        this.speed = speed;
        this.state = VehicleState.IDLE;
    }

    public Id getId() { return id; }
    public Money getPurchaseCost() { return purchaseCost; }
    public Money getMaintenanceCost() { return maintenanceCost; }
    public double getSpeed() { return speed; }
    public double getAge() { return age; }

    public void setOwner(Company owner) { this.owner = owner; }

    public void setWorld(World world) { this.world = world; }

    public void setHomeGarage(Garage garage) { this.homeGarage = garage; }

    public Garage getHomeGarage() { return homeGarage; }

    // Calculate maintenance interval. older vehicles need more frequent maintenance
    private double getMaintenanceInterval() {
        // Gradually reduce interval from 300s to 120s over 20 minutes of age
        // reduction = (age / 1200) * 180
        double reduction = (age / AGE_FOR_MIN_INTERVAL) * (BASE_MAINTENANCE_INTERVAL - MIN_MAINTENANCE_INTERVAL);
        return Math.max(MIN_MAINTENANCE_INTERVAL, BASE_MAINTENANCE_INTERVAL - reduction);
    }

    public boolean needsMaintenance() {
        return homeGarage != null && timeSinceLastMaintenance >= getMaintenanceInterval();
    }

    // Temporary debug access so stops can publish transport event messages.
    public World getWorld() { return world; }

    // Route assignment methods
    public void assignRoute(Route route) {
        if (route == null) throw new IllegalArgumentException("route cannot be null");
        this.assignedRoute = route;
        this.currentStopIndex = -1;
        this.targetStopIndex = -1;
        this.stopTimerRemaining = 0.0;
        this.currentPath = List.of();
        this.currentPathIndex = 0;
        this.tilePos = null;
        this.worldPos = null;
    }

    public Route getAssignedRoute() {
        return assignedRoute;
    }

    public boolean hasRoute() {
        return assignedRoute != null;
    }

    public void clearRoute() {
        this.assignedRoute = null;
        this.state = VehicleState.IDLE;
        this.currentStopIndex = -1;
        this.targetStopIndex = -1;
        this.stopTimerRemaining = 0.0;
        this.currentPath = List.of();
        this.currentPathIndex = 0;
        this.tilePos = null;
        this.worldPos = null;
    }

    // Cargo storage methods
    public Shipment getCargo() { return cargo; }

    public void clearCargo() { this.cargo = null; }

    public int getFreeCapacityUnits() {
        return cargo == null ? capacityUnits : (capacityUnits - cargo.getUnits());
    }

    public boolean hasCargo() {
        return cargo != null && cargo.getUnits() > 0;
    }

    public VehicleState getState() {
        return state;
    }

    public void setState(VehicleState state) {
        if (state == null) throw new IllegalArgumentException("state cannot be null");
        this.state = state;
    }

    public GridPos getTilePos() {
        return tilePos;
    }

    public Vec2 getWorldPos() {
        return worldPos;
    }

    //for deconstruct
    public boolean isUsingTile(GridPos pos) {
        if (pos == null) return false;
        if (tilePos != null && tilePos.equals(pos)) return true;
        return currentPath != null && currentPath.contains(pos);
    }

    public boolean canLoad(Shipment s) {
        if (s == null) return false;
        if (getFreeCapacityUnits() <= 0) return false;

        if (!acceptsKind(s.getKind())) return false;
        if (s.isGoods() && !acceptsGoodsType(s.getGoodsType())) return false;

        if (cargo == null) return true;

        // if already carrying cargo, must be same kind
        if (cargo.getKind() != s.getKind()) return false;
        if (cargo.isGoods() && cargo.getGoodsType() != s.getGoodsType()) return false;

        return true;
    }

    public boolean loadFrom(Stop stop) {
        if (stop == null) return false;

        Shipment loaded = stop.dequeueFor(this);
        if (loaded == null) return false;
        Shipment routedShipment = routeShipmentToNextStop(loaded);

        if (cargo == null) {
            cargo = routedShipment;
        } else {
            // merge units into existing cargo
            int mergedUnits = cargo.getUnits() + routedShipment.getUnits();
            cargo = new Shipment(
                    cargo.getKind(),
                    cargo.getGoodsType(),
                    mergedUnits,
                    cargo.getFromStopId(),
                    routedShipment.getToStopId(),
                    cargo.getValuePerTile()
            );
        }
        if (world != null) {
            // Temporary debug message for verifying load completion.
            world.pushDebugMessage(
                    "Load complete: " + id
                            + " <- " + describeEntity(stop.getServedPlace())
                            + " / Shipment[kind=" + routedShipment.getKind()
                            + ", units=" + routedShipment.getUnits() + "]"
            );
        }
        return true;
    }

    public Money unloadTo(Stop stop) {
        if (stop == null) return Money.ZERO;
        Money payout = stop.deliverFrom(this);

        // Pay the company for successful delivery
        if (owner != null && payout.isPositive()) {
            owner.completeShipmentWithPayout(payout);
            if (world != null) {
                // Temporary debug message for verifying delivery revenue.
                world.pushRevenueMessage("Revenue earned: +" + payout + " at " + describeEntity(stop.getServedPlace()));
            }
        }

        return payout;
    }

    public void tick(double deltaTime) {
        if (Double.isNaN(deltaTime) || Double.isInfinite(deltaTime) || deltaTime <= 0.0) return;

        // Track vehicle age
        age += deltaTime;
        timeSinceLastMaintenance += deltaTime;

        // Handle maintenance in garage
        if (state == VehicleState.IN_GARAGE) {
            maintenanceTimer += deltaTime;
            if (maintenanceTimer >= MAINTENANCE_DURATION) {
                completeMaintenance();
            }
            return;
        }

        // Check if maintenance is needed
        if (needsMaintenance() && state != VehicleState.BLOCKED) {
            if (!startReturnToGarage()) {
                state = VehicleState.BLOCKED;
            }
            return;
        }

        if (!hasRoute() || world == null || assignedRoute.getStopCount() < 2) return;

        // Initialize the vehicle on its first assigned stop before movement begins.
        ensureInitializedOnRoute();

        double remainingTime = deltaTime;
        while (remainingTime > EPSILON) {
            // Stop time is reserved for unloading and loading at the current stop.
            if (stopTimerRemaining > EPSILON) {
                state = VehicleState.LOADING;
                double waited = Math.min(stopTimerRemaining, remainingTime);
                stopTimerRemaining -= waited;
                remainingTime -= waited;

                if (stopTimerRemaining <= EPSILON) {
                    prepareNextLeg();
                }
                continue;
            }

            if (state == VehicleState.BLOCKED) {
                return;
            }

            if (currentPath.isEmpty() || currentPathIndex >= currentPath.size() - 1) {
                prepareNextLeg();
                if (state == VehicleState.BLOCKED) {
                    return;
                }
                continue;
            }

            remainingTime = moveAlongCurrentPath(remainingTime);
        }
    }

    private void ensureInitializedOnRoute() {
        if (currentStopIndex >= 0 && tilePos != null && worldPos != null) {
            return;
        }

        // Start from the first stop in the route and perform the first stop actions there.
        currentStopIndex = 0;
        Stop firstStop = assignedRoute.getStop(currentStopIndex);
        tilePos = firstStop.getOccupiedTile().getPos();
        worldPos = toWorldPos(tilePos);
        unloadTo(firstStop);
        loadFrom(firstStop);
        stopTimerRemaining = STOP_DURATION_SECONDS;
        state = VehicleState.LOADING;
    }

    private void prepareNextLeg() {
        if (assignedRoute == null || assignedRoute.getStopCount() < 2) {
            state = VehicleState.IDLE;
            return;
        }

        // Rebuild the road path from the current stop to the next stop in the route loop.
        targetStopIndex = assignedRoute.getNextStopIndex(currentStopIndex);

        if (cargo != null) {
            for (int i = 0; i < assignedRoute.getStopCount(); i++) {
                if (assignedRoute.getStop(i).getId().equals(cargo.getToStopId())) {
                    targetStopIndex = i;
                    break;
                }
            }
        }

        Stop currentStop = assignedRoute.getStop(currentStopIndex);
        Stop targetStop = assignedRoute.getStop(targetStopIndex);
        currentPath = buildPathBetweenStops(currentStop, targetStop);
        currentPathIndex = 0;

        if (currentPath.size() < 2) {
            state = VehicleState.BLOCKED;
            return;
        }

        tilePos = currentPath.get(0);
        worldPos = toWorldPos(tilePos);
        state = VehicleState.ON_ROUTE;
    }

    private double moveAlongCurrentPath(double remainingTime) {
        // Convert the frame time into travel distance and consume it along the path.
        double remainingDistance = speed * remainingTime;

        while (remainingDistance > EPSILON && currentPathIndex < currentPath.size() - 1) {
            GridPos from = currentPath.get(currentPathIndex);
            GridPos to = currentPath.get(currentPathIndex + 1);
            Vec2 fromPos = toWorldPos(from);
            Vec2 toPos = toWorldPos(to);

            if (worldPos == null) {
                worldPos = fromPos;
            }

            double distanceToSegmentEnd = distance(worldPos, toPos);

            if (distanceToSegmentEnd <= EPSILON) {
                currentPathIndex++;
                tilePos = to;
                worldPos = toPos;
                continue;
            }

            if (remainingDistance + EPSILON >= distanceToSegmentEnd) {
                remainingDistance -= distanceToSegmentEnd;
                currentPathIndex++;
                tilePos = to;
                worldPos = toPos;

                if (currentPathIndex >= currentPath.size() - 1) {
                    if (returningToGarage) {
                        arriveAtGarage();
                    } else {
                        arriveAtStop();
                    }
                    break;
                }
            } else {
                double ratio = remainingDistance / distanceToSegmentEnd;
                worldPos = Vec2.lerp(worldPos, toPos, ratio);
                remainingDistance = 0.0;
            }
        }

        return remainingDistance / speed;
    }

    private void arriveAtStop() {
        // Snap to the stop, then switch back into the stop handling phase.
        Stop stop = assignedRoute.getStop(targetStopIndex);
        currentStopIndex = targetStopIndex;
        tilePos = stop.getOccupiedTile().getPos();
        worldPos = toWorldPos(tilePos);
        currentPath = List.of();
        currentPathIndex = 0;
        unloadTo(stop);
        loadFrom(stop);
        stopTimerRemaining = STOP_DURATION_SECONDS;
        state = VehicleState.LOADING;
    }

    private List<GridPos> buildPathBetweenStops(Stop fromStop, Stop toStop) {
        return buildPathBetweenLocations(
                fromStop.getOccupiedTile().getPos(),
                toStop.getOccupiedTile().getPos()
        );
    }

    private List<GridPos> getRoadAccessTiles(GridPos pos) {
        // These road tiles are the possible entry and exit points for stops, garages, and roads.
        List<GridPos> roadTiles = new ArrayList<>();
        Tile tile = world.getMap().getTile(pos);
        if (tile != null && tile.getRoadPiece() != null) {
            roadTiles.add(pos);
        }
        for (GridPos neighbor : getFourNeighbors(pos)) {
            if (!world.getMap().inBounds(neighbor)) {
                continue;
            }

            tile = world.getMap().getTile(neighbor);
            if (tile.getRoadPiece() != null && !roadTiles.contains(neighbor)) {
                roadTiles.add(neighbor);
            }
        }
        return roadTiles;
    }

    private List<GridPos> buildPathBetweenLocations(GridPos fromPos, GridPos toPos) {
        if (fromPos == null || toPos == null || world == null) {
            return List.of();
        }

        List<GridPos> startRoadTiles = getRoadAccessTiles(fromPos);
        List<GridPos> endRoadTiles = getRoadAccessTiles(toPos);

        List<GridPos> bestRoadPath = List.of();
        for (GridPos startRoad : startRoadTiles) {
            for (GridPos endRoad : endRoadTiles) {
                List<GridPos> roadPath = findRoadPath(startRoad, endRoad);
                if (roadPath.isEmpty()) {
                    continue;
                }
                if (bestRoadPath.isEmpty() || roadPath.size() < bestRoadPath.size()) {
                    bestRoadPath = roadPath;
                }
            }
        }

        if (bestRoadPath.isEmpty()) {
            return List.of();
        }

        List<GridPos> fullPath = new ArrayList<>();
        fullPath.add(fromPos);
        appendIfDifferent(fullPath, bestRoadPath.get(0));
        for (int i = 1; i < bestRoadPath.size(); i++) {
            appendIfDifferent(fullPath, bestRoadPath.get(i));
        }
        appendIfDifferent(fullPath, toPos);
        return fullPath;
    }

    private boolean startReturnToGarage() {
        if (homeGarage == null || world == null || homeGarage.getOccupiedTiles().isEmpty()) {
            return false;
        }

        if (tilePos == null && hasRoute()) {
            ensureInitializedOnRoute();
        }

        GridPos garagePos = homeGarage.getOccupiedTiles().get(0).getPos();
        if (tilePos == null) {
            tilePos = garagePos;
            worldPos = toWorldPos(tilePos);
            returningToGarage = false;
            state = VehicleState.IN_GARAGE;
            maintenanceTimer = 0.0;
            return true;
        }

        saveProgressForMaintenance();
        List<GridPos> pathToGarage = buildPathBetweenLocations(tilePos, garagePos);
        if (pathToGarage.size() < 2) {
            clearSavedMaintenanceProgress();
            return false;
        }

        returningToGarage = true;
        stopTimerRemaining = 0.0;
        currentPath = pathToGarage;
        currentPathIndex = 0;
        targetStopIndex = -1;
        state = VehicleState.ON_ROUTE;
        if (world != null) {
            world.pushDebugMessage("Vehicle " + id + " returning to garage (age: " +
                    String.format("%.0f", age) + "s, interval: " +
                    String.format("%.0f", getMaintenanceInterval()) + "s)");
        }
        return true;
    }

    private void arriveAtGarage() {
        returningToGarage = false;
        currentPath = List.of();
        currentPathIndex = 0;
        stopTimerRemaining = 0.0;
        maintenanceTimer = 0.0;
        state = VehicleState.IN_GARAGE;
        if (world != null) {
            world.pushDebugMessage("Vehicle " + id + " arrived at garage");
        }
    }

    private void completeMaintenance() {
        if (owner != null) {
            owner.performVehicleMaintenance(this);
        }
        timeSinceLastMaintenance = 0.0;
        maintenanceTimer = 0.0;
        restoreProgressAfterMaintenance();
        if (world != null) {
            world.pushDebugMessage("Maintenance complete: " + id);
        }
    }

    private void saveProgressForMaintenance() {
        savedStateBeforeMaintenance = state;
        savedCurrentStopIndex = currentStopIndex;
        savedTargetStopIndex = targetStopIndex;
        savedStopTimerRemaining = stopTimerRemaining;
        savedCurrentPath = currentPath == null ? List.of() : new ArrayList<>(currentPath);
        savedCurrentPathIndex = currentPathIndex;
        savedTilePos = tilePos;
        savedWorldPos = worldPos;
    }

    private void restoreProgressAfterMaintenance() {
        currentStopIndex = savedCurrentStopIndex;
        targetStopIndex = savedTargetStopIndex;
        stopTimerRemaining = savedStopTimerRemaining;
        currentPath = savedCurrentPath.isEmpty() ? List.of() : new ArrayList<>(savedCurrentPath);
        currentPathIndex = savedCurrentPathIndex;
        tilePos = savedTilePos;
        worldPos = savedWorldPos;
        state = hasRoute() ? savedStateBeforeMaintenance : VehicleState.IDLE;
        returningToGarage = false;
        clearSavedMaintenanceProgress();
    }

    private void clearSavedMaintenanceProgress() {
        savedStateBeforeMaintenance = VehicleState.IDLE;
        savedCurrentStopIndex = -1;
        savedTargetStopIndex = -1;
        savedStopTimerRemaining = 0.0;
        savedCurrentPath = List.of();
        savedCurrentPathIndex = 0;
        savedTilePos = null;
        savedWorldPos = null;
    }

    private List<GridPos> findRoadPath(GridPos start, GridPos target) {
        if (start.equals(target)) {
            return List.of(start);
        }

        // A simple BFS is enough because every road step has the same cost.
        Queue<GridPos> queue = new ArrayDeque<>();
        Map<GridPos, GridPos> previous = new HashMap<>();
        queue.add(start);
        previous.put(start, null);

        while (!queue.isEmpty()) {
            GridPos current = queue.poll();
            if (current.equals(target)) {
                return reconstructPath(previous, target);
            }

            for (GridPos neighbor : getFourNeighbors(current)) {
                if (!world.getMap().inBounds(neighbor) || previous.containsKey(neighbor)) {
                    continue;
                }

                Tile neighborTile = world.getMap().getTile(neighbor);
                if (neighborTile.getRoadPiece() == null) {
                    continue;
                }

                previous.put(neighbor, current);
                queue.add(neighbor);
            }
        }

        return List.of();
    }

    private List<GridPos> reconstructPath(Map<GridPos, GridPos> previous, GridPos target) {
        // Rebuild the final path by walking backwards from the target.
        List<GridPos> path = new ArrayList<>();
        GridPos current = target;

        while (current != null) {
            path.add(0, current);
            current = previous.get(current);
        }
        return path;
    }

    private List<GridPos> getFourNeighbors(GridPos pos) {
        return List.of(
                pos.add(1, 0),
                pos.add(-1, 0),
                pos.add(0, 1),
                pos.add(0, -1)
        );
    }

    private void appendIfDifferent(List<GridPos> path, GridPos pos) {
        if (path.isEmpty() || !path.get(path.size() - 1).equals(pos)) {
            path.add(pos);
        }
    }

    private Vec2 toWorldPos(GridPos pos) {
        return new Vec2(pos.x + 0.5, pos.y + 0.5);
    }

    private double distance(Vec2 a, Vec2 b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    // mine -> factory -> city.
    private Shipment routeShipmentToNextStop(Shipment shipment) {
        if (shipment == null || assignedRoute == null || currentStopIndex < 0) {
            return shipment;
        }

        Stop targetStop = resolveTargetStopFor(shipment);
        return new Shipment(
                shipment.getKind(),
                shipment.getGoodsType(),
                shipment.getUnits(),
                shipment.getFromStopId(),
                targetStop.getId(),
                shipment.getValuePerTile()
        );
    }

    private Stop resolveTargetStopFor(Shipment shipment) {
        Stop nextStop = assignedRoute.getNextStop(currentStopIndex);
        if (!shipment.isGoods()) {
            return nextStop;
        }

        GoodsType goodsType = shipment.getGoodsType();
        Stop currentStop = assignedRoute.getStop(currentStopIndex);
        MapEntity sourcePlace = currentStop.getServedPlace();

        // Raw materials loaded at mines must go to a matching factory input.
        if (sourcePlace instanceof Mine) {
            Stop factoryStop = findNextStopMatching(stop -> {
                MapEntity served = stop.getServedPlace();
                return served instanceof Facility facility && facility.getInputType() == goodsType;
            });
            if (factoryStop != null) {
                return factoryStop;
            }
        }

        // Goods loaded at factories must go to a city.
        if (sourcePlace instanceof Factory) {
            Stop cityStop = findNextStopMatching(stop -> stop.getServedPlace() instanceof City);
            if (cityStop != null) {
                return cityStop;
            }
        }

        return nextStop;
    }

    private Stop findNextStopMatching(java.util.function.Predicate<Stop> predicate) {
        for (int i = 1; i < assignedRoute.getStopCount(); i++) {
            Stop candidate = assignedRoute.getStop((currentStopIndex + i) % assignedRoute.getStopCount());
            if (predicate.test(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    // Temporary debug formatter for transport event messages.
    private String describeEntity(MapEntity entity) {
        return entity.getClass().getSimpleName();
    }

    public abstract boolean acceptsKind(ShipmentKind kind);

    public abstract boolean acceptsGoodsType(GoodsType goodsType);
}
