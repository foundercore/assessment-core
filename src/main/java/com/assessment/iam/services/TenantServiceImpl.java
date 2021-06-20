package com.assessment.iam.services;

import com.assessment.common.ConfigUtility;
import com.assessment.common.DateUtility;
import com.assessment.iam.commons.AuthUtils;
import com.assessment.iam.dtos.TenantDto;
import com.assessment.iam.entities.Tenant;
import com.assessment.iam.entities.User;
import com.assessment.iam.repositories.TenantRepository;
import com.assessment.iam.repositories.UserRepository;
import com.google.common.collect.Lists;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public TenantServiceImpl(TenantRepository tenantRepository, UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public void createTenant(TenantDto tenantDto) {
        if (tenantRepository.findById(tenantDto.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Tenant with id %s already exists.", tenantDto.getId()));
        }

        Tenant tenant = new Tenant();
        tenant.setId(tenantDto.getId());
        tenant.setDisplayName(tenantDto.getDisplayName());
        tenant.setEnabled(true);
        tenantRepository.save(tenant);
        createDefaultUser(tenantDto.getId(), tenantDto.getDefaultUserEmail(), tenantDto.getDefaultUserPassword());
    }

    public void createDefaultUser(String tenantId, String userEmail, String userPassword) {
        if (!AuthUtils.matchesPolicy(userPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("User Password %s is invalid!", userPassword));
        }
        User user = new User();
        user.setUserName("admin@".concat(tenantId));
        user.setEmail(userEmail);
        user.setPassword(passwordEncoder.encode(userPassword));
        user.setEnabled(true);
//        user.setCreationDate(new Date());
        user.setFirstName("Tenant Admin");
        user.setLastName("Tenant Admin");
        user.setDisplayName("Tenant Admin");
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_TENANT_ADMIN");
        user.setRoles(roles);
        userRepository.save(user);

        /* creating bot user */
        String botUsername = ConfigUtility.instance().getProperty("app.system-user-name");
        String botPassword = ConfigUtility.instance().getProperty("app.system-user-password");

        User bot = new User();
//        bot.setUserName(botUsername + new Date().getTime() + "@".concat(tenantId));
        bot.setUserName(botUsername + "@".concat(tenantId));
        bot.setEmail(botUsername + "@".concat(tenantId));
        bot.setPassword(passwordEncoder.encode(botPassword));
        bot.setEnabled(true);

        bot.setFirstName("System User to run system jobs");
        bot.setLastName("");
        bot.setDisplayName("BOT");
        Set<String> botRoles = new HashSet<>();
        botRoles.add("ROLE_TENANT_ADMIN");
        bot.setRoles(roles);
        userRepository.save(bot);

    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public void deleteTenant(String tenantId) {
        if (tenantRepository.findById(tenantId).isPresent()) {
            tenantRepository.deleteById(tenantId);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Tenant with id %s not found.", tenantId));
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public void updateTenantStatus(String tenantId, boolean status) {
        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Tenant with id %s not found.", tenantId)));
        if (tenant.isEnabled() == status) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Tenant with id %s has same status as requested.", tenantId));
        }
        tenant.setEnabled(status);
        tenantRepository.save(tenant);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public void updateTenantDisplayName(String tenantId, String displayName) {
        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Tenant with id %s not found.", tenantId)));
        tenant.setDisplayName(displayName);
        tenantRepository.save(tenant);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN','ROLE_TENANT_ADMIN')")
    public void updateMyTenantDisplayName(String displayName) {
        String tenantId = AuthUtils.getCurrentTenantId();
        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Tenant with id %s not found.", tenantId)));
        tenant.setDisplayName(displayName);
        tenantRepository.save(tenant);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tenant> getTenant(String tenantId) {
        return tenantRepository.findById(tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN','ROLE_TENANT_ADMIN')")
    public Tenant getMyTenantDetails() {
        String tenantId = AuthUtils.getCurrentTenantId();
        return tenantRepository.findById(tenantId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Tenant with id %s not found.", tenantId)));
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public List<Tenant> listAllTenants() {
        return Lists.newArrayList(tenantRepository.findAll());
    }

}
