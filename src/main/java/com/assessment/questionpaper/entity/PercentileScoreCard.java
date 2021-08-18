package com.assessment.questionpaper.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import lombok.Data;

@Data
public class PercentileScoreCard {
	private String testId;
	private String testName;
	private String fileName;
	/*
	 * Test level Percentile score card
	 * Key -> Marks Received
	 * Value -> Corresponding Percentile
	 */
	private Map<Double, Double> testLevelPercentile = new HashMap<Double, Double>();
	/*
	 * Test level Percentile score card
	 * Key -> Section Name
	 * Value -> Key -> Marks Received in Section
	 * ******** Value -> Corresponding Section Percentile
	 */
	private Map<String, Map<Double, Double>> sectionLevelPercentile = new HashMap<String, Map<Double, Double>>();
	private String createdBy;
	private Date createdOn;
	@LastModifiedBy
	private String lastUpdatedBy;
	@LastModifiedDate
	private Date lastUpdatedOn;

}