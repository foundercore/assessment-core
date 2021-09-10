package com.assessment.questionpaper.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import lombok.Data;

@Data
public class InstituteAnalysisMetadata {
	private String testId;
	private String testName;
	private String fileName;
	private List<InstituteData> instituteData;
	@CreatedBy
	private String createdBy;
	@CreatedDate
	private Date createdOn;
	@LastModifiedBy
	private String lastUpdatedBy;
	@LastModifiedDate
	private Date lastUpdatedOn;

	@Data
	public static class InstituteData {
		private String instituteName;
		/*
		 * Test level Percentile score
		 */
		private Double testLevelPercentile;
		/*
		 * Test level Percentile score card
		 * Key -> Section Name
		 * Value -> Section Level Percentile
		 */
		private Map<String, Double> sectionLevelPercentile = new HashMap<String, Double>();
	}
}