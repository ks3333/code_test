package com.wjc.codetest.product.model.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


/**
 * 문제: Entity 설계 시 불필요한 Getter/Setter 사용에 따라 Entity의 무결성에 문제가 생길 수 있으며,
 * 각 필드의 제약조건이 명확하지 않아 데이터 일관성에 영향을 줄 수 있씁니다.
 * 원인:
 * 1,@Getter/@Setter 어노테이션의 불필요한 사용.
 * 2.테이블 설계 시 정규화에 대한 고려 부족.
 * 3.필드 제약조건에 대한 고려 부족.
 * 개선안:
 * 1.필요한 필드에 대해서만 Getter/Setter를 생성하고, 불변성을 유지할 수 있는 설계를 채택합니다.
 * 1-1. ID는 수정 불가한 데이터이므로 Setter를 제거합니다.
 * 1-2. 데이터 UPDATE의 처리를 위해 Setter 대신 역할렝 맞는 메서드를 생성하여 활용합니다.
 * 1-3. Getter 어노테이션과 중복되는 Getter 메소드는 제거하거나, Getter 어노테이션을 제거합니다.
 * 2. uniqueConstraintsf를 활용하여 category와 name 필드의 조합이 유일하도록 테이블을 설계하거나,
 *    추후 확장을 고려하여 category와 name필드를 별도 테이블로 분리하여 정규화를 진행합니다.
 * 3. DB 제약조건과 일치하도록 필드에 대한 제약조건을 명확히 정의하여 사용하며(NotNull 등),
 *    DB제역조건이 없을경우 DB 설계 시 제약조건을 재 고려하여 설계에 반영하니다.
 */
@Entity
@Getter
@Setter
public class Product {

    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "category")
    private String category;

    @Column(name = "name")
    private String name;

    protected Product() {
    }

    public Product(String category, String name) {
        this.category = category;
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }
}
