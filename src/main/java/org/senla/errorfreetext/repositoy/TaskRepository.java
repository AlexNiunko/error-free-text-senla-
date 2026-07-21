package org.senla.errorfreetext.repositoy;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.senla.errorfreetext.entity.Task;
import org.senla.errorfreetext.entity.TaskContent;
import org.senla.jooq.generated.tables.records.TaskContentRecord;
import org.springframework.stereotype.Component;

import static org.senla.jooq.generated.Tables.TASK;
import static org.senla.jooq.generated.Tables.TASK_CONTENT;

@Component
@RequiredArgsConstructor
public class TaskRepository {

    private final DSLContext dsl;

    public Optional<Long> saveTask(Task task) {

        Long taskId = dsl.insertInto(TASK)
                .set(TASK.LANGUAGE, task.getLang())
                .set(TASK.STATUS, task.getStatus().toString())
                .returningResult(TASK.ID)
                .fetchOne(TASK.ID);

        return Optional.ofNullable(taskId);

    }

    public boolean saveTaskContent(List<TaskContent> taskContentList,Long taskId) {

        List<TaskContentRecord> taskContentRecord = taskContentList.stream().map(item -> {
            TaskContentRecord contentRecord = dsl.newRecord(TASK_CONTENT);
            contentRecord.setPosition(item.getPosition());
            contentRecord.setContent(item.getContent());
            contentRecord.setTaskId(taskId);
            return contentRecord;
        }).toList();

        return Arrays.stream(dsl.batchInsert(taskContentRecord).execute()).allMatch(x -> x == 1);

    }

}
