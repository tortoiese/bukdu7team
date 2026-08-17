package io.entry.system;

import io.entry.common.ApiMeta;
import io.entry.common.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class HealthController {

    @Value("${spring.profiles.active:local}")
    private String profile;

    private final Optional<BuildProperties> buildProperties;

    public HealthController(Optional<BuildProperties> buildProperties) {
        this.buildProperties = buildProperties;
    }

    @GetMapping("/api/v1/health")
    public ApiResponse<HealthData> health() {
        String version = buildProperties.map(BuildProperties::getVersion).orElse("dev");
        return ApiResponse.of(new HealthData("UP", version, profile), ApiMeta.basic());
    }

    public record HealthData(String status, String version, String profile) {
    }
}
