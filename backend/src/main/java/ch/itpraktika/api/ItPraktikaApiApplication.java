package ch.itpraktika.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ItPraktikaApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ItPraktikaApiApplication.class, args);
    }
}
