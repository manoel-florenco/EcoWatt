package br.edu.ecowatt.dto;

import br.edu.ecowatt.model.ConsumptionMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ApplianceRequest(
        @NotBlank(message = "Informe o nome do aparelho")
        @Size(max = 80, message = "O nome deve ter no máximo 80 caracteres")
        String name,
        @Size(max = 8, message = "O ícone é inválido")
        String icon,
        @NotNull(message = "Informe o modo de consumo")
        ConsumptionMode mode,
        Double powerWatts,
        @NotNull(message = "Informe a quantidade")
        @Min(value = 1, message = "A quantidade deve ser maior que zero")
        Integer quantity,
        Double hoursPerDay,
        Integer daysPerMonth,
        Double monthlyKwh,
        boolean essential
) {
}

