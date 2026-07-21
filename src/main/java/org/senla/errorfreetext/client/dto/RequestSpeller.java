package org.senla.errorfreetext.client.dto;

public record RequestSpeller(
        String text,
        String lang,
        Integer options,
        String format

) {
}
