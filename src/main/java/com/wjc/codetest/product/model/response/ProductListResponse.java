package com.wjc.codetest.product.model.response;

import com.wjc.codetest.product.model.domain.Product;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 문제: 외부로 노출되는 데이터에 대한 응답 모델이 부적절하게 설계되어 있습니다.
 * 원인: products 리스트의 제네릭 타입이 Entity인 Product로 설계되어 있어, 외부에 불필요한 데이터가 노출될 수 있으며,
 *      추후 Entity 내 연관관계 Entity가 추가될 경우, 직렬화 시 무한 루프 문제가 발생할 수 있습니다.
 * 개선안:
 * 1. 응답 모델로 별도의 모델(DTO 혹은 별도 Response 모델)을 생성하여, 외부에 노출되는 데이터를 명확히 정의합니다.
 * 2. Service에서 Entity를 응답 모델로 변환하는 로직을 추가하여, Entity가 그대로 외부에 노출되지 않도록 합니다.
 */
/**
 * <p>
 *
 * </p>
 *
 * @author : 변영우 byw1666@wjcompass.com
 * @since : 2025-10-27
 */
@Getter
@Setter
public class ProductListResponse {
    private List<Product> products;
    private int totalPages;
    private long totalElements;
    private int page;

    public ProductListResponse(List<Product> content, int totalPages, long totalElements, int number) {
        this.products = content;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.page = number;
    }
}
