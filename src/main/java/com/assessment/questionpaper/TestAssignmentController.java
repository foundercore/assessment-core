package com.assessment.questionpaper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.assessment.iam.commons.AuthUtils;
import com.assessment.questionpaper.dto.AssignmentRequestDto;
import com.assessment.questionpaper.dto.AssignmentResponseDto;
import com.assessment.questionpaper.dto.SaveAnswerRequestDto;
import com.assessment.questionpaper.dto.SubmissionResponseDto;

import lombok.extern.slf4j.Slf4j;

@Validated
@Slf4j
@RestController
@RequestMapping("/api/v1/test/assignment")
public class TestAssignmentController {

    @Autowired
    TestAssignmentService service;

    @Autowired
    private Validator validator;

//    @GetMapping("/entities")
//    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
//    public List<String> getAssignmentEntities(){ //TODO implement
//        return AssignedEntityType.getAllEntities();
//    }

    @Transactional
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public Map<String, String> assignTest(@RequestBody AssignmentRequestDto assignmentDto){
        Set<ConstraintViolation<AssignmentRequestDto>> violations = validator.validate(assignmentDto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        Map<String, String> response = new HashMap<>();
        String id = service.assignTest(assignmentDto);
        response.put("assignmentId", id);
        return response;
    }

    @Transactional
    @DeleteMapping("/{assignment-id}/remove")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public void deleteAssignment(@NotBlank @PathVariable("assignment-id") String assignmentId){
        service.deleteAssignment(assignmentId);
    }

    @Transactional
    @PostMapping("/{assignment-id}/update")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public void updateAssignment(@NotBlank @PathVariable("assignment-id") String assignmentId,
                                 @RequestBody AssignmentRequestDto assignmentDto){
        Set<ConstraintViolation<AssignmentRequestDto>> violations = validator.validate(assignmentDto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        service.updateAssignment(assignmentId, assignmentDto);
    }

    @GetMapping("/{assignment-id}")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF', 'ROLE_STUDENT')")
    public AssignmentResponseDto getAssignment(@NotBlank @PathVariable("assignment-id") String assignmentId){
        return service.getAssignment(assignmentId);
    }

    @GetMapping("/by-test/{test-id}/list")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public List<AssignmentResponseDto> listStudentAssignmentsByTest(@NotBlank @PathVariable("test-id") String testId){
        return service.listStudentAssignmentsByTest(testId);
    }

    /**
     * List user assignments (individual & batch)
     * @param emailId is student email-id
     * @return List<Assignment> assignments
     */
    @GetMapping("/by-email/{email-id}/list")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public List<AssignmentResponseDto> listAssignmentsByEmail(@NotBlank @PathVariable("email-id") String emailId){
        return service.listAssignmentsByEmail(emailId);
    }

    @GetMapping("/by-name/{username}/list")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public List<AssignmentResponseDto> listStudentAssignmentsByUsername(@NotBlank @PathVariable("username") String username){
        return service.listStudentAssignmentsByUsername(username);
    }

    @GetMapping("/my-assignments")
	@PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF', 'ROLE_STUDENT')")
    public List<AssignmentResponseDto> listMyAssignments(){
        return service.listMyAssignments();
    }

    @Transactional
    @PostMapping("/{assignment-id}/is-valid-passcode")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT')")
    public Map<String, Boolean> isValidAssignmentPasscode(@NotBlank @PathVariable("assignment-id") String assignmentId,
                                                          @RequestBody @Valid String passcode){
        Map<String, Boolean> response = new HashMap<>();
        String username = AuthUtils.getCurrentQualifiedUsername();
        boolean isValid = service.isValidAssignmentPasscode(assignmentId, username, passcode);
        response.put("isValid", isValid);
        return response;
    }

    @Transactional
    @PostMapping("/submission/{assignment-id}/save-answer")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT')")
    public void saveStudentAnswer(@NotBlank @PathVariable("assignment-id") String assignmentId,
                                  @RequestBody SaveAnswerRequestDto requestDto){
        service.saveStudentAnswer(assignmentId, requestDto);
    }

    @Transactional
    @PostMapping("/submission/{assignment-id}/clear-answer")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT')")
    public void clearStudentAnswer(@NotBlank @PathVariable("assignment-id") String assignmentId,
                                   @RequestBody SaveAnswerRequestDto requestDto){
        service.clearStudentAnswer(assignmentId, requestDto);
    }

    @Transactional
    @PostMapping("/submission/{assignment-id}/mark-for-review")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT')")
    public void markStudentAnswerForReview(@NotBlank @PathVariable("assignment-id") String assignmentId,
                                           @RequestBody SaveAnswerRequestDto requestDto){
        service.markStudentAnswerForReview(assignmentId, requestDto);
    }

    @Transactional
    @PostMapping("/submission/{assignment-id}/submit")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT')")
    public void submitStudentAssignment(@NotBlank @PathVariable("assignment-id") String assignmentId){
        String username = AuthUtils.getCurrentQualifiedUsername();
        service.submitStudentAssignment(assignmentId, username);
    }

    @Transactional
    @DeleteMapping("/submission/{assignment-id}/{username}/remove")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT')")
    public void deleteStudentAssignmentSubmission(@NotBlank @PathVariable("assignment-id") String assignmentId,
                                                  @NotBlank @PathVariable("username") String username){
        service.deleteStudentAssignmentSubmission(assignmentId, username);
    }

    @GetMapping("/submission/{assignment-id}/{username}/submission-state")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF', 'ROLE_STUDENT')")
    public SubmissionResponseDto getStudentAssignmentSubmissionState(@NotBlank @PathVariable("assignment-id") String assignmentId,
                                                                     @NotBlank @PathVariable("username") String username){
        return service.getStudentAssignmentSubmissionState(assignmentId, username);
    }

    @GetMapping("/my-submissions")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT')")
    public List<SubmissionResponseDto> listMySubmissions(){
        return service.listMySubmissions();
    }

    @PostMapping("/submission/{assignment-id}/init-evaluation")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public void initiateAssignmentEvaluation(@NotBlank @PathVariable("assignment-id") String assignmentId){
        service.initiateAssignmentEvaluation(assignmentId);
    }

    @PostMapping("/submission/{assignment-id}/{username}/init-evaluation")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public void initiateStudentAssignmentEvaluation(@NotBlank @PathVariable("assignment-id") String assignmentId,
                                                    @NotBlank @PathVariable("username") String username,
                                                    @RequestParam (required = false) Boolean forceEvaluation) {
        if (forceEvaluation == null){
            forceEvaluation = Boolean.FALSE;
        }
        service.initiateStudentAssignmentEvaluation(assignmentId, username, forceEvaluation);
    }

    @GetMapping("/submission/{assignment-id}/{username}/view-assignment-result")
    @PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public SubmissionResponseDto viewStudentAssignmentResult(@NotBlank @PathVariable("assignment-id") String assignmentId,
                                                             @NotBlank @PathVariable("username") String username){
        return service.viewStudentAssignmentResult(assignmentId, username);
    }

    @GetMapping("/submission/{assignment-id}/my-assignment-result")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT')")
    public SubmissionResponseDto myAssignmentResult(@NotBlank @PathVariable("assignment-id") String assignmentId){
        String username = AuthUtils.getCurrentQualifiedUsername();
        return service.viewStudentAssignmentResult(assignmentId, username);
    }
}
