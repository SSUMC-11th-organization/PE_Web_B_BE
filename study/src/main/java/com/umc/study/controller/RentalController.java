package com.umc.study.controller;

import com.umc.study.service.RentalService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @ApiResponse(responseCode = "201", description = "도서 대여 기록 생성 완료")
    public ResponseEntity<Map<String, String>> createRental(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(value = "{\"userId\": 1, \"bookId\": 1}"))
            )
            @RequestBody Map<String, Object> body) {
        rentalService.createRental(body.get("userId"), body.get("bookId"));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "도서 대여 기록이 생성되었습니다."));
    }
}
