package com.umc.movie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "app.cors.enabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql("/test-data.sql")
class CorsDisabledTests {
    @Autowired MockMvc mvc;
    @Test void returnsDataButOmitsCorsHeaderWhenDisabled() throws Exception {
        mvc.perform(get("/members/nickname/수현").header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.available").value(false))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
