package io.entry.adminauth;

import io.entry.adminauth.dto.AdminLoginRequest;
import io.entry.adminauth.dto.AdminLoginResponse;
import io.entry.common.ApiMeta;
import io.entry.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/api/v1/admin/login")
    public ApiResponse<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return ApiResponse.of(adminAuthService.login(request), ApiMeta.basic());
    }
}
