package com.umc.study.controller;

import com.umc.study.dto.RatingResponse;
import com.umc.study.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Rating", description = "평점 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class RatingController {

    private final RatingService ratingService;

    // GET http://localhost:8080/members/{memberId}/ratings
    @Operation(summary = "유저별 평점 조회", description = "최신순으로 반환")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 유저")
    @GetMapping("/{memberId}/ratings")
    public List<RatingResponse> getMemberRatings(
            @Parameter(description = "회원 ID", example = "1") @PathVariable Long memberId) {
        return ratingService.getRatingsByMember(memberId);
    }
}
