package com.trillion.tikitaka.global.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public abstract class DeletedBaseEntity {

    // 삭제 시각 (논리삭제용)
    @Column(name = "deleted_at")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    protected LocalDateTime deletedAt;

    // deletedAt 설정하는 메서드 (필요 시)
    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    // isDeleted 체크 등 확장 가능
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
