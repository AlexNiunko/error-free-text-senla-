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
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String DELIMITER = ": ";
    private static final String MESSAGE_DELIMITER = "; ";
    private static final String INTERNAL_ERROR_MESSAGE = "Внутренняя ошибка сервера";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        String errorMessage = errors.entrySet().stream()
                .map(entry -> String.join(DELIMITER, entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(MESSAGE_DELIMITER));

        log.warn("Ошибка валидации [{} {}]: {}",
                request.getMethod(),
                request.getRequestURI(),
                errorMessage);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildResponse(
                        ErrorCode.VALIDATION.getCode(),
                        errorMessage,
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ErrorResponse> handleServiceException(
            ServiceException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = ex.getHttpStatus();
        ErrorCode errorCode = mapErrorCode(status);
        String message = ex.getMessage();

        log.warn("Сервисная ошибка [{} {}]: status={}, code={}, message={}",
                request.getMethod(),
                request.getRequestURI(),
                status.value(),
                errorCode.getCode(),
                message);

        return ResponseEntity.status(status)
                .body(buildResponse(
                        errorCode.getCode(),
                        message,
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnhandledException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Непредвиденная ошибка [{} {}]: {}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage(),
                ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildResponse(
                        ErrorCode.INTERNAL_ERROR.getCode(),
                        INTERNAL_ERROR_MESSAGE,
                        request.getRequestURI()
                ));
    }

    private ErrorCode mapErrorCode(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> ErrorCode.VALIDATION;
            case NOT_FOUND -> ErrorCode.NOT_FOUND;
            case INTERNAL_SERVER_ERROR -> ErrorCode.INTERNAL_ERROR;
            default -> ErrorCode.INTERNAL_ERROR;
        };
    }

    private ErrorResponse buildResponse(String errorCode, String errorMessage, String path) {
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
        VALIDATION("40001"),
        NOT_FOUND("40401"),
        INTERNAL_ERROR("50001");

        private final String code;
    }
}