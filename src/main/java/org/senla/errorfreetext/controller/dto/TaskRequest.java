package org.senla.errorfreetext.controller.dto;

import jakarta.validation.constraints.Pattern;

public record TaskRequest(

        @Pattern(
                regexp = "^(?=.*[A-Za-zА-Яа-яЁё]).{3,}$",
                message = "Текст должен содержать минимум 3 символа и не может содержать только спецсимволы и цифры"
        )
        String data,

        @Pattern(
                regexp = "^(EN|RU)$",
                message = "Параметр языка может быть только 'EN' или 'RU'"
        )
        String lang
) {
}
