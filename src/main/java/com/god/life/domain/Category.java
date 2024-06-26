package com.god.life.domain;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer categoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type")
    private CategoryType categoryType;

    public Category(CategoryType categoryType) {
        this.categoryType = categoryType;
    }
}
