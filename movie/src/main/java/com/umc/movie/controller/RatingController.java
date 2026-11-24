// src/main/java/com/umc/movie/controller/RatingController.java
package com.umc.movie.controller;

import com.umc.movie.config.SwaggerConfig;
import com.umc.movie.config.SwaggerExamples;
import com.umc.movie.dto.ApiResponse;
import com.umc.movie.dto.CreateRatingRequest;
import com.umc.movie.dto.RatingResponse;
import com.umc.movie.dto.UpdateRatingRequest;
import com.umc.movie.interceptor.AuthInterceptor;
import com.umc.movie.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "평점", description = "영화 평점 등록/수정/삭제/조회 API")
public class RatingController {

    private final RatingService ratingService;

    // 5주차에 만든 유저별 평점 조회입니다. 공개 API라 토큰을 받지 않습니다.
    @Operation(
            summary = "유저별 평점 조회",
            description = "회원 한 명이 남긴 평점 목록을 최신순으로 조회합니다. 토큰이 없어도 호출할 수 있는 공개 API입니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "평점 목록 조회 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            [
                              {
                                "ratingId": 1, "movieId": 550, "score": 5, "comment": "다시 봐도 재미있어요.",
                                "createdAt": "2026-09-13T21:00:00", "updatedAt": "2026-09-13T21:00:00"
                              }
                            ]
                            """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "MEMBER404_1: 존재하지 않는 회원",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = SwaggerExamples.MEMBER404_1)))
    })
    @GetMapping("/members/{memberId}/ratings")
    public List<RatingResponse> getMemberRatings(@PathVariable Long memberId) {
        return ratingService.getRatingsByMember(memberId);
    }

    // 경로에서 /members/{memberId}가 사라지고 memberId는 인터셉터가 담아둔 값을 씁니다
    @Operation(
            summary = "평점 등록",
            description = "토큰의 주인인 회원이 TMDB 영화 하나에 별점과 후기를 남깁니다. 토큰이 필요합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "평점 등록 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {
                              "isSuccess": true,
                              "code": "COMMON201",
                              "message": "평점을 등록했습니다.",
                              "result": {
                                "ratingId": 1, "movieId": 550, "score": 5, "comment": "다시 봐도 재미있어요.",
                                "createdAt": "2026-09-13T21:00:00", "updatedAt": "2026-09-13T21:00:00"
                              }
                            }
                            """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "COMMON4001: 입력값 검증 실패",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = SwaggerExamples.COMMON4001))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "AUTH401_2 / AUTH401_3 / AUTH401_4: 토큰이 없음 / 유효하지 않음 / 만료됨",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "AUTH401_2", value = SwaggerExamples.AUTH401_2),
                            @ExampleObject(name = "AUTH401_3", value = SwaggerExamples.AUTH401_3),
                            @ExampleObject(name = "AUTH401_4", value = SwaggerExamples.AUTH401_4)
                    })),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "MEMBER404_1: 존재하지 않는 회원",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = SwaggerExamples.MEMBER404_1))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "RATING409_1: 이미 평점을 남긴 영화",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = SwaggerExamples.RATING409_1)))
    })
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @PostMapping("/ratings")
    public ResponseEntity<ApiResponse<RatingResponse>> create(
            @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @RequestBody @Valid CreateRatingRequest request
    ) {
        RatingResponse response = ratingService.create(memberId, request);
        return ResponseEntity.status(201)
                .body(ApiResponse.created("평점을 등록했습니다.", response));
    }

    // 경로에서도 /{memberId}가 사라지고 인터셉터가 담아둔 값을 씁니다
    @Operation(
            summary = "평점 수정",
            description = "본인이 남긴 평점의 별점과 후기를 수정합니다. 토큰이 필요합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "평점 수정 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {
                              "isSuccess": true,
                              "code": "COMMON200",
                              "message": "평점을 수정했습니다.",
                              "result": {
                                "ratingId": 1, "movieId": 550, "score": 4, "comment": "두 번째 보니 조금 아쉬워요.",
                                "createdAt": "2026-09-13T21:00:00", "updatedAt": "2026-09-14T10:30:00"
                              }
                            }
                            """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "COMMON4001: 입력값 검증 실패",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = SwaggerExamples.COMMON4001))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "AUTH401_2 / AUTH401_3 / AUTH401_4: 토큰이 없음 / 유효하지 않음 / 만료됨",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "AUTH401_2", value = SwaggerExamples.AUTH401_2),
                            @ExampleObject(name = "AUTH401_3", value = SwaggerExamples.AUTH401_3),
                            @ExampleObject(name = "AUTH401_4", value = SwaggerExamples.AUTH401_4)
                    })),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "RATING403_1: 본인이 작성한 평점이 아님",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = SwaggerExamples.RATING403_1))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "RATING404_1: 존재하지 않는 평점",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = SwaggerExamples.RATING404_1)))
    })
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @PatchMapping("/ratings/{ratingId}")
    public ResponseEntity<ApiResponse<RatingResponse>> update(
            @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @PathVariable Long ratingId,
            @RequestBody @Valid UpdateRatingRequest request
    ) {
        RatingResponse response = ratingService.update(memberId, ratingId, request);
        return ResponseEntity.ok(ApiResponse.ok("평점을 수정했습니다.", response));
    }

    @Operation(
            summary = "평점 삭제",
            description = "본인이 남긴 평점을 삭제합니다. 토큰이 필요합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "평점 삭제 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {
                              "isSuccess": true,
                              "code": "COMMON200",
                              "message": "평점을 삭제했습니다.",
                              "result": null
                            }
                            """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "AUTH401_2 / AUTH401_3 / AUTH401_4: 토큰이 없음 / 유효하지 않음 / 만료됨",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "AUTH401_2", value = SwaggerExamples.AUTH401_2),
                            @ExampleObject(name = "AUTH401_3", value = SwaggerExamples.AUTH401_3),
                            @ExampleObject(name = "AUTH401_4", value = SwaggerExamples.AUTH401_4)
                    })),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "RATING403_1: 본인이 작성한 평점이 아님",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = SwaggerExamples.RATING403_1))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "RATING404_1: 존재하지 않는 평점",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = SwaggerExamples.RATING404_1)))
    })
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @DeleteMapping("/ratings/{ratingId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @PathVariable Long ratingId
    ) {
        ratingService.delete(memberId, ratingId);
        return ResponseEntity.ok(ApiResponse.ok("평점을 삭제했습니다.", null));
    }
}