package model;

public enum VehicleState {
    IDLE,           // No route assigned or not active
    ON_ROUTE,       // Traveling on route
    LOADING,        // At stop loading/unloading
    BLOCKED,        // Cannot proceed (road occupied)
    IN_GARAGE       // In garage for maintenance
}
