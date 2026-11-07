package br.edu.ecowatt.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_settings")
public class AppSettings {

    public static final Long DEFAULT_ID = 1L;

    @Id
    private Long id;
    private String location;
    private Double tariffPerKwh;

    @Enumerated(EnumType.STRING)
    private TariffFlag tariffFlag;

    private Boolean setupCompleted;
    private Double savingsGoal;
    private Double maxReductionPercent;

    public AppSettings() {
    }

    public static AppSettings defaultSettings() {
        AppSettings settings = new AppSettings();
        settings.id = DEFAULT_ID;
        settings.location = null;
        settings.tariffPerKwh = null;
        settings.tariffFlag = null;
        settings.setupCompleted = false;
        settings.savingsGoal = 50.0;
        settings.maxReductionPercent = 30.0;
        return settings;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Double getTariffPerKwh() { return tariffPerKwh; }
    public void setTariffPerKwh(Double tariffPerKwh) { this.tariffPerKwh = tariffPerKwh; }
    public TariffFlag getTariffFlag() { return tariffFlag; }
    public void setTariffFlag(TariffFlag tariffFlag) { this.tariffFlag = tariffFlag; }
    public boolean isSetupCompleted() { return Boolean.TRUE.equals(setupCompleted); }
    public void setSetupCompleted(Boolean setupCompleted) { this.setupCompleted = setupCompleted; }
    public Double getSavingsGoal() { return savingsGoal; }
    public void setSavingsGoal(Double savingsGoal) { this.savingsGoal = savingsGoal; }
    public Double getMaxReductionPercent() { return maxReductionPercent; }
    public void setMaxReductionPercent(Double maxReductionPercent) { this.maxReductionPercent = maxReductionPercent; }

    public double baseTariff() {
        return tariffPerKwh == null ? 0.0 : tariffPerKwh;
    }

    public double flagSurchargePerKwh() {
        return tariffFlag == null ? 0.0 : tariffFlag.getSurchargePerKwh();
    }

    public double effectiveTariffPerKwh() {
        return baseTariff() + flagSurchargePerKwh();
    }
}

