package com.umc.movie.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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

@Tag(name = "평점", description = "회원별 평점 조회 및 본인 평점 관리")
@RestController
@RequiredArgsConstructor
public class RatingController {
    private final RatingService ratingService;
    @Operation(summary = "회원별 평점 조회", description = "공개 API입니다. 지정한 회원의 평점을 최신 등록 순서로 조회합니다. 평점이 없으면 빈 배열을 반환합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원별 평점 조회 성공", useReturnTypeSchema = true),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "COMMON4001",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "COMMON4001_1", value = "{\"isSuccess\": false, \"code\": \"COMMON4001\", \"message\": \"입력값 또는 요청 JSON의 형식을 확인해주세요.\", \"result\": null}")})),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "MEMBER404_1",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "MEMBER404_1_1", value = "{\"isSuccess\": false, \"code\": \"MEMBER404_1\", \"message\": \"해당 회원을 찾을 수 없습니다.\", \"result\": null}")}))
    })
    @GetMapping("/members/{memberId}/ratings")
    public List<RatingResponse> getMemberRatings(@PathVariable @Positive Long memberId) {
        return ratingService.getRatingsByMember(memberId);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "평점 등록", description = "JWT 토큰이 필요합니다. 작성자는 토큰에서 식별하며 요청에 memberId를 넣지 않습니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "평점 등록 성공", useReturnTypeSchema = true),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "COMMON4001",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "COMMON4001_1", value = "{\"isSuccess\": false, \"code\": \"COMMON4001\", \"message\": \"입력값 또는 요청 JSON의 형식을 확인해주세요.\", \"result\": null}")})),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUTH401_2 / AUTH401_3 / AUTH401_4",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "AUTH401_2_1", value = "{\"isSuccess\": false, \"code\": \"AUTH401_2\", \"message\": \"로그인이 필요한 요청입니다.\", \"result\": null}"), @ExampleObject(name = "AUTH401_3_2", value = "{\"isSuccess\": false, \"code\": \"AUTH401_3\", \"message\": \"유효하지 않은 토큰입니다.\", \"result\": null}"), @ExampleObject(name = "AUTH401_4_3", value = "{\"isSuccess\": false, \"code\": \"AUTH401_4\", \"message\": \"토큰이 만료되었습니다. 다시 로그인해주세요.\", \"result\": null}")})),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "MEMBER404_1",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "MEMBER404_1_1", value = "{\"isSuccess\": false, \"code\": \"MEMBER404_1\", \"message\": \"해당 회원을 찾을 수 없습니다.\", \"result\": null}")})),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "RATING409_1 / COMMON4091",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "RATING409_1_1", value = "{\"isSuccess\": false, \"code\": \"RATING409_1\", \"message\": \"이미 이 영화에 평점을 남기셨습니다.\", \"result\": null}"), @ExampleObject(name = "COMMON4091_2", value = "{\"isSuccess\": false, \"code\": \"COMMON4091\", \"message\": \"이미 존재하거나 저장 제약에 맞지 않는 데이터입니다.\", \"result\": null}")}))
    })
    @PostMapping("/ratings")
    public ResponseEntity<ApiResponse<RatingResponse>> create(@RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
                                                   @Valid @RequestBody CreateRatingRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.created("평점 등록에 성공했습니다.", ratingService.create(memberId, request)));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "본인 평점 수정", description = "JWT 토큰이 필요합니다. 본인이 작성한 평점만 수정할 수 있습니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "본인 평점 수정 성공", useReturnTypeSchema = true),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "COMMON4001",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "COMMON4001_1", value = "{\"isSuccess\": false, \"code\": \"COMMON4001\", \"message\": \"입력값 또는 요청 JSON의 형식을 확인해주세요.\", \"result\": null}")})),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUTH401_2 / AUTH401_3 / AUTH401_4",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "AUTH401_2_1", value = "{\"isSuccess\": false, \"code\": \"AUTH401_2\", \"message\": \"로그인이 필요한 요청입니다.\", \"result\": null}"), @ExampleObject(name = "AUTH401_3_2", value = "{\"isSuccess\": false, \"code\": \"AUTH401_3\", \"message\": \"유효하지 않은 토큰입니다.\", \"result\": null}"), @ExampleObject(name = "AUTH401_4_3", value = "{\"isSuccess\": false, \"code\": \"AUTH401_4\", \"message\": \"토큰이 만료되었습니다. 다시 로그인해주세요.\", \"result\": null}")})),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "RATING403_1",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "RATING403_1_1", value = "{\"isSuccess\": false, \"code\": \"RATING403_1\", \"message\": \"본인이 작성한 평점만 수정하거나 삭제할 수 있습니다.\", \"result\": null}")})),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "RATING404_1",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "RATING404_1_1", value = "{\"isSuccess\": false, \"code\": \"RATING404_1\", \"message\": \"해당 평점을 찾을 수 없습니다.\", \"result\": null}")}))
    })
    @PatchMapping("/ratings/{ratingId}")
    public ResponseEntity<ApiResponse<RatingResponse>> update(@RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
                                                   @PathVariable @Positive Long ratingId,
                                                   @Valid @RequestBody UpdateRatingRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("평점 수정에 성공했습니다.", ratingService.update(memberId, ratingId, request)));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "본인 평점 삭제", description = "JWT 토큰이 필요합니다. 본인의 평점을 삭제하며 성공하면 200과 result=null을 반환합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "본인 평점 삭제 성공", useReturnTypeSchema = true),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "COMMON4001",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "COMMON4001_1", value = "{\"isSuccess\": false, \"code\": \"COMMON4001\", \"message\": \"입력값 또는 요청 JSON의 형식을 확인해주세요.\", \"result\": null}")})),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUTH401_2 / AUTH401_3 / AUTH401_4",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "AUTH401_2_1", value = "{\"isSuccess\": false, \"code\": \"AUTH401_2\", \"message\": \"로그인이 필요한 요청입니다.\", \"result\": null}"), @ExampleObject(name = "AUTH401_3_2", value = "{\"isSuccess\": false, \"code\": \"AUTH401_3\", \"message\": \"유효하지 않은 토큰입니다.\", \"result\": null}"), @ExampleObject(name = "AUTH401_4_3", value = "{\"isSuccess\": false, \"code\": \"AUTH401_4\", \"message\": \"토큰이 만료되었습니다. 다시 로그인해주세요.\", \"result\": null}")})),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "RATING403_1",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "RATING403_1_1", value = "{\"isSuccess\": false, \"code\": \"RATING403_1\", \"message\": \"본인이 작성한 평점만 수정하거나 삭제할 수 있습니다.\", \"result\": null}")})),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "RATING404_1",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "RATING404_1_1", value = "{\"isSuccess\": false, \"code\": \"RATING404_1\", \"message\": \"해당 평점을 찾을 수 없습니다.\", \"result\": null}")}))
    })
    @DeleteMapping("/ratings/{ratingId}")
    public ResponseEntity<ApiResponse<Void>> delete(@RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId, @PathVariable @Positive Long ratingId) {
        ratingService.delete(memberId, ratingId);
        return ResponseEntity.ok(ApiResponse.ok("평점을 삭제했습니다.", null));
    }
}
