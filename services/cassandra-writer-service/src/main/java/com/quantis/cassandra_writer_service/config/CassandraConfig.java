package com.quantis.cassandra_writer_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;
import org.springframework.data.cassandra.core.cql.keyspace.KeyspaceOption;

import java.util.Arrays;
import java.util.List;

/**
 * Cassandra configuration for the Writer Service.
 * 
 * This configuration sets up the Cassandra connection and keyspace creation.
 */
@Configuration
public class CassandraConfig extends AbstractCassandraConfiguration {
    
    @Value("${spring.data.cassandra.keyspace-name:quantis_trading}")
    private String keyspaceName;
    
    @Value("${spring.data.cassandra.contact-points:localhost}")
    private String contactPoints;
    
    @Value("${spring.data.cassandra.port:9042}")
    private int port;
    
    @Value("${spring.data.cassandra.local-datacenter:datacenter1}")
    private String localDatacenter;
    
    @Override
    protected String getKeyspaceName() {
        return keyspaceName;
    }
    
    @Override
    protected String getContactPoints() {
        return contactPoints;
    }
    
    @Override
    protected int getPort() {
        return port;
    }
    
    @Override
    protected String getLocalDataCenter() {
        return localDatacenter;
    }
    
    @Override
    public SchemaAction getSchemaAction() {
        return SchemaAction.CREATE_IF_NOT_EXISTS;
    }
    
    @Override
    protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {
        CreateKeyspaceSpecification specification = CreateKeyspaceSpecification
                .createKeyspace(keyspaceName)
                .ifNotExists()
                .withSimpleReplication(1) // Single node setup
                .with(KeyspaceOption.DURABLE_WRITES, true);
        
        return Arrays.asList(specification);
    }
    
    // Metrics are disabled by default for performance
}
