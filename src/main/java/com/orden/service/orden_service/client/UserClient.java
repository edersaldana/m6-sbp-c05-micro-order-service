package com.orden.service.orden_service.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserClient {

    private final RestTemplate restTemplate;

    @Value("${user.service.url}")
    private String userServiceUrl;

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByIdFallback")
    public User getUserById(Long createdBy) {
        String Url = userServiceUrl + "/api/users/" + createdBy;
        try {
            User user = restTemplate.getForObject(Url, User.class);
            log.info("User retrived successfully from userdb:{}", user);
            return user;
        } catch (Exception e) {
            log.info("Error Calling  User Service :{ }", e.getMessage());
            throw new RuntimeException("Error Calling  User Service" + e.getMessage());
        }
    }

    private User getUserByIdFallback(Long createdBy,Throwable throwable){
        log.warn("Fallback method invoked for getUserById due to {}",throwable.getMessage());
        return  User.builder()
                .name("unknown user")
                .email("unknown email")
                .build();
    }
}
