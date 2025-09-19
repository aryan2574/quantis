package com.quantis.market_data.config;

import graphql.scalars.ExtendedScalars;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

/**
 * GraphQL configuration for Market Data Service
 * Registers custom scalar types
 */
@Configuration
public class GraphQLConfig {

    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return new RuntimeWiringConfigurer() {
            @Override
            public void configure(graphql.schema.idl.RuntimeWiring.Builder builder) {
                builder.scalar(ExtendedScalars.GraphQLLong);
            }
        };
    }
}
