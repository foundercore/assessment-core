package com.assessment.iam.entities;

import lombok.Data;
import org.springframework.data.annotation.*;

import com.assessment.common.validations.ValidDisplayName;
import com.assessment.common.validations.ValidTenantId;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class Tenant {

    @ValidTenantId
    @Id
    private String id;

    @ValidDisplayName
    private String displayName;

    @NotNull
    private boolean enabled;

    @LastModifiedDate
    private Date lastUpdatedOn;

    @LastModifiedBy
    private String lastUpdatedBy;
}