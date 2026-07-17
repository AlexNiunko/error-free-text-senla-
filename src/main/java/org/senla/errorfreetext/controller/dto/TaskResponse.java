package org.senla.errorfreetext.controller.dto;

import java.time.LocalDateTime;

public record TaskResponse(
        Long taskId,
        LocalDateTime createdAt

) {

}
