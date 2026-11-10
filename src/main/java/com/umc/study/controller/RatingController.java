package com.umc.study.controller;

import com.umc.study.apiPayload.ApiResponse;
import com.umc.study.dto.CreateRatingRequest;
import com.umc.study.dto.RatingIdResponse;
import com.umc.study.dto.RatingResponse;
import com.umc.study.dto.UpdateRatingRequest;
import com.umc.study.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Rating", description = "평점 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class RatingController {

    private final RatingService ratingService;

    // GET http://localhost:8080/members/{memberId}/ratings
    @Operation(summary = "유저별 평점 조회", description = "최신순으로 반환 / 404 존재하지 않는 유저")
    @GetMapping("/{memberId}/ratings")
    public List<RatingResponse> getMemberRatings(
            @Parameter(description = "회원 ID", example = "1") @PathVariable Long memberId) {
        return ratingService.getRatingsByMember(memberId);
    }

    // POST http://localhost:8080/members/{memberId}/ratings
    @Operation(summary = "평점 등록",
            description = "201 성공 / 400 입력 검증 실패 / 404 존재하지 않는 유저 / 409 중복 평점(RATING4091)")
    @PostMapping("/{memberId}/ratings")
    public ResponseEntity<ApiResponse<RatingIdResponse>> create(
            @Parameter(description = "회원 ID", example = "1") @PathVariable Long memberId,
            @Valid @RequestBody CreateRatingRequest request) {
        Long ratingId = ratingService.create(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("평점 등록에 성공했습니다.", new RatingIdResponse(ratingId)));
    }

    // PATCH http://localhost:8080/members/{memberId}/ratings/{ratingId}
    @Operation(summary = "평점 수정",
            description = "200 성공 / 400 입력 검증 실패 / 403 본인 평점 아님 / 404 존재하지 않는 평점")
    @PatchMapping("/{memberId}/ratings/{ratingId}")
    public ResponseEntity<ApiResponse<Void>> update(
            @Parameter(description = "회원 ID", example = "1") @PathVariable Long memberId,
            @Parameter(description = "평점 ID", example = "1") @PathVariable Long ratingId,
            @Valid @RequestBody UpdateRatingRequest request) {
        ratingService.update(ratingId, memberId, request);
        return ResponseEntity.ok(ApiResponse.ok("평점 수정에 성공했습니다.", null));
    }

    // DELETE http://localhost:8080/members/{memberId}/ratings/{ratingId}
    @Operation(summary = "평점 삭제", description = "204 성공 / 403 본인 평점 아님 / 404 존재하지 않는 평점")
    @DeleteMapping("/{memberId}/ratings/{ratingId}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "회원 ID", example = "1") @PathVariable Long memberId,
            @Parameter(description = "평점 ID", example = "1") @PathVariable Long ratingId) {
        ratingService.delete(ratingId, memberId);
        return ResponseEntity.noContent().build();
    }
}
