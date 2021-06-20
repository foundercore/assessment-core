package com.assessment.iam.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class UserResponseDto {
    private String userName; //fully qualified username user@tenant example: test@asfd.com
    private String displayName;
    private boolean enabled;
    private String email;
    private String firstName;
    private String lastName;
    private String gender;
    private String address;
    private String state;
    private Date lastUpdatedOn;
    private String lastUpdatedBy;
    Set<String> roles = new HashSet<>();
}
