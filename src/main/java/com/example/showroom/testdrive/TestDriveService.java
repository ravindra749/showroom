package com.example.showroom.testdrive;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.showroom.vehicle.Vehicle;
import com.example.showroom.vehicle.VehicleService;
import com.example.showroom.vehicle.VehicleStatus;
import com.example.showroom.web.ConflictException;
import com.example.showroom.web.NotFoundException;

/**
 * Responses are built inside the transaction because the booking's vehicle is
 * loaded lazily and open-in-view is off.
 */
@Service
@Transactional(readOnly = true)
public class TestDriveService {

    private final TestDriveRepository testDriveRepository;
    private final VehicleService vehicleService;

    public TestDriveService(
            TestDriveRepository testDriveRepository, VehicleService vehicleService) {
        this.testDriveRepository = testDriveRepository;
        this.vehicleService = vehicleService;
    }

    public List<TestDriveResponse> findAll(TestDriveStatus status) {
        List<TestDrive> testDrives = status == null
                ? testDriveRepository.findAllByOrderByPreferredDateAscIdAsc()
                : testDriveRepository.findByStatusOrderByPreferredDateAscIdAsc(status);
        return testDrives.stream().map(TestDriveResponse::from).toList();
    }

    @Transactional
    public TestDriveResponse book(TestDriveRequest request) {
        Vehicle vehicle = vehicleService.findById(request.vehicleId());
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new ConflictException(
                    "This vehicle is " + vehicle.getStatus().name().toLowerCase(Locale.ROOT)
                            + " and cannot be booked for a test drive.",
                    "vehicleId");
        }
        TestDrive testDrive = new TestDrive();
        testDrive.setVehicle(vehicle);
        testDrive.setCustomerName(request.customerName().trim());
        testDrive.setEmail(request.email().trim());
        testDrive.setPhone(request.phone().trim());
        testDrive.setPreferredDate(request.preferredDate());
        testDrive.setStatus(TestDriveStatus.REQUESTED);
        testDrive.setCreatedAt(LocalDateTime.now());
        return TestDriveResponse.from(testDriveRepository.save(testDrive));
    }

    @Transactional
    public TestDriveResponse changeStatus(Long id, TestDriveStatus newStatus) {
        TestDrive testDrive = testDriveRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Test drive not found with id " + id));
        if (!testDrive.getStatus().canChangeTo(newStatus)) {
            throw new ConflictException(
                    "A " + testDrive.getStatus().name().toLowerCase(Locale.ROOT)
                            + " test drive cannot be changed to "
                            + newStatus.name().toLowerCase(Locale.ROOT) + ".",
                    "status");
        }
        testDrive.setStatus(newStatus);
        return TestDriveResponse.from(testDrive);
    }
}
