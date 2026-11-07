package br.edu.ecowatt.service;

import br.edu.ecowatt.model.Appliance;
import br.edu.ecowatt.model.ConsumptionMode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnergyCalculatorTest {

    private final EnergyCalculator calculator = new EnergyCalculator();

    @Test
    void shouldCalculateConsumptionFromPowerAndUsageTime() {
        Appliance television = powerAppliance(100, 2, 5, 30);

        double consumption = calculator.monthlyConsumption(television);

        assertThat(consumption).isEqualTo(30.0);
    }

    @Test
    void shouldUseMonthlyKwhAsTotalWithoutMultiplyingQuantity() {
        Appliance refrigerator = new Appliance();
        refrigerator.setMode(ConsumptionMode.MONTHLY_KWH);
        refrigerator.setMonthlyKwh(38.0);
        refrigerator.setQuantity(3);

        assertThat(calculator.monthlyConsumption(refrigerator)).isEqualTo(38.0);
    }

    @Test
    void shouldCalculateSimulatedConsumptionWithoutChangingTheEntity() {
        Appliance television = powerAppliance(100, 1, 5, 30);

        double simulated = calculator.simulatedConsumption(television, 3);

        assertThat(simulated).isEqualTo(9.0);
        assertThat(television.getHoursPerDay()).isEqualTo(5.0);
    }

    private Appliance powerAppliance(double watts, int quantity, double hours, int days) {
        Appliance appliance = new Appliance();
        appliance.setMode(ConsumptionMode.POWER_TIME);
        appliance.setPowerWatts(watts);
        appliance.setQuantity(quantity);
        appliance.setHoursPerDay(hours);
        appliance.setDaysPerMonth(days);
        return appliance;
    }
}

