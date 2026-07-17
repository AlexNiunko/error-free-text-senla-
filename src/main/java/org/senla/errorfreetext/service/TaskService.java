package org.senla.errorfreetext.service;

import org.senla.errorfreetext.controller.dto.TaskRequest;
import org.senla.errorfreetext.controller.dto.TaskResponse;
import org.senla.errorfreetext.controller.dto.TaskResultResponse;

public interface TaskService {

    TaskResponse createTask(TaskRequest dto);

    TaskResultResponse getTaskResult(Long id);

}
