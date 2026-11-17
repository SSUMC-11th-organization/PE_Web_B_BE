package com.umc.study.dto;

// addToFavorites가 false면 favorite은 null
public record MovieRecordResponse(
        RatingResponse rating,
        FavoriteResponse favorite
) {}
