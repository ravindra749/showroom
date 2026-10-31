package com.example.showroom;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class StaticPagesTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void showroomPageOffersSearchAndBooking() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"searchForm\"")))
                .andExpect(content().string(containsString("id=\"bookingForm\"")))
                .andExpect(content().string(containsString("href=\"staff.html\"")));
        mockMvc.perform(get("/showroom.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("params.set(\"make\"")))
                .andExpect(content().string(containsString("fetch(\"/api/test-drives\"")));
    }

    @Test
    void staffPageManagesInventoryAndBookings() throws Exception {
        mockMvc.perform(get("/staff.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"vehicleForm\"")))
                .andExpect(content().string(containsString("id=\"testDriveTableBody\"")));
        mockMvc.perform(get("/staff.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("method: \"DELETE\"")))
                .andExpect(content().string(containsString("method: \"PATCH\"")));
    }

    @Test
    void scriptsRenderTextSafely() throws Exception {
        for (String script : new String[] {"/common.js", "/showroom.js", "/staff.js"}) {
            mockMvc.perform(get(script))
                    .andExpect(status().isOk())
                    .andExpect(content().string(not(containsString(".innerHTML"))));
        }
        mockMvc.perform(get("/styles.css"))
                .andExpect(status().isOk());
    }
}
