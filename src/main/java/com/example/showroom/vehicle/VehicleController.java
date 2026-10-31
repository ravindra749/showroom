package com.example.showroom.vehicle;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public List<Vehicle> search(
            @RequestParam(required = false) String make,
            @RequestParam(required = false) BodyType bodyType,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) VehicleStatus status) {
        return vehicleService.search(make, bodyType, maxPrice, status);
    }

    @GetMapping("/makes")
    public List<String> makes() {
        return vehicleService.makes();
    }

    @GetMapping("/{id}")
    public Vehicle findById(@PathVariable Long id) {
        return vehicleService.findById(id);
    }

    @PostMapping
    public ResponseEntity<Vehicle> create(@Valid @RequestBody VehicleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.create(request));
    }

    @PutMapping("/{id}")
    public Vehicle update(@PathVariable Long id, @Valid @RequestBody VehicleRequest request) {
        return vehicleService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
