package br.edu.ecowatt.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SimulationRequest(
        @NotNull Long applianceId,
        @NotNull @PositiveOrZero Double simulatedValue
) {
}

