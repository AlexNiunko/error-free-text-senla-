package org.senla.errorfreetext.controller.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record TaskResultResponse(
        String status,
        String data,
        List<String> error

) {
}
