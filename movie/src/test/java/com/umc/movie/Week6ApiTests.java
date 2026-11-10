package com.umc.movie;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.movie.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql("/test-data.sql")
class Week6ApiTests {
    @Autowired MockMvc mvc;
    @Autowired com.umc.movie.util.JwtUtil jwt;
    @Autowired ObjectMapper mapper;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder passwordEncoder;
    @MockitoSpyBean MemberRepository memberRepository;

    private MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder request, long memberId) {
        return request.header("Authorization", "Bearer " + jwt.createAccessToken(memberId, "test@umc.com"));
    }

    private ResultActions json(MockHttpServletRequestBuilder request, Object body) throws Exception {
        return mvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body)));
    }

    private void failure(ResultActions result, int status) throws Exception {
        result.andExpect(status().is(status))
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").isString())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.result").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test void signupStoresBcryptAndNeverReturnsPassword() throws Exception {
        String body = json(post("/members"), Map.of("email", "week6@example.com", "password", "Umc!1234", "nickname", "영화팬6"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON201"))
                .andExpect(jsonPath("$.result.email").value("week6@example.com"))
                .andExpect(jsonPath("$.result.password").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        long memberId = mapper.readTree(body).path("result").path("memberId").asLong();
        String hash = jdbc.queryForObject("SELECT password FROM member WHERE member_id = ?", String.class, memberId);
        assertThat(hash).hasSize(60).isNotEqualTo("Umc!1234");
        assertThat(passwordEncoder.matches("Umc!1234", hash)).isTrue();
    }

    @Test void rejectsDuplicateEmailAndNicknameWithoutSaving() throws Exception {
        failure(json(post("/members"), Map.of("email", "noco@umc.com", "password", "Umc!1234", "nickname", "새회원")), 409);
        failure(json(post("/members"), Map.of("email", "unique@umc.com", "password", "Umc!1234", "nickname", "수현")), 409);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM member", Integer.class)).isEqualTo(3);
    }

    @Test void validatesSignupEmailNicknameAndPassword() throws Exception {
        for (Map<String, String> invalid : java.util.List.of(
                Map.of("email", "not-email", "password", "Umc!1234", "nickname", "정상회원"),
                Map.of("email", "valid@example.com", "password", "weak", "nickname", "정상회원"),
                Map.of("email", "valid@example.com", "password", "Umc!1234", "nickname", "a"),
                Map.of("email", "valid@example.com", "password", "Umc!1234", "nickname", "1234567890123"))) {
            failure(json(post("/members"), invalid), 400);
        }
        failure(json(post("/members"), Map.of()), 400);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM member", Integer.class)).isEqualTo(3);
    }

    @Test void rejectsPasswordsOverBcryptByteLimitAsBadRequest() throws Exception {
        failure(json(post("/members"), Map.of("email", "long@example.com", "password", "Umc!1234" + "가".repeat(22), "nickname", "긴비번")), 400);
    }

    @Test void databaseUniqueViolationStillReturnsConsistentConflict() throws Exception {
        // 동시성 경쟁처럼 사전 중복 확인만 통과시킨 뒤 실제 DB UNIQUE 제약을 검증합니다.
        doReturn(false).when(memberRepository).existsByEmail("noco@umc.com");
        json(post("/members"), Map.of("email", "noco@umc.com", "password", "Umc!1234", "nickname", "경쟁회원"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("COMMON4091"))
                .andExpect(jsonPath("$.isSuccess").value(false));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM member", Integer.class)).isEqualTo(3);
    }

    @Test void createsUpdatesAndDeletesRatingWithReadBack() throws Exception {
        String created = json(auth(post("/ratings"), 3), Map.of("movieId", 550, "score", 5, "comment", "최고예요"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.code").value("COMMON201"))
                .andExpect(jsonPath("$.isSuccess").value(true)).andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(created).path("result").path("ratingId").asLong();
        mvc.perform(get("/members/3/ratings")).andExpect(jsonPath("$[0].ratingId").value(id))
                .andExpect(jsonPath("$[0].score").value(5));
        json(auth(patch("/ratings/" + id), 3), Map.of("score", 4, "comment", "수정한 후기"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value("COMMON200"));
        mvc.perform(get("/members/3/ratings")).andExpect(jsonPath("$[0].score").value(4))
                .andExpect(jsonPath("$[0].comment").value("수정한 후기"));
        mvc.perform(auth(delete("/ratings/" + id), 3)).andExpect(status().isOk()).andExpect(jsonPath("$.result").value(org.hamcrest.Matchers.nullValue()));
        mvc.perform(get("/members/3/ratings")).andExpect(content().json("[]"));
    }

    @Test void rejectsDuplicateRatingWithoutAddingRows() throws Exception {
        failure(json(auth(post("/ratings"), 1), Map.of("movieId", 550, "score", 4)), 409);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM rating WHERE member_id = 1 AND movie_id = 550", Integer.class)).isEqualTo(1);
    }

    @Test void rejectsMissingMemberAndMissingRatings() throws Exception {
        failure(json(auth(post("/ratings"), 999), Map.of("movieId", 550, "score", 5)), 404);
        failure(json(auth(patch("/ratings/999"), 1), Map.of("score", 4)), 404);
        failure(mvc.perform(auth(delete("/ratings/999"), 1)), 404);
    }

    @Test void rejectsOtherMembersUpdateAndDeleteWithoutChangingData() throws Exception {
        failure(json(auth(patch("/ratings/1"), 2), Map.of("score", 1, "comment", "변조 시도")), 403);
        failure(mvc.perform(auth(delete("/ratings/1"), 2)), 403);
        assertThat(jdbc.queryForObject("SELECT score FROM rating WHERE rating_id = 1", Integer.class)).isEqualTo(5);
    }

    @Test void validatesMovieScoreAndCommentOnCreateAndUpdate() throws Exception {
        for (Object invalid : java.util.List.of(
                Map.of("movieId", 0, "score", 3), Map.of("movieId", 550, "score", 0),
                Map.of("movieId", 550, "score", 6), Map.of("movieId", 550, "score", 3.5),
                Map.of("movieId", 550.5, "score", 3), Map.of("movieId", 550),
                Map.of("score", 3), Map.of("movieId", 550, "score", 3, "comment", "가".repeat(501)))) {
            failure(json(auth(post("/ratings"), 3), invalid), 400);
        }
        failure(json(auth(patch("/ratings/1"), 1), Map.of("score", 6)), 400);
        failure(json(auth(patch("/ratings/1"), 1), Map.of("score", 3, "comment", "가".repeat(501))), 400);
        failure(json(auth(patch("/ratings/1"), 1), Map.of("comment", "별점 누락")), 400);
    }

    @Test void allowsNullCommentAndClearsItWhenOmittedOnUpdate() throws Exception {
        json(auth(patch("/ratings/1"), 1), Map.of("score", 3)).andExpect(status().isOk());
        assertThat(jdbc.queryForObject("SELECT comment FROM rating WHERE rating_id = 1", String.class)).isNull();
        json(auth(post("/ratings"), 3), Map.of("movieId", 550, "score", 3)).andExpect(status().isCreated());
    }

    @Test void returnsConsistentBadRequestForMalformedJsonAndPath() throws Exception {
        failure(mvc.perform(post("/members").contentType(MediaType.APPLICATION_JSON).content("{")), 400);
        failure(json(auth(patch("/ratings/abc"), 1), Map.of("movieId", 550, "score", 3)), 400);
        failure(json(auth(patch("/ratings/0"), 1), Map.of("movieId", 550, "score", 3)), 400);
    }

    @Test void allowsWritePreflightsFromFrontend() throws Exception {
        for (String method : java.util.List.of("POST", "PATCH", "DELETE")) {
            mvc.perform(options("/ratings/1").header("Origin", "http://localhost:5173")
                    .header("Access-Control-Request-Method", method).header("Access-Control-Request-Headers", "content-type"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
        }
    }

    @Test void swaggerDocumentsAllNewOperations() throws Exception {
        String spec = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode paths = mapper.readTree(spec).path("paths");
        assertThat(paths.path("/members").has("post")).isTrue();
        assertThat(paths.path("/ratings").has("post")).isTrue();
        assertThat(paths.path("/ratings/{ratingId}").has("patch")).isTrue();
        assertThat(paths.path("/ratings/{ratingId}").has("delete")).isTrue();
    }
}
