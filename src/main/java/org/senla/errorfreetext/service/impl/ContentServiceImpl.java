package org.senla.errorfreetext.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.senla.errorfreetext.client.dto.ResponseSpeller;
import org.senla.errorfreetext.dto.ContentDto;
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

    @Override
    public String buildData(List<ContentDto> parts) {

        log.debug("Сборка текста из фрагментов: количество частей={}",
                parts != null ? parts.size() : null);

        if (parts == null || parts.isEmpty()) {
            log.debug("Список частей пуст, возвращаю пустую строку");
            return "";
        }

        String collect = parts.stream()
                .sorted(Comparator.comparing(ContentDto::position))
                .map(ContentDto::data)
                .collect(Collectors.joining());

        log.debug("Сборка текста завершена: итоговая длина={}", collect.length());
        return collect;
    }

    @Override
    public boolean existDigit(String content) {
        boolean exists = content != null && content.matches(DIGIT);

        log.trace("Проверка наличия цифр в тексте: длина={}, результат={}",
                content != null ? content.length() : null,
                exists);

        return exists;
    }

    @Override
    public boolean existURL(String content) {
        boolean exists = content != null && content.matches(URL);
        log.trace("Проверка наличия URL в тексте: длина={}, результат={}",
                content != null ? content.length() : null,
                exists);
        return exists;
    }

    @Override
    public String process(List<ResponseSpeller> errors, String data) {
        log.debug("Обработка текста по результатам спеллера: errorsCount={}, dataLength={}",
                errors != null ? errors.size() : null,
                data != null ? data.length() : null);

        if (errors == null || errors.isEmpty() || data == null || data.isEmpty()) {
            log.debug("Нет ошибок спеллера или пустой текст, возвращаю исходный текст без изменений");
            return data;
        }

        List<ResponseSpeller> sorted = new ArrayList<>(errors);
        sorted.sort(Comparator.comparingInt(ResponseSpeller::pos));

        StringBuilder result = new StringBuilder();
        int currentIndex = 0;

        for (ResponseSpeller error : sorted) {
            int start = error.pos();
            int end = start + error.len();

            if (start < currentIndex || start >= data.length()) {
                log.trace("Пропускаю некорректную позицию ошибки: start={}, currentIndex={}, dataLength={}",
                        start, currentIndex, data.length());
                continue;
            }

            if (end > data.length()) {
                end = data.length();
            }

            result.append(data, currentIndex, start);


            String replacement = (error.s() != null && !error.s().isEmpty())
                    ? error.s().get(0)
                    : data.substring(start, end);

            log.trace("Применение исправления: pos={}, len={}, replacement='{}'",
                    start, error.len(), replacement);
            result.append(replacement);

            currentIndex = end;
        }

        if (currentIndex < data.length()) {
            result.append(data.substring(currentIndex));
        }

        var resultString = result.toString();
        log.debug("Обработка текста завершена: исходная длина={}, новая длина={}",
                data.length(), resultString.length());

        return resultString;
    }


}
