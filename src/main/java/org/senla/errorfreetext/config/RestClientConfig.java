package org.senla.errorfreetext.config;

import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.senla.errorfreetext.config.properties.RestClientProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(RestClientProperties.class)
public class RestClientConfig {

    private final RestClientProperties props;

    @Bean
    public RestClient yandexRestClient(ClientHttpRequestFactory userRequestFactory) {
        return RestClient.builder()
                .baseUrl(props.yandexUrl())
                .requestFactory(userRequestFactory)
                .build();
    }

    @Bean
    public ClientHttpRequestFactory userRequestFactory() {
        return createRequestFactory();
    }

    private PoolingHttpClientConnectionManager createConnectionManager() {
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(props.http().connectTimeoutMs()))
                .setTimeToLive(TimeValue.ofSeconds(props.http().connectionTtlSeconds()))
                .build();

        SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(Timeout.ofMilliseconds(props.http().socketTimeoutMs()))
                .build();

        PoolingHttpClientConnectionManager connectionManager =
                new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultSocketConfig(socketConfig);
        connectionManager.setDefaultConnectionConfig(connectionConfig);
        connectionManager.setMaxTotal(props.http().maxTotal());
        connectionManager.setDefaultMaxPerRoute(props.http().maxPerRoute());

        return connectionManager;
    }

    private ClientHttpRequestFactory createRequestFactory() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(
                        Timeout.ofMilliseconds(props.http().connectionRequestTimeoutMs())
                )
                .build();

        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(createConnectionManager())
                .setDefaultRequestConfig(requestConfig)
                .evictExpiredConnections()
                .evictIdleConnections(TimeValue.ofSeconds(props.http().idleEvictSeconds()))
                .build();

        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }
}