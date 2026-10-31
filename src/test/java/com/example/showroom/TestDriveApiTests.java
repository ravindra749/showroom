package com.example.showroom;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.example.showroom.testdrive.TestDriveRepository;
import com.example.showroom.vehicle.BodyType;
import com.example.showroom.vehicle.Vehicle;
import com.example.showroom.vehicle.VehicleRepository;
import com.example.showroom.vehicle.VehicleStatus;

@SpringBootTest
@AutoConfigureMockMvc
class TestDriveApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private TestDriveRepository testDriveRepository;

    private Vehicle available;

    @BeforeEach
    void createInventory() {
        testDriveRepository.deleteAll();
        vehicleRepository.deleteAll();
        available = vehicleRepository.save(TestData.vehicle(
                "1NLMS23A7RK100001", "Northline", "Aster", BodyType.SEDAN, "27990", VehicleStatus.AVAILABLE));
    }

    private ResultActions book(long vehicleId, LocalDate date) throws Exception {
        return mockMvc.perform(post("/api/test-drives")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"vehicleId":%d,"customerName":"Ada Lovelace",
                         "email":"ada@example.com","phone":"+1 (555) 010-2030",
                         "preferredDate":"%s"}
                        """.formatted(vehicleId, date)));
    }

    private long bookAndGetId() throws Exception {
        String body = book(available.getId(), LocalDate.now().plusDays(3))
                .andReturn().getResponse().getContentAsString();
        return Long.parseLong(body.replaceAll("^\\{\"id\":(\\d+).*", "$1"));
    }

    private ResultActions changeStatus(long id, String status) throws Exception {
        return mockMvc.perform(patch("/api/test-drives/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"" + status + "\"}"));
    }

    @Test
    void customerCanBookAnAvailableVehicle() throws Exception {
        LocalDate date = LocalDate.now().plusDays(3);
        book(available.getId(), date)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.vehicleId").value(available.getId()))
                .andExpect(jsonPath("$.vehicleName").value("2025 Northline Aster LE"))
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andExpect(jsonPath("$.preferredDate").value(date.toString()))
                .andExpect(jsonPath("$.createdAt").exists());

        mockMvc.perform(get("/api/test-drives"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].customerName").value("Ada Lovelace"));
    }

    @Test
    void soldOrReservedVehiclesCannotBeBooked() throws Exception {
        Vehicle sold = vehicleRepository.save(TestData.vehicle(
                "3SLCC56G3RK100008", "Solano", "Breeze", BodyType.CONVERTIBLE, "46900", VehicleStatus.SOLD));
        Vehicle reserved = vehicleRepository.save(TestData.vehicle(
                "1NLMP67D5RK100005", "Northline", "Haulmark", BodyType.TRUCK, "52990", VehicleStatus.RESERVED));

        book(sold.getId(), LocalDate.now().plusDays(3))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "This vehicle is sold and cannot be booked for a test drive."));
        book(reserved.getId(), LocalDate.now().plusDays(3))
                .andExpect(status().isConflict());
    }

    @Test
    void bookingNeedsAFutureDateAndValidContactDetails() throws Exception {
        book(available.getId(), LocalDate.now())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.preferredDate").exists());

        mockMvc.perform(post("/api/test-drives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vehicleId":%d,"customerName":"","email":"not-an-email",
                                 "phone":"call me","preferredDate":"%s"}
                                """.formatted(available.getId(), LocalDate.now().plusDays(2))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.customerName").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.phone").exists());
    }

    @Test
    void bookingAnUnknownVehicleReturnsNotFound() throws Exception {
        book(999_999, LocalDate.now().plusDays(3))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Vehicle not found with id 999999"));
    }

    @Test
    void bookingMovesThroughItsStatusesInOrder() throws Exception {
        long id = bookAndGetId();

        changeStatus(id, "COMPLETED")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "A requested test drive cannot be changed to completed."));
        changeStatus(id, "CONFIRMED")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
        changeStatus(id, "COMPLETED")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
        changeStatus(id, "CANCELLED")
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/test-drives").param("status", "COMPLETED"))
                .andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get("/api/test-drives").param("status", "REQUESTED"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void unknownBookingAndUnknownStatusAreRejected() throws Exception {
        changeStatus(999_999, "CONFIRMED")
                .andExpect(status().isNotFound());
        long id = bookAndGetId();
        changeStatus(id, "PARKED")
                .andExpect(status().isBadRequest());
    }

    @Test
    void vehicleWithBookingsCannotBeDeleted() throws Exception {
        bookAndGetId();
        mockMvc.perform(delete("/api/vehicles/{id}", available.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "This vehicle has test-drive bookings, so it cannot be deleted. "
                                + "Mark it as sold instead."));
    }
}
