package org.senla.errorfreetext.service.impl;

import org.senla.errorfreetext.controller.dto.TaskRequest;
import org.senla.errorfreetext.controller.dto.TaskResponse;
import org.senla.errorfreetext.controller.dto.TaskResultResponse;
import org.senla.errorfreetext.service.TaskService;
import org.springframework.stereotype.Component;

@Component
public class TaskServiceImpl implements TaskService {

    @Override
    public TaskResponse createTask(TaskRequest dto) {
        return null;
    }

    @Override
    public TaskResultResponse getTaskResult(Long id) {
        return null;
    }

}
