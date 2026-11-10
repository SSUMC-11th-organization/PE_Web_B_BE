package com.umc.study.service;

import com.umc.study.apiPayload.code.ErrorCode;
import com.umc.study.apiPayload.exception.CustomException;
import com.umc.study.dto.CreateRatingRequest;
import com.umc.study.dto.RatingResponse;
import com.umc.study.dto.UpdateRatingRequest;
import com.umc.study.entity.Member;
import com.umc.study.entity.Rating;
import com.umc.study.repository.MemberRepository;
import com.umc.study.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * [7주차] memberId는 이제 클라이언트가 아니라 토큰(인터셉터)에서 옵니다.
 * "누가"가 모든 동작의 주어이므로 인자 순서를 (memberId, ...)로 통일했습니다.
 */
@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final MemberRepository memberRepository;

    // [5주차] 유저별 평점 조회 (공개): 없는 유저면 404
    @Transactional(readOnly = true)
    public List<RatingResponse> getRatingsByMember(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }
        return ratingRepository.findByMember_MemberIdOrderByRatingIdDesc(memberId).stream()
                .map(RatingResponse::from)   // 트랜잭션 안에서 DTO로 변환
                .toList();
    }

    // 평점 등록 (7주차부터 RatingResponse 전체를 반환)
    @Transactional
    public RatingResponse create(Long memberId, CreateRatingRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // (member_id, movie_id) UNIQUE 제약이 최종 방어선이지만,
        // 우리가 정한 메시지로 응답하기 위해 먼저 확인합니다.
        if (ratingRepository.existsByMember_MemberIdAndMovieId(memberId, request.movieId())) {
            throw new CustomException(ErrorCode.DUPLICATE_RATING);
        }

        Rating rating = Rating.builder()
                .member(member)
                .movieId(request.movieId())
                .score(request.score())
                .comment(request.comment())
                .build();

        return RatingResponse.from(ratingRepository.save(rating));
    }

    // 평점 수정 (7주차부터 수정된 RatingResponse를 반환)
    @Transactional
    public RatingResponse update(Long memberId, Long ratingId, UpdateRatingRequest request) {
        Rating rating = findOwnedRating(memberId, ratingId);
        rating.update(request.score(), request.comment());
        ratingRepository.flush();   // UPDATE를 먼저 실행해 갱신된 updatedAt을 응답에 담습니다
        return RatingResponse.from(rating);
    }

    // 평점 삭제
    @Transactional
    public void delete(Long memberId, Long ratingId) {
        Rating rating = findOwnedRating(memberId, ratingId);
        ratingRepository.delete(rating);
    }

    /**
     * 평점을 찾고(없으면 404), 요청자의 것인지 확인합니다(아니면 403).
     * 순서가 중요합니다: 존재하지 않는 평점에 "권한 없음"이라고 답하면 앞뒤가 맞지 않습니다.
     * 인증(누구인가)은 인터셉터가, 인가(네 것인가)는 DB를 봐야 아는 서비스가 담당합니다.
     */
    private Rating findOwnedRating(Long memberId, Long ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new CustomException(ErrorCode.RATING_NOT_FOUND));

        if (!rating.isWrittenBy(memberId)) {
            throw new CustomException(ErrorCode.RATING_FORBIDDEN);
        }
        return rating;
    }
}
