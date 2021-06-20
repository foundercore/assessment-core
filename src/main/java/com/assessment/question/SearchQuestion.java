package com.assessment.question;

import lombok.Data;

import java.util.List;

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

    /* pagination specific */
    private boolean next = true;
    private int pageSize;
    private int pageNumber;
//    private String paginatedRowId;
}
