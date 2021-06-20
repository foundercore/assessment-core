package com.assessment.iam.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.assessment.common.validations.ValidDisplayName;
import com.assessment.common.validations.ValidTenantId;
import com.assessment.iam.dtos.TenantDto;
import com.assessment.iam.entities.Tenant;
import com.assessment.iam.services.TenantService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping("/api/v1/tenants")
public class TenantController {

    @Autowired
    private TenantService tenantService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public void createTenant(@Valid @RequestBody TenantDto tenantDto) {
        tenantService.createTenant(tenantDto);
    }

    @PutMapping("/{id}/enable/{status}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public void updateTenantStatus(@ValidTenantId @PathVariable("id") String tenantId,
                                   @NotNull @PathVariable("status") boolean status) {
        tenantService.updateTenantStatus(tenantId, status);
    }

    @PutMapping("/{id}/display-name/{display-name}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public void updateTenantDisplayName(@ValidTenantId @PathVariable("id") String tenantId,
                                        @ValidDisplayName @PathVariable("display-name") String displayName) {
        tenantService.updateTenantDisplayName(tenantId, displayName);
    }

    @PutMapping("/my/display-name/{display-name}")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN','ROLE_TENANT_ADMIN')")
    public void updateMyTenantDisplayName(@ValidTenantId @PathVariable("display-name") String displayName) {
        tenantService.updateMyTenantDisplayName(displayName);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public Tenant getTenant(@ValidTenantId @PathVariable("id") String tenantId) {
        Tenant tenant = tenantService.getTenant(tenantId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Tenant with id %s not found.", tenantId)));
        return tenant;
    }

    @GetMapping("/my/profile")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN','ROLE_TENANT_ADMIN')")
    public Tenant getTenant() {
        return tenantService.getMyTenantDetails();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public void deleteTenant(@ValidTenantId @PathVariable("id") String tenantId) {
        tenantService.deleteTenant(tenantId);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public List<Tenant> listAllTenants() {
        return tenantService.listAllTenants()
                .stream()
                .sorted(Comparator.comparing(Tenant::getId))
                .collect(Collectors.toList());
    }
}
