package br.edu.ecowatt.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "appliances")
public class Appliance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String icon;

    @Enumerated(EnumType.STRING)
    private ConsumptionMode mode;

    private Double powerWatts;
    private Integer quantity;
    private Double hoursPerDay;
    private Integer daysPerMonth;
    private Double monthlyKwh;
    private boolean essential;

    public Appliance() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public ConsumptionMode getMode() { return mode; }
    public void setMode(ConsumptionMode mode) { this.mode = mode; }
    public Double getPowerWatts() { return powerWatts; }
    public void setPowerWatts(Double powerWatts) { this.powerWatts = powerWatts; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getHoursPerDay() { return hoursPerDay; }
    public void setHoursPerDay(Double hoursPerDay) { this.hoursPerDay = hoursPerDay; }
    public Integer getDaysPerMonth() { return daysPerMonth; }
    public void setDaysPerMonth(Integer daysPerMonth) { this.daysPerMonth = daysPerMonth; }
    public Double getMonthlyKwh() { return monthlyKwh; }
    public void setMonthlyKwh(Double monthlyKwh) { this.monthlyKwh = monthlyKwh; }
    public boolean isEssential() { return essential; }
    public void setEssential(boolean essential) { this.essential = essential; }
}

