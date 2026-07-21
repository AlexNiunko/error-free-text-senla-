package org.senla.errorfreetext.service.impl;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.senla.errorfreetext.controller.dto.TaskRequest;
import org.senla.errorfreetext.controller.dto.TaskResponse;
import org.senla.errorfreetext.controller.dto.TaskResultResponse;
import org.senla.errorfreetext.entity.Task;
import org.senla.errorfreetext.entity.TaskContent;
import org.senla.errorfreetext.entity.TaskStatus;
import org.senla.errorfreetext.exception.BadDataException;
import org.senla.errorfreetext.exception.TaskContentSaveException;
import org.senla.errorfreetext.exception.TaskSaveException;
import org.senla.errorfreetext.mapper.TaskContentMapper;
import org.senla.errorfreetext.mapper.TaskMapper;
import org.senla.errorfreetext.repositoy.TaskRepository;
import org.senla.errorfreetext.service.ContentService;
import org.senla.errorfreetext.service.TaskService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static org.senla.errorfreetext.exception.ErrorMessage.DATA_IS_NULL;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final ContentService contentService;
    private final TaskContentMapper taskContentMapper;
    private final TaskMapper taskMapper;
    private final TaskRepository taskRepository;

    @Transactional
    @Override
    public TaskResponse createTask(TaskRequest dto) {
        String data = dto.data();
        String lang = dto.lang();
        log.info("Создание задачи по запросу: dataLength={}, lang={}",
                data != null ? data.length() : null,
                lang);

        if (data == null) {
            log.warn("Попытка создать задачу с null-данными");
            throw new BadDataException(DATA_IS_NULL);
        }

        List<String> dataList = contentService.divide(data);
        log.debug("Текст разделён на части: count={}", dataList.size());
        List<TaskContent> taskContentList = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            String item = dataList.get(i);
            taskContentList.add(taskContentMapper.toTaskContent(item, i));
        }
        log.debug("Сформировано TaskContent элементов: {}", taskContentList.size());
        Task task = taskMapper.toTask(TaskStatus.CREATED, lang);
        Long savedTaskId = taskRepository.saveTask(task)
                .orElseThrow(() -> new TaskSaveException("Не удалось сохранить задачу в БД"));
        log.info("Задание на обработку текста сохранено в БД, идентификатор - {}", savedTaskId);

        if (!taskRepository.saveTaskContent(taskContentList,savedTaskId)) {
            throw new TaskContentSaveException("Не удалось сохранить текст задания в БД");
        }

        return new TaskResponse(savedTaskId,taskContentList.size());
    }

    @Override
    public TaskResultResponse getTaskResult(Long id) {
        return null;
    }

}
