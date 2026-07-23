package org.senla.errorfreetext.repository.impl;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.senla.errorfreetext.dto.ContentDto;
import org.senla.errorfreetext.mapper.TaskContentMapper;
import org.senla.errorfreetext.repository.TaskContentRepository;
import org.senla.jooq.generated.tables.records.TaskContentRecord;
import org.springframework.stereotype.Component;

import static org.senla.jooq.generated.Tables.TASK;
import static org.senla.jooq.generated.Tables.TASK_CONTENT;

@Component
@RequiredArgsConstructor
public class TaskContentRepositoryImpl implements TaskContentRepository {

    private final DSLContext dsl;
    private final TaskContentMapper taskContentMapper;

    @Override
    public boolean saveTaskContent(List<ContentDto> taskContentList, Long taskId) {

        List<TaskContentRecord> taskContentRecord = taskContentList.stream().map(item -> {
            TaskContentRecord contentRecord = dsl.newRecord(TASK_CONTENT);
            contentRecord.setPosition(item.position());
            contentRecord.setContent(item.data());
            contentRecord.setTaskId(taskId);
            return contentRecord;
        }).toList();

        return Arrays.stream(dsl.batchInsert(taskContentRecord).execute()).allMatch(x -> x == 1);

    }

    @Override
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

    @Override
    public List<ContentDto> getTaskContentForProcess(String status) {

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

    @Override
    public List<ContentDto> getContentDto(Long taskId) {
        return dsl.select(TASK_CONTENT.CONTENT, TASK_CONTENT.POSITION)
                .from(TASK_CONTENT)
                .where(TASK_CONTENT.TASK_ID.eq(taskId))
                .fetch(item -> taskContentMapper.toContentDto(
                        item.get(TASK_CONTENT.POSITION),
                        item.get(TASK_CONTENT.CONTENT)
                ));

    }

    @Override
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

}
