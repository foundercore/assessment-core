package com.assessment.iam.services;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.assessment.common.AmazonSESUtility;
import com.assessment.common.ConfigUtility;
import com.assessment.common.CsvFileDecoder;
import com.assessment.common.FileUtility;
import com.assessment.common.StringUtility;
import com.assessment.iam.commons.AuthUtils;
import com.assessment.iam.dtos.AppRole;
import com.assessment.iam.dtos.UserUpdateRequestDto;
import com.assessment.iam.entities.Tenant;
import com.assessment.iam.entities.User;
import com.assessment.iam.exceptions.TenantInActiveException;
import com.assessment.iam.exceptions.TenantNotFoundException;
import com.assessment.iam.repositories.UserRepository;
import com.google.common.io.Files;
import com.opencsv.exceptions.CsvValidationException;

@Service("userDetailsService")
public class UserServiceImpl implements UserDetailsService, UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);


    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        Assert.hasLength(userName, "UserName cannot be empty.");
        //Get user from db.
//        User user = userRepository.findById(userName)
        User user = userRepository.findByEmail(userName)
                .orElseThrow(() -> new UsernameNotFoundException("User account can not be located!"));

        //Check tenant active for this user.
//        Tenant tenant = tenantService.getTenant(user.getTenantId()).orElseThrow(() -> new TenantNotFoundException(String.format("Tenant not found for user %s", user.getUsername())));
        Tenant tenant = tenantService.getTenant(user.getTenantId()).orElseThrow(() -> new TenantNotFoundException(String.format("Tenant not found for user %s", user.getEmail())));
        if (!tenant.isEnabled()) {
//            throw new TenantInActiveException(String.format("Tenant not active for user %s", user.getUsername()));
            throw new TenantInActiveException(String.format("Tenant not active for user %s", user.getEmail()));
        }
        return user;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public void createUser(User user) throws UnsupportedEncodingException, MessagingException {

        String password = user.getPassword();
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("User with user email %s already exists!", user.getEmail()));
        }
