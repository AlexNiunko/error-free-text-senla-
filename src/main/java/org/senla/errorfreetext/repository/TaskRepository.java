package org.senla.errorfreetext.repository;

import java.util.List;
import java.util.Optional;
import org.senla.errorfreetext.dto.ErrorDto;
import org.senla.errorfreetext.dto.TaskDto;

public interface TaskRepository {
    Optional<Long> saveTask(String lang);

    Long[] getNewTasksForProcess(int numberOfTasks);

    int updateTaskStatus(Long[] tasks);

    Optional<String> getTaskStatusById(Long taskId);

    List<String> getTaskMessageErrors(Long taskId);

    int saveProcessedTask(List<TaskDto> list);

    int saveError(List<ErrorDto> list);
}
