package com.umc.movie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql("/test-data.sql")
class MemberApiTests {
    @Autowired MockMvc mvc;

    @Test void checksExistingAndNewNicknames() throws Exception {
        mvc.perform(get("/members/nickname/수현")).andExpect(status().isOk()).andExpect(jsonPath("$.available").value(false));
        mvc.perform(get("/members/nickname/새닉네임")).andExpect(status().isOk()).andExpect(jsonPath("$.available").value(true));
    }
    @Test void checksExistingAndNewEmails() throws Exception {
        mvc.perform(get("/members/email/noco@umc.com")).andExpect(status().isOk()).andExpect(jsonPath("$.available").value(false));
        mvc.perform(get("/members/email/new@umc.com")).andExpect(status().isOk()).andExpect(jsonPath("$.available").value(true));
    }
    @Test void rejectsInvalidInputs() throws Exception {
        mvc.perform(get("/members/nickname/a")).andExpect(status().isBadRequest());
        mvc.perform(get("/members/nickname/1234567890123")).andExpect(status().isBadRequest());
        mvc.perform(get("/members/email/not-email")).andExpect(status().isBadRequest());
        mvc.perform(get("/members/0/ratings")).andExpect(status().isBadRequest());
        mvc.perform(get("/members/abc/ratings")).andExpect(status().isBadRequest());
    }
    @Test void returnsOnlyRequestedMembersRatingsNewestFirstWithoutPrivateFields() throws Exception {
        mvc.perform(get("/members/1/ratings")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].ratingId").value(2))
                .andExpect(jsonPath("$[0].movieId").value(155))
                .andExpect(jsonPath("$[1].score").value(5))
                .andExpect(jsonPath("$[0].createdAt").isString())
                .andExpect(jsonPath("$[0].updatedAt").isString())
                .andExpect(jsonPath("$[0].member").doesNotExist())
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }
    @Test void distinguishesNoRatingsFromMissingMember() throws Exception {
        mvc.perform(get("/members/3/ratings")).andExpect(status().isOk()).andExpect(content().json("[]"));
        mvc.perform(get("/members/999/ratings")).andExpect(status().isNotFound());
    }
    @Test void permitsOnlyConfiguredOriginAndGetPreflight() throws Exception {
        mvc.perform(get("/members/nickname/수현").header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
        mvc.perform(get("/members/nickname/수현").header("Origin", "http://localhost:5174"))
                .andExpect(status().isForbidden()).andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
        mvc.perform(options("/members/1/ratings").header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Methods", "GET"));
        mvc.perform(options("/members/1/ratings").header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "POST")).andExpect(status().isForbidden());
    }
}
