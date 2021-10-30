package com.assessment.questionpaper.report.dto;

import java.util.List;

import lombok.Data;

@Data
public class StudentTestReportResponseDto {
	private String studentName;
	private String studentEmail;
	private String name;
	private Double markRecieved;
	private Double totalMark;
	private Double percentile;
	private List<SectionReport> sectionReports;

	@Data
	public static class SectionReport {
		private String sectionName;
		private Double markRecieved;
		private Double totalMark;
		private Double percentile;
	}
}
