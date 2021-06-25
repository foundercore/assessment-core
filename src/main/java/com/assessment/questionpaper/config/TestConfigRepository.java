package com.assessment.questionpaper.config;

import org.springframework.data.repository.CrudRepository;

import com.assessment.questionpaper.entity.QuestionPaper;
import com.assessment.questionpaper.entity.QuestionPaperId;

public interface TestConfigRepository extends CrudRepository<QuestionPaper, QuestionPaperId> {
}
