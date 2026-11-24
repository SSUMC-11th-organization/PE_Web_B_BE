// src/main/java/com/umc/movie/controller/MemberController.java
package com.umc.movie.controller;

import com.umc.movie.config.SwaggerConfig;
import com.umc.movie.config.SwaggerExamples;
import com.umc.movie.dto.*;
import com.umc.movie.interceptor.AuthInterceptor;
import com.umc.movie.service.MemberService;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
@Tag(name = "회원", description = "회원가입과 내 정보 API")
public class MemberController {
    private final MemberService memberService;

    @Operation(
            summary = "닉네임 중복 확인",
            description = "사용할 수 있는 닉네임인지 확인합니다. 토큰이 없어도 호출할 수 있는 공개 API입니다."
    )
    @GetMapping("/nickname/{nickname}")
    public NicknameCheckResponse checkNickname(@PathVariable String nickname) {
        return memberService.checkNickname(nickname);
    }

    @Operation(
            summary = "이메일 중복 확인",
            description = "사용할 수 있는 이메일인지 확인합니다. 토큰이 없어도 호출할 수 있는 공개 API입니다."
    )
    @GetMapping("/email/{email}")
    public EmailCheckResponse checkEmail(@PathVariable String email) {
        return memberService.checkEmail(email);
    }

    @Operation(
            summary = "회원가입",
            description = "이메일, 비밀번호, 닉네임으로 회원을 등록합니다. 토큰이 없어도 호출할 수 있는 공개 API입니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<MemberResponse>> signUp(
            @Valid @RequestBody SignUpRequest request
    ) {
        MemberResponse response = memberService.signUp(request);
        return ResponseEntity.status(201)
                .body(ApiResponse.created("회원가입에 성공했습니다.", response));
    }

    @Operation(
            summary = "내 정보 조회",
            description = "토큰의 주인인 회원의 이메일, 닉네임, 가입일을 조회합니다. 토큰이 필요합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "내 정보 조회 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {
                              "isSuccess": true,
                              "code": "COMMON200",
                              "message": "회원 정보를 조회했습니다.",
                              "result": { "email": "challenger@umc.com", "nickname": "영화조아", "createdAt": "2026-09-13T21:00:00" }
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
                    responseCode = "404", description = "MEMBER404_1: 존재하지 않는 회원",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = SwaggerExamples.MEMBER404_1)))
    })
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberInfoResponse>> getMyInfo(
            // 인터셉터가 request.setAttribute("memberId", ...)로 담아둔 값을 꺼냅니다
            @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId
    ) {
        MemberInfoResponse response = memberService.getMyInfo(memberId);
        return ResponseEntity.ok(ApiResponse.ok("회원 정보를 조회했습니다.", response));
    }

    @Operation(
            summary = "내 정보 수정",
            description = "토큰의 주인인 회원의 닉네임을 수정합니다. 토큰이 필요합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "내 정보 수정 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {
                              "isSuccess": true,
                              "code": "COMMON200",
                              "message": "회원 정보를 수정했습니다.",
                              "result": { "email": "challenger@umc.com", "nickname": "영화광", "createdAt": "2026-09-13T21:00:00" }
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
                    responseCode = "409", description = "MEMBER409_1: 이미 사용 중인 닉네임",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = SwaggerExamples.MEMBER409_1)))
    })
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<MemberInfoResponse>> updateMyInfo(
            @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @RequestBody @Valid MemberUpdateRequest request
    ) {
        MemberInfoResponse response = memberService.updateMyInfo(memberId, request);
        return ResponseEntity.ok(ApiResponse.ok("회원 정보를 수정했습니다.", response));
    }
}