//        if (!AuthUtils.matchesPolicy(user.getPassword())) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
//                    String.format("User Password %s is invalid as per configured policy!", user.getPassword()));
//        }

        if (!AppRole.getRoles(false).containsAll(user.getRoles())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role found!");
        }

        String loggedInUserName = AuthUtils.getCurrentQualifiedUsername();
        User loggedInUser = userRepository.findById(loggedInUserName).orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                String.format("User with user name %s not found!", loggedInUserName)));

        if (user.getRoles().contains(AppRole.ROLE_TENANT_ADMIN.value()) && !loggedInUser.getRoles().contains(AppRole.ROLE_TENANT_ADMIN.value())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient privileges to assign Role ROLE_TENANT_ADMIN!");
        }

        /* role conflict validation */
        validateUserRoleConflict(user.getRoles());

        user.setEnabled(true);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        /* Trigger Mail */
        String mailBody = new StringBuilder("<p>Greetings ").append(user.getDisplayName()).append("</p1>")
                .append("<p>You have been registered as ").append(user.getEmail()).append("</p>")
                .append("<p>Your temporary password to login into application is <b>").append(password).append("</b></p1>")
                .append("<p>Regards, <br>")
                .append("Assessment Tech Team</p>")
                .toString();
        String subject = "Assessment Account Registration";
        AmazonSESUtility.sendMailNotification(subject, mailBody, Collections.singletonList(user.getEmail()), null, null);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public void updateUser(String userName, UserUpdateRequestDto dto) {
        validateTenantId(userName);
        if (!AppRole.getRoles(false).containsAll(dto.getRoles())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role found!");
        }

        User currentUser = userRepository.findById(userName).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("User with username %s not found.", userName)));

        String loggedInUserName = AuthUtils.getCurrentQualifiedUsername();
        User loggedInUser = userRepository.findById(loggedInUserName).orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                String.format("User with user name %s not found!", loggedInUserName)));

        //check if role ROLE_TENANT_ADMIN is assigned or removed
        // or if user already has ROLE_TENANT_ADMIN,
        // than logged in user should have role ROLE_TENANT_ADMIN.
        if ((dto.getRoles().contains(AppRole.ROLE_TENANT_ADMIN.value()) || currentUser.getRoles().contains(AppRole.ROLE_TENANT_ADMIN.value()))
                && !loggedInUser.getRoles().contains(AppRole.ROLE_TENANT_ADMIN.value())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient privileges to assign Role ROLE_TENANT_ADMIN!");
        }

        /* role conflict validation */
        validateUserRoleConflict(dto.getRoles());

        currentUser.setDisplayName(dto.getDisplayName());
        currentUser.setFirstName(dto.getFirstName());
        currentUser.setLastName(dto.getLastName());
        currentUser.setEnabled(dto.isEnabled());
        currentUser.setRoles(dto.getRoles());

        currentUser.setGender(dto.getGender());
        currentUser.setAddress(dto.getAddress());
        currentUser.setState(dto.getState());
		currentUser.setAcceptedTerms(dto.isAcceptedTerms());
		currentUser.setAcceptedTermsOn(dto.getAcceptedTermsOn());

        //keep existing password as it is, do not update it.
        userRepository.save(currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_USER_READER')")
    public Optional<User> getUser(String userName) {
        validateTenantId(userName);
        return userRepository.findById(userName);
    }

    @Override
    public User getUserByEmail(@NotBlank String emailId) {
        User user = userRepository.findByEmail(emailId)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User with email %s not found.", emailId)));
        validateTenantId(user.getUsername());
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public User getLoggedInUserDetails() {
        String userName = AuthUtils.getCurrentQualifiedUsername();
        return userRepository.findById(userName).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with username %s not found.", userName)));
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public void updateUserStatus(String userName, boolean status) {
        validateTenantId(userName);
        User user = userRepository.findById(userName).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("User with username %s not found.", userName)));

        String loggedInUserName = AuthUtils.getCurrentQualifiedUsername();
        User loggedInUser = userRepository.findById(loggedInUserName).orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                String.format("User with user name %s not found!", loggedInUserName)));

        if (user.getRoles().contains(AppRole.ROLE_TENANT_ADMIN.value()) && !loggedInUser.getRoles().contains(AppRole.ROLE_TENANT_ADMIN.value())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient privileges, Role ROLE_TENANT_ADMIN required!");
        }

        if (user.isEnabled() == status) {
            return;
        }
        user.setEnabled(status);
        userRepository.save(user);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public void updateUserDisplayName(String userName, String displayName) {
        validateTenantId(userName);
        User user = userRepository.findById(userName).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with username %s not found.", userName)));

        String loggedInUserName = AuthUtils.getCurrentQualifiedUsername();
        User loggedInUser = userRepository.findById(loggedInUserName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        String.format("User with user name %s not found!", loggedInUserName)));
        if (user.getRoles().contains(AppRole.ROLE_TENANT_ADMIN.value()) && !loggedInUser.getRoles().contains(AppRole.ROLE_TENANT_ADMIN.value())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient privileges, Role ROLE_TENANT_ADMIN required!");
        }

        user.setDisplayName(displayName);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateMyDisplayName(String displayName) {
        String userName = AuthUtils.getCurrentQualifiedUsername();
        User user = userRepository.findById(userName).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with username %s not found.", userName)));
        user.setDisplayName(displayName);
        userRepository.save(user);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public void deleteUser(String userName) {
        validateTenantId(userName);
        User user = userRepository.findById(userName).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with username %s not found.", userName)));

        String loggedInUserName = AuthUtils.getCurrentQualifiedUsername();
        User loggedInUser = userRepository.findById(loggedInUserName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        String.format("User with user name %s not found!", loggedInUserName)));
        if (user.getRoles().contains(AppRole.ROLE_TENANT_ADMIN.value()) && !loggedInUser.getRoles().contains(AppRole.ROLE_TENANT_ADMIN.value())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient privileges, Role ROLE_TENANT_ADMIN required!");
        }

        userRepository.deleteById(userName);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public List<User> listAllUsers() {
        return userRepository.findAllByUserNameEndsWith("@" + AuthUtils.getCurrentTenantId());
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public void addRole(String userName, String role) {
        validateTenantId(userName);

        if (!AppRole.getRoles(false).contains(role)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role!");
        }

        User user = userRepository.findById(userName).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with username %s not found", userName)));

        String loggedInUserName = AuthUtils.getCurrentQualifiedUsername();
        User loggedInUser = userRepository.findById(loggedInUserName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        String.format("User with user name %s not found!", loggedInUserName)));
        if ((AppRole.ROLE_TENANT_ADMIN.value().equals(role) || user.getRoles().contains(AppRole.ROLE_TENANT_ADMIN.value()))
                && !loggedInUser.getRoles().contains(AppRole.ROLE_TENANT_ADMIN.value())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient privileges, Role ROLE_TENANT_ADMIN required!");
        }


        Set<String> userRoles = user.getRoles();
        if (!userRoles.contains(role)) {
            userRoles.add(role);
            user.setRoles(userRoles);
            userRepository.save(user);
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
    public void removeRole(String userName, String role) {
        validateTenantId(userName);

        if (!AppRole.getRoles(false).contains(role)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role!");
        }

        User user = userRepository.findById(userName).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with username %s not found", userName)));

        String loggedInUserName = AuthUtils.getCurrentQualifiedUsername();
        User loggedInUser = userRepository.findById(loggedInUserName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        String.format("User with user name %s not found!", loggedInUserName)));
        if ((AppRole.ROLE_TENANT_ADMIN.value().equals(role) || user.getRoles().contains(AppRole.ROLE_TENANT_ADMIN.value()))
                && !loggedInUser.getRoles().contains(AppRole.ROLE_TENANT_ADMIN.value())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient privileges, Role ROLE_TENANT_ADMIN required!");
        }

        Set<String> userRoles = user.getRoles();
        if (userRoles.contains(role)) {
            userRoles.remove(role);
            user.setRoles(userRoles);
            userRepository.save(user);
        }
    }

    @Transactional
    @Override
    public void bulkCreateUsers(MultipartFile[] files) throws IOException, CsvValidationException, MessagingException {
        Map<String, String> userPasswordContainer = new HashMap<>();
        String directory = Files.createTempDir().getAbsolutePath() + File.separator + AuthUtils.getCurrentUsername()
                + File.separator + UUID.randomUUID().toString();
        File tempFile = null;
        List<User> users = new ArrayList<>();
        try {
            Files.createParentDirs(new File(directory + File.separator + "tmp.log"));
            for (MultipartFile file : files) {
                String fileName = file.getOriginalFilename();
                fileName = directory + File.separator + fileName;
                FileUtility.saveFile(file, fileName);
                tempFile = new File(fileName);

                /* load file records */
                CsvFileDecoder decoder = new CsvFileDecoder(fileName);
                while (decoder.hasNext()){
                    Map<String, Object> record = decoder.next();

                    User user = new User();
                    user.setDisplayName(String.valueOf(record.getOrDefault("displayname", "")));
                    user.setEmail(String.valueOf(record.getOrDefault("email", "")));
                    user.setFirstName(String.valueOf(record.getOrDefault("firstname", "")));
                    user.setLastName(String.valueOf(record.getOrDefault("lastname", "")));
                    user.setGender(String.valueOf(record.getOrDefault("gender", "")));
                    user.setAddress(String.valueOf(record.getOrDefault("address", "")));
                    user.setState(String.valueOf(record.getOrDefault("state", "")));
                    user.setPassword(String.valueOf(record.getOrDefault("password", "")));
                    if (user.getDisplayName().trim().isEmpty()){
                        user.setDisplayName(user.getFirstName().trim() + " " + user.getLastName().trim());
                    }

                    String username = generateNewUsername();
                    user.setUserName(username);

                    if (StringUtils.isEmpty(user.getPassword())){
                        user.setPassword(StringUtility.generateCommonLangPassword());
                    }
                    String password = user.getPassword();
                    user.setPassword(passwordEncoder.encode(password));
                    userPasswordContainer.put(user.getUsername(), password);
                    logger.info("User - {}, Password - {}", user.getEmail(), password); //TODO remove once pwd flow is automated
                    user.setEnabled(true);

                    /* roles */
                    String [] rid = String.valueOf(record.getOrDefault("roles", AppRole.ROLE_STUDENT.value())).toUpperCase().split("\\|");
                    Set<String> roles = new HashSet<>(Arrays.asList(rid));
                    user.setRoles(roles);

                    users.add(user);
                }
            }
        } catch (IOException | CsvValidationException e) {
            throw e;
        } finally {
            if (tempFile != null && tempFile.exists()){
                FileUtility.delete(tempFile);
            }
        }

        /* validate users */
        StringBuilder errors = new StringBuilder();
        List<String> batchEmails = new ArrayList<>();
        for (User user : users) {
            try {

                validateTenantId(user.getUsername());

                if (user.getEmail().trim().isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User email not found");
                }
                if (batchEmails.contains(user.getEmail())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Duplicate email %s in batch", user.getEmail()));
                }
                if (user.getFirstName().trim().isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User first name not found for %s ", user.getEmail()));
                }

                if (user.getLastName().trim().isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User last name not found for %s ", user.getLastName()));
                }

                if (userRepository.findById(user.getUsername()).isPresent()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            String.format("User with user name %s already exists!", user.getUsername()));
                }

                if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            String.format("User with user email %s already exists!", user.getEmail()));
                }

                if (!Arrays.asList(AppRole.ROLE_STAFF.value(), AppRole.ROLE_STUDENT.value()).containsAll(user.getRoles())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role found. Allowed roles are ROLE_STAFF & ROLE_STUDENT.");
                }

                String loggedInUserName = AuthUtils.getCurrentQualifiedUsername();
                User loggedInUser = userRepository.findById(loggedInUserName).orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        String.format("User with user name %s not found!", loggedInUserName)));

                if (user.getRoles().contains(AppRole.ROLE_TENANT_ADMIN.value()) && !loggedInUser.getRoles().contains(AppRole.ROLE_TENANT_ADMIN.value())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient privileges to assign Role ROLE_TENANT_ADMIN!");
                }

                /* role conflict validation */
                validateUserRoleConflict(user.getRoles());

                batchEmails.add(user.getEmail());
            } catch (Exception e) {
                errors.append("User - ").append(user.getEmail()).append(", Error - ").append(e.getMessage()).append("\n");
            }
        }

        /* create users */
        if (errors.toString().trim().isEmpty()) {
            for (User user : users) {
                try {
                    userRepository.save(user);
                    /* Trigger Mail */
                    String mailBody = new StringBuilder("<p>Greetings ").append(user.getDisplayName()).append("</p1>")
                            .append("<p>You have been registered as ").append(user.getEmail()).append("</p>")
                            .append("<p>Your temporary password to login into application is <b>").append(userPasswordContainer.get(user.getUsername())).append("</b></p1>")
                            .append("<p>Regards, <br>")
                            .append("Assessment Tech Team</p>")
                            .toString();
                    String subject = "Assessment Account Registration";
                    AmazonSESUtility.sendMailNotification(subject, mailBody, Collections.singletonList(user.getEmail()), null, null);
                } catch (Exception e) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
                }
            }
        }else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errors.toString());
        }
    }

    private void validateUserRoleConflict(Set<String> roles) {
        if (roles.size() > 1 && roles.contains(AppRole.ROLE_STUDENT.value())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format( "Role conflict detected. %s not allowed along with other roles.", AppRole.ROLE_STUDENT.value()));
        }
    }

    @Override
    public String generateNewUsername(){
        return UUID.randomUUID().toString() + "@" + AuthUtils.getCurrentTenantId();
    }

    @Override
    public void updatePassword(String userName, String password) {
        validateTenantId(userName);

        User user = userRepository.findById(userName).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with username %s not found.", userName)));
//        if (!AuthUtils.matchesPolicy(password)) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
//                    String.format("User Password %s is invalid as per configured policy!", password));
//        }
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    @Override
    public void forgotPassword(String emailId) throws UnsupportedEncodingException, MessagingException {
        User user = userRepository.findByEmail(emailId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with email %s not found.", emailId)));

        if (isBotUser(user.getUsername())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can not change state of system user");
        }

        String password = StringUtility.generateCommonLangPassword();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        logger.info("Password - {} updated for user - {}", password, user.getEmail());

        /* Trigger Mail */
        String mailBody = new StringBuilder("<p>Greetings ").append(user.getDisplayName()).append("</p1>")
                .append("<p>Your temporary password to login into application is <b>").append(password).append("</b></p1>")
                .append("<p>Regards, <br>")
                .append("Assessment Tech Team</p>")
                .toString();
        String subject = "Assessment Forgot Password";
        AmazonSESUtility.sendMailNotification(subject, mailBody, Collections.singletonList(user.getEmail()), null, null);
    }

    @Override
    public Map<String, ?> bulkDeleteUsers(List<String> usernames) {
        Map<String, Object> operationStatus = new HashMap<>();
        List<String> success = new ArrayList<>();
        Map<String, String> failed = new HashMap<>();
        operationStatus.put("success", success);
        operationStatus.put("failed", failed);
        for (String u: usernames){
            try{
                deleteUser(u);
                success.add(u);
            }catch (Exception e){
                failed.put(u, e.getMessage());
            }
        }
        return operationStatus;
    }

    @Override
    public boolean isBotUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User account not exist!"));
        String username = ConfigUtility.instance().getProperty("app.system-user-name");
        return user.getUsername().equalsIgnoreCase(username + "@" + user.getTenantId());
    }

    private void validateTenantId(String userName) {
        String currentTenantId = AuthUtils.getCurrentTenantId();
        String tenantIdInQualifiedUserName = StringUtils.substringAfterLast(userName, "@");
        if (!tenantIdInQualifiedUserName.equals(currentTenantId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Invalid Organization Id in fully qualified user name, expecting %s.", currentTenantId));
        }
    }
}
