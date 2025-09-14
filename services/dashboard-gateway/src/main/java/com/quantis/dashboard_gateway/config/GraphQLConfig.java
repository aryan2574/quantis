package com.quantis.dashboard_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import graphql.schema.idl.RuntimeWiring;
import graphql.scalars.ExtendedScalars;

/**
 * GraphQL Configuration for Dashboard Gateway
 * 
 * Configures custom scalars and type resolvers for the GraphQL schema
 */
@Configuration
public class GraphQLConfig {

    /**
     * Configure GraphQL runtime wiring with custom scalars
     */
    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return new RuntimeWiringConfigurer() {
            @Override
            public void configure(RuntimeWiring.Builder builder) {
                builder
                    .scalar(ExtendedScalars.DateTime)
                    .scalar(ExtendedScalars.GraphQLBigDecimal)
                    .scalar(ExtendedScalars.GraphQLLong);
            }
        };
    }
}
