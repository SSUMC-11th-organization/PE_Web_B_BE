package com.umc.study.controller;

import com.umc.study.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/rentals")    // 이 클래스의 기본 주소
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    @PostMapping                           // POST /rentals
    @ResponseStatus(HttpStatus.CREATED)    // 기본 200 대신 201 Created
    public String createRental(@RequestBody Map<String, Object> body) {   // JSON → Map
        rentalService.createRental(body);
        return "대여가 완료되었습니다!";
    }
}
