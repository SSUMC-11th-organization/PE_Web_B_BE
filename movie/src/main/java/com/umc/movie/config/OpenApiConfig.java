package com.umc.movie.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import com.umc.movie.dto.ApiResponse;
import io.swagger.v3.oas.models.examples.Example;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import java.util.Map;

@OpenAPIDefinition(info = @Info(title = "영화 평점 서비스 API", description = "UMC 회원·평점·즐겨찾기 API 문서", version = "v1"))
@Configuration
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
    @Bean
    public OpenApiCustomizer successExamples() {
        // 반환 타입에서 추론한 제네릭 응답 스키마를 유지한 뒤 예시만 덧붙입니다.
        return api -> {
            var signup = api.getPaths().get("/members").getPost().getResponses().get("201");
            signup.getContent().values().forEach(content -> content.addExamples("회원가입 성공",
                    new Example().value(ApiResponse.created("회원가입에 성공했습니다.",
                            Map.of("memberId", 1, "email", "week9@umc.com", "nickname", "구주차회원")))));

            var login = api.getPaths().get("/auth/login").getPost().getResponses().get("200");
            login.getContent().values().forEach(content -> content.addExamples("로그인 성공",
                    new Example().value(ApiResponse.ok("로그인에 성공했습니다.",
                            Map.of("nickname", "구주차회원", "accessToken", "로그인으로 발급받은 실제 JWT를 사용하세요")))));

            var favorite = api.getPaths().get("/favorites").getPost().getResponses().get("201");
            favorite.getContent().values().forEach(content -> content.addExamples("즐겨찾기 등록 성공",
                    new Example().value(ApiResponse.created("즐겨찾기를 등록했습니다.",
                            Map.of("favoriteId", 1, "movieId", 550, "createdAt", "2026-11-24T12:00:00")))));
        };
    }
}
