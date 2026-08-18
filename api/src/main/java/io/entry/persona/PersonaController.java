package io.entry.persona;

import io.entry.common.ApiMeta;
import io.entry.common.ApiResponse;
import io.entry.persona.dto.SimulateRequest;
import io.entry.persona.dto.SimulationResultData;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PersonaController {

    private final PersonaCatalog personaCatalog;
    private final PersonaSimulationService simulationService;

    public PersonaController(PersonaCatalog personaCatalog, PersonaSimulationService simulationService) {
        this.personaCatalog = personaCatalog;
        this.simulationService = simulationService;
    }

    @GetMapping("/api/v1/personas")
    public ApiResponse<List<Persona>> list() {
        return ApiResponse.of(personaCatalog.all(), ApiMeta.basic());
    }

    @PostMapping("/api/v1/personas/{id}/simulate")
    public ApiResponse<SimulationResultData> simulate(@PathVariable String id, @Valid @RequestBody SimulateRequest request) {
        SimulationResultData data = simulationService.simulate(id, request);
        return ApiResponse.of(data, ApiMeta.basic());
    }
}
