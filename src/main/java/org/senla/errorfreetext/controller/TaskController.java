package org.senla.errorfreetext.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.senla.errorfreetext.controller.dto.TaskRequest;
import org.senla.errorfreetext.controller.dto.TaskResponse;
import org.senla.errorfreetext.controller.dto.TaskResultResponse;
import org.senla.errorfreetext.service.TaskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public TaskResponse createTask(@RequestBody @Valid TaskRequest dto) {
        log.info("Создание задачи по запросу: data='{}', lang='{}'", dto.data(), dto.lang());
        TaskResponse task = taskService.createTask(dto);
        log.info("Задача успешно создана: taskId={}, contentCount={}", task.taskId(), task.contentCount());
        return task;
    }

    @GetMapping("/{id}")
    public TaskResultResponse getTask(@PathVariable Long id) {
        log.info("Получение результата задачи: taskId={}", id);
        TaskResultResponse response = taskService.getTaskResult(id);
        log.info("Результат задачи получен: taskId={}, status={}", id, response.status());
        return response;
    }

}

