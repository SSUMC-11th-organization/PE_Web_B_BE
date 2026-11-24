package com.umc.movie.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import com.umc.movie.dto.*;
import com.umc.movie.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증", description = "로그인 및 토큰 발급")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "이메일 로그인", description = "공개 API입니다. 이메일과 비밀번호를 확인해 JWT를 발급합니다. Authorize에는 토큰 값만 입력하세요.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이메일 로그인 성공", useReturnTypeSchema = true),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "COMMON4001",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "COMMON4001_1", value = "{\"isSuccess\": false, \"code\": \"COMMON4001\", \"message\": \"입력값 또는 요청 JSON의 형식을 확인해주세요.\", \"result\": null}")})),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUTH401_1",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = {@ExampleObject(name = "AUTH401_1_1", value = "{\"isSuccess\": false, \"code\": \"AUTH401_1\", \"message\": \"이메일 또는 비밀번호가 올바르지 않습니다.\", \"result\": null}")}))
    })
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("로그인에 성공했습니다.", authService.login(request));
    }
}
