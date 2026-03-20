package model;

import common.Id;
import java.util.ArrayList;
import java.util.List;

public class Route {
    private final Id id;
    private final List<Stop> stops;

    public Route(Id id, List<Stop> stops) {
        if (id == null) throw new IllegalArgumentException("id cannot be null");
        if (stops == null || stops.isEmpty()) {
            throw new IllegalArgumentException("route must have at least one stop");
        }
        this.id = id;
        this.stops = new ArrayList<>(stops);
    }

    public Id getId() {
        return id;
    }

    public List<Stop> getStops() {
        return List.copyOf(stops);
    }

    public int getStopCount() {
        return stops.size();
    }

    public Stop getStop(int index) {
        if (index < 0 || index >= stops.size()) {
            throw new IllegalArgumentException("invalid stop index: " + index);
        }
        return stops.get(index);
    }

    public int getNextStopIndex(int currentIndex) {
        if (currentIndex < 0 || currentIndex >= stops.size()) {
            throw new IllegalArgumentException("invalid current index: " + currentIndex);
        }
        // Circular route: wrap around to 0 after last stop
        return (currentIndex + 1) % stops.size();
    }

    public Stop getNextStop(int currentIndex) {
        return getStop(getNextStopIndex(currentIndex));
    }
}
