package com.umc.study.dto;

import com.umc.study.entity.Member;

import java.time.LocalDateTime;

// 엔티티를 그대로 응답하면 password까지 나가므로 DTO로 옮겨 담습니다
public record MemberInfoResponse(
        String email,
        String nickname,
        LocalDateTime createdAt
) {
    public static MemberInfoResponse from(Member member) {
        return new MemberInfoResponse(member.getEmail(), member.getNickname(), member.getCreatedAt());
    }
}
