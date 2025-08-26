// src/main/java/com/example/freeboard/exception/ResourceNotFoundException.java
package com.example.freeboard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND) // HTTP 404 응답으로 설정
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) { // 필요 시 Throwable cause 생성자 추가
        super(message, cause);
    }
}
