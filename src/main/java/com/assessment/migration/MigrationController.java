package com.assessment.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.assessment.common.ConfigUtility;
import com.assessment.iam.entities.Tenant;
import com.assessment.iam.services.TenantService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Validated
@Slf4j
@RestController
@RequestMapping("/api/v1/migration")
public class MigrationController {

    @Autowired
    private MigrationService service;

    @Autowired
    private TenantService tenantService;

    @javax.annotation.Resource(name="authenticationManager")
    private AuthenticationManager authManager;

    @Transactional
    @PostMapping("/question")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public ResponseEntity<Resource> questionSchemaMigration(@RequestParam("file") MultipartFile file,
                                                            @RequestParam (required = false) Boolean removeHtmlContent) {
        try {
            if (removeHtmlContent == null){
                removeHtmlContent = Boolean.FALSE;
            }
            File outputFile = service.questionSchemaMigration(file, removeHtmlContent);
            Path path = Paths.get(outputFile.toURI());
            Resource resource = new UrlResource(path.toUri());
            return ResponseEntity.ok()
//                    .contentType(MediaType.parseMediaType("text/csv"))
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Transactional
    @PostMapping("/init-question-automation")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public Map<String, Object> intiQuestionAutomation(@RequestParam String inputLocation,
                                                      @RequestParam (required = false, defaultValue = "10") int parallel,
                                                      @RequestParam (required = false) Boolean removeHtmlContent) {
        try {
            if (removeHtmlContent == null){
                removeHtmlContent = Boolean.FALSE;
            }
            if (parallel <= 0 || parallel > 10) parallel = 10;

            String requestId = UUID.randomUUID().toString();
            String basePath = ConfigUtility.instance().getProperty("user.dir") + File.separator + "data" + File.separator
                    + "question-automation" + File.separator + requestId;
            String error = basePath + File.separator + "error";
            String output = basePath + File.separator + "output";
            String archive = basePath + File.separator + "archive";

            com.google.common.io.Files.createParentDirs(new File(error + File.separator + "tmp.log"));
            com.google.common.io.Files.createParentDirs(new File(output + File.separator + "tmp.log"));
            com.google.common.io.Files.createParentDirs(new File(archive + File.separator + "tmp.log"));

            List<Path> paths = Files.list(Paths.get(inputLocation)).collect(Collectors.toList());
            ThreadPoolExecutor executor = new ThreadPoolExecutor(parallel, parallel, 10, TimeUnit.DAYS, new ArrayBlockingQueue<>(10_000));
            Boolean finalRemoveHtmlContent = removeHtmlContent;
            paths.forEach(item -> executor.execute(()-> executeQuestionAutomation(item, finalRemoveHtmlContent, error, output, archive)));

            Map<String, Object> response = new HashMap<String, Object>(){{
                put("requestId", requestId);
                put("inputDir", inputLocation);
                put("outputDir", output);
                put("errorDir", error);
                put("fileCount", paths.size());
                put("removeHtmlContent", finalRemoveHtmlContent);
            }};
            return response;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private void executeQuestionAutomation(Path inputFile, boolean removeHtmlContent, String errorDir, String outputDir, String archiveDir){
        List<Tenant> tenants = tenantService.listAllTenants();
        for (Tenant tenant : tenants) {
            String internalTenant = ConfigUtility.instance().getProperty("app.system.tenant");
            if (tenant.getId().equalsIgnoreCase(internalTenant)) continue;
            login(tenant);

            service.intiAutomateQuestion(inputFile, removeHtmlContent, errorDir, outputDir, archiveDir);
        }
    }

    private void login(Tenant tenant) {
        log.info("Running Automation Question Migration tenant - {}", tenant.getId());
        String username = ConfigUtility.instance().getProperty("app.system-user-name");
        String password = ConfigUtility.instance().getProperty("app.system-user-password");
        String loginId = username + "@" + tenant.getId();
        UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(loginId, password);
        Authentication auth = authManager.authenticate(authReq);
        SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(auth);
    }
}
