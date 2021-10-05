package com.assessment.questionpaper.report;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;

import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.assessment.common.XLWriter;
import com.assessment.questionpaper.report.dto.StudentTestAnalysisDto;
import com.assessment.questionpaper.report.dto.StudentTestAnalysisDto.StudentTestAnalysisResponseDto;
import com.assessment.questionpaper.report.dto.StudentTestReportResponseDto;

import lombok.extern.slf4j.Slf4j;

@Validated
@Slf4j
@RestController
@RequestMapping("/api/v1/test/report")
public class TestReportController {

	@Autowired
	TestReportService service;

	@GetMapping("/{assignment-id}/{batch-id}/students-batch-ranking")
	@PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
	public List<Map<String, Object>> getStudentBatchRankingReport(
			@NotBlank @PathVariable("assignment-id") String assignmentId,
			@NotBlank @PathVariable("batch-id") String batchId) {
		return service.getStudentBatchRankingReport(assignmentId, batchId);
	}

	@GetMapping("/submission/{assignment-id}/students-overall-ranking")
	@PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
	public List<Map<String, Object>> getStudentAssignmentRanking(
			@NotBlank @PathVariable("assignment-id") String assignmentId) {
		return service.getStudentAssignmentRanking(assignmentId);
	}

	// @GetMapping("/submission/{assignment-id}/students")
	// @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN',
	// 'ROLE_STAFF')")
	// public List<Map<String, Object>> getStudentAssignedToTest(@NotBlank
	// @PathVariable("assignment-id") String assignmentId) {
	// return service.getStudentAssignedToTest(assignmentId);
	// }

	@GetMapping("/{test-id}/students-test-report")
	@PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
	public List<StudentTestReportResponseDto> getStudentTestReport(@NotBlank @PathVariable("test-id") String testId) {
		return service.getStudentTestReport(testId);
	}

	@GetMapping("/{test-id}/students-test-report/export")
	@PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
	public void getStudentTestReportExport(@NotBlank @PathVariable("test-id") String testId,
			HttpServletResponse response) throws IOException {
		try {

			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", "attachment;filename=StudentTestReport-"
					+ new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".xlsx");
			ByteArrayInputStream stream = XLWriter.writeExcelToByteStream(service.getStudentTestReportExport(testId));
			IOUtils.copy(stream, response.getOutputStream());
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}

	}

	@PostMapping("/{test-id}/students-test-analysis")
	@PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF', 'ROLE_STUDENT')")
	public StudentTestAnalysisResponseDto getStudentTestAnalysisReport(
			@RequestBody StudentTestAnalysisDto studentTestAnalysisData) {
		return service.getStudentTestAnalysisReport(studentTestAnalysisData);
	}
}