package com.orden.service.orden_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan; // <--- Este falta
import org.springframework.cloud.openfeign.EnableFeignClients; // <--- Este falta
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = { 
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class 
})
@EnableFeignClients
@ComponentScan(basePackages = "com.orden.service.orden_service")
@EntityScan(basePackages = "com.orden.service.orden_service.entity")
public class OrdenServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrdenServiceApplication.class, args);
	}

}
