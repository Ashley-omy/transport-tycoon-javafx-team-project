package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TransactionTypeTest {

    @Test
    void enumValuesExist() {
        assertNotNull(TransactionType.valueOf("INITIAL_CAPITAL"));
        assertNotNull(TransactionType.valueOf("DELIVERY_INCOME"));
        assertNotNull(TransactionType.valueOf("VEHICLE_PURCHASE"));
        assertNotNull(TransactionType.valueOf("VEHICLE_SALE"));
        assertNotNull(TransactionType.valueOf("VEHICLE_MAINTENANCE"));
        assertNotNull(TransactionType.valueOf("ROAD_CONSTRUCTION"));
        assertNotNull(TransactionType.valueOf("STOP_CONSTRUCTION"));
        assertNotNull(TransactionType.valueOf("INFRASTRUCTURE"));
        assertNotNull(TransactionType.valueOf("OTHER_INCOME"));
        assertNotNull(TransactionType.valueOf("OTHER_EXPENSE"));
    }
}
