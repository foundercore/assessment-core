package com.assessment.questionpaper.report;

import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

@Validated
public interface TestReportService {

    List<Map<String, Object>> getStudentBatchRankingReport(String assignmentId, String batchId);

    List<Map<String, Object>> getStudentAssignmentRanking(String assignmentId);

    List<Map<String, Object>> getStudentAssignedToTest(String assignmentId);
}
