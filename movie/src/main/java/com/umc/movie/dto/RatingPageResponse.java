package com.umc.movie.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record RatingPageResponse(
        List<RatingResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static RatingPageResponse from(Page<RatingResponse> page) {
        return new RatingPageResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}