package org.senla.errorfreetext.repositoy;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.senla.errorfreetext.entity.Task;
import org.senla.errorfreetext.entity.TaskContent;
import org.senla.errorfreetext.mapper.TaskMapper;
import org.senla.jooq.generated.tables.records.TaskRecord;
import org.springframework.stereotype.Component;

import static org.senla.jooq.generated.Tables.TASK;


@Component
@RequiredArgsConstructor
public class TaskRepository {

    private final DSLContext dsl;
    private final TaskMapper taskMapper;

    public TaskRecord saveTask(Task task) {
        return dsl.insertInto(TASK)
                .set(taskMapper.toTaskRecord(task))
                .returning(TASK.ID)
                .fetchOne();

    }

    public int saveTaskContent(List<TaskContent> taskContentList){

        return 0;
    }


}
