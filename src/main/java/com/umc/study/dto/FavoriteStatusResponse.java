package com.umc.study.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

// boolean 컴포넌트의 "is" 접두사가 JSON에서 빠지지 않도록 이름을 명시합니다 (ApiResponse의 isSuccess와 동일)
public record FavoriteStatusResponse(
        @JsonProperty("isFavorite") boolean isFavorite
) {}
