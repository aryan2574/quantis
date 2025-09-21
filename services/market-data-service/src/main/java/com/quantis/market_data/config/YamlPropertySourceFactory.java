package com.quantis.market_data.config;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.lang.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * YAML Property Source Factory
 * 
 * Enables loading YAML configuration files as Spring property sources
 */
public class YamlPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(@Nullable String name, EncodedResource encodedResource)
            throws IOException {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(encodedResource.getResource());

        Properties properties = factory.getObject();
        if (properties == null) {
            throw new FileNotFoundException("YAML file not found: " + encodedResource.getResource().getFilename());
        }

        return new PropertiesPropertySource(name != null ? name : encodedResource.getResource().getFilename(), properties);
    }
}
