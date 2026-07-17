package org.senla.errorfreetext.service.impl;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.senla.errorfreetext.controller.dto.TaskRequest;
import org.senla.errorfreetext.controller.dto.TaskResponse;
import org.senla.errorfreetext.controller.dto.TaskResultResponse;
import org.senla.errorfreetext.exception.BadDataException;
import org.senla.errorfreetext.service.ContentService;
import org.senla.errorfreetext.service.TaskService;
import org.springframework.stereotype.Component;

import static org.senla.errorfreetext.exception.ErrorMessage.DATA_IS_NULL;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final ContentService contentService;

    @Override
    public TaskResponse createTask(TaskRequest dto) {
        String data = dto.data();
        log.info("Создание задачи по запросу: dataLength={}, lang={}",
                data != null ? data.length() : null,
                dto.lang());

        if (data == null) {
            log.warn("Попытка создать задачу с null-данными");
            throw new BadDataException(DATA_IS_NULL);
        }

        List<String> dataList = contentService.divide(data);
        log.debug("Текст разделён на части: count={}", dataList.size());



        return null;
    }

    @Override
    public TaskResultResponse getTaskResult(Long id) {
        return null;
    }

}
