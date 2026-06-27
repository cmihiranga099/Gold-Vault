package lk.goldvault.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GoldvaultBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(GoldvaultBackendApplication.class, args);
    }
}