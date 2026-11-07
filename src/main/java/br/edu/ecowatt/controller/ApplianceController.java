package br.edu.ecowatt.controller;

import br.edu.ecowatt.dto.ApplianceRequest;
import br.edu.ecowatt.dto.ApplianceResponse;
import br.edu.ecowatt.service.ApplianceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/appliances")
public class ApplianceController {

    private final ApplianceService service;

    public ApplianceController(ApplianceService service) {
        this.service = service;
    }

    @GetMapping
    public List<ApplianceResponse> findAll() {
        return service.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplianceResponse create(@Valid @RequestBody ApplianceRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public ApplianceResponse update(@PathVariable Long id, @Valid @RequestBody ApplianceRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAll() {
        service.deleteAll();
    }

    @PostMapping("/demo")
    public List<ApplianceResponse> loadDemoResidence() {
        return service.loadDemoResidence();
    }
}

