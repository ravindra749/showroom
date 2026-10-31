package com.example.showroom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Runs against a disposable MySQL 8.4 container, never a developer database.
 * Skipped when Docker is not available.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
class MySqlShowroomIntegrationTests {

    @Container
    static final MySQLContainer MYSQL = new MySQLContainer(DockerImageName.parse("mysql:8.4"))
            .withDatabaseName("showroom_test");

    @DynamicPropertySource
    static void configureDisposableDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
        // Also loads the sample inventory, which proves that script runs on MySQL.
        registry.add("spring.flyway.locations", () -> "classpath:db/migration,classpath:db/sample");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearBookings() {
        jdbcTemplate.update("DELETE FROM test_drive");
    }

    @Test
    void migrationsAndSampleInventoryLoadOnMySql() throws Exception {
        List<String> vehicleColumns = jdbcTemplate.queryForList(
                "SELECT COLUMN_NAME FROM information_schema.columns "
                        + "WHERE table_schema = DATABASE() AND table_name = 'vehicle' "
                        + "ORDER BY ordinal_position",
                String.class);
        assertEquals(List.of("id", "vin", "make", "model", "model_year", "trim_level",
                "body_type", "color", "price", "mileage", "status"), vehicleColumns);

        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(8));
        mockMvc.perform(get("/api/vehicles")
                        .param("make", "NORTHLINE").param("bodyType", "SUV").param("maxPrice", "40000"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].trimLevel").value("Limited"));

        // The unique VIN constraint holds even for writes that bypass the API.
        assertThrows(DataIntegrityViolationException.class, () -> jdbcTemplate.update(
                "INSERT INTO vehicle (vin, make, model, model_year, body_type, color, price, mileage, status) "
                        + "VALUES ('1NLMS23A7RK100001', 'X', 'Y', 2025, 'SEDAN', 'Red', 1, 0, 'AVAILABLE')"));
    }

    @Test
    void bookingFlowWorksOnMySql() throws Exception {
        Long vehicleId = jdbcTemplate.queryForObject(
                "SELECT id FROM vehicle WHERE vin = '1NLMS23A7RK100001'", Long.class);
        String booking = mockMvc.perform(post("/api/test-drives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vehicleId":%d,"customerName":"Grace Hopper",
                                 "email":"grace@example.com","phone":"555-010-2040",
                                 "preferredDate":"%s"}
                                """.formatted(vehicleId, LocalDate.now().plusDays(5))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andReturn().getResponse().getContentAsString();
        long bookingId = Long.parseLong(booking.replaceAll("^\\{\"id\":(\\d+).*", "$1"));

        mockMvc.perform(patch("/api/test-drives/{id}/status", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CONFIRMED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        Long soldId = jdbcTemplate.queryForObject(
                "SELECT id FROM vehicle WHERE status = 'SOLD' LIMIT 1", Long.class);
        mockMvc.perform(post("/api/test-drives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vehicleId":%d,"customerName":"Grace Hopper",
                                 "email":"grace@example.com","phone":"555-010-2040",
                                 "preferredDate":"%s"}
                                """.formatted(soldId, LocalDate.now().plusDays(5))))
                .andExpect(status().isConflict());
    }
}
