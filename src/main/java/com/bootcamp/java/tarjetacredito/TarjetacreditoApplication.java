package com.bootcamp.java.tarjetacredito;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class TarjetacreditoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TarjetacreditoApplication.class, args);
    }

}
