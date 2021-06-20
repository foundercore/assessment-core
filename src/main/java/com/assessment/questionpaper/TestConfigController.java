package com.assessment.questionpaper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.assessment.question.Question;
import com.assessment.questionpaper.dto.*;
import com.assessment.questionpaper.entity.QuestionPaper;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Validated
@Slf4j
@RestController
@RequestMapping("/api/v1/test/config")
public class TestConfigController {

    @Autowired
    TestConfigService testConfigService;

    @Autowired
    TestAssignmentService testAssignmentService;

    @GetMapping("/status")
    public List<String> getQuestionPaperStatus(){
        return QuestionPaperStatus.getAllStatus();
    }

    @GetMapping("/subjects")
    public List<String> getQuestionPaperSubjects(){
        return testConfigService.getQuestionPaperSubjects();
    }

    @Transactional
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public Map<String, String> createQuestionPaper(@RequestBody @Valid QuestionPaperRequestDto questionPaperRequestDto){
        Map<String, String> response = new HashMap<>();
        String id = testConfigService.createQuestionPaper(questionPaperRequestDto);
        response.put("testId", id);
        return response;
    }

    @Transactional
    @DeleteMapping("/{test-id}/remove")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public void deleteQuestionPaper(@NotBlank @PathVariable("test-id") String paperId){
        if(!testAssignmentService.isAssignmentExistForTest(paperId)){
            testConfigService.deleteQuestionPaper(paperId);
        }else {
            testConfigService.softDeleteQuestionPaper(paperId);
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can not delete test. Linked assignments exists in system");
        }
    }

    @Transactional
    @PostMapping("/{test-id}/update-metadata")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public void updateQuestionPaperMetadata(@NotBlank @PathVariable("test-id") String paperId, @RequestBody QuestionPaperRequestDto questionPaperRequestDto){
        testConfigService.updateQuestionPaperMetadata(paperId, questionPaperRequestDto);
    }

    @Transactional
    @PostMapping("/{test-id}/section/add")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public Map<String, String> addQuestionPaperSection(@NotBlank @PathVariable("test-id") String paperId, @RequestBody @Valid QuestionPaperRequestDto.PaperSectionRequestDto paperSectionRequestDto){
        Map<String, String> response = new HashMap<>();
        String id = testConfigService.addQuestionPaperSection(paperId, paperSectionRequestDto);
        response.put("sectionId", id);
        return response;
    }

    @Transactional
    @PostMapping("/{test-id}/section/{section-id}/update-metadata")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public void updateQuestionPaperSectionMetadata(@NotBlank @PathVariable("test-id") String paperId,
                                                   @NotBlank @PathVariable("section-id") String sectionId,
                                                   @RequestBody @Valid QuestionPaperRequestDto.PaperSectionRequestDto paperSectionRequestDto){
        testConfigService.updateQuestionPaperSectionMetadata(paperId, sectionId, paperSectionRequestDto);
    }

    @Transactional
    @DeleteMapping("/{test-id}/section/{section-id}/remove")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public void removeQuestionPaperSection(@NotBlank @PathVariable("test-id") String paperId,
                                           @NotBlank @PathVariable("section-id") String sectionId){
        testConfigService.removeQuestionPaperSection(paperId, sectionId);
    }

    @Transactional
    @PostMapping("/{test-id}/section/{section-id}/question/add")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public void addNewQuestionsInQuestionPaperSection(@NotBlank @PathVariable("test-id") String paperId,
                                                      @NotBlank @PathVariable("section-id") String sectionId,
                                                      @RequestBody @Valid List<QuestionPaperRequestDto.TestQuestionRequestDto> questionDtos){
        testConfigService.addNewQuestionsInQuestionPaperSection(paperId, sectionId, questionDtos);
    }

    @Transactional
    @PostMapping("/{test-id}/section/{section-id}/question/remove")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public void removeQuestionsFromQuestionPaperSection(@NotBlank @PathVariable("test-id") String paperId,
                                                        @NotBlank @PathVariable("section-id") String sectionId,
                                                        @RequestBody @Valid List<String> questions){
        testConfigService.removeQuestionsFromQuestionPaperSection(paperId, sectionId, questions);
    }

    @Transactional
    @PostMapping("/{test-id}/section/{section-id}/question/update")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public void updateQuestionsInQuestionPaperSection(@NotBlank @PathVariable("test-id") String paperId,
                                                      @NotBlank @PathVariable("section-id") String sectionId,
                                                      @RequestBody @Valid List<QuestionPaperRequestDto.TestQuestionRequestDto> questionDtos) {
        testConfigService.updateQuestionsInQuestionPaperSection(paperId, sectionId, questionDtos);
    }

    @Transactional
    @PostMapping("/{test-id}/initiate-verification")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public void initiateQuestionPaperVerification(@NotBlank @PathVariable("test-id") String paperId){
        testConfigService.initiateQuestionPaperVerification(paperId);
    }

    @Transactional
    @PostMapping("/{test-id}/verify")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public void verifyQuestionPaper(@NotBlank @PathVariable("test-id") String paperId){
        testConfigService.verifyQuestionPaper(paperId);
    }

    @Transactional
    @PostMapping("/{test-id}/reject-verification")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public void rejectQuestionPaperVerification(@NotBlank @PathVariable("test-id") String paperId,
                                                @RequestBody @NotBlank String rejectionReason){
        testConfigService.rejectQuestionPaperVerification(paperId, rejectionReason);
    }

    @GetMapping("/pending-verification")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public List<QuestionPaperResponseDto> getQuestionPapersPendingToVerify(){
        return testConfigService.getQuestionPapersPendingToVerify();
    }

    @GetMapping("/{test-id}")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF', 'ROLE_STUDENT')")
    public QuestionPaperResponseDto getQuestionPaper(@NotBlank @PathVariable("test-id") String paperId){
        return testConfigService.getQuestionPaper(paperId);
    }

    @GetMapping("/{test-id}/linked-questions")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF', 'ROLE_STUDENT')")
    public List<Question> getQuestionPaperLinkedQuestions(@NotBlank @PathVariable("test-id") String paperId){
        return testConfigService.getQuestionPaperLinkedQuestions(paperId);
    }

    @GetMapping("/{test-id}/linked-questions/{question-id}")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF', 'ROLE_STUDENT')")
    public Question getQuestionPaperLinkedQuestion(@NotBlank @PathVariable("test-id") String paperId,
                                                   @NotBlank @PathVariable("question-id") String questionId){
        return testConfigService.getQuestionPaperLinkedQuestion(paperId, questionId);
    }

    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF', 'ROLE_STUDENT')")
    public QuestionPaperPaginatedResponse searchQuestionPapers(@RequestBody SearchQuestionPaperDto filter){
        return testConfigService.searchQuestionPapers(filter);
    }

    @Transactional
    @PostMapping("/{test-id}/archive")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public void archiveQuestionPaper(@NotBlank @PathVariable("test-id") String paperId){
        testConfigService.archiveQuestionPaper(paperId);
    }

    @Transactional
    @PostMapping("/{test-id}/publish")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public void publishQuestionPaper(@NotBlank @PathVariable("test-id") String paperId){
        testConfigService.publishQuestionPaper(paperId);
    }

    @Transactional
    @PostMapping("/{test-id}/update-control-params")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public void updateTestControlParams(@NotBlank @PathVariable("test-id") String paperId,
                                        @RequestBody QuestionPaper.TestControlParams controlParams){
        testConfigService.updateTestControlParams(paperId, controlParams);
    }
}
