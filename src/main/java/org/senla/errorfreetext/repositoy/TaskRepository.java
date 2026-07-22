package org.senla.errorfreetext.repositoy;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.senla.errorfreetext.dto.ContentDto;
import org.senla.errorfreetext.entity.Task;
import org.senla.errorfreetext.entity.TaskContent;
import org.senla.errorfreetext.entity.TaskStatus;
import org.senla.errorfreetext.mapper.TaskContentMapper;
import org.senla.jooq.generated.tables.records.TaskContentRecord;
import org.senla.jooq.generated.tables.records.TaskRecord;
import org.springframework.stereotype.Component;

import static org.senla.jooq.generated.Tables.TASK;
import static org.senla.jooq.generated.Tables.TASK_CONTENT;
import static org.senla.jooq.generated.Tables.TASK_ERROR;

@Component
@RequiredArgsConstructor
public class TaskRepository {

    private final DSLContext dsl;
    private final TaskContentMapper taskContentMapper;

    public Optional<Long> saveTask(Task task) {

        Long taskId = dsl.insertInto(TASK)
                .set(TASK.LANGUAGE, task.getLang())
                .set(TASK.STATUS, task.getStatus().toString())
                .returningResult(TASK.ID)
                .fetchOne(TASK.ID);

        return Optional.ofNullable(taskId);

    }

    public boolean saveTaskContent(List<TaskContent> taskContentList, Long taskId) {

        List<TaskContentRecord> taskContentRecord = taskContentList.stream().map(item -> {
            TaskContentRecord contentRecord = dsl.newRecord(TASK_CONTENT);
            contentRecord.setPosition(item.getPosition());
            contentRecord.setContent(item.getContent());
            contentRecord.setTaskId(taskId);
            return contentRecord;
        }).toList();

        return Arrays.stream(dsl.batchInsert(taskContentRecord).execute()).allMatch(x -> x == 1);

    }

    public Long[] getNewTasksForProcess(int numberOfTasks) {

        return dsl.select(TASK.ID, TASK.LANGUAGE)
                .from(TASK)
                .where(TASK.STATUS.eq(TaskStatus.CREATED.toString()))
                .limit(numberOfTasks)
                .forUpdate()
                .skipLocked()
                .fetchArray(TASK.ID);
    }

    public boolean updateTaskStatus(Long[] tasks) {
        List<TaskRecord> taskRecords = Arrays.stream(tasks).map(item -> {
            TaskRecord taskRecord = dsl.newRecord(TASK);
            taskRecord.setId(item);
            taskRecord.setStatus(TaskStatus.IN_PROGRESS.toString());
            return taskRecord;
        }).toList();

        return Arrays.stream(dsl.batchUpdate(taskRecords).execute()).allMatch(x -> x == 1);

    }

    public List<ContentDto> getTaskContent(Long[] tasks) {
        if (tasks == null || tasks.length == 0) {
            return List.of();
        }

        return dsl.select(
                        TASK_CONTENT.ID,
                        TASK_CONTENT.CONTENT,
                        TASK_CONTENT.POSITION,
                        TASK.LANGUAGE,
                        TASK.ID
                )
                .from(TASK_CONTENT)
                .join(TASK)
                .on(TASK_CONTENT.TASK_ID.eq(TASK.ID))
                .where(TASK_CONTENT.TASK_ID.in(tasks))
                .forUpdate()
                .skipLocked()
                .fetch(item -> taskContentMapper.toContentDto(
                        item.get(TASK_CONTENT.POSITION),
                        item.get(TASK_CONTENT.CONTENT),
                        item.get(TASK.LANGUAGE),
                        item.get(TASK_CONTENT.ID),
                        item.get(TASK.ID)
                ));
    }

    public Optional<String> getTaskStatusById(Long taskId) {
        return Optional.ofNullable(dsl.select(TASK.STATUS)
                .from(TASK)
                .where(TASK.ID.eq(taskId))
                .fetchOne(TASK.STATUS));

    }

    public List<String> getTaskMessageErrors(Long taskId) {
        return dsl.select(TASK_ERROR.MESSAGE)
                .from(TASK_ERROR)
                .where(TASK_ERROR.TASK_ID.eq(taskId))
                .fetch(TASK_ERROR.MESSAGE);
    }

    public List<ContentDto> getContentDto(Long taskId){
        return dsl.select(TASK_CONTENT.CONTENT,TASK_CONTENT.POSITION)
                .from(TASK_CONTENT)
                .where(TASK_CONTENT.TASK_ID.eq(taskId))
                .fetch(item->taskContentMapper.toContentDto(
                        item.get(TASK_CONTENT.POSITION),
                        item.get(TASK_CONTENT.CONTENT)
                ));

    }

}
