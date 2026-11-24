// src/main/java/com/umc/movie/controller/MovieRecordController.java
package com.umc.movie.controller;

import com.umc.movie.config.SwaggerConfig;
import com.umc.movie.config.SwaggerExamples;
import com.umc.movie.dto.ApiResponse;
import com.umc.movie.dto.CreateMovieRecordRequest;
import com.umc.movie.dto.MovieRecordResponse;
import com.umc.movie.interceptor.AuthInterceptor;
import com.umc.movie.service.MovieRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "영화 기록", description = "평점과 즐겨찾기를 함께 저장하는 API")
public class MovieRecordController {

    private final MovieRecordService movieRecordService;

    @Operation(
            summary = "영화 기록 저장",
            description = "평점 등록과 즐겨찾기 추가를 한 번에 처리합니다. 토큰이 필요합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "영화 기록 저장 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {
                              "isSuccess": true,
                              "code": "COMMON201",
                              "message": "영화 기록을 저장했습니다.",
                              "result": {
                                "rating": {
                                  "ratingId": 1, "movieId": 550, "score": 5, "comment": "다시 봐도 재미있어요.",
                                  "createdAt": "2026-09-13T21:00:00", "updatedAt": "2026-09-13T21:00:00"
                                },
                                "favorite": { "favoriteId": 1, "movieId": 550, "createdAt": "2026-09-13T21:00:00" }
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
                    responseCode = "409", description = "RATING409_1 / FAVORITE409_1: 이미 평점을 남긴 영화 / 이미 즐겨찾기한 영화",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "RATING409_1", value = SwaggerExamples.RATING409_1),
                            @ExampleObject(name = "FAVORITE409_1", value = SwaggerExamples.FAVORITE409_1)
                    }))
    })
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @PostMapping("/movie-records")
    public ResponseEntity<ApiResponse<MovieRecordResponse>> create(
            @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @RequestBody @Valid CreateMovieRecordRequest request
    ) {
        MovieRecordResponse response = movieRecordService.create(memberId, request);
        return ResponseEntity.status(201)
                .body(ApiResponse.created("영화 기록을 저장했습니다.", response));
    }
}
