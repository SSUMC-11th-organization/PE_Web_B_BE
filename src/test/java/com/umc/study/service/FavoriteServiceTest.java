package com.umc.study.service;

import com.umc.study.apiPayload.code.ErrorCode;
import com.umc.study.apiPayload.exception.CustomException;
import com.umc.study.dto.CreateFavoriteRequest;
import com.umc.study.dto.FavoriteResponse;
import com.umc.study.entity.Favorite;
import com.umc.study.entity.Member;
import com.umc.study.repository.FavoriteRepository;
import com.umc.study.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * [9주차 실습 4] 단위 테스트: DB 없이 FavoriteService의 "판단"만 확인합니다.
 * Repository 자리에는 Mockito가 만든 가짜(Mock)를 끼워 넣습니다.
 */
@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository; // 진짜 DB 대신 들어가는 가짜 Repository

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private FavoriteService favoriteService; // 위의 가짜 두 개를 주입받은 진짜 Service

    private final Member member = Member.builder()
            .email("challenger@umc.com")
            .password("암호화된비밀번호")
            .nickname("영화조아")
            .build();

    @Test
    @DisplayName("아직 즐겨찾기하지 않은 영화면 즐겨찾기를 저장한다")
    void createFavorite() {
        // given: 회원은 존재하고, 아직 이 영화를 즐겨찾기하지 않은 상황
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(favoriteRepository.existsByMember_MemberIdAndMovieId(1L, 550L)).willReturn(false);
        given(favoriteRepository.save(any(Favorite.class)))
                .willReturn(Favorite.builder().member(member).movieId(550L).build());

        // when: 즐겨찾기를 등록하면
        FavoriteResponse response = favoriteService.create(1L, new CreateFavoriteRequest(550L));

        // then: 저장된 영화 ID가 응답에 담기고, 저장이 실제로 호출된다
        assertThat(response.movieId()).isEqualTo(550L);
        verify(favoriteRepository).save(any(Favorite.class));
    }

    @Test
    @DisplayName("이미 즐겨찾기한 영화면 FAVORITE409_1 예외를 던지고 저장하지 않는다")
    void createFavoriteWhenDuplicated() {
        // given: 같은 회원이 같은 영화를 이미 즐겨찾기해 둔 상황
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(favoriteRepository.existsByMember_MemberIdAndMovieId(1L, 550L)).willReturn(true);

        // when & then: 예외가 나고, 저장은 시도조차 하지 않는다
        assertThatThrownBy(() -> favoriteService.create(1L, new CreateFavoriteRequest(550L)))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_FAVORITE);

        verify(favoriteRepository, never()).save(any(Favorite.class));
    }
}
