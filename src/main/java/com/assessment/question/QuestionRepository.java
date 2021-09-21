package com.assessment.question;

import org.springframework.data.repository.CrudRepository;

import com.assessment.question.dto.Question;
import com.assessment.question.dto.QuestionId;

public interface QuestionRepository extends CrudRepository<Question, QuestionId> {
}
