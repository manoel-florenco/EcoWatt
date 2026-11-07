package br.edu.ecowatt.controller;

import br.edu.ecowatt.dto.SavingsPlanResponse;
import br.edu.ecowatt.dto.SimulationRequest;
import br.edu.ecowatt.dto.SimulationResponse;
import br.edu.ecowatt.dto.SummaryResponse;
import br.edu.ecowatt.service.AnalysisService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final AnalysisService service;

    public AnalysisController(AnalysisService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    public SummaryResponse summary() {
        return service.summary();
    }

    @GetMapping("/savings-plan")
    public SavingsPlanResponse savingsPlan() {
        return service.savingsPlan();
    }

    @PostMapping("/simulation")
    public SimulationResponse simulate(@Valid @RequestBody SimulationRequest request) {
        return service.simulate(request);
    }
}

