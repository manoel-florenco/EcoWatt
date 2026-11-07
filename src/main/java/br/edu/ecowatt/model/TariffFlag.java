package br.edu.ecowatt.model;

public enum TariffFlag {
    GREEN("Verde", 0.0),
    YELLOW("Amarela", 0.01885),
    RED_LEVEL_1("Vermelha — Patamar 1", 0.04463),
    RED_LEVEL_2("Vermelha — Patamar 2", 0.07877);

    private final String label;
    private final double surchargePerKwh;

    TariffFlag(String label, double surchargePerKwh) {
        this.label = label;
        this.surchargePerKwh = surchargePerKwh;
    }

    public String getLabel() {
        return label;
    }

    public double getSurchargePerKwh() {
        return surchargePerKwh;
    }
}

