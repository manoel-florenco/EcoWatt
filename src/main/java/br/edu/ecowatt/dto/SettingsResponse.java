package br.edu.ecowatt.dto;

import br.edu.ecowatt.model.TariffFlag;

public record SettingsResponse(
        String location,
        Double tariffPerKwh,
        TariffFlag tariffFlag,
        String tariffFlagLabel,
        double flagSurchargePerKwh,
        double effectiveTariffPerKwh,
        double savingsGoal,
        double maxReductionPercent,
        boolean configured
) {
}

