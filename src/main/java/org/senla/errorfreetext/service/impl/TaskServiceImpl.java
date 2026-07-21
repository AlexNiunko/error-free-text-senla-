package org.senla.errorfreetext.service.impl;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.senla.errorfreetext.controller.dto.TaskRequest;
import org.senla.errorfreetext.controller.dto.TaskResponse;
import org.senla.errorfreetext.controller.dto.TaskResultResponse;
import org.senla.errorfreetext.dto.ContentDto;
import org.senla.errorfreetext.entity.Task;
import org.senla.errorfreetext.entity.TaskContent;
import org.senla.errorfreetext.entity.TaskStatus;
import org.senla.errorfreetext.exception.BadDataException;
import org.senla.errorfreetext.exception.EntityNotFoundException;
import org.senla.errorfreetext.exception.TaskContentSaveException;
import org.senla.errorfreetext.exception.TaskSaveException;
import org.senla.errorfreetext.mapper.TaskContentMapper;
import org.senla.errorfreetext.mapper.TaskMapper;
import org.senla.errorfreetext.repositoy.TaskRepository;
import org.senla.errorfreetext.service.ContentService;
import org.senla.errorfreetext.service.TaskService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static javax.management.remote.JMXConnectionNotification.FAILED;
import static org.senla.errorfreetext.entity.TaskStatus.COMPLETED;
import static org.senla.errorfreetext.exception.ErrorMessage.DATA_IS_NULL;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final ContentService contentService;
    private final TaskContentMapper taskContentMapper;
    private final TaskMapper taskMapper;
    private final TaskRepository taskRepository;

    @Override
    @Transactional
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

        if (!taskRepository.saveTaskContent(taskContentList, savedTaskId)) {
            throw new TaskContentSaveException("Не удалось сохранить в БД TaskContent - фрагменты исходного текста");
        }

        return new TaskResponse(savedTaskId, taskContentList.size());
    }

    @Override
    public TaskResultResponse getTaskResult(Long id) {
        String status = taskRepository
                .getTaskStatusById(id)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                String.format("Задание на обработку текста с идентификатором - %d не найдено", id)));

        log.info("Статус задачи с идентификатором - {}, {}", id, status);

        TaskStatus taskStatus = TaskStatus.valueOf(status);

        switch (taskStatus) {
            case FAILED -> {
                return getTaskResultResponseFailed(id, status);
            }
            case COMPLETED -> {
                return getTaskResultResponseCompleted(task, statusName);
            }
            default -> {
                return taskMapper.toTaskResultResponse(statusName);
            }

        }


        return null;
    }

    private TaskResultResponse getTaskResultResponseFailed(Long taskId, String statusName) {
        List<String> errorMessages = taskRepository.getTaskMessageErrors(taskId);
        return TaskResultResponse.builder().status(statusName).error(errorMessages).build();
    }

    private TaskResultResponse getTaskResultResponseCompleted(Long taskId, String statusName) {

      return null;
    }


    @Override
    @Transactional
    public List<ContentDto> getTaskContentForProcess(int taskCount) {

        Long[] taskIds = taskRepository.getNewTasksForProcess(taskCount);
        var length = taskIds.length;
        log.info("В обработку взято - {} фрагментов текста", length);
        if (length == 0) {
            return List.of();
        }

        if (!taskRepository.updateTaskStatus(taskIds)) {
            throw new TaskContentSaveException("Не удалось обновить статус заданий");
        }


        return taskRepository.getTaskContent(taskIds);
    }


}
