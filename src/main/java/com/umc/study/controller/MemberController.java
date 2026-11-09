package com.umc.study.controller;

import com.umc.study.apiPayload.ApiResponse;
import com.umc.study.dto.EmailCheckResponse;
import com.umc.study.dto.MemberResponse;
import com.umc.study.dto.NicknameCheckResponse;
import com.umc.study.dto.SignUpRequest;
import com.umc.study.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
            description = "201 성공 / 400 입력 검증 실패(COMMON4001) / 409 이메일 중복(MEMBER4091)·닉네임 중복(MEMBER4092)")
    @PostMapping
    public ResponseEntity<ApiResponse<MemberResponse>> signUp(@Valid @RequestBody SignUpRequest request) {
        MemberResponse response = memberService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("회원가입에 성공했습니다.", response));
    }
}
