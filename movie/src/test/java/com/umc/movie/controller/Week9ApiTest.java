package com.umc.movie.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.movie.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql("/test-data.sql")
class Week9ApiTest {
    @Autowired MockMvc mvc;
    @Autowired JwtUtil jwt;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper mapper;

    private String token() { return "Bearer " + jwt.createAccessToken(3L, "empty@umc.com"); }

    @Test
    @DisplayName("토큰 없이 POST /ratings를 호출하면 401 AUTH401_2이며 저장하지 않는다")
    void ratingRequiresToken() throws Exception {
        mvc.perform(post("/ratings").contentType(MediaType.APPLICATION_JSON).content("{\"movieId\":550,\"score\":5}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTH401_2"));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM rating WHERE member_id=3", Integer.class)).isZero();
    }

    @Test
    @DisplayName("정상 토큰으로 POST /ratings를 호출하면 201 COMMON201이며 토큰 회원의 평점이 저장된다")
    void ratingCreatesWithToken() throws Exception {
        mvc.perform(post("/ratings").header("Authorization", token()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"movieId\":550,\"score\":5}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.code").value("COMMON201"))
                .andExpect(jsonPath("$.result.movieId").value(550));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM rating WHERE member_id=3 AND movie_id=550", Integer.class)).isEqualTo(1);
    }

    @Test
    @DisplayName("토큰 없이 즐겨찾기를 등록하면 401 AUTH401_2를 반환한다")
    void favoriteRequiresToken() throws Exception {
        mvc.perform(post("/favorites").contentType(MediaType.APPLICATION_JSON).content("{\"movieId\":550}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("AUTH401_2"));
    }

    @Test
    @DisplayName("토큰으로 즐겨찾기를 등록하면 201과 저장한 영화 번호를 반환한다")
    void favoriteCreatesWithToken() throws Exception {
        mvc.perform(post("/favorites").header("Authorization", token()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"movieId\":550}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.code").value("COMMON201"))
                .andExpect(jsonPath("$.result.movieId").value(550));
    }

    @Test
    @DisplayName("즐겨찾기를 중복 등록하면 409 FAVORITE409_1을 반환한다")
    void favoriteRejectsDuplicate() throws Exception {
        jdbc.update("INSERT INTO favorite(member_id,movie_id,created_at,updated_at) VALUES(3,550,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)");
        mvc.perform(post("/favorites").header("Authorization", token()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"movieId\":550}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("FAVORITE409_1"));
    }

    @Test
    @DisplayName("즐겨찾기하지 않은 영화는 200과 isFavorite=false를 반환한다")
    void favoriteStatusIsFalseWhenAbsent() throws Exception {
        mvc.perform(get("/favorites/550").header("Authorization", token()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.result.isFavorite").value(false));
    }

    @Test
    @DisplayName("공개 Swagger 문서에 API 설명·인증 구분·실패 코드와 요청 예시가 표시된다")
    void apiDocsDescribeContractsAndSecurity() throws Exception {
        String spec = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var doc = mapper.readTree(spec);
        assertThat(doc.path("info").path("title").asText()).isEqualTo("영화 평점 서비스 API");
        var paths = doc.path("paths");
        for (String pair : List.of("/ratings post", "/ratings/{ratingId} patch", "/ratings/{ratingId} delete",
                "/members/me get", "/members/me patch", "/favorites get", "/favorites post",
                "/favorites/{movieId} get", "/favorites/{movieId} delete", "/movie-records post")) {
            String[] parts = pair.split(" ");
            var operation = paths.path(parts[0]).path(parts[1]);
            assertThat(operation.path("summary").asText()).isNotBlank();
            assertThat(operation.path("description").asText()).contains("JWT");
            assertThat(operation.path("security").toString()).contains("bearerAuth");
            assertThat(operation.path("responses").has("401")).isTrue();
            // 인증된 memberId가 클라이언트 요청 파라미터로 노출되지 않아야 합니다.
            for (var parameter : operation.path("parameters")) {
                assertThat(parameter.path("name").asText()).isNotEqualTo("memberId");
            }
        }
        for (String pair : List.of("/members post", "/auth/login post", "/members/{memberId}/ratings get")) {
            String[] parts = pair.split(" ");
            var operation = paths.path(parts[0]).path(parts[1]);
            assertThat(operation.path("summary").asText()).isNotBlank();
            assertThat(operation.path("security").isMissingNode() || operation.path("security").isEmpty()).isTrue();
        }
        assertThat(paths.path("/ratings").path("post").path("responses").path("409").toString()).contains("RATING409_1");
        assertThat(paths.path("/ratings/{ratingId}").path("patch").path("responses").path("403").toString()).contains("RATING403_1");
        assertThat(paths.path("/favorites").path("post").path("responses").path("409").toString()).contains("FAVORITE409_1");
        assertThat(paths.path("/ratings").path("post").path("responses").path("201").path("content").toString()).contains("schema");
        var favoriteSuccess = paths.path("/favorites").path("post").path("responses").path("201").path("content");
        assertThat(favoriteSuccess.toString()).contains("schema", "COMMON201", "favoriteId");
        assertThat(doc.path("components").path("schemas").path("CreateFavoriteRequest").path("properties")
                .path("movieId").path("example").asLong()).isEqualTo(550);
        assertThat(doc.path("components").path("schemas").path("LoginRequest").path("properties")
                .path("email").path("example").asText()).isEqualTo("week9@umc.com");
    }
}
