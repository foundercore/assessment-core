package com.assessment.question.dto;

import java.util.List;

import lombok.Data;

@Data
public class SearchQuestion {

	/* filters */
	private String questionId;
	private String type;
	private String subject;
	private List<String> tags;
	private String updateStartTime, updateEndTime;  // YYYY-MM-DD HH:MM:SS
	private String nameRegexPattern;
	private String filename;
	private String topic;
	private String subTopic;

	/* migration fields */
	private String migrationTestId;
	private String migrationTestName;
	private String migrationSectionId;
	private String migrationSectionName;

	/* sorting details */
	private String sortColumn;
	private String sortOrder;

	/* Exclude question from test */
	private String testIdToBeExcluded;

	/* Include details of which all tests this question is used in */
	private boolean includeUsedTestDetails;

	/* pagination specific */
	private boolean next = true;
	private int pageSize;
	private int pageNumber;
	// private String paginatedRowId;
}
