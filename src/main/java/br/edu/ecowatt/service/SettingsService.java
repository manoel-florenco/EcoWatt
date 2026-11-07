package br.edu.ecowatt.service;

import br.edu.ecowatt.dto.SettingsRequest;
import br.edu.ecowatt.dto.SettingsResponse;
import br.edu.ecowatt.model.AppSettings;
import br.edu.ecowatt.repository.AppSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettingsService {

    private final AppSettingsRepository repository;

    public SettingsService(AppSettingsRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public SettingsResponse get() {
        return toResponse(getEntity());
    }

    @Transactional
    public SettingsResponse update(SettingsRequest request) {
        AppSettings settings = getEntity();
        settings.setLocation(request.location().trim());
        settings.setTariffPerKwh(request.tariffPerKwh());
        settings.setTariffFlag(request.tariffFlag());
        settings.setSetupCompleted(true);
        settings.setSavingsGoal(request.savingsGoal());
        settings.setMaxReductionPercent(request.maxReductionPercent());
        return toResponse(repository.save(settings));
    }

    @Transactional(readOnly = true)
    public AppSettings getEntity() {
        return repository.findById(AppSettings.DEFAULT_ID)
                .orElseGet(AppSettings::defaultSettings);
    }

    private SettingsResponse toResponse(AppSettings settings) {
        return new SettingsResponse(
                settings.getLocation(),
                settings.getTariffPerKwh(),
                settings.getTariffFlag(),
                settings.getTariffFlag() == null ? null : settings.getTariffFlag().getLabel(),
                settings.flagSurchargePerKwh(),
                settings.effectiveTariffPerKwh(),
                settings.getSavingsGoal(),
                settings.getMaxReductionPercent(),
                settings.isSetupCompleted()
        );
    }
}

