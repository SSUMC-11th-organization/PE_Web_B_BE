package com.umc.study.service;

import com.umc.study.apiPayload.code.ErrorCode;
import com.umc.study.apiPayload.exception.CustomException;
import com.umc.study.dto.CreateFavoriteRequest;
import com.umc.study.dto.FavoriteResponse;
import com.umc.study.dto.FavoriteStatusResponse;
import com.umc.study.entity.Favorite;
import com.umc.study.entity.Member;
import com.umc.study.repository.FavoriteRepository;
import com.umc.study.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * [8주차] 즐겨찾기. memberId는 항상 토큰(인터셉터)에서 옵니다.
 * 모든 조회 조건에 memberId가 들어가므로 "내 즐겨찾기"만 다룰 수 있습니다.
 */
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final MemberRepository memberRepository;

    // 즐겨찾기 등록
    @Transactional
    public FavoriteResponse create(Long memberId, CreateFavoriteRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 사전 검사: 사용자에게 명확한 FAVORITE409_1을 주기 위함.
        // 동시에 들어온 요청은 이 검사를 함께 통과할 수 있으므로
        // 최종 방어선은 DB의 uk_favorite_member_movie (GlobalExceptionHandler가 같은 코드로 변환)
        if (favoriteRepository.existsByMember_MemberIdAndMovieId(memberId, request.movieId())) {
            throw new CustomException(ErrorCode.DUPLICATE_FAVORITE);
        }

        Favorite favorite = Favorite.builder()
                .member(member)
                .movieId(request.movieId())
                .build();

        return FavoriteResponse.from(favoriteRepository.save(favorite));
    }

    // 내 즐겨찾기 목록 (비어 있어도 정상 → 빈 배열)
    @Transactional(readOnly = true)
    public List<FavoriteResponse> getMyFavorites(Long memberId) {
        return favoriteRepository.findByMember_MemberIdOrderByFavoriteIdDesc(memberId).stream()
                .map(FavoriteResponse::from)
                .toList();
    }

    // 특정 영화 즐겨찾기 여부 (안 했어도 정상 → isFavorite: false)
    @Transactional(readOnly = true)
    public FavoriteStatusResponse getStatus(Long memberId, Long movieId) {
        return new FavoriteStatusResponse(
                favoriteRepository.existsByMember_MemberIdAndMovieId(memberId, movieId));
    }

    // 즐겨찾기 해제: 내 것 중에서만 찾으므로 남의 즐겨찾기는 애초에 조회되지 않음
    @Transactional
    public void delete(Long memberId, Long movieId) {
        Favorite favorite = favoriteRepository.findByMember_MemberIdAndMovieId(memberId, movieId)
                .orElseThrow(() -> new CustomException(ErrorCode.FAVORITE_NOT_FOUND));

        favoriteRepository.delete(favorite);
    }
}
