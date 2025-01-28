package com.trillion.tikitaka.category.domain.model;

import com.trillion.tikitaka.global.common.DeletedBaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

/**
 * 카테고리 엔티티
 * - Soft delete(@SQLDelete, @SQLRestriction): deleted_at 필드 사용
 * - 부모(1차) / 자식(2차) 구조
 */
@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE categories SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Category extends DeletedBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 25자 이하 & not null
    @Column(nullable = false, length = 25)
    private String name;

    // 부모 카테고리 (1차 카테고리는 null)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    // 자식 카테고리 목록
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Category> children = new ArrayList<>();

    @Builder
    public Category(String name, Category parent) {
        this.name = name;
        this.parent = parent;
    }

    // 이름 변경
    public void updateName(String newName) {
        this.name = newName;
    }
}
