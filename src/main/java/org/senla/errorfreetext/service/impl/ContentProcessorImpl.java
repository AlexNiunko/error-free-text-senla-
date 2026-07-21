package org.senla.errorfreetext.service.impl;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.senla.errorfreetext.client.YandexSpellerClient;
import org.senla.errorfreetext.client.dto.ResponseSpeller;
import org.senla.errorfreetext.dto.ContentDto;
import org.senla.errorfreetext.dto.ProcessedContentDto;
import org.senla.errorfreetext.exception.ContentProcessException;
import org.senla.errorfreetext.mapper.TaskContentMapper;
import org.senla.errorfreetext.service.ContentProcessor;
import org.senla.errorfreetext.service.ContentService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContentProcessorImpl implements ContentProcessor {

    private final YandexSpellerClient yandexSpellerClient;
    private final ContentService contentService;
    private final TaskContentMapper taskContentMapper;

    @Override
    @Async("taskProcessingExecutor")
    public CompletableFuture<ProcessedContentDto> processTaskAsync(ContentDto contentDto) {

        if (contentDto == null) {
            log.error("Фрагмент текста равен null");
            return CompletableFuture
                    .failedFuture(new ContentProcessException("Ошибка при обработке фрагмента текста, ссылка на null"));
        }
        var contentDataId = contentDto.contentId();
        var taskId = contentDto.taskId();
        var data = contentDto.data();
        var lang = contentDto.lang();
        log.debug("Обработка фрагмента задачи(ContentDto): taskId={}, contentDataId={}", taskId, contentDataId);

        try {
            List<ResponseSpeller> response =
                    yandexSpellerClient.checkText(taskContentMapper.toCheckTextDto(data, lang));
            String fixedData = contentService.process(response, data);
            ProcessedContentDto result = new ProcessedContentDto(taskContentMapper.toContentDto(contentDto, fixedData), null);
            return CompletableFuture.completedFuture(result);
        } catch (RestClientException exception) {
            log.error("Ошибка при обработке фрагмента текста contentDataId - {}, taskId - {}", contentDataId, taskId);
            String errorMessage =
                    String.format("Ошибка при обработке фрагмента текста contentDataId - %d, taskId - %d", contentDataId, taskId);
            return CompletableFuture.completedFuture(new ProcessedContentDto(contentDto, errorMessage));
        }

    }
}
