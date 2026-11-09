package com.umc.study.service;

import com.umc.study.apiPayload.code.ErrorCode;
import com.umc.study.apiPayload.exception.CustomException;
import com.umc.study.dto.EmailCheckResponse;
import com.umc.study.dto.MemberResponse;
import com.umc.study.dto.NicknameCheckResponse;
import com.umc.study.dto.SignUpRequest;
import com.umc.study.entity.Member;
import com.umc.study.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // [5주차] 닉네임 중복 체크: 이미 있으면 사용 불가
    @Transactional(readOnly = true)
    public NicknameCheckResponse checkNickname(String nickname) {
        return new NicknameCheckResponse(!memberRepository.existsByNickname(nickname));
    }

    // [5주차] 이메일 중복 체크
    @Transactional(readOnly = true)
    public EmailCheckResponse checkEmail(String email) {
        return new EmailCheckResponse(!memberRepository.existsByEmail(email));
    }

    // [6주차] 회원가입
    @Transactional
    public MemberResponse signUp(SignUpRequest request) {
        // 1. 사전 중복 체크 (이메일 / 닉네임을 다른 code로 구분)
        if (memberRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (memberRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        // 2. 비밀번호 bcrypt 해시 → 3. 저장
        //    동시에 들어온 요청이 둘 다 1번을 통과했다면,
        //    늦게 INSERT한 쪽은 DB UNIQUE 제약에 걸려 DataIntegrityViolationException → 409
        Member member = Member.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .build();
        Member savedMember = memberRepository.save(member);

        // 4. password를 뺀 DTO로 반환
        return MemberResponse.from(savedMember);
    }
}
