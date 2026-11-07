package br.edu.ecowatt.repository;

import br.edu.ecowatt.model.AppSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppSettingsRepository extends JpaRepository<AppSettings, Long> {
}

