package com.god.life.repository;

import com.god.life.domain.Category;
import com.god.life.domain.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Category findByCategoryType(CategoryType categoryType);
}
