package br.edu.ecowatt.dto;

import br.edu.ecowatt.model.ConsumptionMode;

public record SavingsAction(
        Long applianceId,
        String applianceName,
        String icon,
        ConsumptionMode mode,
        double currentValue,
        double suggestedValue,
        double monthlySavings
) {
}

