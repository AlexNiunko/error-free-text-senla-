package org.senla.errorfreetext.dto;

import lombok.Builder;

@Builder
public record ContentDto(
        Integer position,
        String data,
        String lang,
        Long contentId,
        Long taskId,
        boolean isCorrect
) {

}
