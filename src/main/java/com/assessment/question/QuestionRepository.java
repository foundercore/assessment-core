package com.assessment.question;

import org.springframework.data.repository.CrudRepository;

public interface QuestionRepository extends CrudRepository<Question, QuestionId> {
}
