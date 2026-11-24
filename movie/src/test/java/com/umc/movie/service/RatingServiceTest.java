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
    @DisplayName("이미 평점을 남긴 영화에 다시 등록하면 409 RATING409_1 예외를 던지고 저장하지 않는다")
    void createRatingWhenDuplicated() {
        // given: 1번 회원이 550번 영화에 이미 평점을 남긴 상황
        given(memberRepository.findById(1L)).willReturn(Optional.of(createMember(1L)));
        given(ratingRepository.existsByMember_MemberIdAndMovieId(1L, 550L)).willReturn(true);

        // when & then: 상태 코드와 code가 모두 약속한 값이고, 저장은 시도조차 하지 않는다
        assertThatThrownBy(() -> ratingService.create(1L, new CreateRatingRequest(550L, 5, "최고예요")))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT)
                .hasFieldOrPropertyWithValue("code", "RATING409_1");

        verify(ratingRepository, never()).save(any(Rating.class));
    }

    @Test
    @DisplayName("남의 평점을 수정하려 하면 403 RATING403_1 예외를 던지고 평점을 바꾸지 않는다")
    void updateRatingWhenNotOwner() {
        // given: 10번 평점의 주인은 1번 회원
        Rating rating = Rating.builder()
                .member(createMember(1L))
                .movieId(550L)
                .score(5)
                .comment("최고예요")
                .build();
        given(ratingRepository.findById(10L)).willReturn(Optional.of(rating));

        // when & then: 2번 회원이 수정하려 하면 막히고, 원래 평점은 그대로 남아 있다
        assertThatThrownBy(() -> ratingService.update(2L, 10L, new UpdateRatingRequest(1, "별로예요")))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.FORBIDDEN)
                .hasFieldOrPropertyWithValue("code", "RATING403_1");

        assertThat(rating.getScore()).isEqualTo(5);
        assertThat(rating.getComment()).isEqualTo("최고예요");
        verify(ratingRepository, never()).save(any(Rating.class));
    }

    // memberId는 DB가 채워주는 값이라 빌더로 넣을 수 없어, 테스트에서만 직접 채웁니다.
    private Member createMember(Long memberId) {
        Member member = Member.builder()
                .email("challenger@umc.com")
                .password("암호화된비밀번호")
                .nickname("영화조아")
                .build();
        ReflectionTestUtils.setField(member, "memberId", memberId);
        return member;
    }
}
