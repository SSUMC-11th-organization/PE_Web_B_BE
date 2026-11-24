package com.umc.movie.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import com.umc.movie.dto.ApiResponse;
import com.umc.movie.dto.CreateFavoriteRequest;
import com.umc.movie.dto.FavoriteResponse;
import com.umc.movie.dto.FavoriteStatusResponse;
import com.umc.movie.interceptor.AuthInterceptor;
import com.umc.movie.service.FavoriteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@Tag(name = "즐겨찾기", description = "로그인한 회원의 영화 즐겨찾기")
@RestController
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "즐겨찾기 등록", description = "JWT 토큰이 필요합니다. 토큰에 해당하는 회원이 TMDB 영화를 즐겨찾기에 추가합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "즐겨찾기 등록 성공", useReturnTypeSchema = true),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "COMMON4001",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "COMMON4001_1", value = "{\"isSuccess\": false, \"code\": \"COMMON4001\", \"message\": \"입력값 또는 요청 JSON의 형식을 확인해주세요.\", \"result\": null}")})),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUTH401_2 / AUTH401_3 / AUTH401_4",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "AUTH401_2_1", value = "{\"isSuccess\": false, \"code\": \"AUTH401_2\", \"message\": \"로그인이 필요한 요청입니다.\", \"result\": null}"), @ExampleObject(name = "AUTH401_3_2", value = "{\"isSuccess\": false, \"code\": \"AUTH401_3\", \"message\": \"유효하지 않은 토큰입니다.\", \"result\": null}"), @ExampleObject(name = "AUTH401_4_3", value = "{\"isSuccess\": false, \"code\": \"AUTH401_4\", \"message\": \"토큰이 만료되었습니다. 다시 로그인해주세요.\", \"result\": null}")})),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "MEMBER404_1",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "MEMBER404_1_1", value = "{\"isSuccess\": false, \"code\": \"MEMBER404_1\", \"message\": \"해당 회원을 찾을 수 없습니다.\", \"result\": null}")})),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "FAVORITE409_1 / COMMON4091",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "FAVORITE409_1_1", value = "{\"isSuccess\": false, \"code\": \"FAVORITE409_1\", \"message\": \"이미 이 영화를 즐겨찾기하셨습니다.\", \"result\": null}"), @ExampleObject(name = "COMMON4091_2", value = "{\"isSuccess\": false, \"code\": \"COMMON4091\", \"message\": \"이미 존재하거나 저장 제약에 맞지 않는 데이터입니다.\", \"result\": null}")}))
    })
    @PostMapping("/favorites")
    public ResponseEntity<ApiResponse<FavoriteResponse>> create(
            @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @RequestBody @Valid CreateFavoriteRequest request
    ) {
        FavoriteResponse response = favoriteService.create(memberId, request);
        return ResponseEntity.status(201)
                .body(ApiResponse.created("즐겨찾기를 등록했습니다.", response));
    }

    @Operation(summary = "내 즐겨찾기 목록 조회", description = "JWT 토큰이 필요합니다. 본인의 목록만 최신 등록 순서로 조회하며 목록이 없으면 result=[]입니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "내 즐겨찾기 목록 조회 성공", useReturnTypeSchema = true),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUTH401_2 / AUTH401_3 / AUTH401_4",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "AUTH401_2_1", value = "{\"isSuccess\": false, \"code\": \"AUTH401_2\", \"message\": \"로그인이 필요한 요청입니다.\", \"result\": null}"), @ExampleObject(name = "AUTH401_3_2", value = "{\"isSuccess\": false, \"code\": \"AUTH401_3\", \"message\": \"유효하지 않은 토큰입니다.\", \"result\": null}"), @ExampleObject(name = "AUTH401_4_3", value = "{\"isSuccess\": false, \"code\": \"AUTH401_4\", \"message\": \"토큰이 만료되었습니다. 다시 로그인해주세요.\", \"result\": null}")}))
    })
    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<List<FavoriteResponse>>> getMyFavorites(
            @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId
    ) {
        List<FavoriteResponse> response = favoriteService.getMyFavorites(memberId);
        return ResponseEntity.ok(ApiResponse.ok("내 즐겨찾기 목록을 조회했습니다.", response));
    }

    @Operation(summary = "영화의 즐겨찾기 여부 조회", description = "JWT 토큰이 필요합니다. 즐겨찾기하지 않은 영화도 200과 isFavorite=false를 반환합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "영화의 즐겨찾기 여부 조회 성공", useReturnTypeSchema = true),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "COMMON4001",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "COMMON4001_1", value = "{\"isSuccess\": false, \"code\": \"COMMON4001\", \"message\": \"입력값 또는 요청 JSON의 형식을 확인해주세요.\", \"result\": null}")})),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUTH401_2 / AUTH401_3 / AUTH401_4",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "AUTH401_2_1", value = "{\"isSuccess\": false, \"code\": \"AUTH401_2\", \"message\": \"로그인이 필요한 요청입니다.\", \"result\": null}"), @ExampleObject(name = "AUTH401_3_2", value = "{\"isSuccess\": false, \"code\": \"AUTH401_3\", \"message\": \"유효하지 않은 토큰입니다.\", \"result\": null}"), @ExampleObject(name = "AUTH401_4_3", value = "{\"isSuccess\": false, \"code\": \"AUTH401_4\", \"message\": \"토큰이 만료되었습니다. 다시 로그인해주세요.\", \"result\": null}")}))
    })
    @GetMapping("/favorites/{movieId}")
    public ResponseEntity<ApiResponse<FavoriteStatusResponse>> getStatus(
            @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @PathVariable @Positive Long movieId
    ) {
        FavoriteStatusResponse response = favoriteService.getStatus(memberId, movieId);
        return ResponseEntity.ok(ApiResponse.ok("즐겨찾기 여부를 조회했습니다.", response));
    }

    @Operation(summary = "즐겨찾기 해제", description = "JWT 토큰이 필요합니다. favoriteId가 아닌 movieId를 입력합니다. 성공하면 200과 result=null을 반환합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "즐겨찾기 해제 성공", useReturnTypeSchema = true),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "COMMON4001",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "COMMON4001_1", value = "{\"isSuccess\": false, \"code\": \"COMMON4001\", \"message\": \"입력값 또는 요청 JSON의 형식을 확인해주세요.\", \"result\": null}")})),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUTH401_2 / AUTH401_3 / AUTH401_4",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "AUTH401_2_1", value = "{\"isSuccess\": false, \"code\": \"AUTH401_2\", \"message\": \"로그인이 필요한 요청입니다.\", \"result\": null}"), @ExampleObject(name = "AUTH401_3_2", value = "{\"isSuccess\": false, \"code\": \"AUTH401_3\", \"message\": \"유효하지 않은 토큰입니다.\", \"result\": null}"), @ExampleObject(name = "AUTH401_4_3", value = "{\"isSuccess\": false, \"code\": \"AUTH401_4\", \"message\": \"토큰이 만료되었습니다. 다시 로그인해주세요.\", \"result\": null}")})),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "FAVORITE404_1",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "FAVORITE404_1_1", value = "{\"isSuccess\": false, \"code\": \"FAVORITE404_1\", \"message\": \"즐겨찾기한 영화를 찾을 수 없습니다.\", \"result\": null}")}))
    })
    @DeleteMapping("/favorites/{movieId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @PathVariable @Positive Long movieId
    ) {
        favoriteService.delete(memberId, movieId);
        return ResponseEntity.ok(ApiResponse.ok("즐겨찾기를 해제했습니다.", null));
    }
}
