package br.edu.ecowatt.dto;

import br.edu.ecowatt.model.TariffFlag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SettingsRequest(
        @NotBlank(message = "Informe sua cidade e UF")
        @Size(max = 100, message = "A localização deve ter no máximo 100 caracteres")
        String location,
        @NotNull @DecimalMin(value = "0.01", message = "A tarifa deve ser maior que zero")
        Double tariffPerKwh,
        @NotNull(message = "Informe a bandeira tarifária vigente")
        TariffFlag tariffFlag,
        @NotNull @DecimalMin(value = "0.0", message = "A meta não pode ser negativa")
        Double savingsGoal,
        @NotNull @DecimalMin(value = "0.0") @DecimalMax(value = "100.0")
        Double maxReductionPercent
) {
}

