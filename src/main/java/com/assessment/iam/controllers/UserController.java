package com.assessment.iam.controllers;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.assessment.common.StringUtility;
import com.assessment.common.validations.ValidDisplayName;
import com.assessment.iam.commons.AuthUtils;
import com.assessment.iam.dtos.AppRole;
import com.assessment.iam.dtos.UserCreateRequestDto;
import com.assessment.iam.dtos.UserResponseDto;
import com.assessment.iam.dtos.UserUpdateRequestDto;
import com.assessment.iam.entities.User;
import com.assessment.iam.services.UserService;
import com.assessment.studentbatch.StudentBatch;
import com.assessment.studentbatch.StudentBatchService;
import com.opencsv.exceptions.CsvValidationException;

import lombok.extern.slf4j.Slf4j;

@Validated
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private StudentBatchService studentBatchService;

    @Autowired
    private Validator validator;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public void createUser(@RequestBody @Valid UserCreateRequestDto userCreateRequestDto) {
        User user = new User();
        if (StringUtils.isEmpty(userCreateRequestDto.getDisplayName())){
            user.setDisplayName(userCreateRequestDto.getFirstName() + " " + userCreateRequestDto.getLastName());
        }else {
            user.setDisplayName(userCreateRequestDto.getDisplayName());
        }
        if (StringUtils.isEmpty(userCreateRequestDto.getPassword())){
            user.setPassword(StringUtility.generateCommonLangPassword());
        }else {
            user.setPassword(userCreateRequestDto.getPassword());
        }
        user.setEmail(userCreateRequestDto.getEmail());
        user.setFirstName(userCreateRequestDto.getFirstName());
        user.setLastName(userCreateRequestDto.getLastName());
        user.setGender(userCreateRequestDto.getGender());
        user.setAddress(userCreateRequestDto.getAddress());
        user.setState(userCreateRequestDto.getState());
        if(userCreateRequestDto.isAcceptedTerms()) {
        	user.setAcceptedTerms(userCreateRequestDto.isAcceptedTerms());
        	user.setAcceptedTermsOn(new Date());
        }
        if (userCreateRequestDto.getRoles() != null && !userCreateRequestDto.getRoles().isEmpty()){
            user.setRoles(userCreateRequestDto.getRoles());
        }else {
            user.setRoles(new HashSet<>(Collections.singletonList(AppRole.ROLE_STUDENT.value())));
        }
        try {
            user.setUserName(userService.generateNewUsername());
            userService.createUser(user);
        } catch (UnsupportedEncodingException|MessagingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping("/bulk-upload")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public void createUser(@RequestParam("file") MultipartFile[] files) {
        try {
            userService.bulkCreateUsers(files);
        } catch (IOException|CsvValidationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (MessagingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PutMapping("/{username}")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public void updateUser(@NotBlank @PathVariable("username") String userName,
                           @RequestBody UserUpdateRequestDto userUpdateRequestDto) {
        Set<ConstraintViolation<UserUpdateRequestDto>> violations = validator.validate(userUpdateRequestDto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        if (userService.isBotUser(userName)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can not change state of system user");
        }
        userService.updateUser(userName, userUpdateRequestDto);
    }

    @GetMapping("/{username}")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public UserResponseDto getUser(@NotBlank @PathVariable("username") String userName) {
        User user = userService.getUser(userName).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with username %s not found", userName)));
        return prepareUserResponseDto(user);
    }

    @GetMapping("/by-email/{email-id}")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public UserResponseDto getUserByEmailId(@NotBlank @PathVariable("email-id") String emailId) {
        User user = userService.getUserByEmail(emailId);
        return prepareUserResponseDto(user);
    }

    @GetMapping("/my/profile")
    public UserResponseDto getMyProfile() {
        User user = userService.getLoggedInUserDetails();
        return prepareUserResponseDto(user);
    }

    @DeleteMapping("/{username}")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public void deleteUser(@NotBlank @PathVariable("username") String userName) {
        List<StudentBatch> batches = null;
        try {
            batches = studentBatchService.userAssociatedBatches(getUser(userName).getEmail());
        }catch (Exception ignored){}

        if (batches != null && !batches.isEmpty()){
            List<String> sb = new ArrayList<>();
            for (StudentBatch s: batches){
                sb.add(s.getName());
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Can't Delete. Student part of batch(s). Batch %s", StringUtils.join(sb)));
        }
        if (userService.isBotUser(userName)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can not change state of system user");
        }
        userService.deleteUser(userName);
    }

    @Transactional
    @PostMapping("/bulk-remove")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public Map<String, ?> bulkDeleteUsers(@RequestBody List<String> usernames){
        Map<String, String> reasons = new HashMap<>();
        List<String> failed = new ArrayList<>();
        for (String u: usernames){
            try {
                List<StudentBatch> batches = studentBatchService.userAssociatedBatches(getUser(u).getEmail());
                if (batches != null && !batches.isEmpty()) {
                    List<String> sb = new ArrayList<>();
                    for (StudentBatch s : batches) {
                        sb.add(s.getName());
                    }
                    failed.add(u);
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Can't Delete. Student part of batch(s). Batch %s", StringUtils.join(sb)));
                }
            }catch (Exception e){
                reasons.put(u, e.getMessage());
            }
            if (!failed.contains(u) && userService.isBotUser(u)){
                failed.add(u);
                reasons.put(u, "Can't Delete. Can not change state of system user");
            }
        }
        List<String> remaining = new ArrayList<>();
        for (String u: usernames){
            if (!failed.contains(u)){
                remaining.add(u);
            }
        }
        if (!remaining.isEmpty()){
            Map<String, ?> state = userService.bulkDeleteUsers(remaining);
            ((Map<String, String>)state.get("failed")).putAll(reasons);
            return state;
        }else {
            Map<String, Object> operationStatus = new HashMap<>();
            operationStatus.put("success", new ArrayList<>());
            operationStatus.put("failed", reasons);
            return operationStatus;
        }
    }

    @PutMapping("/{username}/enable/{status}")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public void updateUserStatus(@NotBlank @PathVariable("username") String userName, @NotBlank @PathVariable("status") boolean status) {
        if (userService.isBotUser(userName)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can not change state of system user");
        }
        userService.updateUserStatus(userName, status);
    }

    @PutMapping("/{username}/display-name/{display-name}")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public void updateUserDisplayName(@NotBlank @PathVariable("username") String userName, @ValidDisplayName @PathVariable("display-name") String displayName) {
        if (userService.isBotUser(userName)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can not change state of system user");
        }
        userService.updateUserDisplayName(userName, displayName);
    }

    @PutMapping("/{username}/roles/{role-name}/add")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public void addRole(@NotBlank @PathVariable("username") String userName, @NotBlank @PathVariable("role-name") String roleName) {
        if (userService.isBotUser(userName)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can not change state of system user");
        }
        userService.addRole(userName, roleName);
    }

    @PutMapping("/{username}/roles/{role-name}/remove")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public void removeRole(@NotBlank @PathVariable("username") String userName, @NotBlank @PathVariable("role-name") String roleName) {
        if (userService.isBotUser(userName)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can not change state of system user");
        }
        userService.removeRole(userName, roleName);
    }

    @PutMapping("/my/password")
    public void updatePassword(@NotBlank @RequestBody String newPassword) {
        if ("admin@demo.com".equalsIgnoreCase(AuthUtils.getCurrentQualifiedUsername())){ //TODO handle later via block pwd change for specific users
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Action Not Allowed");
        }
        if (userService.isBotUser(AuthUtils.getCurrentQualifiedUsername())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can not change state of system user");
        }
        userService.updatePassword(AuthUtils.getCurrentQualifiedUsername(), newPassword);
    }

	@PutMapping("/update/password/id")
	@PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
	public void updatePasswordById(@NotBlank @RequestParam String userId, @NotBlank @RequestParam String newPassword) {
		if ("admin@demo.com".equalsIgnoreCase(userId)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Action Not Allowed");
		}
		if (userService.isBotUser(userId)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can not change state of system user");
		}
		userService.updatePassword(userId, newPassword);
	}
    @PutMapping("/public/forgot-password")
    public void forgotPassword(@NotBlank @RequestBody String emailId) {
        try {
            if ("test@demo.com".equalsIgnoreCase(emailId)){ //TODO handle later via block pwd change for specific users
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Action Not Allowed");
            }
            userService.forgotPassword(emailId);
        } catch (UnsupportedEncodingException|MessagingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PutMapping("/my/display-name/{display-name}")
    public void updateMyDisplayName(@ValidDisplayName @PathVariable("display-name") String displayName) {
        if (userService.isBotUser(AuthUtils.getCurrentQualifiedUsername())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can not change state of system user");
        }
        userService.updateMyDisplayName(displayName);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public List<UserResponseDto> listUsers() {
        return userService.listAllUsers().stream()
                .map(user -> {
                    return prepareUserResponseDto(user);
                })
                .sorted(Comparator.comparing(UserResponseDto::getUserName))
                .collect(toList());
    }

    @GetMapping("/by-role/{role-name}")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public List<UserResponseDto> listUsersByRole(@NotBlank @PathVariable("role-name") String roleName) {
        return userService.listAllUsers().stream().filter(user-> user.getRoles().contains(roleName))
                .map(user -> {
                    return prepareUserResponseDto(user);
                })
                .sorted(Comparator.comparing(UserResponseDto::getUserName))
                .collect(toList());
    }

    @GetMapping("/{username}/roles")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public Set<String> listUserRoles(@NotBlank @PathVariable("username") String userName) {
        User user = userService.getUser(userName).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with username %s not found", userName)));
        return user.getRoles();
    }

    @GetMapping("/app-configured-roles")
    public List<String> appConfiguredRoles() {
        return AppRole.getRoles(false);
    }

    public UserResponseDto prepareUserResponseDto(User user) {
        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setUserName(user.getUsername());
        userResponseDto.setDisplayName(user.getDisplayName());
        userResponseDto.setEnabled(user.isEnabled());
        userResponseDto.setEmail(user.getEmail());
        userResponseDto.setFirstName(user.getFirstName());
        userResponseDto.setLastName(user.getLastName());
        userResponseDto.setGender(user.getGender());
        userResponseDto.setAddress(user.getAddress());
        userResponseDto.setState(user.getState());
        userResponseDto.setLastUpdatedOn(user.getLastUpdatedOn());
        userResponseDto.setLastUpdatedBy(user.getLastUpdatedBy());
        userResponseDto.setRoles(user.getRoles());
        userResponseDto.setAcceptedTerms(user.isAcceptedTerms());
        userResponseDto.setAcceptedTermsOn(user.getAcceptedTermsOn());
        
        return userResponseDto;
    }
}
