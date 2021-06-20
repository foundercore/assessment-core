package com.assessment.question;

import lombok.Data;

import java.util.List;

@Data
public class QuestionPaginatedResponse {
    private List<Question> questions;
    private int pageSize;
    private int pageNumber;
    private long totalRecords;
//    private String paginatedRowId;
}
