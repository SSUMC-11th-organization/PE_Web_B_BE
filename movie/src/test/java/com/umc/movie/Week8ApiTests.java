package com.umc.movie;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.movie.entity.Favorite;
import com.umc.movie.repository.FavoriteRepository;
import com.umc.movie.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql("/test-data.sql")
class Week8ApiTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired JwtUtil jwt;
    @Autowired JdbcTemplate jdbc;
    @MockitoSpyBean FavoriteRepository favorites;

    private MockHttpServletRequestBuilder as(MockHttpServletRequestBuilder request, long memberId) {
        return request.header("Authorization", "Bearer " + jwt.createAccessToken(memberId, "test@umc.com"));
    }

    private ResultActions json(MockHttpServletRequestBuilder request, Object body) throws Exception {
        return mvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(body)));
    }

    private void favorite(long memberId, long movieId) throws Exception {
        json(as(post("/favorites"), memberId), Map.of("movieId", movieId))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.code").value("COMMON201"))
                .andExpect(jsonPath("$.result.favoriteId").isNumber())
                .andExpect(jsonPath("$.result.movieId").value(movieId))
                .andExpect(jsonPath("$.result.createdAt").isString())
                .andExpect(jsonPath("$.result.member").doesNotExist());
    }

    private int count(String table, long memberId, long movieId) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + table + " WHERE member_id = ? AND movie_id = ?",
                Integer.class, memberId, movieId);
    }

    @Test void favoriteLifecycleAndDuplicateFailure() throws Exception {
        mvc.perform(as(get("/favorites"), 1)).andExpect(status().isOk()).andExpect(jsonPath("$.result").isEmpty());
        mvc.perform(as(get("/favorites/550"), 1)).andExpect(jsonPath("$.result.isFavorite").value(false));
        favorite(1, 550);
        json(as(post("/favorites"), 1), Map.of("movieId", 550))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("FAVORITE409_1"));
        mvc.perform(as(get("/favorites/550"), 1)).andExpect(jsonPath("$.result.isFavorite").value(true));
        mvc.perform(as(get("/favorites"), 1)).andExpect(jsonPath("$.result.length()").value(1));
        mvc.perform(as(delete("/favorites/550"), 1)).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result").value(org.hamcrest.Matchers.nullValue()));
        mvc.perform(as(get("/favorites/550"), 1)).andExpect(jsonPath("$.result.isFavorite").value(false));
        mvc.perform(as(get("/favorites"), 1)).andExpect(jsonPath("$.result").isEmpty());
        mvc.perform(as(delete("/favorites/550"), 1)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("FAVORITE404_1"));
    }

    @Test void membersHaveIndependentFavoritesAndNewestFirst() throws Exception {
        favorite(1, 550);
        favorite(1, 238);
        favorite(2, 550);
        mvc.perform(as(get("/favorites"), 1)).andExpect(jsonPath("$.result.length()").value(2))
                .andExpect(jsonPath("$.result[0].movieId").value(238))
                .andExpect(jsonPath("$.result[1].movieId").value(550));
        mvc.perform(as(get("/favorites"), 2)).andExpect(jsonPath("$.result.length()").value(1));
        mvc.perform(as(delete("/favorites/238"), 2)).andExpect(status().isNotFound());
        assertThat(count("favorite", 1, 238)).isEqualTo(1);
        mvc.perform(as(delete("/favorites/550"), 2)).andExpect(status().isOk());
        assertThat(count("favorite", 1, 550)).isEqualTo(1);
    }

    @Test void allNewEndpointsRequireAuthentication() throws Exception {
        for (MockHttpServletRequestBuilder request : new MockHttpServletRequestBuilder[]{
                post("/favorites"), get("/favorites"), get("/favorites/550"), delete("/favorites/550"), post("/movie-records")}) {
            mvc.perform(request).andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("AUTH401_2"));
        }
    }

    @Test void validatesFavoriteInputAndPath() throws Exception {
        for (Object body : new Object[]{Map.of(), Map.of("movieId", 0), Map.of("movieId", -1), Map.of("movieId", 1.5)}) {
            json(as(post("/favorites"), 1), body).andExpect(status().isBadRequest());
        }
        mvc.perform(as(get("/favorites/0"), 1)).andExpect(status().isBadRequest());
        mvc.perform(as(delete("/favorites/abc"), 1)).andExpect(status().isBadRequest());
        json(as(post("/favorites"), 999), Map.of("movieId", 550)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MEMBER404_1"));
    }

    @Test void recordSavesBothAndRepeatedRequestChangesNeither() throws Exception {
        Map<String, Object> body = Map.of("movieId", 238, "score", 5, "comment", "영화 기록", "addToFavorites", true);
        json(as(post("/movie-records"), 3), body).andExpect(status().isCreated())
                .andExpect(jsonPath("$.result.rating.movieId").value(238))
                .andExpect(jsonPath("$.result.favorite.movieId").value(238));
        json(as(post("/movie-records"), 3), body).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RATING409_1"));
        assertThat(count("rating", 3, 238)).isEqualTo(1);
        assertThat(count("favorite", 3, 238)).isEqualTo(1);
    }

    @Test void recordWithoutFavoriteReturnsNullEvenIfFavoriteAlreadyExists() throws Exception {
        favorite(3, 238);
        json(as(post("/movie-records"), 3), Map.of("movieId", 238, "score", 4, "addToFavorites", false))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.result.rating.score").value(4))
                .andExpect(jsonPath("$.result.favorite").value(org.hamcrest.Matchers.nullValue()));
        assertThat(count("rating", 3, 238)).isEqualTo(1);
        assertThat(count("favorite", 3, 238)).isEqualTo(1);
        json(as(post("/movie-records"), 3), Map.of("movieId", 155, "score", 3, "addToFavorites", false))
                .andExpect(status().isCreated());
        assertThat(count("favorite", 3, 155)).isZero();
    }

    @Test void existingFavoriteRejectsRecordBeforeRatingSave() throws Exception {
        favorite(3, 238);
        json(as(post("/movie-records"), 3), Map.of("movieId", 238, "score", 5, "addToFavorites", true))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("FAVORITE409_1"));
        assertThat(count("rating", 3, 238)).isZero();
    }

    @Test void databaseUniqueConstraintRollsBackEarlierRatingInsert() throws Exception {
        favorite(3, 238);
        // 동시 요청처럼 사전 검사만 통과시킨 뒤 실제 복합 UNIQUE 제약을 만나게 합니다.
        doReturn(false).when(favorites).existsByMember_MemberIdAndMovieId(3L, 238L);
        json(as(post("/movie-records"), 3), Map.of("movieId", 238, "score", 5, "addToFavorites", true))
                .andExpect(status().isConflict());
        assertThat(count("rating", 3, 238)).isZero();
        assertThat(count("favorite", 3, 238)).isEqualTo(1);
    }

    @Test void runtimeFailureAfterActualRatingInsertRollsBackTransaction() throws Exception {
        doAnswer(invocation -> {
            // 메서드 밖의 조회와 달리, 여기서는 같은 트랜잭션의 INSERT가 보입니다.
            assertThat(count("rating", 3, 238)).isEqualTo(1);
            throw new com.umc.movie.exception.ApiException(org.springframework.http.HttpStatus.CONFLICT,
                    "TEST409", "테스트에서만 발생시키는 저장 실패");
        }).when(favorites).save(any(Favorite.class));
        json(as(post("/movie-records"), 3), Map.of("movieId", 238, "score", 5, "addToFavorites", true))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("TEST409"));
        assertThat(count("rating", 3, 238)).isZero();
        assertThat(count("favorite", 3, 238)).isZero();
    }

    @Test void validatesRecordBodyBeforeWritingEitherTable() throws Exception {
        for (Object body : new Object[]{Map.of("movieId", 238, "score", 5),
                Map.of("movieId", 0, "score", 5, "addToFavorites", true),
                Map.of("movieId", 238, "score", 6, "addToFavorites", true),
                Map.of("movieId", 238, "score", 3.5, "addToFavorites", true),
                Map.of("movieId", 238, "score", 5, "comment", "가".repeat(501), "addToFavorites", true)}) {
            json(as(post("/movie-records"), 3), body).andExpect(status().isBadRequest());
        }
        assertThat(count("rating", 3, 238)).isZero();
        assertThat(count("favorite", 3, 238)).isZero();
    }

    @Test void swaggerShowsNewOperationsWithBearerSecurity() throws Exception {
        String spec = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var paths = mapper.readTree(spec).path("paths");
        assertThat(paths.path("/favorites").has("post")).isTrue();
        assertThat(paths.path("/favorites").has("get")).isTrue();
        assertThat(paths.path("/favorites/{movieId}").has("delete")).isTrue();
        assertThat(paths.path("/favorites/{movieId}").has("get")).isTrue();
        assertThat(paths.path("/movie-records").path("post").path("security").toString()).contains("bearerAuth");
    }
}
