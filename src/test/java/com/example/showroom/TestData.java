package com.example.showroom;

import java.math.BigDecimal;

import com.example.showroom.vehicle.BodyType;
import com.example.showroom.vehicle.Vehicle;
import com.example.showroom.vehicle.VehicleStatus;

/** Builds vehicles for tests without going through the API. */
final class TestData {

    private TestData() {
    }

    static Vehicle vehicle(String vin, String make, String model, BodyType bodyType,
                           String price, VehicleStatus status) {
        Vehicle vehicle = new Vehicle();
        vehicle.setVin(vin);
        vehicle.setMake(make);
        vehicle.setModel(model);
        vehicle.setModelYear(2025);
        vehicle.setTrimLevel("LE");
        vehicle.setBodyType(bodyType);
        vehicle.setColor("Silver");
        vehicle.setPrice(new BigDecimal(price));
        vehicle.setMileage(10);
        vehicle.setStatus(status);
        return vehicle;
    }

    static String vehicleJson(String vin, String status) {
        return """
                {"vin":"%s","make":"Northline","model":"Aster","modelYear":2025,
                 "trimLevel":"LE","bodyType":"SEDAN","color":"Pearl White",
                 "price":27990.00,"mileage":12,"status":"%s"}
                """.formatted(vin, status);
    }
}
