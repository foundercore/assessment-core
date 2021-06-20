package com.assessment.iam.entities;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.assessment.common.validations.ValidDisplayName;

import javax.validation.constraints.*;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class User implements UserDetails {

    @Id
    @NotBlank(message = "Please provide fully qualified user name with tenant id, example: user@tenant_id.")
    @Size(max = 100)
    private String userName; //fully qualified username user@tenant example: test@asfd.com

    @NotBlank
    @Size(max = 100)
    private String password;

    @ValidDisplayName
    private String displayName;

    @NotNull
    private boolean enabled;

    @NotNull
    @Email
    @Size(max = 100)
    private String email;

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

    @LastModifiedDate
    private Date lastUpdatedOn;

    @LastModifiedBy
    private String lastUpdatedBy;

    @NotNull
    @NotEmpty
    Set<@NotBlank @Size(max = 100) String> roles = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<SimpleGrantedAuthority> set = new HashSet<>();
        for (String role : roles) {
            SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(role);
            set.add(simpleGrantedAuthority);
        }
        return set;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTenantId() {
        return StringUtils.substringAfterLast(this.userName, "@");
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Date getLastUpdatedOn() {
        return lastUpdatedOn;
    }

    public void setLastUpdatedOn(Date lastUpdatedOn) {
        this.lastUpdatedOn = lastUpdatedOn;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
