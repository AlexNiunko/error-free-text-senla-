package org.senla.errorfreetext.service.impl;

import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.senla.errorfreetext.client.YandexSpellerClient;
import org.senla.errorfreetext.dto.ContentDto;
import org.senla.errorfreetext.dto.ProcessedContentDto;
import org.senla.errorfreetext.service.ContentProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContentProcessorImpl implements ContentProcessor {

    private final YandexSpellerClient yandexSpellerClient;


    @Override
    public CompletableFuture<ProcessedContentDto> processTaskAsync(ContentDto contentDto) {


        return null;
    }
}
