package org.senla.errorfreetext.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import org.senla.errorfreetext.service.impl.TaskServiceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private ContentService contentService;

    @Mock
    private TaskContentMapper taskContentMapper;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskContentRepository taskContentRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    @Test
    void shouldCreateNewTask() {
        var data = "Hello world";
        var position = 0;
        var id = 1L;
        var lang = "EN";
        TaskRequest dto = new TaskRequest(data, lang);
        List<String> stringList = List.of(data);
        ContentDto contentDto = ContentDto.builder().position(position).data(data).build();
        List<ContentDto> contentList =
                List.of(contentDto);

        when(contentService.divide(data)).thenReturn(stringList);
        when(taskContentMapper.toTaskContent(data, position)).thenReturn(contentDto);
        when(taskRepository.saveTask(lang)).thenReturn(Optional.of(id));
        when(taskContentRepository.saveTaskContent(contentList, id)).thenReturn(true);

        TaskResponse actual = taskService.createTask(dto);
        TaskResponse expected = new TaskResponse(id, 1);

        assertEquals(expected, actual);

        verify(contentService).divide(data);
        verify(taskContentMapper).toTaskContent(data, position);
        verify(taskRepository).saveTask(lang);
        verify(taskContentRepository).saveTaskContent(contentList, id);

    }

    @Test
    void shouldThrowBadDataExceptionWhenDataIsNull() {
        String lang = "EN";
        TaskRequest dto = new TaskRequest(null, lang);

        BadDataException ex = assertThrows(
                BadDataException.class,
                () -> taskService.createTask(dto)
        );

        assertEquals("Текст для корректировки - null", ex.getMessage());

        verifyNoInteractions(contentService, taskContentMapper, taskRepository, taskContentRepository);
    }

    @Test
    void shouldThrowTaskSaveExceptionWhenTaskNotPersisted() {
        var data = "Hello world";
        var position = 0;
        var lang = "EN";
        TaskRequest dto = new TaskRequest(data, lang);

        List<String> stringList = List.of(data);
        ContentDto contentDto = ContentDto.builder().position(position).data(data).build();

        when(contentService.divide(data)).thenReturn(stringList);
        when(taskContentMapper.toTaskContent(data, position)).thenReturn(contentDto);
        when(taskRepository.saveTask(lang)).thenReturn(Optional.empty());

        TaskSaveException ex = assertThrows(
                TaskSaveException.class,
                () -> taskService.createTask(dto)
        );

        assertEquals("Не удалось сохранить задачу в БД", ex.getMessage());

        verify(contentService).divide(data);
        verify(taskContentMapper).toTaskContent(data, position);
        verify(taskRepository).saveTask(lang);
        verifyNoInteractions(taskContentRepository);
    }

    @Test
    void shouldThrowTaskContentSaveExceptionWhenContentNotPersisted() {
        var data = "Hello world";
        var position = 0;
        var id = 1L;
        var lang = "EN";
        TaskRequest dto = new TaskRequest(data, lang);

        List<String> stringList = List.of(data);
        ContentDto contentDto = ContentDto.builder().position(position).data(data).build();
        List<ContentDto> contentList = List.of(contentDto);

        when(contentService.divide(data)).thenReturn(stringList);
        when(taskContentMapper.toTaskContent(data, position)).thenReturn(contentDto);
        when(taskRepository.saveTask(lang)).thenReturn(Optional.of(id));
        when(taskContentRepository.saveTaskContent(contentList, id)).thenReturn(false);

        TaskContentSaveException ex = assertThrows(
                TaskContentSaveException.class,
                () -> taskService.createTask(dto)
        );

        assertEquals(
                "Не удалось сохранить в БД TaskContent - фрагменты исходного текста",
                ex.getMessage()
        );

        verify(contentService).divide(data);
        verify(taskContentMapper).toTaskContent(data, position);
        verify(taskRepository).saveTask(lang);
        verify(taskContentRepository).saveTaskContent(contentList, id);
    }

    @Test
    void getTaskResultShouldThrowEntityNotFoundWhenTaskMissing() {
        Long taskId = 1L;

        when(taskRepository.getTaskStatusById(taskId)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> taskService.getTaskResult(taskId)
        );

        assertTrue(ex.getMessage().contains("не найдено"));
        verify(taskRepository).getTaskStatusById(taskId);
        verifyNoInteractions(taskMapper, taskContentRepository, contentService);
    }

    @Test
    void getTaskResultShouldReturnFailedWithErrors() {
        Long taskId = 1L;
        String status = TaskStatus.FAILED.toString();
        List<String> errors = List.of("error1", "error2");

        when(taskRepository.getTaskStatusById(taskId)).thenReturn(Optional.of(status));
        when(taskRepository.getTaskMessageErrors(taskId)).thenReturn(errors);

        TaskResultResponse actual = taskService.getTaskResult(taskId);

        assertEquals(status, actual.status());
        assertEquals(errors, actual.error());
        assertNull(actual.data());

        verify(taskRepository).getTaskStatusById(taskId);
        verify(taskRepository).getTaskMessageErrors(taskId);
        verifyNoInteractions(taskContentRepository, contentService, taskMapper);
    }

    @Test
    void getTaskResultShouldReturnCompletedWithData() {
        Long taskId = 1L;
        String status = TaskStatus.COMPLETED.toString();

        List<ContentDto> content = List.of(
                ContentDto.builder().contentId(10L).position(0).data("part1").build(),
                ContentDto.builder().contentId(11L).position(1).data("part2").build()
        );
        String builtData = "part1part2";

        when(taskRepository.getTaskStatusById(taskId)).thenReturn(Optional.of(status));
        when(taskContentRepository.getContentDto(taskId)).thenReturn(content);
        when(contentService.buildData(content)).thenReturn(builtData);

        TaskResultResponse actual = taskService.getTaskResult(taskId);

        assertEquals(status, actual.status());
        assertEquals(builtData, actual.data());
        assertNull(actual.error());

        verify(taskRepository).getTaskStatusById(taskId);
        verify(taskContentRepository).getContentDto(taskId);
        verify(contentService).buildData(content);
        verifyNoInteractions(taskMapper);
    }

    @Test
    void getTaskResultShouldReturnStatusOnlyWhenCompletedAndNoContent() {
        Long taskId = 1L;
        String status = TaskStatus.COMPLETED.toString();

        when(taskRepository.getTaskStatusById(taskId)).thenReturn(Optional.of(status));
        when(taskContentRepository.getContentDto(taskId)).thenReturn(List.of());

        TaskResultResponse actual = taskService.getTaskResult(taskId);

        assertEquals(status, actual.status());
        assertNull(actual.data());
        assertNull(actual.error());

        verify(taskRepository).getTaskStatusById(taskId);
        verify(taskContentRepository).getContentDto(taskId);
        verifyNoInteractions(contentService, taskMapper);
    }

    @Test
    void getTaskResultShouldUseMapperForNonTerminalStatuses() {
        Long taskId = 1L;
        String status = TaskStatus.IN_PROGRESS.toString();
        TaskResultResponse mapped = TaskResultResponse.builder()
                .status(status)
                .build();

        when(taskRepository.getTaskStatusById(taskId)).thenReturn(Optional.of(status));
        when(taskMapper.toTaskResultResponse(status)).thenReturn(mapped);

        TaskResultResponse actual = taskService.getTaskResult(taskId);

        assertEquals(mapped, actual);

        verify(taskRepository).getTaskStatusById(taskId);
        verify(taskMapper).toTaskResultResponse(status);
        verifyNoInteractions(taskContentRepository, contentService);
    }

    @Test
    void getTaskContentForProcessShouldFetchAndUpdateTasks() {
        int batchSize = 2;
        Long[] taskIds = new Long[]{1L, 2L};
        int updatedCount = taskIds.length;

        List<ContentDto> content = List.of(
                ContentDto.builder().taskId(1L).contentId(10L).position(0).data("part1").build(),
                ContentDto.builder().taskId(2L).contentId(20L).position(0).data("part2").build()
        );

        when(taskRepository.getNewTasksForProcess(batchSize,TaskStatus.CREATED.toString())).thenReturn(taskIds);
        when(taskRepository.updateTaskStatus(taskIds)).thenReturn(updatedCount);
        when(taskContentRepository.getTaskContentForProcess(TaskStatus.IN_PROGRESS.toString()))
                .thenReturn(content);

        List<ContentDto> actual = taskService.getTaskContentForProcess(batchSize);

        assertEquals(content, actual);

        verify(taskRepository).getNewTasksForProcess(batchSize,TaskStatus.CREATED.toString());
        verify(taskRepository).updateTaskStatus(taskIds);
        verify(taskContentRepository).getTaskContentForProcess(TaskStatus.IN_PROGRESS.toString());
    }


    @Test
    void saveProcessedTaskShouldUpdateTasksAndContentWhenAllSuccessful() {
        Long taskId = 1L;
        Long contentId = 10L;

        ContentDto original = ContentDto.builder()
                .taskId(taskId)
                .contentId(contentId)
                .position(0)
                .data("original")
                .build();

        ContentDto processedContentDto = ContentDto.builder()
                .taskId(taskId)
                .contentId(contentId)
                .position(0)
                .data("processed")
                .isCorrect(true)
                .build();

        ProcessedContentDto processed = new ProcessedContentDto(processedContentDto, null);

        Map<Long, List<ProcessedContentDto>> input = new HashMap<>();
        input.put(taskId, List.of(processed));

        ContentDto contentForUpdate = ContentDto.builder()
                .contentId(contentId)
                .data("processed")
                .isCorrect(true)
                .build();

        when(taskContentRepository.getTaskContentByTaskId(new Long[]{taskId}))
                .thenReturn(List.of(original));

        when(taskRepository.saveProcessedTask(
                List.of(new TaskDto(taskId, TaskStatus.COMPLETED.toString()))
        )).thenReturn(1);

        when(taskContentRepository.saveProcessedContent(
                List.of(contentForUpdate)
        )).thenReturn(1);

        when(taskRepository.saveError(List.of())).thenReturn(0);

        int result = taskService.saveProcessedTask(input);

        assertEquals(1, result);

        verify(taskContentRepository).getTaskContentByTaskId(new Long[]{taskId});
        verify(taskRepository).saveProcessedTask(
                List.of(new TaskDto(taskId, TaskStatus.COMPLETED.toString()))
        );
        verify(taskContentRepository).saveProcessedContent(
                List.of(contentForUpdate)
        );
        verify(taskRepository).saveError(List.of());
    }

    @Test
    void saveProcessedTaskShouldMarkTaskFailedWhenErrorsPresent() {
        Long taskId = 1L;
        Long contentId = 10L;

        ContentDto original = ContentDto.builder()
                .taskId(taskId)
                .contentId(contentId)
                .position(0)
                .data("original")
                .build();

        ContentDto errorContentDto = ContentDto.builder()
                .taskId(taskId)
                .contentId(contentId)
                .position(0)
                .data("original")
                .isCorrect(false)
                .build();

        ProcessedContentDto processedWithError =
                new ProcessedContentDto(errorContentDto, "Ошибка обработки");

        Map<Long, List<ProcessedContentDto>> input = new HashMap<>();
        input.put(taskId, List.of(processedWithError));

        ContentDto contentForUpdate = ContentDto.builder()
                .contentId(contentId)
                .data("original")
                .isCorrect(false)
                .build();

        List<ErrorDto> expectedErrors = List.of(
                ErrorDto.builder()
                        .taskId(taskId)
                        .message("Ошибка обработки")
                        .build()
        );

        when(taskContentRepository.getTaskContentByTaskId(new Long[]{taskId}))
                .thenReturn(List.of(original));

        when(taskRepository.saveProcessedTask(
                List.of(new TaskDto(taskId, TaskStatus.FAILED.toString()))
        )).thenReturn(1);

        when(taskContentRepository.saveProcessedContent(
                List.of(contentForUpdate)
        )).thenReturn(1);

        when(taskRepository.saveError(expectedErrors)).thenReturn(1);

        int result = taskService.saveProcessedTask(input);

        assertEquals(1, result);

        verify(taskContentRepository).getTaskContentByTaskId(new Long[]{taskId});
        verify(taskRepository).saveProcessedTask(
                List.of(new TaskDto(taskId, TaskStatus.FAILED.toString()))
        );
        verify(taskContentRepository).saveProcessedContent(
                List.of(contentForUpdate)
        );
        verify(taskRepository).saveError(expectedErrors);
    }

}
