package com.orden.service.orden_service.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductClient {

    private final RestTemplate restTemplate;

    @Value("${product.service.url}")
    private String productServiceUrl;

    @CircuitBreaker(name = "productService", fallbackMethod = "getProductByIdFallback")
    public Product getProductById(Long idProduct) {
        String Url = productServiceUrl + "/api/products/" + idProduct;
        try {
            Product product = restTemplate.getForObject(Url, Product.class);

            log.info("Product retrived successfully from userdb:{}", product);
            return product;
        } catch (Exception e) {
            log.info("Error Calling  Product Service :{ }", e.getMessage());
            throw new RuntimeException("Error Calling  Product Service" + e.getMessage());
        }
    }

    private Product getProductByIdFallback(Long idProduct,Throwable throwable){
        log.warn("Fallback method invoked for getUserById due to {}",throwable.getMessage());
        return  Product.builder()
                .name("unknown name")
                .price(BigDecimal.ZERO)
                .build();
    }
}
