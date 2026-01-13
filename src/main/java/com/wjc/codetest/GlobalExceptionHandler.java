package com.wjc.codetest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * 문제: ExceptionHandler에서 RuntimeException만 처리하고 있어, 다른 예외 상황에 대한 처리가 누락되어 있으며,
 *      에러 발생 시 모든 에러가 동일한 500 Internal Server Error로 응답되어 클라이언트에서 문제를 정확히 파악하기가 어렵습니다.
 * 원인:
 * 1.에러 응답이 status code만 변경하며, body로 상세한 에러 정보를 제공하지 않음.
 * 2.서비스 로직에서 발생할 수 있는 다양한 예외 상황에 사용할 수 있는 커스텀 예외 설계가 없음.
 * 개선안:
 * 1.에러 응답에 상세한 에러 메시지와 에러 코드를 포함하여 클라이언트가 문제를 쉽게 파악할 수 있도록 개선.
 * 2.공통으로 에러 응답에 사용할 Resposne 객체를 설계하여 일관된 에러 응답 구조를 제공.(EX : ErrorResponse)
 * 2-1. ErrorResponse 예시 {errorCode: "404", errorMessage: "Product does not exist."}
 * 3.서비스 로직에서 발생할 수 있는 다양한 예외 상황에 사용할 수 있는 커스텀 예외 클래스를 설계
 * (EX DateNotExistException등 상황에 맞게 사용할 수 있는 커스템 Exception 추가 및 일반 Exception 외 서비스 로직 상 사용하는
 * 커스텀 Exception임을 파악할 수 있는 공통의 부모클래스를 생성하여 부모 클래스를 상속받는 형태로 관리)
 * 4.각 커스텀 예외에 대해 별도의 ExceptionHandler 메서드를 구현하여 상황에 맞는 HTTP 상태 코드와 에러 메시지를 반환하도록 개선.
 * 5.@Vaildation 어노테이션을 활용한 입력값 검증 사용 시 검증에 통과하지 못한 요청에 대해 발생하는 예외 처리 로직 및 응답 처리 추가.
 */
@Slf4j
@ControllerAdvice(value = {"com.wjc.codetest.product.controller"})
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> runTimeException(Exception e) {
        log.error("status :: {}, errorType :: {}, errorCause :: {}",
                HttpStatus.INTERNAL_SERVER_ERROR,
                "runtimeException",
                e.getMessage()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
