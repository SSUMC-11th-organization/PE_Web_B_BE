package com.umc.study.service;

import com.umc.study.apiPayload.code.ErrorCode;
import com.umc.study.apiPayload.exception.CustomException;
import com.umc.study.dto.CreateMovieRecordRequest;
import com.umc.study.dto.FavoriteResponse;
import com.umc.study.dto.MovieRecordResponse;
import com.umc.study.dto.RatingResponse;
import com.umc.study.entity.Favorite;
import com.umc.study.entity.Member;
import com.umc.study.entity.Rating;
import com.umc.study.repository.FavoriteRepository;
import com.umc.study.repository.MemberRepository;
import com.umc.study.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * [8주차] "영화 기록" = 평점 저장 + (선택) 즐겨찾기 저장을 하나의 트랜잭션으로 묶습니다.
 *
 * RatingService.create()와 FavoriteService.create()를 컨트롤러에서 연달아 부르면
 * 각각 별도 트랜잭션으로 커밋되어, 즐겨찾기가 실패해도 평점만 남을 수 있습니다.
 * 하나의 @Transactional 메서드 안에서 두 저장을 하면
 * - 정상 종료 → 둘 다 commit
 * - 런타임 예외(CustomException, DataIntegrityViolationException 등)가 밖으로 나감 → 둘 다 rollback
 */
@Service
@RequiredArgsConstructor
public class MovieRecordService {

    private final MemberRepository memberRepository;
    private final RatingRepository ratingRepository;
    private final FavoriteRepository favoriteRepository;

    @Transactional
    public MovieRecordResponse create(Long memberId, CreateMovieRecordRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 저장하기 전에 두 규칙을 모두 먼저 검사해 불필요한 INSERT/rollback을 줄입니다.
        if (ratingRepository.existsByMember_MemberIdAndMovieId(memberId, request.movieId())) {
            throw new CustomException(ErrorCode.DUPLICATE_RATING);
        }
        if (request.addToFavorites()
                && favoriteRepository.existsByMember_MemberIdAndMovieId(memberId, request.movieId())) {
            throw new CustomException(ErrorCode.DUPLICATE_FAVORITE);
        }

        Rating savedRating = ratingRepository.save(Rating.builder()
                .member(member)
                .movieId(request.movieId())
                .score(request.score())
                .comment(request.comment())
                .build());

        // 여기서 예외가 나면(예: 동시 요청으로 UNIQUE 위반) 위에서 저장한 평점도 함께 rollback됩니다.
        FavoriteResponse favoriteResponse = null;
        if (request.addToFavorites()) {
            Favorite savedFavorite = favoriteRepository.save(Favorite.builder()
                    .member(member)
                    .movieId(request.movieId())
                    .build());
            favoriteResponse = FavoriteResponse.from(savedFavorite);
        }

        return new MovieRecordResponse(RatingResponse.from(savedRating), favoriteResponse);
    }
}
