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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {
    @Mock RatingRepository ratingRepository;
    @Mock MemberRepository memberRepository;
    @InjectMocks RatingService ratingService;

    @Test
    @DisplayName("같은 영화에 평점을 중복 등록하면 409 RATING409_1을 던지고 저장하지 않는다")
    void rejectsDuplicateRatingWithoutSaving() {
        // given
        Member member = new Member("week9@umc.com", "test-hash", "구주차");
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(ratingRepository.existsByMember_MemberIdAndMovieId(1L, 550L)).willReturn(true);

        // when / then
        assertThatThrownBy(() -> ratingService.create(1L, new CreateRatingRequest(550L, 5, "후기")))
                .isInstanceOfSatisfying(ApiException.class, error -> {
                    assertThat(error.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(error.getCode()).isEqualTo("RATING409_1");
                });
        verify(ratingRepository, never()).save(any());
    }

    @Test
    @DisplayName("남의 평점을 수정하면 403 RATING403_1을 던지고 원래 평점을 유지한다")
    void rejectsOtherMembersUpdateWithoutChangingRating() {
        // given
        Member owner = mock(Member.class);
        given(owner.getMemberId()).willReturn(1L);
        Rating rating = new Rating(owner, 550L, 5, "원래 후기");
        given(ratingRepository.findById(10L)).willReturn(Optional.of(rating));

        // when / then
        assertThatThrownBy(() -> ratingService.update(2L, 10L, new UpdateRatingRequest(1, "변경 시도")))
                .isInstanceOfSatisfying(ApiException.class, error -> {
                    assertThat(error.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(error.getCode()).isEqualTo("RATING403_1");
                });
        assertThat(rating.getScore()).isEqualTo(5);
        assertThat(rating.getComment()).isEqualTo("원래 후기");
        verify(ratingRepository, never()).save(any());
    }
}
