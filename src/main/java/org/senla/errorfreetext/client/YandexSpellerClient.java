package org.senla.errorfreetext.client;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.senla.errorfreetext.client.dto.CheckTextDto;
import org.senla.errorfreetext.client.dto.ResponseSpeller;
import org.senla.errorfreetext.service.ContentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import static org.senla.errorfreetext.client.dto.CheckTextDto.Fields.format;
import static org.senla.errorfreetext.client.dto.CheckTextDto.Fields.text;
import static org.senla.errorfreetext.client.dto.CheckTextDto.Fields.lang;
import static org.senla.errorfreetext.client.dto.CheckTextDto.Fields.options;

@Slf4j
@Component
@RequiredArgsConstructor
public class YandexSpellerClient {

    private static final int IGNORE_DIGITS = 2;
    private static final int IGNORE_URLS = 4;

    private final RestClient restClient;
    private final ContentService contentService;

    public List<ResponseSpeller> checkText(CheckTextDto dto) {
        log.debug("Отправка текста в Yandex Speller: textLength={}, lang={}, format={}",
                dto.text() != null ? dto.text().length() : null,
                dto.lang(),
                dto.format());

        MultiValueMap<String, String> body = buildRequestBody(dto);

        List<List<ResponseSpeller>> allTexts = restClient.post()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (allTexts == null || allTexts.isEmpty()) {
            log.debug("Yandex Speller вернул пустой список ошибок");
            return List.of();
        }

        List<ResponseSpeller> result = allTexts.get(0);
        log.debug("Yandex Speller обработал текст: errorsCount={}", result.size());
        return result;
    }

    private MultiValueMap<String, String> buildRequestBody(CheckTextDto dto) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        String data = dto.text();
        String optionsValue = getOptions(data);

        body.add(text, data);
        body.add(lang, dto.lang());
        body.add(options, optionsValue);
        body.add(format, dto.format());

        log.trace("Формирование HTTP-запроса к Yandex Speller: lang={}, options={}, format={}",
                dto.lang(), optionsValue, dto.format());

        return body;
    }

    private String getOptions(String text) {
        int options = 0;
        boolean hasDigit = contentService.existDigit(text);
        boolean hasUrl = contentService.existURL(text);

        if (hasDigit) {
            options += IGNORE_DIGITS;
        }
        if (hasUrl) {
            options += IGNORE_URLS;
        }

        String result = Integer.toString(options);
        log.trace("Расчёт опций для текста: hasDigit={}, hasUrl={}, options={}",
                hasDigit, hasUrl, result);
        return result;
    }

}
