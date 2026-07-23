package org.senla.errorfreetext.repository.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.senla.errorfreetext.dto.ErrorDto;
import org.senla.errorfreetext.dto.TaskDto;
import org.senla.errorfreetext.dto.TaskStatus;
import org.senla.errorfreetext.repository.TaskRepository;
import org.senla.jooq.generated.tables.records.TaskErrorRecord;
import org.senla.jooq.generated.tables.records.TaskRecord;
import org.springframework.stereotype.Component;

import static org.senla.jooq.generated.Tables.TASK;
import static org.senla.jooq.generated.Tables.TASK_ERROR;

@Component
@RequiredArgsConstructor
public class TaskRepositoryImpl implements TaskRepository {

    private final DSLContext dsl;

    @Override
    public Optional<Long> saveTask(String lang) {

        Long taskId = dsl.insertInto(TASK)
                .set(TASK.LANGUAGE, lang)
                .set(TASK.STATUS, TaskStatus.CREATED.toString())
                .returningResult(TASK.ID)
                .fetchOne(TASK.ID);

        return Optional.ofNullable(taskId);

    }

    @Override
    public Long[] getNewTasksForProcess(int numberOfTasks) {

        return dsl.select(TASK.ID, TASK.LANGUAGE)
                .from(TASK)
                .where(TASK.STATUS.eq(TaskStatus.CREATED.toString()))
                .limit(numberOfTasks)
                .forUpdate()
                .skipLocked()
                .fetchArray(TASK.ID);
    }

    @Override
    public int updateTaskStatus(Long[] tasks) {
        List<TaskRecord> taskRecords = Arrays.stream(tasks).map(item -> {
            TaskRecord taskRecord = dsl.newRecord(TASK);
            taskRecord.setId(item);
            taskRecord.setStatus(TaskStatus.IN_PROGRESS.toString());
            return taskRecord;
        }).toList();

        return dsl.batchUpdate(taskRecords).execute().length;

    }

    @Override
    public Optional<String> getTaskStatusById(Long taskId) {
        return Optional.ofNullable(dsl.select(TASK.STATUS)
                .from(TASK)
                .where(TASK.ID.eq(taskId))
                .fetchOne(TASK.STATUS));

    }

    @Override
    public List<String> getTaskMessageErrors(Long taskId) {
        return dsl.select(TASK_ERROR.MESSAGE)
                .from(TASK_ERROR)
                .where(TASK_ERROR.TASK_ID.eq(taskId))
                .fetch(TASK_ERROR.MESSAGE);
    }

    @Override
    public int saveProcessedTask(List<TaskDto> list) {
        List<TaskRecord> recordList = list.stream().map(
                item -> {
                    TaskRecord taskRecord = dsl.newRecord(TASK);
                    taskRecord.setStatus(item.status());
                    taskRecord.setId(item.id());
                    return taskRecord;
                }
        ).toList();

        return dsl.batchUpdate(recordList).execute().length;
    }

    @Override
    public int saveError(List<ErrorDto> list) {
        List<TaskErrorRecord> errors = list.stream().map(
                item -> {
                    TaskErrorRecord error = dsl.newRecord(TASK_ERROR);
                    error.setTaskId(item.taskId());
                    error.setMessage(item.message());
                    return error;
                }
        ).toList();
        return dsl.batchInsert(errors).execute().length;
    }

}
