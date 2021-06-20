package com.assessment.migration;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class MigrationSection {
    private String id;
    private String name;
    private String calculator;
    private int duration;
    private boolean isContinuousQuestionNo;
    List<MigrationQuestion> questions;
}
