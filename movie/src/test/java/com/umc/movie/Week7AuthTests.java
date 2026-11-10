package com.umc.movie;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.movie.entity.Member;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.util.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql("/test-data.sql")
class Week7AuthTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired MemberRepository members;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtUtil jwt;
    @Value("${jwt.secret}") String secret;

    private ResultActions json(MockHttpServletRequestBuilder request, Object body) throws Exception {
        return mvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(body)));
    }
    private MockHttpServletRequestBuilder as(MockHttpServletRequestBuilder request, String token) {
        return request.header("Authorization", "Bearer " + token);
    }
    private String login(String email, String password) throws Exception {
        String response = json(post("/auth/login"), Map.of("email", email, "password", password))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value("COMMON200"))
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(response).path("result").path("accessToken").asText();
    }
    @Test void fullNineStepScenarioUsesTokensAndPreservesOwnerDataOnForbidden() throws Exception {
        for (String name : new String[]{"alpha", "bravo"}) {
            json(post("/members"), Map.of("email", name + "@umc.com", "password", "Umc!1234", "nickname", name))
                    .andExpect(status().isCreated());
        }
        String a = login("alpha@umc.com", "Umc!1234"), b = login("bravo@umc.com", "Umc!1234");
        String created = json(as(post("/ratings"), a), Map.of("movieId", 550, "score", 5))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.result.ratingId").isNumber())
                .andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(created).path("result").path("ratingId").asLong();
        json(as(post("/ratings"), a), Map.of("movieId", 550, "score", 5))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("RATING409_1"));
        json(as(patch("/ratings/" + id), a), Map.of("score", 4)).andExpect(status().isOk());
        json(as(patch("/ratings/" + id), b), Map.of("score", 1))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("RATING403_1"));
        mvc.perform(as(delete("/ratings/" + id), b)).andExpect(status().isForbidden());
        json(patch("/ratings/" + id), Map.of("score", 1))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTH401_2"));
        mvc.perform(as(delete("/ratings/99999"), a)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RATING404_1"));
        json(as(patch("/ratings/" + id), a), Map.of("score", 4))
                .andExpect(jsonPath("$.result.score").value(4));
        mvc.perform(as(delete("/ratings/" + id), a)).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result").value(org.hamcrest.Matchers.nullValue()));
    }
    @Test void loginFailuresMatchAndPayloadContainsNoPassword() throws Exception {
        members.saveAndFlush(new Member("login@umc.com", encoder.encode("Umc!1234"), "로그인"));
        String token = login("login@umc.com", "Umc!1234");
        String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8);
        assertThat(payload).contains("sub", "email", "iat", "exp").doesNotContain("password", "Umc!1234");
        String wrongPassword = json(post("/auth/login"), Map.of("email", "login@umc.com", "password", "wrong"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTH401_1"))
                .andReturn().getResponse().getContentAsString();
        json(post("/auth/login"), Map.of("email", "missing@umc.com", "password", "wrong"))
                .andExpect(status().isUnauthorized()).andExpect(content().json(wrongPassword));
        json(post("/auth/login"), Map.of("email", "login@umc.com", "password", "가".repeat(73)))
                .andExpect(status().isUnauthorized());
    }
    @Test void missingTamperedExpiredAndInvalidClaimsAreDistinct() throws Exception {
        mvc.perform(get("/members/me")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH401_2"));
        String token = jwt.createAccessToken(1L, "noco@umc.com");
        String[] parts = token.split("\\.");
        String changed = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8).replace("\"1\"", "\"2\"");
        String tampered = parts[0] + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(changed.getBytes(StandardCharsets.UTF_8)) + "." + parts[2];
        mvc.perform(as(get("/members/me"), tampered)).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH401_3"));
        String expired = Jwts.builder().subject("1").expiration(Date.from(Instant.now().minusSeconds(10)))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))).compact();
        mvc.perform(as(get("/members/me"), expired)).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH401_4"));
        for (String subject : new String[]{"abc", "0", "-1"}) {
            String bad = Jwts.builder().subject(subject).expiration(Date.from(Instant.now().plusSeconds(100)))
                    .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))).compact();
            mvc.perform(as(get("/members/me"), bad)).andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("AUTH401_3"));
        }
    }
    @Test void myInfoAndNicknameRulesAndPublicBoundaries() throws Exception {
        String token = jwt.createAccessToken(1L, "noco@umc.com");
        mvc.perform(as(get("/members/me"), token)).andExpect(status().isOk())
                .andExpect(jsonPath("$.result.email").value("noco@umc.com"))
                .andExpect(jsonPath("$.result.createdAt").isString()).andExpect(jsonPath("$.result.password").doesNotExist());
        json(as(patch("/members/me"), token), Map.of("nickname", "수현")).andExpect(status().isOk());
        json(as(patch("/members/me"), token), Map.of("nickname", "민서"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("MEMBER409_1"));
        json(as(patch("/members/me"), token), Map.of("nickname", "새닉네임")).andExpect(status().isOk());
        mvc.perform(as(get("/members/me"), token)).andExpect(jsonPath("$.result.nickname").value("새닉네임"));
        json(as(patch("/members/me"), token), Map.of("nickname", " ")).andExpect(status().isBadRequest());
        mvc.perform(as(get("/members/me"), jwt.createAccessToken(999L, "missing@umc.com")))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("MEMBER404_1"));
        mvc.perform(get("/members/1/ratings")).andExpect(status().isOk());
        // 삭제된 쓰기 매핑은 핸들러 선택 단계에서 405입니다. 공개 조회만 남아야 합니다.
        json(post("/members/1/ratings"), Map.of("movieId", 999, "score", 5)).andExpect(status().isMethodNotAllowed());
        mvc.perform(options("/ratings").header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "POST").header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk()).andExpect(header().exists("Access-Control-Allow-Origin"));
    }
}
