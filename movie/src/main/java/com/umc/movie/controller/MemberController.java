package com.umc.movie.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import com.umc.movie.interceptor.AuthInterceptor;
import com.umc.movie.dto.MemberUpdateRequest;
import com.umc.movie.dto.MemberInfoResponse;
import com.umc.movie.dto.EmailCheckResponse;
import com.umc.movie.dto.NicknameCheckResponse;
import com.umc.movie.dto.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.umc.movie.dto.MemberResponse;
import com.umc.movie.dto.SignUpRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import com.umc.movie.service.MemberService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원", description = "회원가입·중복 확인·내 정보")
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {
    private final MemberService memberService;
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "내 정보 조회", description = "JWT 토큰이 필요합니다. 토큰에 해당하는 회원의 정보를 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "내 정보 조회 성공", useReturnTypeSchema = true),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUTH401_2 / AUTH401_3 / AUTH401_4",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "AUTH401_2_1", value = "{\"isSuccess\": false, \"code\": \"AUTH401_2\", \"message\": \"로그인이 필요한 요청입니다.\", \"result\": null}"), @ExampleObject(name = "AUTH401_3_2", value = "{\"isSuccess\": false, \"code\": \"AUTH401_3\", \"message\": \"유효하지 않은 토큰입니다.\", \"result\": null}"), @ExampleObject(name = "AUTH401_4_3", value = "{\"isSuccess\": false, \"code\": \"AUTH401_4\", \"message\": \"토큰이 만료되었습니다. 다시 로그인해주세요.\", \"result\": null}")})),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "MEMBER404_1",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "MEMBER404_1_1", value = "{\"isSuccess\": false, \"code\": \"MEMBER404_1\", \"message\": \"해당 회원을 찾을 수 없습니다.\", \"result\": null}")}))
    })
    @GetMapping("/me")
    public ApiResponse<MemberInfoResponse> getMyInfo(
            @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId) {
        return ApiResponse.ok("회원 정보를 조회했습니다.", memberService.getMyInfo(memberId));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "내 정보 수정", description = "JWT 토큰이 필요합니다. 닉네임은 2~12자이며 본인의 현재 닉네임은 유지할 수 있습니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "내 정보 수정 성공", useReturnTypeSchema = true),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "COMMON4001",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "COMMON4001_1", value = "{\"isSuccess\": false, \"code\": \"COMMON4001\", \"message\": \"입력값 또는 요청 JSON의 형식을 확인해주세요.\", \"result\": null}")})),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUTH401_2 / AUTH401_3 / AUTH401_4",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "AUTH401_2_1", value = "{\"isSuccess\": false, \"code\": \"AUTH401_2\", \"message\": \"로그인이 필요한 요청입니다.\", \"result\": null}"), @ExampleObject(name = "AUTH401_3_2", value = "{\"isSuccess\": false, \"code\": \"AUTH401_3\", \"message\": \"유효하지 않은 토큰입니다.\", \"result\": null}"), @ExampleObject(name = "AUTH401_4_3", value = "{\"isSuccess\": false, \"code\": \"AUTH401_4\", \"message\": \"토큰이 만료되었습니다. 다시 로그인해주세요.\", \"result\": null}")})),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "MEMBER404_1",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "MEMBER404_1_1", value = "{\"isSuccess\": false, \"code\": \"MEMBER404_1\", \"message\": \"해당 회원을 찾을 수 없습니다.\", \"result\": null}")})),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "MEMBER409_1 / COMMON4091",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "MEMBER409_1_1", value = "{\"isSuccess\": false, \"code\": \"MEMBER409_1\", \"message\": \"이미 사용 중인 닉네임입니다.\", \"result\": null}"), @ExampleObject(name = "COMMON4091_2", value = "{\"isSuccess\": false, \"code\": \"COMMON4091\", \"message\": \"이미 존재하거나 저장 제약에 맞지 않는 데이터입니다.\", \"result\": null}")}))
    })
    @PatchMapping("/me")
    public ApiResponse<MemberInfoResponse> updateMyInfo(
            @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @Valid @RequestBody MemberUpdateRequest request) {
        return ApiResponse.ok("회원 정보를 수정했습니다.", memberService.updateMyInfo(memberId, request));
    }

    @Operation(summary = "회원가입", description = "토큰 없이 호출하는 공개 API입니다. 비밀번호는 bcrypt로 저장하며 응답에 포함하지 않습니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공", useReturnTypeSchema = true),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "COMMON4001",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "COMMON4001_1", value = "{\"isSuccess\": false, \"code\": \"COMMON4001\", \"message\": \"입력값 또는 요청 JSON의 형식을 확인해주세요.\", \"result\": null}")})),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "COMMON4092 / COMMON4091",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "COMMON4092_1", value = "{\"isSuccess\": false, \"code\": \"COMMON4092\", \"message\": \"이미 사용 중인 이메일입니다.\", \"result\": null}"), @ExampleObject(name = "COMMON4092_2", value = "{\"isSuccess\": false, \"code\": \"COMMON4092\", \"message\": \"이미 사용 중인 닉네임입니다.\", \"result\": null}"), @ExampleObject(name = "COMMON4091_3", value = "{\"isSuccess\": false, \"code\": \"COMMON4091\", \"message\": \"이미 존재하거나 저장 제약에 맞지 않는 데이터입니다.\", \"result\": null}")}))
    })
    @PostMapping
    public ResponseEntity<ApiResponse<MemberResponse>> signUp(@Valid @RequestBody SignUpRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.created("회원가입에 성공했습니다.", memberService.signUp(request)));
    }
    @Operation(summary = "닉네임 사용 가능 여부", description = "공개 API입니다. available=true이면 사용 가능한 닉네임입니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "닉네임 사용 가능 여부 성공", useReturnTypeSchema = true),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "COMMON4001",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "COMMON4001_1", value = "{\"isSuccess\": false, \"code\": \"COMMON4001\", \"message\": \"입력값 또는 요청 JSON의 형식을 확인해주세요.\", \"result\": null}")}))
    })
    @GetMapping("/nickname/{nickname}")
    public NicknameCheckResponse checkNickname(@PathVariable @NotBlank @Size(min = 2, max = 12) String nickname) {
        return memberService.checkNickname(nickname);
    }
    @Operation(summary = "이메일 사용 가능 여부", description = "공개 API입니다. available=true이면 사용 가능한 이메일입니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이메일 사용 가능 여부 성공", useReturnTypeSchema = true),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "COMMON4001",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "COMMON4001_1", value = "{\"isSuccess\": false, \"code\": \"COMMON4001\", \"message\": \"입력값 또는 요청 JSON의 형식을 확인해주세요.\", \"result\": null}")}))
    })
    @GetMapping("/email/{email}")
    public EmailCheckResponse checkEmail(@PathVariable @NotBlank @Email @Size(max = 254) String email) {
        return memberService.checkEmail(email);
    }
}
