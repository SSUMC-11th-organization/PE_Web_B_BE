package com.umc.study.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "book")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long bookId;

    private String title;

    private String description;

    // book.category_id(FK) -> category 테이블
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "is_available")
    private boolean available;

    // 신규 도서는 대여 가능 상태로 등록됩니다.
    public Book(Category category, String title, String description) {
        this.category = category;
        this.title = title;
        this.description = description;
        this.available = true;
    }

    public String getCategoryName() {
        return category == null ? null : category.getName();
    }
}
