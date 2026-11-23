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

@ExtendWith(MockitoExtension.class) // @Mock이 붙은 가짜 객체를 만들어주는 확장입니다
class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository; // 진짜 DB 대신 들어가는 가짜 Repository

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private RatingService ratingService; // 위의 가짜 두 개를 주입받은 진짜 Service

    @Test
    @DisplayName("같은 영화에 평점을 중복 등록하면 409와 RATING409_1 예외를 던지고 저장하지 않는다")
    void createRatingWhenDuplicated() {
        // given: 회원은 존재하고, 이미 이 영화에 평점을 남긴 상황
        Member member = Member.builder().email("challenger@umc.com").password("암호화된비밀번호").nickname("영화조아").build();
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(ratingRepository.existsByMember_MemberIdAndMovieId(1L, 550L)).willReturn(true);

        // when & then: RATING409_1 예외가 나고, 저장은 시도조차 하지 않는다
        assertThatThrownBy(() -> ratingService.create(1L, new CreateRatingRequest(550L, 5, "재밌어요")))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> {
                    ApiException exception = (ApiException) e;
                    assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(exception.getCode()).isEqualTo("RATING409_1");
                });

        verify(ratingRepository, never()).save(any(Rating.class));
    }

    @Test
    @DisplayName("남의 평점을 수정하려 하면 403과 RATING403_1 예외를 던지고 수정하지 않는다")
    void updateRatingOfOtherMember() {
        // given: 평점의 주인은 2번 회원인데, 1번 회원이 수정하려는 상황
        Member owner = Member.builder().email("owner@umc.com").password("암호화된비밀번호").nickname("주인").build();
        ReflectionTestUtils.setField(owner, "memberId", 2L); // 저장 전에는 memberId가 비어 있어서 직접 채워 줍니다
        Rating rating = Rating.builder().member(owner).movieId(550L).score(4).comment("좋아요").build();
        given(ratingRepository.findById(10L)).willReturn(Optional.of(rating));

        // when & then: RATING403_1 예외가 나고, 평점 값은 바뀌지 않는다
        assertThatThrownBy(() -> ratingService.update(1L, 10L, new UpdateRatingRequest(1, "별로예요")))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> {
                    ApiException exception = (ApiException) e;
                    assertThat(exception.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(exception.getCode()).isEqualTo("RATING403_1");
                });

        assertThat(rating.getScore()).isEqualTo(4);
        verify(ratingRepository, never()).save(any(Rating.class));
    }
}
