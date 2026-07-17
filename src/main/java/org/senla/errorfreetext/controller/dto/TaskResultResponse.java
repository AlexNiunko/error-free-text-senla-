package org.senla.errorfreetext.controller.dto;

import lombok.Builder;

@Builder
public record TaskResultResponse(
        String status,
        String data,
        String error

) {
}
