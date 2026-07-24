package org.senla.errorfreetext.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.senla.errorfreetext.dto.ContentDto;
import org.senla.errorfreetext.dto.ProcessedContentDto;

public interface ContentProcessor {

    CompletableFuture<ProcessedContentDto> processTaskAsync(Long taskId, List<ContentDto> contentDto);


}
