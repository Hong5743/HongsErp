package com.hongs.hongs_erp.posheet.adapter.in.web;

import com.hongs.hongs_erp.posheet.application.dto.request.UpdateSettingRequest;
import com.hongs.hongs_erp.posheet.application.dto.response.*;
import com.hongs.hongs_erp.posheet.application.port.in.AdminPoSheetUseCase;
import com.hongs.hongs_erp.posheet.application.port.in.PermissionUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/posheet")
public class PoAdminController {

    private final AdminPoSheetUseCase adminUseCase;
    private final PermissionUseCase permissionUseCase;

    public PoAdminController(AdminPoSheetUseCase adminUseCase, PermissionUseCase permissionUseCase) {
        this.adminUseCase = adminUseCase;
        this.permissionUseCase = permissionUseCase;
    }

    @GetMapping("/trash")
    public List<TrashItemResponse> trash() {
        return adminUseCase.listTrash();
    }

    @DeleteMapping("/trash/purge")
    public Map<String, Integer> purge() {
        return Map.of("purged", adminUseCase.purgeExpired());
    }

    @GetMapping("/versions")
    public List<VersionHistoryResponse> versions() {
        return adminUseCase.listVersionHistory();
    }

    @GetMapping("/settings")
    public List<SettingResponse> settings() {
        return adminUseCase.listSettings();
    }

    @PutMapping("/settings/{key}")
    public SettingResponse updateSetting(@PathVariable String key,
                                         @Valid @RequestBody UpdateSettingRequest req,
                                         Authentication auth) {
        Long adminId = Long.parseLong((String) auth.getPrincipal());
        adminUseCase.updateSetting(key, req.value(), adminId);
        return new SettingResponse(key, req.value(), LocalDateTime.now());
    }

    @GetMapping("/permissions")
    public List<PermissionResponse> permissions() {
        return permissionUseCase.listAll();
    }

    @PostMapping("/permissions/{empId}/grant")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void grant(@PathVariable Long empId, Authentication auth) {
        Long adminId = Long.parseLong((String) auth.getPrincipal());
        permissionUseCase.grant(empId, adminId);
    }

    @DeleteMapping("/permissions/{empId}/revoke")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revoke(@PathVariable Long empId, Authentication auth) {
        Long adminId = Long.parseLong((String) auth.getPrincipal());
        permissionUseCase.revoke(empId, adminId);
    }
}
