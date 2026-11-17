package com.umc.study.dto;

import com.umc.study.entity.Favorite;

import java.time.LocalDateTime;

public record FavoriteResponse(
        Long favoriteId,
        Long movieId,
        LocalDateTime createdAt
) {
    // 엔티티 → 응답 DTO 변환
    public static FavoriteResponse from(Favorite favorite) {
        return new FavoriteResponse(
                favorite.getFavoriteId(),
                favorite.getMovieId(),
                favorite.getCreatedAt()
        );
    }
}
