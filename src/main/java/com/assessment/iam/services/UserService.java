package com.assessment.iam.services;

import com.assessment.common.validations.ValidDisplayName;
import com.assessment.iam.dtos.UserUpdateRequestDto;
import com.assessment.iam.entities.User;
import com.opencsv.exceptions.CsvValidationException;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;


import javax.mail.MessagingException;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Validated
public interface UserService {


    void createUser(@Valid User user) throws UnsupportedEncodingException, MessagingException;

    void updateUser(@NotBlank String userName, @Valid UserUpdateRequestDto userUpdateRequestDto);

    Optional<User> getUser(@NotBlank String userName);

    User getUserByEmail(@NotBlank String emailId);

    User getLoggedInUserDetails();

    void updateUserStatus(@NotBlank String userName, @NotNull boolean status);

    void updateUserDisplayName(@NotBlank String userName, @ValidDisplayName String displayName);

    void updateMyDisplayName(@ValidDisplayName String displayName);

    void deleteUser(@NotBlank String userName);

    List<User> listAllUsers();

    void addRole(@NotBlank String userName, @NotBlank String role);

    void removeRole(@NotBlank String userName, @NotBlank String role);

    void bulkCreateUsers(MultipartFile[] files) throws IOException, CsvValidationException, MessagingException;

    String generateNewUsername();

    void updatePassword(String userName, String password);

    void forgotPassword(String emailId) throws UnsupportedEncodingException, MessagingException;

    Map<String,?> bulkDeleteUsers(List<String> usernames);

    boolean isBotUser(String userId);
}