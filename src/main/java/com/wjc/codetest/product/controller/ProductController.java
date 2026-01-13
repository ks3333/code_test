package com.wjc.codetest.product.controller;

import com.wjc.codetest.product.model.request.CreateProductRequest;
import com.wjc.codetest.product.model.request.GetProductListRequest;
import com.wjc.codetest.product.model.domain.Product;
import com.wjc.codetest.product.model.request.UpdateProductRequest;
import com.wjc.codetest.product.model.response.ProductListResponse;
import com.wjc.codetest.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 문제:
 * 1. API 엔드포인트가 REST 원칙을 준수하지 않고 있으며, URI 패턴의 경우 일관성이 없이 설계되어 있어 가독성 및 추후 유지보수에 문제가 생길 수 있으먀,
 *    엔드포인트를 제공받는 클라이언트 측에서도 혼란이 발생할 수 있습니다.
 * 2. 요청값에 대한 검증이 부족하여 잘못된 요청으로 예기지 못한 오류가 발생할 수 있습니다.
 * 3. 응답 시 도메인 Entity를 직접 반환하고 있어 응답 구조가 유연하지 못하고, 도메인 모델이 변경될 경우 API 응답에도 영향을 미칠 수 있으며,
 *    도메인 모델이 그대로 노출됨에 따라 보안상 이슈가 발생할 수 있습니다.
 *    또한 묵시적 OSIV 사용으로 인해 추후 지연 로딩이 필요한 연관 엔티티를 반환 시 JSON 직렬화 과정에서 예기치 못한 문제가 발생할 수 있으며,
 *    Controller 레이어에서 Entity 조회 후 별도 작업 시 트랜잭션을 유지하게 되어 성능 저하가 발생할 수 있습니다.
 * 원인:
 * 1-1. URI가 리소스 중심이 아닌 동작 중심으로 설계됨(URI에 create, delete등 동사형태의 단어가 사용됨)
 * 1-2. URI 패턴이 일관성이 없음(같은 조회 API임에도 불구하고 /get/product/by/{productId} vs /product/list 처럼 패턴에 일관성이 부족)
 * 1-3. HTTP 메서드가 RESTFUL 원칙과 다르게 사용됨(데이터 조희의 경우 GET, POST가 혼용되어 있으며 수정 삭제 역시 POST를 이용하여 처리)
 * 2. @RequestBody로 전달되는 DTO에 대한 유효성 검증 로직이 없음
 * 3. 도메인 Entity를 Service 레이어에서 그대로 Controller로 전달 후 응답으로 사용하는 구조의 문제
 * 개선안:
 * 1-1. URI를 리소스 중심으로 재설계(예: /get/product/by/{productId} -> /products/{productId}, /create/product -> /products 등 명사형태로 통일)
 * 1-2. HTTP 메서드를 RESTFUL 원칙에 맞게 재설계(GET: 조회, POST: 생성, PUT: 수정, DELETE: 삭제)
 * 2. DTO 클래스에 @Valid 어노테이션을 추가하고, 필요한 필드에 제약 조건을 설정하여 요청값 검증 강화
 * 3-1  응답용 모델 객체를 별도로 설계하여 도메인 Entity와 분리, Controller에서는 응답용 모델을 반환하도록 수정
 * 3-1. 서비스 레이어에서 필요한 로직에 따라 응답을 모델 객체로 변환하여 전달할 수 있도록 구조 개선
 * 3-2. OSIV 설정을 비활성화하고, 필요한 경우 Service 레이어에서 트랜잭션을 관리하여 성능 최적화
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    /**
     * 문제:
     * 1. API 엔드포인트가 REST 원칙을 준수하지 않고 있습니다.
     * 2. 응답 시 도메인 Entity를 직접 반환하고 있습니다.
     * 원인:
     * 1. URI가 리소스 중심이 아닌 동작 중심으로 설계됨(URI에 get, by 동사형태의 단어가 사용됨)
     * 2. Service 레이어에서 반환한 Entity를 그대로 응답으로 사용
     * 개선안:
     * 1. URI를 리소스 중심으로 재설계(예: /get/product/by/{productId} -> /products/{productId})
     * 2. 응답용 모델 객체를 별도로 설계하여 도메인 Entity와 분리, Controller에서는 응답용 모델을 반환하도록 수정
     */
    @GetMapping(value = "/get/product/by/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable(name = "productId") Long productId){
        Product product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }

    /**
     * 문제:
     * 1. API 엔드포인트가 REST 원칙을 준수하지 않고 있습니다.
     * 2. 응답 시 도메인 Entity를 직접 반환하고 있습니다.
     * 3. 요청값에 대한 검증이 부족하여 잘못된 요청으로 인해 데이터 수정 시 원치않는 처리가 발생할 수 있습니다.
     * 원인:
     * 1. URI가 리소스 중심이 아닌 동작 중심으로 설계됨(URI에 create 등 동사형태의 단어가 사용됨)
     * 2. Service 레이어에서 반환한 Entity를 그대로 응답으로 사용
     * 3. @RequestBody로 전달되는 DTO에 대한 유효성 검증 로직이 없음
     * 개선안:
     * 1. URI를 리소스 중심으로 재설계(예: /create/product -> /product)
     * 2. 응답용 모델 객체를 별도로 설계하여 도메인 Entity와 분리, Controller에서는 응답용 모델을 반환하도록 수정
     * 3-1. DTO 클래스에 @Valid 어노테이션을 추가하고, 필요한 필드에 제약 조건을 설정하여 요청값 검증 강화(예: name, category 필드에 NotNull, NotBlank 등 추가)
     * 3-2. 컨트롤러 메서드 파라미터에 @Valid 어노테이션 추가 및 Errors 파라미터 추가하여 검증 결과 처리
     * 3-3. GlobalExceptionHandler 혹은 별도 ExceptionHandler에서 MethodArgumentNotValidException 처리 로직 추가
     */
    @PostMapping(value = "/create/product")
    public ResponseEntity<Product> createProduct(@RequestBody CreateProductRequest dto){
        Product product = productService.create(dto);
        return ResponseEntity.ok(product);
    }

    /**
     * 문제:
     * 1. API 엔드포인트가 REST 원칙을 준수하지 않고 있습니다.
     * 원인:
     * 1. URI가 리소스 중심이 아닌 동작 중심으로 설계됨(URI에 delete 등 동사형태의 단어가 사용됨)
     * 2. HttpMethod가 RESTFUL 원칙과 다르게 사용됨(삭제 작업임에도 불구하고 POST 메서드를 사용)
     * 개선안:
     * 1. URI를 리소스 중심으로 재설계(예: /delete/product/{productId} -> /products/{productId})
     * 2. 행위는 HTTP 메소드로 표현(예: POST -> DELETE 메서드 사용)
     */
    @PostMapping(value = "/delete/product/{productId}")
    public ResponseEntity<Boolean> deleteProduct(@PathVariable(name = "productId") Long productId){
        productService.deleteById(productId);
        return ResponseEntity.ok(true);
    }

    /**
     * 문제:
     * 1. API 엔드포인트가 REST 원칙을 준수하지 않고 있습니다.
     * 2. 응답 시 도메인 Entity를 직접 반환하고 있습니다.
     * 3. 요청값에 대한 검증이 부족하여 잘못된 요청으로 인해 데이터 수정 시 원치않는 처리가 발생할 수 있습니다.
     * 원인:
     * 1. URI가 리소스 중심이 아닌 동작 중심으로 설계됨(URI에 update 등 동사형태의 단어가 사용됨)
     * 2. HttpMethod가 RESTFUL 원칙과 다르게 사용됨(수정 작업임에도 불구하고 POST 메서드를 사용)
     * 3. @RequestBody로 전달되는 DTO에 대한 유효성 검증 로직이 없음
     * 개선안:
     * 1. URI를 리소스 중심으로 재설계(예: /update/product -> /products/{productId})
     * 2. 행위는 HTTP 메소드로 표현(예: POST -> PUT 메서드 사용)
     * 3-1. DTO 클래스에 @Valid 어노테이션을 추가하고, 필요한 필드에 제약 조건을 설정하여 요청값 검증 강화(예: name, category 필드에 NotNull, NotBlank 등 추가)
     * 3-2. 컨트롤러 메서드 파라미터에 @Valid 어노테이션 추가 및 Errors 파라미터 추가하여 검증 결과 처리
     * 3-3. GlobalExceptionHandler 혹은 별도 ExceptionHandler에서 MethodArgumentNotValidException 처리 로직 추가
     */
    @PostMapping(value = "/update/product")
    public ResponseEntity<Product> updateProduct(@RequestBody UpdateProductRequest dto){
        Product product = productService.update(dto);
        return ResponseEntity.ok(product);
    }

    /**
     * 문제:
     * 1. API 엔드포인트가 REST 원칙을 준수하지 않고 있습니다.
     * 2. 응답 시 도메인 Entity를 직접 반환하고 있습니다.
     * 원인:
     * 1-1  URI가 리소스 중심이 아닌 동작 중심으로 설계됨(URI에 list 등 동사형태의 단어가 사용됨)
     * 1-2. HttpMethod가 RESTFUL 원칙과 다르게 사용됨(조회 작업임에도 불구하고 POST 메서드를 사용)
     * 1-3. 쿼리 파라미터를 사용하지 않고, POST 요청의 Body로 전달받아 조회 작업을 수행
     * 2. Service 레이어에서 반환한 Entity를 그대로 응답으로 사용
     * 개선안:
     * 1-1. URI를 리소스 중심으로 재설계(예: /product/list -> /products)
     * 1-2. POST 요청의 Body가 아닌 쿼리 파라미터로 전달받아 조회 작업 수행(예: /products?category={category}&page={page}&size={size} 형태로 변경)
     * 1-3. 행위는 HTTP 메소드로 표현(예: POST -> GET 메서드 사용)
     * 1-4. @RequestBody를 제거하고 쿼리 파라미터로 전달받도록 수정
     * 1-5 페이지네이션 관련 파라미터는 기본값을 설정하여 클라이언트가 전달하지 않을 경우에도 동작하도록 개선
     * 1-6 Category값의 필수 여부에 따라 별도의 검증 로직 혹은 필수값 적용 추가 고려
     * 2. Page 내 Content에 사용할 응답용 모델 객체를 별도로 설계하여 도메인 Entity와 분리, Service 레이어에서 응답용 모델로 변경하도록 수정
     */
    @PostMapping(value = "/product/list")
    public ResponseEntity<ProductListResponse> getProductListByCategory(@RequestBody GetProductListRequest dto){
        Page<Product> productList = productService.getListByCategory(dto);
        return ResponseEntity.ok(new ProductListResponse(productList.getContent(), productList.getTotalPages(), productList.getTotalElements(), productList.getNumber()));
    }

    /**
     * 문제:
     * 1. API 엔드포인트가 REST 원칙을 준수하지 않고 있습니다.
     * 원인:
     * 1. URI가 리소스 중심이 아닌 동작 중심으로 설계됨(URI에 delete 등 동사형태의 단어가 사용됨)
     * 개선안:
     * 1. URI를 리소스 중심으로 재설계(예: /product/category/list -> /products/categories/)
     */
    @GetMapping(value = "/product/category/list")
    public ResponseEntity<List<String>> getProductListByCategory(){
        List<String> uniqueCategories = productService.getUniqueCategories();
        return ResponseEntity.ok(uniqueCategories);
    }
}