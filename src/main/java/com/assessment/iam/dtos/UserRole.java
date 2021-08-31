package com.assessment.iam.dtos;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public enum UserRole {
    ROLE_SUPER_ADMIN("ROLE_SUPER_ADMIN"),
    ROLE_TENANT_ADMIN("ROLE_TENANT_ADMIN"),
    ROLE_USER_ADMIN("ROLE_USER_ADMIN"),
    ROLE_STAFF("ROLE_STAFF"),
    ROLE_STUDENT("ROLE_STUDENT")
    ;

    private final String value;

    private UserRole(String value) {
        this.value = value;
    }

    public String toString() {
        return this.value + " " + this.name();
    }

    public String value() {
        return this.value;
    }

    public static UserRole valueOf(UserRole role) {
        return valueOf(role.value);
    }

    @NotNull
    public static UserRole resolve(String role) {
        UserRole[] var1 = values();
        for (UserRole status : var1) {
            if (Objects.equals(status.value, role)) {
                return status;
            }
        }

        return null;
    }

    public static List<String> getRoles(){
        List<String> roles = new ArrayList<>();
        UserRole[] var1 = values();
        for (UserRole role : var1) {
            roles.add(role.value);
        }
        return roles;
    }

    public static List<String> getRoles(boolean tenantEnabled){
        List<String> roles = new ArrayList<>();
        UserRole[] var1 = values();
        for (UserRole role : var1) {
            if (!tenantEnabled && !UserRole.ROLE_SUPER_ADMIN.value.equalsIgnoreCase(role.value) && !UserRole.ROLE_TENANT_ADMIN.value.equalsIgnoreCase(role.value)){
                roles.add(role.value);
            }
        }
        return roles;
    }
}
