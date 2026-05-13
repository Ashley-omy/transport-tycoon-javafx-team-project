package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CompanyTest {

    @Test
    void initShouldCreateCompanyProperly() {
        Company company = new Company();
        assertNotNull(company.getEconomy());
        assertNotNull(company.getFleet());
    }

    @Test
    void sellVehicleRemovesFromFleet() {
        Company company = new Company();
        Truck truck = new Truck(common.Id.genNew(), 50, common.Money.of(100), common.Money.of(5), 2.0);
        company.buyVehicle(truck);
        
        assertEquals(1, company.getFleet().size());
        company.sellVehicle(truck);
        assertEquals(0, company.getFleet().size());
    }
}
