package org.senla.errorfreetext.config.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "rest-client")
public record RestClientProperties(
        @NotBlank String yandexUrl,
        Http http
) {

    public record Http(
            @Min(1) long connectTimeoutMs,
            @Min(1) long socketTimeoutMs,
            @Min(1) long connectionRequestTimeoutMs,
            @Min(1) long connectionTtlSeconds,
            @Min(1) long idleEvictSeconds,
            @Min(1) int maxTotal,
            @Min(1) int maxPerRoute
    ) {
    }
}