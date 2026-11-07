package br.edu.ecowatt.dto;

import br.edu.ecowatt.model.ConsumptionMode;

public record ApplianceResponse(
        Long id,
        String name,
        String icon,
        ConsumptionMode mode,
        Double powerWatts,
        Integer quantity,
        Double hoursPerDay,
        Integer daysPerMonth,
        Double monthlyKwh,
        boolean essential,
        double consumptionKwh,
        double baseMonthlyCost,
        double flagAdditionalCost,
        double monthlyCost
) {
}

