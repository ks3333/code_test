package com.wjc.codetest.product.model.request;

import lombok.Getter;
import lombok.Setter;

/**
 * 문제: 모델 내 변수들의 데이터 검증이 부족합니다.
 * 원인:
 * 1. 필드에 대한 제약조건 미설정.
 * 2. 카테고리만 입력 가능한 생성자 제공.
 * 개선안:
 * 1. 각 필드에 적절한 검증 어노테이션 추가. (예: @NotNull, @NotEmpty 등)
 * 2. 카테고리만 입력받아 데이터를 생성하는 경우가 없을 것으로 예상되는 설꼐이므로, 카테고리 값만 전달받는 생성자 제거.
 */
@Getter
@Setter
public class CreateProductRequest {
    private String category;
    private String name;

    public CreateProductRequest(String category) {
        this.category = category;
    }

    public CreateProductRequest(String category, String name) {
        this.category = category;
        this.name = name;
    }
}

