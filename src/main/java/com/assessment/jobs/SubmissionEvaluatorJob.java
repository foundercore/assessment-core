package com.assessment.jobs;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import com.assessment.common.ConfigUtility;
import com.assessment.iam.commons.AuthUtils;
import com.assessment.iam.entities.Tenant;
import com.assessment.iam.services.TenantService;
import com.assessment.questionpaper.TestAssignmentService;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class SubmissionEvaluatorJob implements Job {

    @Autowired
    private TenantService tenantService;

    @Autowired
    TestAssignmentService service;

    @Resource(name="authenticationManager")
    private AuthenticationManager authManager;


    @Override
    public void execute(JobExecutionContext context) {
        try {
            List<Tenant> tenants = tenantService.listAllTenants();
            for (Tenant tenant : tenants) {
                String internalTenant = ConfigUtility.instance().getProperty("app.system.tenant");
                if (tenant.getId().equalsIgnoreCase(internalTenant)) continue;

                log.info("Running evaluation for tenant - {}", tenant.getId());

                String username = ConfigUtility.instance().getProperty("app.system-user-name");
                String password = ConfigUtility.instance().getProperty("app.system-user-password");
                String loginId = username + "@" + tenant.getId();
                UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(loginId, password);

//                Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
//                authorities.add(new SimpleGrantedAuthority("ROLE_TENANT_ADMIN"));
//                User userPrincipal = new User("admin@" + tenant.getId(), "", true, true, true, true, authorities);
//                UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(userPrincipal, "Pass@123", authorities);

                Authentication auth = authManager.authenticate(authReq);
                SecurityContext sc = SecurityContextHolder.getContext();
                sc.setAuthentication(auth);

                service.triggerScheduledStudentSubmissionEvaluation(tenant.getId());
                log.info("Evaluation completed for tenant - {}", tenant.getId());
            }
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }
}
