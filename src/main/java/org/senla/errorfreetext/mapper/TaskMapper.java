package org.senla.errorfreetext.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.senla.errorfreetext.entity.Task;
import org.senla.errorfreetext.entity.TaskStatus;
import org.senla.jooq.generated.tables.records.TaskRecord;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(target = "status", source = "status")
    Task toTask(TaskStatus status, String lang);

    TaskRecord toTaskRecord(Task Task);

}
