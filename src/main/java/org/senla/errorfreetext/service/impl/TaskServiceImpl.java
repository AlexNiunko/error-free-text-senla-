package org.senla.errorfreetext.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.senla.errorfreetext.controller.dto.TaskRequest;
import org.senla.errorfreetext.controller.dto.TaskResponse;
import org.senla.errorfreetext.controller.dto.TaskResultResponse;
import org.senla.errorfreetext.dto.ContentDto;
import org.senla.errorfreetext.dto.ErrorDto;
import org.senla.errorfreetext.dto.ProcessedContentDto;
import org.senla.errorfreetext.dto.TaskDto;
import org.senla.errorfreetext.dto.TaskStatus;
import org.senla.errorfreetext.exception.BadDataException;
import org.senla.errorfreetext.exception.EntityNotFoundException;
import org.senla.errorfreetext.exception.TaskContentSaveException;
import org.senla.errorfreetext.exception.TaskSaveException;
import org.senla.errorfreetext.mapper.TaskContentMapper;
import org.senla.errorfreetext.mapper.TaskMapper;
import org.senla.errorfreetext.repository.TaskContentRepository;
import org.senla.errorfreetext.repository.TaskRepository;
import org.senla.errorfreetext.service.ContentService;
import org.senla.errorfreetext.service.TaskService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final ContentService contentService;
    private final TaskContentMapper taskContentMapper;
    private final TaskMapper taskMapper;
    private final TaskRepository taskRepository;
    private final TaskContentRepository taskContentRepository;

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
            throw new BadDataException("Текст для корректировки - null");
        }

        List<String> dataList = contentService.divide(data);
        log.debug("Текст разделён на части: count={}", dataList.size());
        List<ContentDto> taskContentList = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            String item = dataList.get(i);
            taskContentList.add(taskContentMapper.toTaskContent(item, i));
        }

        log.debug("Сформировано TaskContent элементов: {}", taskContentList.size());
        Long savedTaskId = taskRepository.saveTask(lang)
                .orElseThrow(() -> new TaskSaveException("Не удалось сохранить задачу в БД"));
        log.info("Задание на обработку текста сохранено в БД, идентификатор - {}", savedTaskId);

        if (!taskContentRepository.saveTaskContent(taskContentList, savedTaskId)) {
            throw new TaskContentSaveException("Не удалось сохранить в БД TaskContent - фрагменты исходного текста");
        }

        return new TaskResponse(savedTaskId, taskContentList.size());
    }

    @Override
    @Transactional(readOnly = true)
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
                return getTaskResultResponseCompleted(id, status);
            }
            default -> {
                return taskMapper.toTaskResultResponse(status);
            }

        }

    }

    @Transactional
    @Override
    public int saveProcessedTask(Map<Long, List<ProcessedContentDto>> input) {
        Long[] keys = input.keySet().toArray(Long[]::new);

        List<ContentDto> contentBeforeProcess = taskContentRepository.getTaskContentByTaskId(keys);
        log.info("Получен из БД список фрагментов текста для сверки - {}", contentBeforeProcess);

        Map<Long, List<ContentDto>> mapBeforeProcess = contentBeforeProcess.stream()
                .collect(Collectors.groupingBy(ContentDto::taskId));
        log.info("Карта - {}", mapBeforeProcess);

        Map<Long, List<ProcessedContentDto>> afterProcessCorrect = new HashMap<>();
        Map<Long, List<ProcessedContentDto>> afterProcessFailed = new HashMap<>();

        for (Map.Entry<Long, List<ProcessedContentDto>> inputEntry : input.entrySet()) {
            Long key = inputEntry.getKey();

            List<ProcessedContentDto> value = inputEntry.getValue();

            List<ProcessedContentDto> errorContent = value.stream().filter(item -> item.errorMessage() != null).toList();

            if (errorContent.isEmpty() && mapBeforeProcess.get(key).size() == value.size()) {
                afterProcessCorrect.put(key, value);
            } else {
                afterProcessFailed.put(key, errorContent);
            }

        }

        var result = taskRepository.saveProcessedTask(getTaskForUpdate(afterProcessCorrect, afterProcessFailed));

        var processedContent = taskContentRepository.saveProcessedContent(getContentForUpdate(input));
        log.info("Обработано - {} фрагментов текста", processedContent);

        int errorResult = taskRepository.saveError(getErrorDtoList(afterProcessFailed));
        log.info("Были сохранены ошибки обработки фрагментов текста в количестве - {}", errorResult);

        return result;
    }


    @Override
    @Transactional
    public List<ContentDto> getTaskContentForProcess(int taskCount) {

        Long[] taskIds = taskRepository.getNewTasksForProcess(taskCount);

        log.info("Найдены задачи со статусом CREATED, идентификаторы - {} ", Arrays.toString(taskIds));

        int updatedTasks = taskRepository.updateTaskStatus(taskIds);

        log.info("Количество задач с измененным статусом - {} ", updatedTasks);

        return taskContentRepository.getTaskContentForProcess(TaskStatus.IN_PROGRESS.toString());
    }

    private TaskResultResponse getTaskResultResponseFailed(Long taskId, String statusName) {
        List<String> errorMessages = taskRepository.getTaskMessageErrors(taskId);
        return TaskResultResponse.builder().status(statusName).error(errorMessages).build();
    }

    private TaskResultResponse getTaskResultResponseCompleted(Long taskId, String statusName) {
        List<ContentDto> content = taskContentRepository.getContentDto(taskId);

        if (content.isEmpty()) {
            log.info("По задаче с идентификатором - {} отсутствует контент", taskId);
            return TaskResultResponse.builder().status(statusName).build();
        }

        return TaskResultResponse.builder().status(statusName).data(contentService.buildData(content)).build();
    }

    private List<ErrorDto> getErrorDtoList(Map<Long, List<ProcessedContentDto>> afterProcessFailed) {
        return afterProcessFailed.values().stream().flatMap(List::stream)
                .map(item -> ErrorDto.builder().taskId(item.contentDto().taskId())
                        .message(item.errorMessage()).build()).toList();
    }

    private List<ContentDto> getContentForUpdate(Map<Long, List<ProcessedContentDto>> input) {
        return input.values().stream()
                .flatMap(List::stream)
                .map(item -> ContentDto.builder()
                        .contentId(item.contentDto().contentId())
                        .isCorrect(item.contentDto().isCorrect())
                        .data(item.contentDto().data())
                        .build())
                .toList();
    }

    private List<TaskDto> getTaskForUpdate(Map<Long, List<ProcessedContentDto>> afterProcessCorrect,
                                           Map<Long, List<ProcessedContentDto>> afterProcessFailed) {
        return Stream.concat(
                        afterProcessCorrect.keySet().stream()
                                .map(item -> new TaskDto(item, TaskStatus.COMPLETED.toString())),
                        afterProcessFailed.keySet().stream()
                                .map(item -> new TaskDto(item, TaskStatus.FAILED.toString())))
                .toList();
    }

}
