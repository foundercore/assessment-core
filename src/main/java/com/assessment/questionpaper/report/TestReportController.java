package com.assessment.questionpaper.report;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

@Validated
@Slf4j
@RestController
@RequestMapping("/api/v1/test/report")
public class TestReportController {

    @Autowired
    TestReportService service;

    @GetMapping("/{assignment-id}/{batch-id}/students-batch-ranking")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public List<Map<String, Object>> getStudentBatchRankingReport(@NotBlank @PathVariable("assignment-id") String assignmentId,
                                                                  @NotBlank @PathVariable("batch-id") String batchId) {
        return service.getStudentBatchRankingReport(assignmentId, batchId);
    }

    @GetMapping("/submission/{assignment-id}/students-overall-ranking")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public List<Map<String, Object>> getStudentAssignmentRanking(@NotBlank @PathVariable("assignment-id") String assignmentId) {
        return service.getStudentAssignmentRanking(assignmentId);
    }

//    @GetMapping("/submission/{assignment-id}/students")
//    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
//    public List<Map<String, Object>> getStudentAssignedToTest(@NotBlank @PathVariable("assignment-id") String assignmentId) {
//        return service.getStudentAssignedToTest(assignmentId);
//    }
}