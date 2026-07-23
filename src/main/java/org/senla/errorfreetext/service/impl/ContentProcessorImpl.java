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
            return CompletableFuture.failedFuture(
                    new ContentProcessException("Ошибка при обработке фрагмента текста: ссылка на null")
            );
        }

        Long contentDataId = contentDto.contentId();
        Long taskId = contentDto.taskId();
        String data = contentDto.data();
        String lang = contentDto.lang();

        log.info("Обработка фрагмента задачи: taskId={}, contentDataId={}", taskId, contentDataId);

        try {
            List<ResponseSpeller> response =
                    yandexSpellerClient.checkText(taskContentMapper.toCheckTextDto(data, lang));

            String fixedData = contentService.process(response, data);

            ContentDto processedContent = taskContentMapper.toContentDto(contentDto, fixedData);
            return CompletableFuture.completedFuture(new ProcessedContentDto(processedContent, null));

        } catch (RestClientException ex) {
            return buildFailedResult(
                    contentDto,
                    contentDataId,
                    taskId,
                    "Ошибка вызова сервиса при обработке фрагмента текста",
                    ex
            );
        } catch (Exception ex) {
            return buildFailedResult(
                    contentDto,
                    contentDataId,
                    taskId,
                    "Непредвиденная ошибка при обработке фрагмента текста",
                    ex
            );
        }
    }

    private CompletableFuture<ProcessedContentDto> buildFailedResult(
            ContentDto source,
            Long contentDataId,
            Long taskId,
            String messagePrefix,
            Exception ex
    ) {
        log.error("{}: contentDataId={}, taskId={}, error={}",
                messagePrefix, contentDataId, taskId, ex.getMessage());

        String errorMessage = String.format(
                "%s: contentDataId=%d, taskId=%d, error=%s",
                messagePrefix,
                contentDataId,
                taskId,
                ex.getMessage()
        );

        ContentDto errorContent = taskContentMapper.toContentDto(source, false);
        return CompletableFuture.completedFuture(new ProcessedContentDto(errorContent, errorMessage));
    }
}