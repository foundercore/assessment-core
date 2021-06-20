package com.assessment.questionpaper.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuestionPaperPaginatedResponse {
    private List<QuestionPaperResponseDto> tests;
    private int pageSize;
    private int pageNumber;
    private long totalRecords;
//    private String paginatedRowId;
}
