package org.senla.errorfreetext.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.senla.errorfreetext.client.dto.CheckTextDto;
import org.senla.errorfreetext.dto.ContentDto;

@Mapper(componentModel = "spring")
public interface TaskContentMapper {

    @Mapping(target = "position", source = "position")
    @Mapping(target = "data", source = "content")
    ContentDto toTaskContent(String content, Integer position);

    @Mapping(target = "taskId", source = "taskId")
    @Mapping(target = "contentId", source = "contentId")
    @Mapping(target = "lang", source = "lang")
    @Mapping(target = "data", source = "data")
    @Mapping(target = "position", source = "position")
    ContentDto toContentDto(Integer position, String data, String lang, Long contentId, Long taskId);

    @Mapping(target = "text", source = "content")
    @Mapping(target = "lang", source = "lang")
    @Mapping(target = "format", constant = "plain")
    CheckTextDto toCheckTextDto(String content, String lang);

    @Mapping(target = "taskId", source = "dto.taskId")
    @Mapping(target = "contentId", source = "dto.contentId")
    @Mapping(target = "lang", source = "dto.lang")
    @Mapping(target = "data", source = "fixedData")
    @Mapping(target = "position", source = "dto.position")
    ContentDto toContentDto(ContentDto dto, String fixedData);

    @Mapping(target = "data", source = "data")
    @Mapping(target = "position", source = "position")
    ContentDto toContentDto(Integer position, String data);

    @Mapping(target = "isCorrect", source = "isCorrect")
    @Mapping(target = "taskId", source = "dto.taskId")
    @Mapping(target = "contentId", source = "dto.contentId")
    @Mapping(target = "lang", source = "dto.lang")
    @Mapping(target = "data", source = "dto.data")
    @Mapping(target = "position", source = "dto.position")
    ContentDto toContentDto(ContentDto dto, boolean isCorrect);

}
