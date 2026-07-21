package org.senla.errorfreetext.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class YandexSpellerClient {

    private static final int IGNORE_DIGITS = 2;
    private static final int IGNORE_URLS = 4;

}
