// src/main/java/com/umc/movie/dto/MemberResponse.java
package com.umc.movie.dto;

import com.umc.movie.entity.Member;

public record MemberResponse(
        Long memberId,
        String email,
        String nickname
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getMemberId(),
                member.getEmail(),
                member.getNickname()
        );
    }
}
