package com.example.showroom.testdrive;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** A customer's booking request. Sizes match the V1 migration's columns. */
public record TestDriveRequest(
        @NotNull Long vehicleId,

        @NotBlank @Size(max = 100) String customerName,

        @NotBlank @Email @Size(max = 255) String email,

        @NotBlank
        @Pattern(regexp = "[0-9+()\\- ]{7,20}",
                message = "must be 7 to 20 digits, spaces, or + ( ) -")
        String phone,

        @NotNull @Future LocalDate preferredDate) {
}
