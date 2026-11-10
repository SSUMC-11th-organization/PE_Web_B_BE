package com.umc.study.interceptor;

import com.umc.study.apiPayload.code.ErrorCode;
import com.umc.study.apiPayload.exception.CustomException;
import com.umc.study.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 컨트롤러 앞에서 Authorization 헤더의 토큰을 검증하는 공통 관문.
 * 1. 헤더에서 토큰을 꺼내고  2. 검증해서 memberId를 알아낸 뒤
 * 3. 요청 속성에 담아 컨트롤러가 꺼내 쓸 수 있게 합니다.
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    public static final String MEMBER_ID = "memberId";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // CORS preflight(OPTIONS)에는 토큰이 붙지 않으므로 통과
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        // 회원가입(POST /members)만 공개. 나중에 GET /members 등이 생겨도 함께 열리지 않도록 메서드까지 확인
        if ("/members".equals(request.getRequestURI()) && HttpMethod.POST.matches(request.getMethod())) {
            return true;
        }

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            throw new CustomException(ErrorCode.TOKEN_MISSING);
        }

        // 검증 실패 시 JwtUtil이 예외를 던지므로, 여기를 지나면 유효한 토큰
        Long memberId = jwtUtil.getMemberId(header.substring(BEARER_PREFIX.length()));
        request.setAttribute(MEMBER_ID, memberId);
        return true;
    }
}
