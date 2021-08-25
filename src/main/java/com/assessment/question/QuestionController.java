package com.assessment.question;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.constraints.NotEmpty;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.assessment.iam.commons.AuthUtils;
import com.opencsv.exceptions.CsvValidationException;

import lombok.extern.slf4j.Slf4j;

@Validated
@Slf4j
@RestController
@RequestMapping("/api/v1/question")
public class QuestionController {

    @Autowired
    private Validator validator;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    PassageRepository passageRepository;

    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    QuestionService questionService;


    @Transactional
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public Map<String, String> createQuestion(@RequestBody Question question){
        Map<String, String> response = new HashMap<>();
        String id = questionService.createQuestion(question);
        response.put("questionId", id);
        return response;
    }

    @Transactional
    @PostMapping("/bulk-upload")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public void createQuestions(@RequestParam("file") MultipartFile file) {
        try {
            questionService.bulkCreateQuestions(file);
        } catch (IOException | CsvValidationException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

	@Transactional
	@PostMapping("/bulk-metadata-update")
	@PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN')")
	public void metadataQuestionUpdate(@RequestParam("file") MultipartFile file) {
		try {
			questionService.metadataQuestionBulkUpdate(file);
		} catch (IOException | CsvValidationException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

    @GetMapping("/{question-id}")
    public Question getQuestion(@PathVariable("question-id") String questionId){
        QuestionId id = new QuestionId();
        id.setQuestionId(questionId);
        id.setTenantId(AuthUtils.getCurrentTenantId());

        Question question =  questionRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Question with id %s not found", questionId)));
        /* set passage content */
        if (StringUtils.isNotEmpty(question.getPassageId())){
            PassageId pid = new PassageId();
            pid.setTenantId(question.getId().getTenantId());
            pid.setPassageId(question.getPassageId());
            passageRepository.findById(pid).ifPresent(passage -> question.setPassageContent(passage.getContent()));
        }
        return question;
    }

    @Transactional
    @PostMapping("/{question-id}/update")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public void updateQuestion(@PathVariable("question-id") String questionId, @RequestBody Question question){
        QuestionId id = new QuestionId();
        id.setQuestionId(questionId);
        id.setTenantId(AuthUtils.getCurrentTenantId());
        question.setId(id);
        question.setUsedInPapers(new HashMap<>());

        Set<ConstraintViolation<Question>> violations = validator.validate(question);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        questionService.updateQuestion(question);
    }

    @Transactional
    @PutMapping("/{question-id}/update-tags")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public void updateQuestionTags(@PathVariable("question-id") String questionId, @RequestBody @NotEmpty List<String> tags){
        questionService.updateQuestionTags(questionId, tags);
    }

    @Transactional
    @DeleteMapping("/{question-id}/remove")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public void deleteQuestion(@PathVariable("question-id") String questionId){
        questionService.deleteQuestion(questionId);
    }

    @Transactional
    @PostMapping("/bulk-remove")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public Map<String, ?> bulkDeleteQuestions(@RequestBody List<String> questionIds){
         return questionService.bulkDeleteQuestions(questionIds);
    }

//    @PostMapping("/by-tag")
//    public List<Question> getQuestionsByTag(@NotBlank @RequestBody String tag){
//        return questionService.getQuestionsByTag(tag);
//    }
//
//    @PostMapping("/by-filename")
//    public List<Question> getQuestionsByFileName(@NotBlank @RequestBody String fileName){
//        return questionService.getQuestionsByFileName(fileName);
//    }

    @GetMapping("/tags")
    public List<String> getQuestionTags(){
        return questionService.getQuestionTags();
    }

    @GetMapping("/subjects")
    public List<String> getQuestionSubjects(){
        return questionService.getQuestionSubjects();
    }

    @GetMapping("/topics")
    public List<String> getQuestionTopics(){
        return questionService.getQuestionTopics();
    }

    @GetMapping("/sub-topics")
    public List<String> getQuestionSubTopics(){
        return questionService.getQuestionSubTopics();
    }

    @GetMapping("/types")
    public List<String> getConfiguredQuestionTypes(){
        return QuestionType.getQuestionTypes();
    }

    @PostMapping("/search")
    public QuestionPaginatedResponse searchQuestions(@RequestBody SearchQuestion filter){
        return questionService.searchQuestions(filter);
    }
}
