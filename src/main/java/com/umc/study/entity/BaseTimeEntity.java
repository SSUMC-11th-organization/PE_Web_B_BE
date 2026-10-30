package com.umc.study.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass                                   // 테이블 X, 자식 엔티티에 컬럼만 물려줌
public abstract class BaseTimeEntity {

    @CreationTimestamp                              // INSERT 때 한 번만 채워짐
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp                                // 저장될 때마다 갱신
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
