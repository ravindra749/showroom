package com.example.showroom.testdrive;

/** A booking moves REQUESTED -> CONFIRMED -> COMPLETED, and can be CANCELLED before completion. */
public enum TestDriveStatus {
    REQUESTED, CONFIRMED, COMPLETED, CANCELLED;

    public boolean canChangeTo(TestDriveStatus next) {
        return switch (this) {
            case REQUESTED -> next == CONFIRMED || next == CANCELLED;
            case CONFIRMED -> next == COMPLETED || next == CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }
}
