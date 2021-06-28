package com.assessment.iam.dtos;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.assessment.common.validations.ValidDisplayName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequestDto {
    @ValidDisplayName
    private String displayName;

    @NotNull
    private boolean enabled;

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Size(max = 1)
    private String gender;

    @Size(max = 500)
    private String address;

    @Size(max = 20)
    private String state;

    @NotNull
    @NotEmpty
    Set<@NotBlank @Size(max = 100) String> roles = new HashSet<>();

	private boolean acceptedTerms;

	private Date acceptedTermsOn;
}