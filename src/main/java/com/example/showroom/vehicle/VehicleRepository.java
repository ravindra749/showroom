package com.example.showroom.vehicle;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    boolean existsByVinIgnoreCase(String vin);

    boolean existsByVinIgnoreCaseAndIdNot(String vin, Long id);

    /** Every filter is optional; a null value matches all vehicles. */
    @Query("""
            SELECT v FROM Vehicle v
            WHERE (:make IS NULL OR LOWER(v.make) = LOWER(:make))
              AND (:bodyType IS NULL OR v.bodyType = :bodyType)
              AND (:maxPrice IS NULL OR v.price <= :maxPrice)
              AND (:status IS NULL OR v.status = :status)
            ORDER BY v.make, v.model, v.modelYear DESC, v.id
            """)
    List<Vehicle> search(
            @Param("make") String make,
            @Param("bodyType") BodyType bodyType,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("status") VehicleStatus status);

    @Query("SELECT DISTINCT v.make FROM Vehicle v ORDER BY v.make")
    List<String> findDistinctMakes();
}
