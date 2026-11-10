package com.umc.study.controller;

import com.umc.study.apiPayload.ApiResponse;
import com.umc.study.config.SwaggerConfig;
import com.umc.study.dto.EmailCheckResponse;
import com.umc.study.dto.MemberInfoResponse;
import com.umc.study.dto.MemberResponse;
import com.umc.study.dto.MemberUpdateRequest;
import com.umc.study.dto.NicknameCheckResponse;
import com.umc.study.dto.SignUpRequest;
import com.umc.study.interceptor.AuthInterceptor;
import com.umc.study.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member", description = "회원 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    // GET http://localhost:8080/members/me  (Authorization: Bearer {token})
    // 고정 경로(/me)는 변수 경로(/{memberId})보다 위에 둡니다
    @Operation(summary = "내 정보 조회", description = "200 성공 / 401 토큰 없음·위조·만료(AUTH401_2~4) / 404 회원 없음")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberInfoResponse>> getMyInfo(
            @Parameter(hidden = true) @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId) {
        MemberInfoResponse response = memberService.getMyInfo(memberId);
        return ResponseEntity.ok(ApiResponse.ok("회원 정보를 조회했습니다.", response));
    }

    // PATCH http://localhost:8080/members/me  (Authorization: Bearer {token})
    @Operation(summary = "내 정보 수정", description = "200 성공 / 400 입력 검증 실패 / 401 인증 실패 / 409 닉네임 중복(MEMBER409_1)")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<MemberInfoResponse>> updateMyInfo(
            @Parameter(hidden = true) @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @Valid @RequestBody MemberUpdateRequest request) {
        MemberInfoResponse response = memberService.updateMyInfo(memberId, request);
        return ResponseEntity.ok(ApiResponse.ok("회원 정보를 수정했습니다.", response));
    }

    // GET http://localhost:8080/members/nickname/{nickname}
    @Operation(summary = "닉네임 중복 체크", description = "사용 가능하면 available: true")
    @GetMapping("/nickname/{nickname}")
    public NicknameCheckResponse checkNickname(
            @Parameter(description = "확인할 닉네임", example = "수현") @PathVariable String nickname) {
        return memberService.checkNickname(nickname);
    }

    // GET http://localhost:8080/members/email/{email}
    @Operation(summary = "이메일 중복 체크", description = "사용 가능하면 available: true")
    @GetMapping("/email/{email}")
    public EmailCheckResponse checkEmail(
            @Parameter(description = "확인할 이메일", example = "noco@umc.com") @PathVariable String email) {
        return memberService.checkEmail(email);
    }

    // POST http://localhost:8080/members
    @Operation(summary = "회원가입",
            description = "201 성공 / 400 입력 검증 실패(COMMON4001) / 409 닉네임 중복(MEMBER409_1)·이메일 중복(MEMBER409_2)")
    @PostMapping
    public ResponseEntity<ApiResponse<MemberResponse>> signUp(@Valid @RequestBody SignUpRequest request) {
        MemberResponse response = memberService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("회원가입에 성공했습니다.", response));
    }
}
