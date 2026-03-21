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
    
    public void setOwner(Company owner) { this.owner = owner; }

    public void setWorld(World world) { this.world = world; }

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

        if (cargo == null) {
            cargo = loaded;
        } else {
            // merge units into existing cargo
            int mergedUnits = cargo.getUnits() + loaded.getUnits();
            cargo = new Shipment(
                    cargo.getKind(),
                    cargo.getGoodsType(),
                    mergedUnits,
                    cargo.getFromStopId(),
                    cargo.getToStopId(),
                    cargo.getValuePerTile()
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
        }
        
        return payout;
    }

    public void tick(double deltaTime) {
        if (Double.isNaN(deltaTime) || Double.isInfinite(deltaTime) || deltaTime <= 0.0) return;
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
                    arriveAtStop();
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
        GridPos fromStopPos = fromStop.getOccupiedTile().getPos();
        GridPos toStopPos = toStop.getOccupiedTile().getPos();

        // Stops are beside roads, so the actual route starts and ends on adjacent road tiles.
        List<GridPos> startRoadTiles = getAdjacentRoadTiles(fromStopPos);
        List<GridPos> endRoadTiles = getAdjacentRoadTiles(toStopPos);

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
        fullPath.add(fromStopPos);
        appendIfDifferent(fullPath, bestRoadPath.get(0));
        for (int i = 1; i < bestRoadPath.size(); i++) {
            appendIfDifferent(fullPath, bestRoadPath.get(i));
        }
        appendIfDifferent(fullPath, toStopPos);
        return fullPath;
    }

    private List<GridPos> getAdjacentRoadTiles(GridPos pos) {
        // These road tiles are the possible entry and exit points for a stop.
        List<GridPos> roadTiles = new ArrayList<>();
        for (GridPos neighbor : getFourNeighbors(pos)) {
            if (!world.getMap().inBounds(neighbor)) {
                continue;
            }

            Tile tile = world.getMap().getTile(neighbor);
            if (tile.getRoadPiece() != null) {
                roadTiles.add(neighbor);
            }
        }
        return roadTiles;
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

    public abstract boolean acceptsKind(ShipmentKind kind);

    public abstract boolean acceptsGoodsType(GoodsType goodsType);
}
