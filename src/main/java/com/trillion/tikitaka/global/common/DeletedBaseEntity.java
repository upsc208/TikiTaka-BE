package com.trillion.tikitaka.global.common;

import jakarta.persistence.MappedSuperclass;

import java.time.LocalDateTime;

@MappedSuperclass
public class DeletedBaseEntity extends BaseEntity {
    private LocalDateTime deletedAt;
}
