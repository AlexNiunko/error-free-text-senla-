package org.senla.errorfreetext.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.senla.errorfreetext.dto.ContentDto;
import org.senla.errorfreetext.service.TaskService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final TaskService taskService;

    @PostMapping("/task-content")
    public List<ContentDto> createTask(@RequestBody Integer amount) {
        return taskService.getTaskContentForProcess(amount);

    }
}
