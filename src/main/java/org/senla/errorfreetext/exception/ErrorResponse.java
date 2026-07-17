package org.senla.errorfreetext.exception;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record ErrorResponse(
        String errorMessage,
        String errorCode,
        LocalDateTime timestamp,
        String path
) {
}
