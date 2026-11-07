package br.edu.ecowatt.dto;

import java.util.List;

public record SummaryResponse(
        double totalConsumptionKwh,
        double baseEnergyCost,
        double flagAdditionalCost,
        double totalMonthlyCost,
        int applianceCount,
        double theoreticalSavings,
        List<ApplianceResponse> ranking
) {
}

