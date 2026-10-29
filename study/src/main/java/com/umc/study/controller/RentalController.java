package com.umc.study.controller;

import com.umc.study.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    @PostMapping
    public Map<String, String> createRental(@RequestBody Map<String, Object> body) {
        rentalService.createRental(body);
        return Map.of("message", "대여 기록이 등록되었습니다!");
    }

    @PatchMapping("/{rentalId}/return")
    public Map<String, String> returnBook(@PathVariable Long rentalId) {
        rentalService.returnBook(rentalId);
        return Map.of("message", "반납 처리가 완료되었습니다!");
    }
}