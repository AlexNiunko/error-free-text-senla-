package org.senla.errorfreetext.client.dto;

import lombok.experimental.FieldNameConstants;

@FieldNameConstants
public record CheckTextDto(
        String text,
        String lang,
        Integer options,
        String format
) {
}
