package com.umc.study.service;

import com.umc.study.apiPayload.code.ErrorCode;
import com.umc.study.apiPayload.exception.CustomException;
import com.umc.study.dto.CreateRatingRequest;
import com.umc.study.dto.UpdateRatingRequest;
import com.umc.study.entity.Member;
import com.umc.study.entity.Rating;
import com.umc.study.repository.MemberRepository;
import com.umc.study.repository.RatingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * [9주차 미션] 평점 서비스 단위 테스트
 * - 같은 영화에 중복 등록 → RATING409_1
 * - 남의 평점 수정 → RATING403_1
 */
@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private RatingService ratingService;

    // memberId는 DB가 채워주는 값이라 빌더로 못 넣습니다. 테스트에서는 리플렉션으로 직접 넣습니다.
    private Member memberWithId(Long memberId, String nickname) {
        Member member = Member.builder()
                .email(nickname + "@umc.com")
                .password("암호화된비밀번호")
                .nickname(nickname)
                .build();
        ReflectionTestUtils.setField(member, "memberId", memberId);
        return member;
    }

    @Test
    @DisplayName("같은 영화에 이미 평점을 남겼으면 RATING409_1 예외를 던지고 저장하지 않는다")
    void createRatingWhenDuplicated() {
        // given: 1번 회원이 680번 영화에 이미 평점을 남긴 상황
        given(memberRepository.findById(1L)).willReturn(Optional.of(memberWithId(1L, "영화조아")));
        given(ratingRepository.existsByMember_MemberIdAndMovieId(1L, 680L)).willReturn(true);

        // when & then: 같은 영화에 다시 등록하면 409 예외, 저장은 시도조차 하지 않는다
        assertThatThrownBy(() -> ratingService.create(1L, new CreateRatingRequest(680L, 5, "또 봐도 좋다")))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_RATING);

        assertThat(ErrorCode.DUPLICATE_RATING.getCode()).isEqualTo("RATING409_1");
        verify(ratingRepository, never()).save(any(Rating.class));
    }

    @Test
    @DisplayName("남이 작성한 평점을 수정하려 하면 RATING403_1 예외를 던지고 내용은 바뀌지 않는다")
    void updateRatingOfOtherMember() {
        // given: 10번 평점은 2번 회원(작성자)의 것
        Member writer = memberWithId(2L, "작성자");
        Rating othersRating = Rating.builder()
                .member(writer)
                .movieId(680L)
                .score(5)
                .comment("인생 영화")
                .build();
        given(ratingRepository.findById(10L)).willReturn(Optional.of(othersRating));

        // when & then: 1번 회원이 수정하려 하면 403 예외
        assertThatThrownBy(() -> ratingService.update(1L, 10L, new UpdateRatingRequest(1, "별로")))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RATING_FORBIDDEN);

        // 그리고 남의 평점 내용은 그대로 남아 있어야 한다
        assertThat(ErrorCode.RATING_FORBIDDEN.getCode()).isEqualTo("RATING403_1");
        assertThat(othersRating.getScore()).isEqualTo(5);
        assertThat(othersRating.getComment()).isEqualTo("인생 영화");
        verify(ratingRepository, never()).flush();
    }
}
