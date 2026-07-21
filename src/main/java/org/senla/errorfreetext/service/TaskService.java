package org.senla.errorfreetext.service;

import java.util.List;
import org.senla.errorfreetext.controller.dto.TaskRequest;
import org.senla.errorfreetext.controller.dto.TaskResponse;
import org.senla.errorfreetext.controller.dto.TaskResultResponse;
import org.senla.errorfreetext.dto.ContentDto;

public interface TaskService {

    TaskResponse createTask(TaskRequest dto);

    TaskResultResponse getTaskResult(Long id);

    List<ContentDto> getTaskContentForProcess(int taskCount);

}
