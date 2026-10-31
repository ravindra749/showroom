package com.example.showroom.testdrive;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** What the API returns for a booking, including a readable vehicle name. */
public record TestDriveResponse(
        Long id,
        Long vehicleId,
        String vehicleName,
        String customerName,
        String email,
        String phone,
        LocalDate preferredDate,
        TestDriveStatus status,
        LocalDateTime createdAt) {

    static TestDriveResponse from(TestDrive testDrive) {
        return new TestDriveResponse(
                testDrive.getId(),
                testDrive.getVehicle().getId(),
                testDrive.getVehicle().displayName(),
                testDrive.getCustomerName(),
                testDrive.getEmail(),
                testDrive.getPhone(),
                testDrive.getPreferredDate(),
                testDrive.getStatus(),
                testDrive.getCreatedAt());
    }
}
