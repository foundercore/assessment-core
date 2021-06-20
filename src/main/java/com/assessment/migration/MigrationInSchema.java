package com.assessment.migration;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class MigrationInSchema {

    private String id;
    private String name;
    private boolean isSectionalTimer;
    private String instruction;
    List<MigrationSection> sections;
}
