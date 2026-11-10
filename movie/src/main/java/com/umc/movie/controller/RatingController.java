package com.umc.movie.controller;
import com.umc.movie.dto.RatingResponse;
import com.umc.movie.interceptor.AuthInterceptor;
import com.umc.movie.dto.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.umc.movie.dto.CreateRatingRequest;
import com.umc.movie.dto.UpdateRatingRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import com.umc.movie.service.RatingService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class RatingController {
    private final RatingService ratingService;
    @GetMapping("/members/{memberId}/ratings")
    public List<RatingResponse> getMemberRatings(@PathVariable @Positive Long memberId) {
        return ratingService.getRatingsByMember(memberId);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/ratings")
    public ResponseEntity<ApiResponse<RatingResponse>> create(@RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
                                                   @Valid @RequestBody CreateRatingRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.created("평점 등록에 성공했습니다.", ratingService.create(memberId, request)));
    }

    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/ratings/{ratingId}")
    public ResponseEntity<ApiResponse<RatingResponse>> update(@RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
                                                   @PathVariable @Positive Long ratingId,
                                                   @Valid @RequestBody UpdateRatingRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("평점 수정에 성공했습니다.", ratingService.update(memberId, ratingId, request)));
    }

    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/ratings/{ratingId}")
    public ResponseEntity<ApiResponse<Void>> delete(@RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId, @PathVariable @Positive Long ratingId) {
        ratingService.delete(memberId, ratingId);
        return ResponseEntity.ok(ApiResponse.ok("평점을 삭제했습니다.", null));
    }
}
