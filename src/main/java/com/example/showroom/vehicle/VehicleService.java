package com.example.showroom.vehicle;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.showroom.testdrive.TestDriveRepository;
import com.example.showroom.web.ConflictException;
import com.example.showroom.web.NotFoundException;

@Service
@Transactional(readOnly = true)
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final TestDriveRepository testDriveRepository;

    public VehicleService(
            VehicleRepository vehicleRepository, TestDriveRepository testDriveRepository) {
        this.vehicleRepository = vehicleRepository;
        this.testDriveRepository = testDriveRepository;
    }

    public List<Vehicle> search(
            String make, BodyType bodyType, BigDecimal maxPrice, VehicleStatus status) {
        String makeFilter = make == null || make.isBlank() ? null : make.trim();
        return vehicleRepository.search(makeFilter, bodyType, maxPrice, status);
    }

    public List<String> makes() {
        return vehicleRepository.findDistinctMakes();
    }

    public Vehicle findById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Vehicle not found with id " + id));
    }

    @Transactional
    public Vehicle create(VehicleRequest request) {
        String vin = normalizeVin(request.vin());
        if (vehicleRepository.existsByVinIgnoreCase(vin)) {
            throw duplicateVin(vin);
        }
        Vehicle vehicle = new Vehicle();
        apply(vehicle, request, vin);
        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public Vehicle update(Long id, VehicleRequest request) {
        Vehicle vehicle = findById(id);
        String vin = normalizeVin(request.vin());
        if (vehicleRepository.existsByVinIgnoreCaseAndIdNot(vin, id)) {
            throw duplicateVin(vin);
        }
        apply(vehicle, request, vin);
        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public void delete(Long id) {
        Vehicle vehicle = findById(id);
        // Bookings keep their vehicle for the dealership's records.
        if (testDriveRepository.existsByVehicleId(id)) {
            throw new ConflictException(
                    "This vehicle has test-drive bookings, so it cannot be deleted. "
                            + "Mark it as sold instead.");
        }
        vehicleRepository.delete(vehicle);
    }

    private static void apply(Vehicle vehicle, VehicleRequest request, String vin) {
        vehicle.setVin(vin);
        vehicle.setMake(request.make().trim());
        vehicle.setModel(request.model().trim());
        vehicle.setModelYear(request.modelYear());
        vehicle.setTrimLevel(request.trimLevel() == null || request.trimLevel().isBlank()
                ? null
                : request.trimLevel().trim());
        vehicle.setBodyType(request.bodyType());
        vehicle.setColor(request.color().trim());
        vehicle.setPrice(request.price());
        vehicle.setMileage(request.mileage());
        vehicle.setStatus(request.status() == null ? VehicleStatus.AVAILABLE : request.status());
    }

    private static String normalizeVin(String vin) {
        return vin.trim().toUpperCase(Locale.ROOT);
    }

    private static ConflictException duplicateVin(String vin) {
        return new ConflictException("A vehicle with VIN " + vin + " already exists.", "vin");
    }
}
