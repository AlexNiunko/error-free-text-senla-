package org.senla.errorfreetext.dto;

public record ProcessedContentDto(
        ContentDto contentDto,
        Boolean isCorrect
) {
}
