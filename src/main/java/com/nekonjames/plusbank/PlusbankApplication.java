package com.nekonjames.plusbank;

import java.util.TimeZone;
import javax.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@ComponentScan(basePackages = "com.nekonjames.plusbank")
@EnableJpaAuditing
public class PlusbankApplication {
    
    public static void main(String[] args) {
            SpringApplication.run(PlusbankApplication.class, args);
    }
}
