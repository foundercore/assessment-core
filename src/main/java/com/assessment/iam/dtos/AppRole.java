package com.assessment.iam.dtos;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public enum AppRole {
    ROLE_SUPER_ADMIN("ROLE_SUPER_ADMIN"),
    ROLE_TENANT_ADMIN("ROLE_TENANT_ADMIN"),
    ROLE_USER_ADMIN("ROLE_USER_ADMIN"),
    ROLE_STAFF("ROLE_STAFF"),
    ROLE_STUDENT("ROLE_STUDENT")
    ;

    private final String value;

    private AppRole(String value) {
        this.value = value;
    }

    public String toString() {
        return this.value + " " + this.name();
    }

    public String value() {
        return this.value;
    }

    public static AppRole valueOf(AppRole role) {
        return valueOf(role.value);
    }

    @NotNull
    public static AppRole resolve(String role) {
        AppRole[] var1 = values();
        for (AppRole status : var1) {
            if (Objects.equals(status.value, role)) {
                return status;
            }
        }

        return null;
    }

    public static List<String> getRoles(){
        List<String> roles = new ArrayList<>();
        AppRole[] var1 = values();
        for (AppRole role : var1) {
            roles.add(role.value);
        }
        return roles;
    }

    public static List<String> getRoles(boolean tenantEnabled){
        List<String> roles = new ArrayList<>();
        AppRole[] var1 = values();
        for (AppRole role : var1) {
            if (!tenantEnabled && !AppRole.ROLE_SUPER_ADMIN.value.equalsIgnoreCase(role.value) && !AppRole.ROLE_TENANT_ADMIN.value.equalsIgnoreCase(role.value)){
                roles.add(role.value);
            }
        }
        return roles;
    }
}
