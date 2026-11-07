package br.edu.ecowatt.dto;

import java.util.List;

public record SavingsPlanResponse(
        double goal,
        double maximumPossibleSavings,
        double plannedSavings,
        double amountMissing,
        boolean achievable,
        List<SavingsAction> actions
) {
}
