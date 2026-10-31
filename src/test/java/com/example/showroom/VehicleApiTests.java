package com.example.showroom;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.showroom.testdrive.TestDriveRepository;
import com.example.showroom.vehicle.BodyType;
import com.example.showroom.vehicle.Vehicle;
import com.example.showroom.vehicle.VehicleRepository;
import com.example.showroom.vehicle.VehicleStatus;

@SpringBootTest
@AutoConfigureMockMvc
class VehicleApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private TestDriveRepository testDriveRepository;

    @BeforeEach
    void clearData() {
        testDriveRepository.deleteAll();
        vehicleRepository.deleteAll();
    }

    @Test
    void vehicleCanBeAddedViewedUpdatedAndDeleted() throws Exception {
        String created = mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestData.vehicleJson("1nlms23a7rk100001", "AVAILABLE")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.vin").value("1NLMS23A7RK100001"))
                .andExpect(jsonPath("$.price").value(27990.00))
                .andReturn().getResponse().getContentAsString();
        long id = Long.parseLong(created.replaceAll(".*\"id\":(\\d+).*", "$1"));

        mockMvc.perform(get("/api/vehicles/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("Aster"));

        mockMvc.perform(put("/api/vehicles/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestData.vehicleJson("1NLMS23A7RK100001", "RESERVED")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESERVED"));

        mockMvc.perform(delete("/api/vehicles/{id}", id))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/vehicles/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Vehicle not found with id " + id));
    }

    @Test
    void missingStatusDefaultsToAvailable() throws Exception {
        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestData.vehicleJson("1NLMS23A7RK100001", "AVAILABLE")
                                .replace(",\"status\":\"AVAILABLE\"", "")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void searchCombinesOptionalFilters() throws Exception {
        vehicleRepository.save(TestData.vehicle(
                "1NLMS23A7RK100001", "Northline", "Aster", BodyType.SEDAN, "27990", VehicleStatus.AVAILABLE));
        vehicleRepository.save(TestData.vehicle(
                "1NLMT45C2RK100003", "Northline", "Ridgeway", BodyType.SUV, "41200", VehicleStatus.AVAILABLE));
        vehicleRepository.save(TestData.vehicle(
                "2KVHB12E9PK100006", "Kestrel", "Swift", BodyType.HATCHBACK, "19800", VehicleStatus.SOLD));

        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
        mockMvc.perform(get("/api/vehicles").param("make", " northline "))
                .andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(get("/api/vehicles").param("bodyType", "SUV"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].model").value("Ridgeway"));
        mockMvc.perform(get("/api/vehicles").param("maxPrice", "30000"))
                .andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(get("/api/vehicles")
                        .param("maxPrice", "30000").param("status", "AVAILABLE"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].model").value("Aster"));
        mockMvc.perform(get("/api/vehicles/makes"))
                .andExpect(jsonPath("$[0]").value("Kestrel"))
                .andExpect(jsonPath("$[1]").value("Northline"));
    }

    @Test
    void invalidVehicleIsRejectedWithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vin":"SHORTVIN","make":"","model":"Aster","modelYear":1950,
                                 "bodyType":"SEDAN","color":"Red","price":-1,"mileage":5}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.vin").exists())
                .andExpect(jsonPath("$.errors.make").exists())
                .andExpect(jsonPath("$.errors.modelYear").exists())
                .andExpect(jsonPath("$.errors.price").exists());
    }

    @Test
    void unknownBodyTypeIsRejected() throws Exception {
        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestData.vehicleJson("1NLMS23A7RK100001", "AVAILABLE")
                                .replace("SEDAN", "SPACESHIP")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/vehicles").param("bodyType", "SPACESHIP"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value 'SPACESHIP' for 'bodyType'."));
    }

    @Test
    void duplicateVinIsRejectedInAnyLetterCase() throws Exception {
        Vehicle existing = vehicleRepository.save(TestData.vehicle(
                "1NLMS23A7RK100001", "Northline", "Aster", BodyType.SEDAN, "27990", VehicleStatus.AVAILABLE));
        Vehicle other = vehicleRepository.save(TestData.vehicle(
                "1NLMT45C2RK100003", "Northline", "Ridgeway", BodyType.SUV, "41200", VehicleStatus.AVAILABLE));

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestData.vehicleJson("1nlms23a7rk100001", "AVAILABLE")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "A vehicle with VIN 1NLMS23A7RK100001 already exists."))
                .andExpect(jsonPath("$.errors.vin").exists());

        mockMvc.perform(put("/api/vehicles/{id}", other.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestData.vehicleJson(existing.getVin(), "AVAILABLE")))
                .andExpect(status().isConflict());

        // Keeping a vehicle's own VIN on update is allowed.
        mockMvc.perform(put("/api/vehicles/{id}", existing.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestData.vehicleJson(existing.getVin(), "SOLD")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SOLD"));
    }
}
