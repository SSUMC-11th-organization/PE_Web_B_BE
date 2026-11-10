package com.umc.study.config;

import com.umc.study.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")                              // 모든 경로
                .allowedOrigins("http://localhost:5173")         // 이 출처만 허용 (끝에 / 금지)
                .allowedMethods("GET", "POST", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");                            // Authorization 헤더 포함
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                // 전부 잠그고, 로그인 없이 써야 하는 경로만 엽니다 (깜빡해도 잠긴 쪽으로 기울어짐)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/**",               // 로그인
                        // POST /members(회원가입)는 메서드까지 봐야 해서 AuthInterceptor 안에서 엽니다
                        "/members/nickname/**",   // 닉네임 중복 체크
                        "/members/email/**",      // 이메일 중복 체크
                        "/members/*/ratings",     // 유저별 평점 조회 (공개 목록)
                        "/swagger-ui/**",         // Swagger UI
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/error"                  // 스프링 내부 에러 경로
                );
    }
}
