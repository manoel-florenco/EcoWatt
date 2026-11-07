package br.edu.ecowatt.service;

import br.edu.ecowatt.dto.ApplianceResponse;
import br.edu.ecowatt.dto.SavingsAction;
import br.edu.ecowatt.dto.SavingsPlanResponse;
import br.edu.ecowatt.dto.SimulationRequest;
import br.edu.ecowatt.dto.SimulationResponse;
import br.edu.ecowatt.dto.SummaryResponse;
import br.edu.ecowatt.exception.BusinessException;
import br.edu.ecowatt.model.Appliance;
import br.edu.ecowatt.model.AppSettings;
import br.edu.ecowatt.model.ConsumptionMode;
import br.edu.ecowatt.repository.ApplianceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AnalysisService {

    private final ApplianceRepository repository;
    private final ApplianceService applianceService;
    private final SettingsService settingsService;
    private final EnergyCalculator calculator;

    public AnalysisService(
            ApplianceRepository repository,
            ApplianceService applianceService,
            SettingsService settingsService,
            EnergyCalculator calculator
    ) {
        this.repository = repository;
        this.applianceService = applianceService;
        this.settingsService = settingsService;
        this.calculator = calculator;
    }

    @Transactional(readOnly = true)
    public SummaryResponse summary() {
        AppSettings settings = settingsService.getEntity();
        double tariff = settings.effectiveTariffPerKwh();
        List<ApplianceResponse> ranking = repository.findAll().stream()
                .map(appliance -> applianceService.toResponse(appliance, settings))
                .sorted(Comparator.comparingDouble(ApplianceResponse::monthlyCost).reversed())
                .toList();

        double consumption = ranking.stream().mapToDouble(ApplianceResponse::consumptionKwh).sum();
        double baseCost = ranking.stream().mapToDouble(ApplianceResponse::baseMonthlyCost).sum();
        double flagCost = ranking.stream().mapToDouble(ApplianceResponse::flagAdditionalCost).sum();
        double cost = ranking.stream().mapToDouble(ApplianceResponse::monthlyCost).sum();
        double savings = ranking.stream()
                .filter(item -> !item.essential())
                .mapToDouble(item -> item.monthlyCost() * settings.getMaxReductionPercent() / 100.0)
                .sum();

        return new SummaryResponse(
                calculator.round(consumption), calculator.round(baseCost), calculator.round(flagCost),
                calculator.round(cost), ranking.size(),
                calculator.round(savings), ranking
        );
    }

    @Transactional(readOnly = true)
    public SimulationResponse simulate(SimulationRequest request) {
        Appliance appliance = applianceService.findEntity(request.applianceId());
        double simulatedValue = request.simulatedValue();
        if (appliance.getMode() == ConsumptionMode.POWER_TIME && simulatedValue > 24) {
            throw new BusinessException("A simulação deve ter no máximo 24 horas por dia");
        }

        double originalConsumption = calculator.monthlyConsumption(appliance);
        double simulatedConsumption = calculator.simulatedConsumption(appliance, simulatedValue);
        double tariff = settingsService.getEntity().effectiveTariffPerKwh();
        double savings = (originalConsumption - simulatedConsumption) * tariff;
        double reduction = originalConsumption == 0 ? 0 : savings / (originalConsumption * tariff) * 100;
        double originalValue = appliance.getMode() == ConsumptionMode.MONTHLY_KWH
                ? appliance.getMonthlyKwh()
                : appliance.getHoursPerDay();

        return new SimulationResponse(
                appliance.getId(), appliance.getName(), appliance.getMode(),
                originalValue, simulatedValue,
                calculator.round(originalConsumption), calculator.round(simulatedConsumption),
                calculator.round(savings), calculator.round(savings * 12), calculator.round(reduction)
        );
    }

    @Transactional(readOnly = true)
    public SavingsPlanResponse savingsPlan() {
        AppSettings settings = settingsService.getEntity();
        double tariff = settings.effectiveTariffPerKwh();
        double limit = settings.getMaxReductionPercent() / 100.0;
        double remaining = settings.getSavingsGoal();
        double maximum = 0;
        double planned = 0;
        List<SavingsAction> actions = new ArrayList<>();

        List<Appliance> eligible = repository.findAll().stream()
                .filter(appliance -> !appliance.isEssential())
                .sorted(Comparator.comparingDouble(
                        (Appliance appliance) -> calculator.monthlyCost(appliance, tariff)
                ).reversed())
                .toList();

        for (Appliance appliance : eligible) {
            double cost = calculator.monthlyCost(appliance, tariff);
            maximum += cost * limit;
            if (remaining <= 0 || cost <= 0) {
                continue;
            }
            double saving = Math.min(cost * limit, remaining);
            double ratio = saving / cost;
            double currentValue = appliance.getMode() == ConsumptionMode.MONTHLY_KWH
                    ? appliance.getMonthlyKwh()
                    : appliance.getHoursPerDay();
            double suggestedValue = currentValue * (1 - ratio);
            actions.add(new SavingsAction(
                    appliance.getId(), appliance.getName(), appliance.getIcon(), appliance.getMode(),
                    calculator.round(currentValue), calculator.round(suggestedValue), calculator.round(saving)
            ));
            planned += saving;
            remaining -= saving;
        }

        double missing = Math.max(settings.getSavingsGoal() - planned, 0);
        return new SavingsPlanResponse(
                calculator.round(settings.getSavingsGoal()), calculator.round(maximum),
                calculator.round(planned), calculator.round(missing), missing < 0.01, actions
        );
    }
}

