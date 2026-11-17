package com.umc.study.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [8주차] 즐겨찾기 = 영화의 속성이 아니라 "회원과 영화의 관계"
 * (member_id, movie_id) 복합 UNIQUE: 같은 회원이 같은 영화를 두 번 즐겨찾기하지 못하게 DB도 막습니다.
 * movie_id만 UNIQUE라면 서비스 전체에서 한 영화를 한 명만 즐겨찾기할 수 있게 되므로 반드시 한 쌍으로 묶습니다.
 */
@Entity
@Table(name = "favorite",
        uniqueConstraints = @UniqueConstraint(name = "uk_favorite_member_movie",
                columnNames = {"member_id", "movie_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Favorite extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorite_id")
    private Long favoriteId;

    @ManyToOne(fetch = FetchType.LAZY)                    // 즐겨찾기 N : 회원 1 (주인은 로그인한 회원)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "movie_id", nullable = false)          // TMDB id, 우리 DB의 외래 키 아님
    private Long movieId;

    @Builder
    public Favorite(Member member, Long movieId) {
        this.member = member;
        this.movieId = movieId;
    }
}
