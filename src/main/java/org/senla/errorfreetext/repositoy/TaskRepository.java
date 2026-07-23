package org.senla.errorfreetext.repositoy;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.senla.errorfreetext.dto.ContentDto;
import org.senla.errorfreetext.dto.ErrorDto;
import org.senla.errorfreetext.dto.TaskDto;
import org.senla.errorfreetext.entity.TaskContent;
import org.senla.errorfreetext.entity.TaskStatus;
import org.senla.errorfreetext.mapper.TaskContentMapper;
import org.senla.jooq.generated.tables.records.TaskContentRecord;
import org.senla.jooq.generated.tables.records.TaskErrorRecord;
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

    public Optional<Long> saveTask(String lang) {

        Long taskId = dsl.insertInto(TASK)
                .set(TASK.LANGUAGE, lang)
                .set(TASK.STATUS, TaskStatus.CREATED.toString())
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

    public int updateTaskStatus(Long[] tasks) {
        List<TaskRecord> taskRecords = Arrays.stream(tasks).map(item -> {
            TaskRecord taskRecord = dsl.newRecord(TASK);
            taskRecord.setId(item);
            taskRecord.setStatus(TaskStatus.IN_PROGRESS.toString());
            return taskRecord;
        }).toList();

        return dsl.batchUpdate(taskRecords).execute().length;

    }

    public List<ContentDto> getTaskContentByTaskId(Long[] ids) {
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
                .where(TASK.ID.in(ids))
                .fetch(item -> taskContentMapper.toContentDto(
                        item.get(TASK_CONTENT.POSITION),
                        item.get(TASK_CONTENT.CONTENT),
                        item.get(TASK.LANGUAGE),
                        item.get(TASK_CONTENT.ID),
                        item.get(TASK.ID)
                ));
    }

    public List<ContentDto> getTaskContentByTaskStatus(String status) {

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
                .where(TASK.STATUS.eq(status).and(TASK_CONTENT.IS_CORRECT.eq(Boolean.FALSE)))
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

    public List<ContentDto> getContentDto(Long taskId) {
        return dsl.select(TASK_CONTENT.CONTENT, TASK_CONTENT.POSITION)
                .from(TASK_CONTENT)
                .where(TASK_CONTENT.TASK_ID.eq(taskId))
                .fetch(item -> taskContentMapper.toContentDto(
                        item.get(TASK_CONTENT.POSITION),
                        item.get(TASK_CONTENT.CONTENT)
                ));

    }

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

    public int saveProcessedContent(List<ContentDto> list) {
        List<TaskContentRecord> contentRecordList = list.stream()
                .map(item -> {
                    TaskContentRecord taskContentRecord = dsl.newRecord(TASK_CONTENT);
                    taskContentRecord.setId(item.contentId());
                    taskContentRecord.setIsCorrect(Boolean.TRUE);
                    taskContentRecord.setContent(item.data());
                    return taskContentRecord;
                }).toList();

        return dsl.batchUpdate(contentRecordList).execute().length;
    }

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
