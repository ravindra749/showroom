package com.example.showroom.testdrive;

import jakarta.validation.constraints.NotNull;

public record TestDriveStatusChange(@NotNull TestDriveStatus status) {
}
