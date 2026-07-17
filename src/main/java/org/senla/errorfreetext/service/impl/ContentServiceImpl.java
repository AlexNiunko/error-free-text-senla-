package org.senla.errorfreetext.service.impl;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.senla.errorfreetext.service.ContentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {

    private static final String CUT_REGEX = "[\\n\\s\\p{P}]";
    private static final String DIGIT = ".*\\d.*";
    private static final String URL =
            ".*(https?://\\S+|www\\.[A-Za-z0-9.-]+\\.[A-Za-z]{2,}|[A-Za-z0-9.-]+\\.[A-Za-z]{2,}).*";

    @Value("${content-size}")
    private final Integer maxSize;


    @Override
    public List<String> divide(String data) {
        log.debug("Начало разделения текста на части: длина={}, maxSize={}",
                data != null ? data.length() : null,
                maxSize);

        if (data == null || data.isEmpty()) {
            log.debug("Пустой или null-текст, возвращаю пустой список");
            return List.of();
        }

        List<String> result = new ArrayList<>();
        int start = 0;
        int cursor = maxSize;
        int length = data.length();

        while (start < length) {
            if (cursor >= length) {
                cursor = length - 1;
            }

            int cutPos = cursor;
            String ch = data.substring(cutPos, cutPos + 1);

            while (!ch.matches(CUT_REGEX) && cutPos > start) {
                cutPos--;
                ch = data.substring(cutPos, cutPos + 1);
            }

            if (!ch.matches(CUT_REGEX)) {
                cutPos = cursor;
            }

            var substring = data.substring(start, cutPos + 1);
            result.add(substring);

            log.trace("Сформирован фрагмент текста: start={}, end={}, length={}",
                    start, cutPos + 1, substring.length());
            start = cutPos + 1;
            cursor = start + maxSize;
        }

        log.debug("Разделение завершено, количество частей={}", result.size());
        return result;
    }


}
