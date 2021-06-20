package com.assessment.iam.entities;

import com.assessment.common.validations.ValidTenantId;

import lombok.Data;

@Data
public class TenantEntity {

    @ValidTenantId
    protected String tenantId;
}
