package com.umc.study.repository;

import com.umc.study.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    // 내 즐겨찾기 목록, 최신 등록순
    List<Favorite> findByMember_MemberIdOrderByFavoriteIdDesc(Long memberId);

    // 해제할 즐겨찾기 찾기 (내 것만 조회되므로 403 검사가 따로 필요 없음)
    Optional<Favorite> findByMember_MemberIdAndMovieId(Long memberId, Long movieId);

    // 중복 등록 사전 검사 / 즐겨찾기 여부 조회
    boolean existsByMember_MemberIdAndMovieId(Long memberId, Long movieId);
}
