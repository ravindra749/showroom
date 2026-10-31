package com.example.showroom.vehicle;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * The fields a client may send when adding or editing a vehicle. Sizes match
 * the V1 migration's columns. A missing status means AVAILABLE.
 */
public record VehicleRequest(
        // VINs are 17 characters and never use I, O, or Q.
        @NotBlank
        @Pattern(regexp = "[A-HJ-NPR-Za-hj-npr-z0-9]{17}",
                message = "must be 17 letters and digits, without I, O, or Q")
        String vin,

        @NotBlank @Size(max = 60) String make,

        @NotBlank @Size(max = 60) String model,

        @NotNull @Min(1980) @Max(2100) Integer modelYear,

        @Size(max = 60) String trimLevel,

        @NotNull BodyType bodyType,

        @NotBlank @Size(max = 40) String color,

        @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal price,

        @NotNull @Min(0) @Max(2_000_000) Integer mileage,

        VehicleStatus status) {
}
