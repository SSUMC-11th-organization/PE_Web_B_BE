package com.umc.study.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.study.dto.CreateRatingRequest;
import com.umc.study.entity.Member;
import com.umc.study.repository.MemberRepository;
import com.umc.study.repository.RatingRepository;
import com.umc.study.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * [9주차 미션] 평점 API 통합 테스트
 * - 토큰 없이 POST /ratings → 401 AUTH401_2
 * - 토큰을 담아 POST /ratings → 201 COMMON201
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RatingApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String authorizationHeader;

    @BeforeEach
    void setUp() {
        Member member = memberRepository.save(Member.builder()
                .email("rater@umc.com")
                .password("$2a$10$abcdefghijklmnopqrstuv")
                .nickname("평점왕")
                .build());

        authorizationHeader = "Bearer " + jwtUtil.createAccessToken(member.getMemberId(), member.getEmail());
    }

    @Test
    @DisplayName("토큰 없이 평점을 등록하면 401과 AUTH401_2를 응답하고 평점은 저장되지 않는다")
    void createRatingWithoutToken() throws Exception {
        long before = ratingRepository.count();

        mockMvc.perform(post("/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateRatingRequest(680L, 5, "인생 영화"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("AUTH401_2"));

        assertThat(ratingRepository.count()).isEqualTo(before);
    }

    @Test
    @DisplayName("토큰을 담아 평점을 등록하면 201과 COMMON201, 등록한 영화 ID와 별점을 응답한다")
    void createRating() throws Exception {
        mockMvc.perform(post("/ratings")
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateRatingRequest(680L, 5, "인생 영화"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON201"))
                .andExpect(jsonPath("$.result.movieId").value(680))
                .andExpect(jsonPath("$.result.score").value(5));
    }
}
