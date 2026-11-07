package br.edu.ecowatt.dto;

import br.edu.ecowatt.model.ConsumptionMode;

public record SimulationResponse(
        Long applianceId,
        String applianceName,
        ConsumptionMode mode,
        double originalValue,
        double simulatedValue,
        double originalConsumptionKwh,
        double simulatedConsumptionKwh,
        double monthlySavings,
        double annualSavings,
        double reductionPercent
) {
}

