package com.quantis.order_ingress;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for the Order Ingress Service.
 * 
 * This is the entry point of the application that:
 * 1. Starts the Spring Boot application context
 * 2. Scans for components (controllers, services, etc.)
 * 3. Starts the embedded web server
 * 4. Connects to Kafka using the configuration in application.yml
 * 
 * @SpringBootApplication is a convenience annotation that adds:
 * - @Configuration: Marks this class as a source of bean definitions
 * - @EnableAutoConfiguration: Tells Spring Boot to start adding beans based on classpath
 * - @ComponentScan: Tells Spring to look for other components in this package
 */
@SpringBootApplication
public class OrderIngressApplication {

    /**
     * Main method - the entry point of the application.
     * 
     * @param args Command line arguments passed to the application
     */
	public static void main(String[] args) {
		// Start the Spring Boot application
		// This will:
		// 1. Create the application context
		// 2. Start the embedded Tomcat server on port 8080
		// 3. Connect to Kafka using the configuration in application.yml
		// 4. Make the REST endpoints available at http://localhost:8080/api/orders
		SpringApplication.run(OrderIngressApplication.class, args);
	}

}
