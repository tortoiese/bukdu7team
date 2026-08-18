package io.entry.admin;

import io.entry.admin.dto.IntentDashboardData;
import io.entry.common.ApiMeta;
import io.entry.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {

    private final AdminIntentDashboardService dashboardService;

    public AdminController(AdminIntentDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/api/v1/admin/intent-dashboard")
    public ApiResponse<IntentDashboardData> dashboard() {
        return ApiResponse.of(dashboardService.get(), ApiMeta.basic());
    }
}
