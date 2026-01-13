package com.wjc.codetest.product.repository;

import com.wjc.codetest.product.model.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 문제: 함수의 인자명이 모호하여 어떤 값을 전달해야 하는지 명확하지 않습니다.
     * 원인: Query Method method명과 인자명이 불일치하여 혼란을 가져옴.
     * 개선안: 인자명을 'category'로 변경하여 method명과 일치하도록 수정.
     */
    Page<Product> findAllByCategory(String name, Pageable pageable);

    @Query("SELECT DISTINCT p.category FROM Product p")
    List<String> findDistinctCategories();
}
