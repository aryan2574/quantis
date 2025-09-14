package com.quantis.elasticsearch_writer_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Elasticsearch configuration for the Writer Service.
 * 
 * This configuration sets up the Elasticsearch client and connection settings.
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.quantis.elasticsearch_writer_service.repository")
public class ElasticsearchConfig extends ElasticsearchConfiguration {
    
    @Value("${spring.elasticsearch.uris:http://localhost:9200}")
    private String elasticsearchUris;
    
    @Value("${spring.elasticsearch.username:}")
    private String username;
    
    @Value("${spring.elasticsearch.password:}")
    private String password;
    
    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(elasticsearchUris.replace("http://", "").replace("https://", ""))
                .withConnectTimeout(10000)
                .withSocketTimeout(60000)
                .build();
    }
}
