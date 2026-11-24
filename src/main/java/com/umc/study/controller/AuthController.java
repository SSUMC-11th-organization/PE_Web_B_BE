package com.umc.study.controller;

import com.umc.study.apiPayload.ApiResponse;
import com.umc.study.dto.LoginRequest;
import com.umc.study.dto.LoginResponse;
import com.umc.study.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "로그인과 토큰 발급 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    // POST http://localhost:8080/auth/login
    // (/auth/login은 "명사형 자원 경로" 원칙의 예외. 원칙대로라면 POST /auth/tokens)
    @Operation(summary = "로그인",
            description = "🔓 공개 API. 이메일과 비밀번호로 로그인하고 Access Token을 발급받습니다. "
                    + "발급받은 토큰을 위쪽 Authorize 버튼에 넣어두면 🔒 API에 자동으로 실립니다.<br>"
                    + "200 성공 / 400 입력 검증 실패(COMMON4001) / 401 이메일 또는 비밀번호 불일치(AUTH401_1)")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("로그인에 성공했습니다.", response));
    }
}
