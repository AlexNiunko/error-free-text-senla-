package org.senla.errorfreetext.dto;

public record ProcessedContentDto(
        ContentDto contentDto,
        String errorMessage
) {
}
