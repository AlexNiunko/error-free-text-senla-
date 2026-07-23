package org.senla.errorfreetext.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.senla.errorfreetext.controller.dto.TaskResultResponse;

@Mapper(componentModel = "spring")
public interface TaskMapper {


    @Mapping(target = "status", source = "status")
    TaskResultResponse toTaskResultResponse(String status);

}
