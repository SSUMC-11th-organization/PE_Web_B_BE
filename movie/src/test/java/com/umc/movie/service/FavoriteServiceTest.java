package com.umc.movie.service;

import com.umc.movie.dto.CreateFavoriteRequest;
import com.umc.movie.entity.Favorite;
import com.umc.movie.entity.Member;
import com.umc.movie.exception.ApiException;
import com.umc.movie.repository.FavoriteRepository;
import com.umc.movie.repository.MemberRepository;
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
class FavoriteServiceTest {
    @Mock FavoriteRepository favoriteRepository;
    @Mock MemberRepository memberRepository;
    @InjectMocks FavoriteService favoriteService;

    private final Member member = new Member("week9@umc.com", "test-hash", "구주차");

    @Test
    @DisplayName("아직 즐겨찾기하지 않은 영화면 해당 회원과 영화로 저장한다")
    void createsFavoriteForMember() {
        // given
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(favoriteRepository.save(any(Favorite.class))).willAnswer(call -> call.getArgument(0));

        // when
        var response = favoriteService.create(1L, new CreateFavoriteRequest(550L));

        // then
        assertThat(response.movieId()).isEqualTo(550L);
        verify(favoriteRepository).save(argThat(favorite ->
                favorite.getMember() == member && favorite.getMovieId().equals(550L)));
    }

    @Test
    @DisplayName("즐겨찾기 중복이면 409 FAVORITE409_1을 던지고 저장하지 않는다")
    void rejectsDuplicateWithoutSaving() {
        // given
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(favoriteRepository.existsByMember_MemberIdAndMovieId(1L, 550L)).willReturn(true);

        // when / then
        assertThatThrownBy(() -> favoriteService.create(1L, new CreateFavoriteRequest(550L)))
                .isInstanceOfSatisfying(ApiException.class, error -> {
                    assertThat(error.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(error.getCode()).isEqualTo("FAVORITE409_1");
                });
        verify(favoriteRepository, never()).save(any());
    }
}
