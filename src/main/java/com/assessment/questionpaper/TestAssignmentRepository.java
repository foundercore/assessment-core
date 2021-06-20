package com.assessment.questionpaper;

import org.springframework.data.repository.CrudRepository;

import com.assessment.questionpaper.entity.Assignment;
import com.assessment.questionpaper.entity.AssignmentId;

public interface TestAssignmentRepository extends CrudRepository<Assignment, AssignmentId> {
}
