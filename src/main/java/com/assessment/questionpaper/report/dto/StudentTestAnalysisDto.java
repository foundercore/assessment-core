package com.assessment.questionpaper.report.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class StudentTestAnalysisDto {
	private String testId;
	private Double testMark;
	private Map<String, Double> sectionLevelMark;

	@Data
	public static class StudentTestAnalysisResponseDto {
		private String testId;
		private Double testPercentile;
		private List<SectionAnalysisResponseDto> sectionLevelPercentile = new ArrayList<SectionAnalysisResponseDto>();
		private List<String> instituesSelectedIn = new ArrayList<>();
	}

	@Data
	public static class SectionAnalysisResponseDto {
		private String sectionId;
		private String sectionName;
		private Double sectionPercentile;
	}

}

