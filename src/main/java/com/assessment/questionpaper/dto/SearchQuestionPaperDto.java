package com.assessment.questionpaper.dto;

import lombok.Data;

import java.util.List;

@Data
public class SearchQuestionPaperDto {
    /* filters */
    private String questionPaperId;
    private String status;
    private String subject;
    private List<String> tags;
    private String updateStartTime, updateEndTime;  // YYYY-MM-DD HH:MM:SS
    private String nameRegexPattern;

    /* sorting details */
    private String sortColumn;
    private String sortOrder;

    /* pagination specific */
    private boolean next = true;
    private int pageSize;
    private int pageNumber;
//    private String paginatedRowId;
}
