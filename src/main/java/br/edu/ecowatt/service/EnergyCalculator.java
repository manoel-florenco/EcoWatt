package br.edu.ecowatt.service;

import br.edu.ecowatt.model.Appliance;
import br.edu.ecowatt.model.ConsumptionMode;
import org.springframework.stereotype.Component;

@Component
public class EnergyCalculator {

    public double monthlyConsumption(Appliance appliance) {
        if (appliance.getMode() == ConsumptionMode.MONTHLY_KWH) {
            return appliance.getMonthlyKwh();
        }
        return appliance.getPowerWatts()
                * appliance.getQuantity()
                * appliance.getHoursPerDay()
                * appliance.getDaysPerMonth()
                / 1000.0;
    }

    public double simulatedConsumption(Appliance appliance, double simulatedValue) {
        if (appliance.getMode() == ConsumptionMode.MONTHLY_KWH) {
            return simulatedValue;
        }
        return appliance.getPowerWatts()
                * appliance.getQuantity()
                * simulatedValue
                * appliance.getDaysPerMonth()
                / 1000.0;
    }

    public double baseMonthlyCost(Appliance appliance, double baseTariff) {
        return monthlyConsumption(appliance) * baseTariff;
    }

    public double flagAdditionalCost(Appliance appliance, double surchargePerKwh) {
        return monthlyConsumption(appliance) * surchargePerKwh;
    }

    public double monthlyCost(Appliance appliance, double effectiveTariff) {
        return monthlyConsumption(appliance) * effectiveTariff;
    }

    public double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

