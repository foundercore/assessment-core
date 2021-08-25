package com.assessment.questionpaper.report;

import org.springframework.data.repository.CrudRepository;

import com.assessment.questionpaper.entity.Submission;
import com.assessment.questionpaper.entity.SubmissionId;

public interface TestSubmissionRepository extends CrudRepository<Submission, SubmissionId> {
}
