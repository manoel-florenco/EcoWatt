package br.edu.ecowatt.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TariffFlagTest {

    @Test
    void shouldExposeOfficialSurchargePerKwhForEachFlag() {
        assertThat(TariffFlag.GREEN.getSurchargePerKwh()).isZero();
        assertThat(TariffFlag.YELLOW.getSurchargePerKwh()).isEqualTo(0.01885);
        assertThat(TariffFlag.RED_LEVEL_1.getSurchargePerKwh()).isEqualTo(0.04463);
        assertThat(TariffFlag.RED_LEVEL_2.getSurchargePerKwh()).isEqualTo(0.07877);
    }
}
