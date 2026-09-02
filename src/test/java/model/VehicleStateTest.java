package model;

import common.GridPos;
import common.Id;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VehicleStateTest {

    @Test
    void assignRouteStoresRouteWhileStartingFromIdle() {
        Truck truck = VehicleFactory.createSmallTruck(Id.genNew());
        Route route = new Route(Id.genNew(), List.of(dummyStop(), dummyStop()));

        assertEquals(VehicleState.IDLE, truck.getState());

        truck.assignRoute(route);

        assertTrue(truck.hasRoute());
        assertEquals(route, truck.getAssignedRoute());
    }

    @Test
    void clearRouteRemovesRouteAndSetsIdle() {
        Truck truck = VehicleFactory.createSmallTruck(Id.genNew());
        Route route = new Route(Id.genNew(), List.of(dummyStop(), dummyStop()));
        truck.assignRoute(route);

        truck.clearRoute();

        assertFalse(truck.hasRoute());
        assertEquals(VehicleState.IDLE, truck.getState());
    }

    @Test
    void assignRouteRejectsNull() {
        Truck truck = VehicleFactory.createSmallTruck(Id.genNew());

        assertThrows(IllegalArgumentException.class, () -> truck.assignRoute(null));
    }

    private Stop dummyStop() {
        return new Stop(
                Id.genNew(),
                new Tile(new GridPos(1, 1), new Land()),
                new City(Id.genNew())
        );
    }
}
