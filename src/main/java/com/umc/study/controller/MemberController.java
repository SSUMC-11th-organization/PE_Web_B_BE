package com.umc.study.controller;

import com.umc.study.dto.EmailCheckResponse;
import com.umc.study.dto.NicknameCheckResponse;
import com.umc.study.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
}
