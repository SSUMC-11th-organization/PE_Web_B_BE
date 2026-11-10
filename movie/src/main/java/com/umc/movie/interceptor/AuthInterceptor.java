package com.umc.movie.interceptor;

import com.umc.movie.exception.ApiException;
import com.umc.movie.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    public static final String MEMBER_ID = "memberId";
    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String method = request.getMethod();
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if ("OPTIONS".equals(method)) return true;
        // 공개 예외는 HTTP 메서드까지 확인합니다. 예전 평점 쓰기 경로는 열지 않습니다.
        if ("POST".equals(method) && ("/members".equals(path) || "/auth/login".equals(path))) return true;
        if ("GET".equals(method) && (path.startsWith("/members/nickname/")
                || path.startsWith("/members/email/") || path.matches("/members/[^/]+/ratings"))) return true;
        String header = request.getHeader("Authorization");
        if (header == null || !header.regionMatches(true, 0, "Bearer ", 0, 7) || header.substring(7).isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH401_2", "로그인이 필요한 요청입니다.");
        }
        request.setAttribute(MEMBER_ID, jwtUtil.getMemberId(header.substring(7)));
        return true;
    }
}
