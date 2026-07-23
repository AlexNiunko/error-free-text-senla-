package org.senla.errorfreetext.dto;

import lombok.Builder;

@Builder
public record ErrorDto(
        Long taskId,
        String message
) {
}
