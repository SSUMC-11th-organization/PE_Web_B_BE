// src/test/java/com/umc/movie/service/RatingServiceTest.java
package com.umc.movie.service;

import com.umc.movie.dto.CreateRatingRequest;
import com.umc.movie.dto.UpdateRatingRequest;
import com.umc.movie.entity.Member;
import com.umc.movie.entity.Rating;
import com.umc.movie.exception.ApiException;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.repository.RatingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private RatingService ratingService;

    @Test
    @DisplayName("같은 영화에 평점을 중복 등록하면 409 RATING409_1 예외를 던지고 저장하지 않는다")
    void createRatingWhenDuplicated() {
        // given: 1번 회원이 550번 영화에 이미 평점을 남겨 둔 상황
        Member member = Member.builder()
                .email("challenger@umc.com")
                .password("암호화된비밀번호")
                .nickname("영화조아")
                .build();
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(ratingRepository.existsByMember_MemberIdAndMovieId(1L, 550L)).willReturn(true);

        // when & then: 상태 코드와 code가 함께 실린 예외가 나고, 저장은 시도하지 않는다
        assertThatThrownBy(() -> ratingService.create(1L, new CreateRatingRequest(550L, 5, "또 봐도 좋다")))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT)
                .hasFieldOrPropertyWithValue("code", "RATING409_1");

        verify(ratingRepository, never()).save(any(Rating.class));
    }

    @Test
    @DisplayName("다른 회원의 평점을 수정하려 하면 403 RATING403_1 예외를 던지고 평점은 그대로 둔다")
    void updateRatingOfOtherMember() {
        // given: 10번 평점은 1번 회원의 것이고, 2번 회원이 수정을 요청하는 상황
        Member owner = Member.builder()
                .email("owner@umc.com")
                .password("암호화된비밀번호")
                .nickname("주인")
                .build();
        // memberId는 DB가 채워주는 값이라 테스트에서는 직접 넣어줍니다
        ReflectionTestUtils.setField(owner, "memberId", 1L);

        Rating rating = Rating.builder()
                .member(owner)
                .movieId(550L)
                .score(4)
                .comment("원래 후기")
                .build();
        given(ratingRepository.findById(10L)).willReturn(Optional.of(rating));

        // when & then: 인가 실패 예외가 난다
        assertThatThrownBy(() -> ratingService.update(2L, 10L, new UpdateRatingRequest(1, "남이 바꾼 후기")))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.FORBIDDEN)
                .hasFieldOrPropertyWithValue("code", "RATING403_1");

        // 그리고 평점 내용은 바뀌지 않았고 저장도 호출되지 않았다
        assertThat(rating.getScore()).isEqualTo(4);
        assertThat(rating.getComment()).isEqualTo("원래 후기");
        verify(ratingRepository, never()).save(any(Rating.class));
    }
}
