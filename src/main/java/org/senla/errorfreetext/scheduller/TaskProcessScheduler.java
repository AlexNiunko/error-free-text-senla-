package org.senla.errorfreetext.scheduller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.senla.errorfreetext.dto.ContentDto;
import org.senla.errorfreetext.dto.ProcessedContentDto;
import org.senla.errorfreetext.service.ContentProcessor;
import org.senla.errorfreetext.service.TaskService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskProcessScheduler {

    @Value("${scheduler.batch-size}")
    private final Integer batchSize;

    private final TaskService taskService;
    private final ContentProcessor contentProcessor;

    @Scheduled(fixedDelayString = "${scheduler.interval-ms}")
    public void runBatchProcessor() {
        try {
            log.info("Запуск пакетной обработки задач на корректировку текста ");

            List<ContentDto> listForProcess = taskService.getTaskContentForProcess(batchSize);

            if (listForProcess.isEmpty()) {
                log.info("Новых задач для обработки не найдено");
                return;
            }

            log.info("Найдено {} задач для обработки", listForProcess.size());

            List<CompletableFuture<ProcessedContentDto>> contentInProcess = new ArrayList<>();

            for (ContentDto task : listForProcess) {
                contentInProcess.add(contentProcessor.processTaskAsync(task));
            }

            List<ProcessedContentDto> processed = contentInProcess.stream()
                    .map(CompletableFuture::join)
                    .toList();

            Map<Long, List<ProcessedContentDto>> byContentId =
                    processed.stream().collect(Collectors.groupingBy(item -> item.contentDto().taskId()));

            log.info("Обработано фрагментов текста - {}", processed.size());

            var saveProcessedTask = taskService.saveProcessedTask(byContentId);

            log.info("Финал пакетной обработки на корректировку текста, обработано - {} фрагментов текста ", saveProcessedTask);

        } catch (Exception e) {
            log.error("Ошибка в шедуллере пакетной обработки задач, batchSize={}: {}",
                    batchSize, e.getMessage(), e);
        }

    }
}
