package com.assessment.studentbatch;

import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface StudentBatchService {

    void addStudentsInBatch(StudentBatchId id, List<String> emails);

    void removeStudentsFromBatch(StudentBatchId id, List<String> emails);

    List<StudentBatch> userAssociatedBatches(String emailId);

    StudentBatch getStudentBatch(String batchId);
}
