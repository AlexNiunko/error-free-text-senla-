package org.senla.errorfreetext.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.senla.errorfreetext.entity.TaskContent;

@Mapper(componentModel = "spring")
public interface TaskContentMapper {

    @Mapping(target = "position", source = "position")
    @Mapping(target = "content", source = "content")
    TaskContent toTaskContent(String content, Integer position);

}
