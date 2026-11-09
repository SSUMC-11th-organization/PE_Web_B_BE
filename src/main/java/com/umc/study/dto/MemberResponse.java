package com.umc.study.dto;

import com.umc.study.entity.Member;

// password는 절대 담지 않습니다
public record MemberResponse(
        Long memberId,
        String email,
        String nickname
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getMemberId(), member.getEmail(), member.getNickname());
    }
}
