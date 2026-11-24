package com.umc.movie.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import com.umc.movie.dto.ApiResponse;
import com.umc.movie.dto.CreateMovieRecordRequest;
import com.umc.movie.dto.MovieRecordResponse;
import com.umc.movie.interceptor.AuthInterceptor;
import com.umc.movie.service.MovieRecordService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "bearerAuth")
@Tag(name = "영화 기록", description = "평점과 즐겨찾기의 트랜잭션 저장")
@RestController
@RequiredArgsConstructor
public class MovieRecordController {

    private final MovieRecordService movieRecordService;

    @Operation(summary = "영화 기록 저장", description = "JWT 토큰이 필요합니다. addToFavorites=true이면 평점과 즐겨찾기를 하나의 트랜잭션으로 저장하고, false이면 평점만 저장합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "영화 기록 저장 성공", useReturnTypeSchema = true),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "COMMON4001",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "COMMON4001_1", value = "{\"isSuccess\": false, \"code\": \"COMMON4001\", \"message\": \"입력값 또는 요청 JSON의 형식을 확인해주세요.\", \"result\": null}")})),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUTH401_2 / AUTH401_3 / AUTH401_4",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "AUTH401_2_1", value = "{\"isSuccess\": false, \"code\": \"AUTH401_2\", \"message\": \"로그인이 필요한 요청입니다.\", \"result\": null}"), @ExampleObject(name = "AUTH401_3_2", value = "{\"isSuccess\": false, \"code\": \"AUTH401_3\", \"message\": \"유효하지 않은 토큰입니다.\", \"result\": null}"), @ExampleObject(name = "AUTH401_4_3", value = "{\"isSuccess\": false, \"code\": \"AUTH401_4\", \"message\": \"토큰이 만료되었습니다. 다시 로그인해주세요.\", \"result\": null}")})),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "MEMBER404_1",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "MEMBER404_1_1", value = "{\"isSuccess\": false, \"code\": \"MEMBER404_1\", \"message\": \"해당 회원을 찾을 수 없습니다.\", \"result\": null}")})),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "RATING409_1 / COMMON4091 / FAVORITE409_1",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "RATING409_1_1", value = "{\"isSuccess\": false, \"code\": \"RATING409_1\", \"message\": \"이미 이 영화에 평점을 남기셨습니다.\", \"result\": null}"), @ExampleObject(name = "COMMON4091_2", value = "{\"isSuccess\": false, \"code\": \"COMMON4091\", \"message\": \"이미 존재하거나 저장 제약에 맞지 않는 데이터입니다.\", \"result\": null}"), @ExampleObject(name = "FAVORITE409_1_3", value = "{\"isSuccess\": false, \"code\": \"FAVORITE409_1\", \"message\": \"이미 이 영화를 즐겨찾기하셨습니다.\", \"result\": null}"), @ExampleObject(name = "COMMON4091_4", value = "{\"isSuccess\": false, \"code\": \"COMMON4091\", \"message\": \"이미 존재하거나 저장 제약에 맞지 않는 데이터입니다.\", \"result\": null}")}))
    })
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
