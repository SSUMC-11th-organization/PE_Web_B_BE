package com.umc.study.repository;

import com.umc.study.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    // member 필드 안의 memberId로 검색, 최신순
    List<Rating> findByMember_MemberIdOrderByRatingIdDesc(Long memberId);

    // 같은 회원이 같은 영화에 이미 평점을 남겼는지
    boolean existsByMember_MemberIdAndMovieId(Long memberId, Long movieId);
}
