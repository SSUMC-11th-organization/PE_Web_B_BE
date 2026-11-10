package com.umc.study.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(nullable = false, length = 60)              // bcrypt 해시는 항상 60자
    private String password;

    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    @Builder
    public Member(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }

    // [7주차] @Setter 대신 의도가 드러나는 수정 메서드만 엽니다 (변경 감지로 UPDATE)
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}
