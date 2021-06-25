package com.assessment.iam.dtos;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.*;

import com.assessment.common.validations.ValidDisplayName;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class UserCreateRequestDto {
//    @NotBlank(message = "Please provide fully qualified user name with tenant id, example: user@tenant_id.")
//    @Size(max = 100)
//    private String userName; //fully qualified username user@tenant example:

    @NotBlank
    @Size(max = 100)
    private String password;

    private String displayName;

//    @NotNull
//    private boolean enabled;

    @NotNull
    @Email
    @Size(max = 100)
    private String email;

    @NotNull
    @NotEmpty
    @Size(max = 100)
    private String firstName;

    @NotNull
    @NotEmpty
    @Size(max = 100)
    private String lastName;

    @Size(max = 1)
    private String gender;

    @Size(max = 500)
    private String address;

    @Size(max = 20)
    private String state;

    Set<@Size(max = 100) String> roles = new HashSet<>();
    
    private boolean acceptedTerms = false;
}