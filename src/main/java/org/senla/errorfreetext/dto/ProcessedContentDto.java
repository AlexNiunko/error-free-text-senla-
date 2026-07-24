package org.senla.errorfreetext.dto;

import java.util.List;

public record ProcessedContentDto(
        List<ContentDto> contentDto,
        String errorMessage,
        Long taskId
) {
}
