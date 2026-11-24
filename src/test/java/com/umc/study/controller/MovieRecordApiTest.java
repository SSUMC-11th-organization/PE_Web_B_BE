package com.umc.study.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.study.dto.CreateMovieRecordRequest;
import com.umc.study.entity.Favorite;
import com.umc.study.entity.Member;
import com.umc.study.repository.FavoriteRepository;
import com.umc.study.repository.MemberRepository;
import com.umc.study.repository.RatingRepository;
import com.umc.study.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.willReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * [9주차 선택 미션] POST /movie-records의 트랜잭션이 정말 되돌리는지 증명합니다.
 *
 * 주의: 이 클래스에는 @Transactional을 붙이지 않습니다.
 * 테스트 전체를 트랜잭션으로 감싸면 서비스의 @Transactional이 그 안에 합류해버려서
 * "서비스 트랜잭션이 스스로 rollback 했는가"를 확인할 수 없습니다.
 * 대신 @AfterEach에서 직접 데이터를 지웁니다.
 */
@SpringBootTest
@AutoConfigureMockMvc
class MovieRecordApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RatingRepository ratingRepository;

    // 진짜 Repository를 감싼 스파이: 평소엔 그대로 동작하고, 필요한 메서드만 대답을 바꿀 수 있습니다.
    @MockitoSpyBean
    private FavoriteRepository favoriteRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private Member member;
    private String authorizationHeader;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.builder()
                .email("record@umc.com")
                .password("$2a$10$abcdefghijklmnopqrstuv")
                .nickname("기록왕")
                .build());
        authorizationHeader = "Bearer " + jwtUtil.createAccessToken(member.getMemberId(), member.getEmail());

        // 이미 238번 영화를 즐겨찾기해 둔 상황
        favoriteRepository.save(Favorite.builder().member(member).movieId(238L).build());
    }

    @AfterEach
    void tearDown() {
        favoriteRepository.deleteAll();
        ratingRepository.deleteAll();
        memberRepository.deleteAll();
    }

    private String recordRequest() throws Exception {
        return objectMapper.writeValueAsString(
                new CreateMovieRecordRequest(238L, 5, "다시 보고 싶은 영화입니다.", true));
    }

    @Test
    @DisplayName("이미 즐겨찾기한 영화로 영화 기록을 저장하면 409 FAVORITE409_1로 실패하고 평점도 남지 않는다")
    void movieRecordFailsWhenAlreadyFavorite() throws Exception {
        long ratingCountBefore = ratingRepository.count();

        mockMvc.perform(post("/movie-records")
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recordRequest()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("FAVORITE409_1"));

        // "예외가 났다"에서 멈추지 않고, "데이터가 남지 않았다"까지 확인합니다.
        assertThat(ratingRepository.count()).isEqualTo(ratingCountBefore);
        assertThat(ratingRepository.existsByMember_MemberIdAndMovieId(member.getMemberId(), 238L)).isFalse();
    }

    @Test
    @DisplayName("평점 INSERT 후 즐겨찾기 INSERT가 UNIQUE 제약에 걸리면 먼저 저장한 평점까지 rollback된다")
    void movieRecordRollsBackRatingWhenFavoriteInsertFails() throws Exception {
        // given: 동시에 들어온 요청처럼 "아직 즐겨찾기 안 했다"는 사전 검사를 통과시킵니다.
        //        → 평점은 실제로 INSERT되고, 그 뒤 즐겨찾기 INSERT가 DB UNIQUE 제약(uk_favorite_member_movie)에 걸립니다.
        willReturn(false).given(favoriteRepository).existsByMember_MemberIdAndMovieId(anyLong(), anyLong());
        long ratingCountBefore = ratingRepository.count();

        // when: 영화 기록 저장 → 409
        mockMvc.perform(post("/movie-records")
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recordRequest()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.isSuccess").value(false));

        // then: 이미 INSERT됐던 평점도 트랜잭션과 함께 사라져야 합니다.
        //       MovieRecordService.create()의 @Transactional을 지우면 여기서 실패합니다.
        assertThat(ratingRepository.count()).isEqualTo(ratingCountBefore);
        assertThat(ratingRepository.existsByMember_MemberIdAndMovieId(member.getMemberId(), 238L)).isFalse();
    }
}
