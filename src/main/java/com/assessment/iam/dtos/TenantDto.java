package com.assessment.iam.dtos;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.assessment.common.validations.ValidDisplayName;
import com.assessment.common.validations.ValidTenantId;

@Getter
@Setter
public class TenantDto {

    @ValidTenantId
    private String id;

    @ValidDisplayName
    private String displayName;

    @NotBlank
    @Size(max = 100)
    private String defaultUserPassword;

    @NotNull
    @Email
    @Size(max = 100)
    private String defaultUserEmail;
}
