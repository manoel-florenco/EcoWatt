package br.edu.ecowatt;

import br.edu.ecowatt.model.AppSettings;
import br.edu.ecowatt.repository.AppSettingsRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EcoWattApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcoWattApplication.class, args);
    }

    @Bean
    CommandLineRunner initializeSettings(AppSettingsRepository repository) {
        return args -> repository.findById(AppSettings.DEFAULT_ID)
                .orElseGet(() -> repository.save(AppSettings.defaultSettings()));
    }
}

