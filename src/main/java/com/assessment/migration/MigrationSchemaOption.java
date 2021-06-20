package com.assessment.migration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MigrationSchemaOption {
    private String id;
    private String option;
    private boolean correct;
}
