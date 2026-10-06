package com.umc.study.repository;

import com.umc.study.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    // 메서드 이름이 곧 쿼리: 전체 조회 + book_id 내림차순(최신순)
    List<Book> findAllByOrderByBookIdDesc();
}