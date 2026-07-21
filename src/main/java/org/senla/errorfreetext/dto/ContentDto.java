package org.senla.errorfreetext.dto;

public record ContentDto(
        Integer position,
        String data,
        String lang,
        Long contentId,
        Long taskId
) {
}
