package org.senla.errorfreetext.repository;

import java.util.List;
import org.senla.errorfreetext.dto.ContentDto;

public interface TaskContentRepository {

    boolean saveTaskContent(List<ContentDto> taskContentList, Long taskId);

    List<ContentDto> getTaskContentByTaskId(Long[] ids);

    List<ContentDto> getTaskContentForProcess(String status);

    List<ContentDto> getContentDto(Long taskId);

    int saveProcessedContent(List<ContentDto> list);
}
