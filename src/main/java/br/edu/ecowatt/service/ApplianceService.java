package br.edu.ecowatt.service;

import br.edu.ecowatt.dto.ApplianceRequest;
import br.edu.ecowatt.dto.ApplianceResponse;
import br.edu.ecowatt.exception.BusinessException;
import br.edu.ecowatt.exception.ResourceNotFoundException;
import br.edu.ecowatt.model.Appliance;
import br.edu.ecowatt.model.AppSettings;
import br.edu.ecowatt.model.ConsumptionMode;
import br.edu.ecowatt.repository.ApplianceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApplianceService {

    private final ApplianceRepository repository;
    private final SettingsService settingsService;
    private final EnergyCalculator calculator;

    public ApplianceService(
            ApplianceRepository repository,
            SettingsService settingsService,
            EnergyCalculator calculator
    ) {
        this.repository = repository;
        this.settingsService = settingsService;
        this.calculator = calculator;
    }

    @Transactional(readOnly = true)
    public List<ApplianceResponse> findAll() {
        AppSettings settings = settingsService.getEntity();
        return repository.findAll().stream()
                .map(appliance -> toResponse(appliance, settings))
                .toList();
    }

    @Transactional(readOnly = true)
    public Appliance findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aparelho não encontrado"));
    }

    @Transactional
    public ApplianceResponse create(ApplianceRequest request) {
        validateModeFields(request);
        Appliance appliance = new Appliance();
        copy(request, appliance);
        Appliance saved = repository.save(appliance);
        return toResponse(saved, settingsService.getEntity());
    }

    @Transactional
    public ApplianceResponse update(Long id, ApplianceRequest request) {
        validateModeFields(request);
        Appliance appliance = findEntity(id);
        copy(request, appliance);
        Appliance saved = repository.save(appliance);
        return toResponse(saved, settingsService.getEntity());
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Aparelho não encontrado");
        }
        repository.deleteById(id);
    }

    @Transactional
    public void deleteAll() {
        repository.deleteAll();
    }

    @Transactional
    public List<ApplianceResponse> loadDemoResidence() {
        repository.deleteAll();
        List<Appliance> demo = List.of(
                power("Geladeira", "🧊", 150, 1, 8, 30, true),
                power("Ar-condicionado", "❄️", 1100, 1, 6, 20, false),
                power("Chuveiro elétrico", "🚿", 5500, 1, 0.4, 30, false),
                power("Televisão", "📺", 100, 1, 5, 30, false),
                power("Air fryer", "🍟", 1500, 1, 0.5, 20, false),
                power("Ventilador", "🌀", 80, 1, 6, 30, false),
                power("Lâmpadas", "💡", 10, 8, 5, 30, false),
                power("Notebook", "💻", 60, 1, 6, 22, false)
        );
        repository.saveAll(demo);
        return findAll();
    }

    public ApplianceResponse toResponse(Appliance appliance, AppSettings settings) {
        return new ApplianceResponse(
                appliance.getId(), appliance.getName(), appliance.getIcon(), appliance.getMode(),
                appliance.getPowerWatts(), appliance.getQuantity(), appliance.getHoursPerDay(),
                appliance.getDaysPerMonth(), appliance.getMonthlyKwh(), appliance.isEssential(),
                calculator.round(calculator.monthlyConsumption(appliance)),
                calculator.round(calculator.baseMonthlyCost(appliance, settings.baseTariff())),
                calculator.round(calculator.flagAdditionalCost(appliance, settings.flagSurchargePerKwh())),
                calculator.round(calculator.monthlyCost(appliance, settings.effectiveTariffPerKwh()))
        );
    }

    private void validateModeFields(ApplianceRequest request) {
        if (request.mode() == ConsumptionMode.POWER_TIME) {
            if (request.powerWatts() == null || request.powerWatts() <= 0) {
                throw new BusinessException("Informe uma potência maior que zero");
            }
            if (request.hoursPerDay() == null || request.hoursPerDay() < 0 || request.hoursPerDay() > 24) {
                throw new BusinessException("As horas por dia devem estar entre 0 e 24");
            }
            if (request.daysPerMonth() == null || request.daysPerMonth() < 1 || request.daysPerMonth() > 31) {
                throw new BusinessException("Os dias por mês devem estar entre 1 e 31");
            }
        } else if (request.monthlyKwh() == null || request.monthlyKwh() < 0) {
            throw new BusinessException("Informe um consumo mensal em kWh maior ou igual a zero");
        }
    }

    private void copy(ApplianceRequest source, Appliance target) {
        target.setName(source.name().trim());
        target.setIcon(source.icon() == null || source.icon().isBlank() ? "🔌" : source.icon());
        target.setMode(source.mode());
        target.setQuantity(source.quantity());
        target.setEssential(source.essential());

        if (source.mode() == ConsumptionMode.POWER_TIME) {
            target.setPowerWatts(source.powerWatts());
            target.setHoursPerDay(source.hoursPerDay());
            target.setDaysPerMonth(source.daysPerMonth());
            target.setMonthlyKwh(null);
        } else {
            target.setPowerWatts(null);
            target.setHoursPerDay(null);
            target.setDaysPerMonth(null);
            target.setMonthlyKwh(source.monthlyKwh());
        }
    }

    private Appliance power(
            String name, String icon, double watts, int quantity,
            double hours, int days, boolean essential
    ) {
        Appliance appliance = new Appliance();
        appliance.setName(name);
        appliance.setIcon(icon);
        appliance.setMode(ConsumptionMode.POWER_TIME);
        appliance.setPowerWatts(watts);
        appliance.setQuantity(quantity);
        appliance.setHoursPerDay(hours);
        appliance.setDaysPerMonth(days);
        appliance.setEssential(essential);
        return appliance;
    }
}

