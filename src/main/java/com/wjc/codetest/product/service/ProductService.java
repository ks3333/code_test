package com.wjc.codetest.product.service;

import com.wjc.codetest.product.model.request.CreateProductRequest;
import com.wjc.codetest.product.model.request.GetProductListRequest;
import com.wjc.codetest.product.model.domain.Product;
import com.wjc.codetest.product.model.request.UpdateProductRequest;
import com.wjc.codetest.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 문제:
 * 1. Entity를 직접 반환하여 응답 구조가 유연하지 못하며, Entity 구조가 외부로 노출되어 보안상 문제가 발생할 수 있습니다.
 * 2. Transactional을 사용하지 않아 데이터 일관성에 문제가 발생할 수 있습니다.
 * 원인:
 * 1. Service 레이어에서 도메인 Entity를 그대로 반환.
 * 2. 데이터 변경 작업 시 @Transactional 어노테이션이 누락.
 * 개선안:
 * 1-1. 응답을 반환할 모델 객체를 별도로 설계하여 Entity가 Service 레이어 외부로 노출되는것을 방자.
 * 1-2. Service 레이어에서 필요한 로직에 따라 응답을 모델 객체로 변환하여 전달.
 * 2. 데이터 변경 작업이 이루어지는 메서드에 @Transactional 어노테이션 추가하여 예외 발생 시 데이터 일관성 보장.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * 문제:
     * 1. Entity를 직접 반환하여 응답 구조가 유연하지 못합니다.
     * 2. Transactional을 사용하지 않아 데이터 일관성에 문제가 발생할 수 있습니다.
     * 3. 인자로 전달된 DTO 내 필드에 대한 유효성 검증이 없습니다.
     * 원인:
     * 1. Service 레이어에서 도메인 Entity를 그대로 반환.
     * 2. 데이터 변경 작업 시 @Transactional 어노테이션이 누락.
     * 3. DTO 내 필드에 대한 검증 로직이 없음
     * 개선안:
     * 1-1. 응답을 반환할 모델 객체를 별도로 설계하여 Entity가 Service 레이어 외부로 노출되는것을 방지.
     * 1-2. Service 레이어에서 필요한 로직에 따라 응답을 모델 객체로 변환하여 전달.
     * 2. 데이터 변경 작업이 이루어지는 메서드에 @Transactional 어노테이션 추가하여 예외 발생 시 데이터 일관성 보장.
     *    (추후 연관관계 매핑이 추가 될 경우 로직 실행 중간 예외 발생 시 롤백을 위해 필요)
     * 3. Spring Assert 또는 Custom Assert를 활용하여 로직 실행 전 DTO 내 필드에 대한 유효성 검증 로직 추가(Assert.notEmpty(), Assert.notNull() 등)
     */
    public Product create(CreateProductRequest dto) {
        Product product = new Product(dto.getCategory(), dto.getName());
        return productRepository.save(product);
    }

    /**
     * 문제:
     * 1. Optional을 원래 의도와 다르게 null 체크 대용으로 사용하고 있습니다.
     * 2. 예외 처리가 구체적이지 못합니다.
     * 3. Entity를 직접 반환하여 응답 구조가 유연하지 못합니다.
     * 원인:
     * 1. JPA를 통해 데이터 조회 후 Optional.isPresent()로 null 체크를 수행.
     * 2. RuntimeException을 통해 예외를 처리.
     * 2. Service 레이어에서 도메인 Entity를 그대로 반환.
     * 개선안:
     * 1. productOptional.orElseThrow()를 통해 Exception을 바로 throw 하도록 수정하거나,
     *    Optional을 사용하지 않고 조회 후 별도로 null 체크를 수행하도록 변경.
     * 2. 커스텀 예외 클래스를 생성하여 상황에 맞는 예외를 던지도록 수정합니다.(예: ProductNotFoundException)
     * 3-1. 응답을 반환할 모델 객체를 별도로 설계하여 Entity가 Service 레이어 외부로 노출되는것을 방지.
     * 3-2. Service 레이어에서 필요한 로직에 따라 응답을 모델 객체로 변환하여 전달.
     */
    public Product getProductById(Long productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (!productOptional.isPresent()) {
            throw new RuntimeException("product not found");
        }
        return productOptional.get();
    }

    /**
     * 문제:
     * 1. Entity를 직접 반환하여 응답 구조가 유연하지 못합니다.
     * 2. Transactional을 사용하지 않아 데이터 일관성에 문제가 발생할 수 있습니다.
     * 3. 인자로 전달된 DTO 내 필드에 대한 유효성 검증이 없습니다.
     * 4. 업데이트 대상 필드가 많아질 경우 유지보수가 어려울 수 있습니다.
     * 원인:
     * 1. Service 레이어에서 도메인 Entity를 그대로 반환,
     * 2. 데이터 변경 작업 시 @Transactional 어노테이션이 누락,
     * 3. DTO 내 필드에 대한 검증 로직이 없음,
     * 4. Setter를 통한 필드 업데이트.
     * 개선안:
     * 1-1. 응답을 반환할 모델 객체를 별도로 설계하여 Entity가 Service 레이어 외부로 노출되는것을 방지.
     * 1-2. Service 레이어에서 필요한 로직에 따라 응답을 모델 객체로 변환하여 전달.
     * 2. 데이터 변경 작업이 이루어지는 메서드에 @Transactional 어노테이션 추가하여 예외 발생 시 데이터 일관성 보장.
     *    (추후 연관관계 매핑이 추가 될 경우 로직 실행 중간 예외 발생 시 롤백을 위해 필요)
     * 3. Spring Assert 또는 Custom Assert를 활용하여 로직 실행 전 DTO 내 필드에 대한 유효성 검증 로직 추가(Assert.notEmpty(), Assert.notNull() 등)
     * 4-1. Product Entity 내에 DTO객체를 인자로 받아 update 처리를 할 수 있는 메서드를 생성하여 필드 업데이트 로직을 캡슐화하고,
     *      필요에 따라 부분 업데이트를 처리할 수 있도록 수정합니다.
     * 4-2. 필요할 경우 별도의 Mapper Util 클래스 혹은 Mapper 라이브러리를 활용을 검토합니다.
     */
    public Product update(UpdateProductRequest dto) {
        Product product = getProductById(dto.getId());
        product.setCategory(dto.getCategory());
        product.setName(dto.getName());
        Product updatedProduct = productRepository.save(product);
        return updatedProduct;

    }

    /**
     * 문제: 불필요한 데이터 조회가 발생하고 있습니다.
     * 원인: getProductById 메서드를 통해 데이터를 조회한 후 삭제 작업을 수행.
     * 개선안: delete가  아닌 deleteById 메서드를 사용하여 불필요한 조회를 제거.
     */
    public void deleteById(Long productId) {
        Product product = getProductById(productId);
        productRepository.delete(product);
    }

    /**
     * 문제: Page 객체의 content에 Entity를 직접 사용하여 응답 구조가 유연하지 못합니다.
     * 원인: Service 레이어에서 도메인 Entity를 그대로 반환.
     * 개선안:
     * 1-1. 응답을 반환할 모델 객체를 별도로 설계하여 Entity가 Service 레이어 외부로 노출되는것을 방지.
     * 1-2. Service 레이어에서 필요한 로직에 따라 응답을 모델 객체로 변환하여 전달.
     */
    public Page<Product> getListByCategory(GetProductListRequest dto) {
        PageRequest pageRequest = PageRequest.of(dto.getPage(), dto.getSize(), Sort.by(Sort.Direction.ASC, "category"));
        return productRepository.findAllByCategory(dto.getCategory(), pageRequest);
    }

    public List<String> getUniqueCategories() {
        return productRepository.findDistinctCategories();
    }
}