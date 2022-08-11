package ru.pugart.messag.eservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.data.elasticsearch.client.reactive.ReactiveRestClients;
import org.springframework.data.elasticsearch.config.AbstractReactiveElasticsearchConfiguration;
import org.springframework.data.elasticsearch.core.RefreshPolicy;
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;

@Configuration
@EnableReactiveElasticsearchRepositories
@ComponentScan
public class ElasticsearchReactiveClientConfig extends AbstractReactiveElasticsearchConfiguration {

    @Value("${baseUrl.es}")
    private String baseUrl;

    @Override
    @Bean
    public ReactiveElasticsearchClient reactiveElasticsearchClient() {
        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(baseUrl)
                .build();
        return ReactiveRestClients.create(clientConfiguration);
    }

    @Override
    protected RefreshPolicy refreshPolicy() {
        return RefreshPolicy.IMMEDIATE;
    }
}
