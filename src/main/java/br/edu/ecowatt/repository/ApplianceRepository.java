package br.edu.ecowatt.repository;

import br.edu.ecowatt.model.Appliance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplianceRepository extends JpaRepository<Appliance, Long> {
}

