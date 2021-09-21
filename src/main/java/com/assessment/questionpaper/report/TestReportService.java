package com.assessment.questionpaper.report;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotBlank;

import org.springframework.validation.annotation.Validated;

import com.assessment.questionpaper.report.dto.StudentTestAnalysisDto;
import com.assessment.questionpaper.report.dto.StudentTestAnalysisDto.StudentTestAnalysisResponseDto;
import com.assessment.questionpaper.report.dto.StudentTestReportResponseDto;

@Validated
public interface TestReportService {

    List<Map<String, Object>> getStudentBatchRankingReport(String assignmentId, String batchId);

    List<Map<String, Object>> getStudentAssignmentRanking(String assignmentId);

    List<Map<String, Object>> getStudentAssignedToTest(String assignmentId);

	List<StudentTestReportResponseDto> getStudentTestReport(@NotBlank String testId);

	StudentTestAnalysisResponseDto getStudentTestAnalysisReport(StudentTestAnalysisDto studentTestAnalysisData);
}
