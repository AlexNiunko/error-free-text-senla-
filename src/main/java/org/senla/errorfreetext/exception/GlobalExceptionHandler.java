package org.senla.errorfreetext.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    public static final String DELIMITER = ": ";
    public static final String MESSAGE_DELIMITER = "; ";

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        var status = ex.getStatusCode();

        var errorMessage = errors.entrySet().stream()
                .map(entry -> String.join(DELIMITER, entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(MESSAGE_DELIMITER));

        log.warn("Validation error on [{} {}]: {} -> {}",
                request.getMethod(),
                request.getRequestURI(),
                errors,
                errorMessage
        );

        return ResponseEntity.status(status)
                .body(getResponseDto(ErrorCode.VALIDATION_ERROR.getCode(), errorMessage, request.getRequestURI()));
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ErrorResponse> handleBusinessErrors(ServiceException ex, HttpServletRequest request) {
        var httpStatus = ex.getHttpStatus();
        String message = ex.getMessage();

        log.warn("Бизнес-ошибка при обработке запроса [{} {}]: status={}, code={}, message={}",
                request.getMethod(),
                request.getRequestURI(),
                httpStatus.value(),
                ErrorCode.TASK_NOT_FOUND.getCode(),
                message);

        return ResponseEntity.status(httpStatus)
                .body(getResponseDto(ErrorCode.TASK_NOT_FOUND.getCode(), message, request.getRequestURI()));
    }

    private ErrorResponse getResponseDto(String errorCode, String errorMessage, String path) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }

    @Getter
    @AllArgsConstructor
    enum ErrorCode {
        VALIDATION_ERROR("40001"),
        TASK_NOT_FOUND("40401");

        private final String code;

    }

}
