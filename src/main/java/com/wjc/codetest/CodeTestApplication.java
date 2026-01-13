package com.wjc.codetest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * 기타
 * 문제:
 * 1. API 명세 확인 및 클라이언트와 외부에 명세 제공이 여러움.
 * 2. 테스트 코드 미사용으로 추후 확장 및 기능추가 시 기존로직에 대한 테스트 및 검증이 어려움.
 * 3. 3 tire 구조를 사용함에도 불구하고 Entity를 외부로 노출하는 설계와 영속성 컨텍스트 관리 미흡.
 * 원인:
 * 1. API 명세서 미작성
 * 2. 테스트 코드 관련 설정 부재
 * 3. 서비스 레이어에서 도메인 Entity를 그대로 Controller로 전달 후 응답으로 사용하는 구조의 문제 및 트랜잭션 설정 부재.
 * 개선안:
 * 1. Swagger 혹은 Rest Api Docs와 같은 API 명세서 자동화 도구 도입.
 * 1-1. Swagger 도입 시 API 명세 제공 및 클라이언트 및 외부에서 명세확인과 동시에 호출 테스트 가능
 * 1-2. Rest Api Docs 도입 시  API 명세서 자동 생성을 테스틐코드와 연계하여 관리가 가능함.
 * 1-3. 추후 명세가 복잡해 질 경우 Swagger와 Rest Api Docs 연동 설정 고려.
 * 2. Spring Boot Test 관련 종속성 추가 및 Junit5와 mockito를 활용한 단위 테스트 코드 작성.
 * 3-1. 응답용 모델 객체를 별도로 설계하여 도메인 Entity와 분리, Controller에서는 응답용 모델을 반환하도록 수정.
 * 3-2. 트랜잭션 설정을 추가하여 Service 레이어에서 트랜잭션 관리.
 * 3-3. 3 tire구조가 아닌 4 tire 구조로 변경하여, Controller -> Service -> Domain -> Repository 구조로 변경 고려.
 *      (Domain 레이어에서 데이터 조회 관련 로직을 처리 및 DTO 모댈 매핑을 처리하고, Service 레이어에서는 비즈니스 로직에 집중할 수 있도록 역할 분리)
 * 3-4. 추후 확장을 고려하여 필요시 QueryDSL 같은 도구를 사용하여 복잡한 조회 로직을 별도 관리를 고려.
 *      QueryDSL 장점 : 타입 안전성 보장, 동적 쿼리 작성 용이, 가독성 향상 등.
 *      QueryDSL 단점 : 러닝커브 존재, 프로젝트 초기 설정 복잡. Qentity 클래스 생성에 따른 관리포인트 추가 및 빌드시간 증가.
 */
@SpringBootApplication
public class CodeTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeTestApplication.class, args);
    }

}
