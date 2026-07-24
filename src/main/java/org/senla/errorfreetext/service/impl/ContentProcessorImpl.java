package org.senla.errorfreetext.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.senla.errorfreetext.client.YandexSpellerClient;
import org.senla.errorfreetext.client.dto.ResponseSpeller;
import org.senla.errorfreetext.dto.ContentDto;
import org.senla.errorfreetext.dto.ProcessedContentDto;
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
    public CompletableFuture<ProcessedContentDto> processTaskAsync(Long taskId, List<ContentDto> contentDto) {
        List<ContentDto> processedContentDto = new ArrayList<>();
        try {
            for (ContentDto dto : contentDto) {

                String data = dto.data();
                String lang = dto.lang();
                List<ResponseSpeller> response =
                        yandexSpellerClient.checkText(taskContentMapper.toCheckTextDto(data, lang));

                String fixedData = contentService.process(response, data);

                ContentDto processedContent = taskContentMapper.toContentDto(dto, fixedData);
                processedContentDto.add(processedContent);
            }
            return CompletableFuture.completedFuture(new ProcessedContentDto(processedContentDto, null, taskId));
        } catch (RestClientException ex) {
            return buildFailedResult(
                    processedContentDto,
                    taskId,
                    "Ошибка вызова сервиса при обработке фрагмента текста",
                    ex
            );
        } catch (Exception e) {
            return buildFailedResult(
                    processedContentDto,
                    taskId,
                    "Непредвиденная ошибка при обработке фрагмента текста",
                    e
            );
        }
    }

    private CompletableFuture<ProcessedContentDto> buildFailedResult(
            List<ContentDto> source,
            Long taskId,
            String messagePrefix,
            Exception ex
    ) {
        log.error("{}:, taskId={}, error={}",
                messagePrefix, taskId, ex.getMessage());

        String errorMessage = String.format(
                "%s: taskId=%d, error=%s",
                messagePrefix,
                taskId,
                ex.getMessage()
        );

        return CompletableFuture.completedFuture(new ProcessedContentDto(source, errorMessage, taskId));
    }
}