package com.example.showroom.testdrive;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestDriveRepository extends JpaRepository<TestDrive, Long> {

    boolean existsByVehicleId(Long vehicleId);

    // The entity graph loads each booking's vehicle in the same query.
    @EntityGraph(attributePaths = "vehicle")
    List<TestDrive> findAllByOrderByPreferredDateAscIdAsc();

    @EntityGraph(attributePaths = "vehicle")
    List<TestDrive> findByStatusOrderByPreferredDateAscIdAsc(TestDriveStatus status);
}
