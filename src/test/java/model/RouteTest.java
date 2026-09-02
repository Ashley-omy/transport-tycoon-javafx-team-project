package model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RouteTest {

    @Test
    void constructorShouldStoreFields() {
        common.Id id = common.Id.genNew();
        Stop s1 = new Stop(common.Id.genNew(), new Tile(new common.GridPos(0, 0), new Land()), new City(common.Id.genNew()));
        Stop s2 = new Stop(common.Id.genNew(), new Tile(new common.GridPos(5, 5), new Land()), new City(common.Id.genNew()));
        List<Stop> stops = List.of(s1, s2);
        
        Route route = new Route(id, stops);
        assertEquals(id, route.getId());
        assertEquals(2, route.getStopCount());
        assertEquals(stops, route.getStops());
    }

    @Test
    void circularRouteLogicShouldWork() {
        Stop s1 = new Stop(common.Id.genNew(), new Tile(new common.GridPos(0, 0), new Land()), new City(common.Id.genNew()));
        Stop s2 = new Stop(common.Id.genNew(), new Tile(new common.GridPos(5, 5), new Land()), new City(common.Id.genNew()));
        Route route = new Route(common.Id.genNew(), List.of(s1, s2));
        
        assertTrue(route.hasNextStop(0));
        assertEquals(1, route.getNextStopIndex(0));
        assertEquals(s2, route.getNextStop(0));
        
        assertTrue(route.hasNextStop(1));
        assertEquals(0, route.getNextStopIndex(1));
        assertEquals(s1, route.getNextStop(1));
    }
}
