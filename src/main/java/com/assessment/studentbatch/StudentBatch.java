package com.assessment.studentbatch;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.assessment.common.validations.ValidDisplayName;

import javax.validation.constraints.Size;
import java.util.List;

@Data
@Document(collection = StudentBatch.COLLECTION_NAME)
public class StudentBatch {
    public static final String COLLECTION_NAME = "student_batch";

    @Id
    private StudentBatchId id;

    @ValidDisplayName
    private String name;

    @Size(max = 500)
    private String description;

    private List<String> students;
}
