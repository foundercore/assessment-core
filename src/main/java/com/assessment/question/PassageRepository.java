package com.assessment.question;

import org.springframework.data.repository.CrudRepository;

import com.assessment.question.dto.Passage;
import com.assessment.question.dto.PassageId;

public interface PassageRepository extends CrudRepository<Passage, PassageId> {}
