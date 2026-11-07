package br.edu.ecowatt;

import br.edu.ecowatt.model.AppSettings;
import br.edu.ecowatt.model.TariffFlag;
import br.edu.ecowatt.repository.ApplianceRepository;
import br.edu.ecowatt.repository.AppSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EcoWattApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplianceRepository applianceRepository;

    @Autowired
    private AppSettingsRepository settingsRepository;

    @BeforeEach
    void resetDatabase() {
        applianceRepository.deleteAll();
        settingsRepository.deleteAll();
        AppSettings settings = AppSettings.defaultSettings();
        settings.setLocation("Goiânia/GO");
        settings.setTariffPerKwh(0.90);
        settings.setTariffFlag(TariffFlag.GREEN);
        settings.setSetupCompleted(true);
        settingsRepository.save(settings);
    }

    @Test
    void shouldCreateApplianceAndIncludeItInSummary() throws Exception {
        String television = """
                {
                  "name": "Televisão",
                  "icon": "📺",
                  "mode": "POWER_TIME",
                  "powerWatts": 100,
                  "quantity": 1,
                  "hoursPerDay": 5,
                  "daysPerMonth": 30,
                  "monthlyKwh": null,
                  "essential": false
                }
                """;

        mockMvc.perform(post("/api/appliances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(television))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.consumptionKwh").value(15.0))
                .andExpect(jsonPath("$.monthlyCost").value(13.5));

        mockMvc.perform(get("/api/analysis/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applianceCount").value(1))
                .andExpect(jsonPath("$.totalConsumptionKwh").value(15.0));
    }

    @Test
    void shouldRejectInconsistentPowerUsageData() throws Exception {
        String invalid = """
                {
                  "name": "Aparelho inválido",
                  "mode": "POWER_TIME",
                  "powerWatts": 100,
                  "quantity": 1,
                  "hoursPerDay": 25,
                  "daysPerMonth": 30,
                  "essential": false
                }
                """;

        mockMvc.perform(post("/api/appliances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("As horas por dia devem estar entre 0 e 24"));
    }

    @Test
    void shouldAddTheSelectedTariffFlagToMonthlyCost() throws Exception {
        AppSettings settings = settingsRepository.findById(AppSettings.DEFAULT_ID).orElseThrow();
        settings.setTariffFlag(TariffFlag.YELLOW);
        settingsRepository.save(settings);

        String appliance = """
                {
                  "name": "Aparelho de teste",
                  "mode": "POWER_TIME",
                  "powerWatts": 100,
                  "quantity": 1,
                  "hoursPerDay": 10,
                  "daysPerMonth": 10,
                  "essential": false
                }
                """;

        mockMvc.perform(post("/api/appliances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(appliance))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.consumptionKwh").value(10.0))
                .andExpect(jsonPath("$.baseMonthlyCost").value(9.0))
                .andExpect(jsonPath("$.flagAdditionalCost").value(0.19))
                .andExpect(jsonPath("$.monthlyCost").value(9.19));
    }
}

