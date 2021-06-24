package com.assessment.questionpaper;

import org.springframework.validation.annotation.Validated;

import com.assessment.questionpaper.dto.AssignmentRequestDto;
import com.assessment.questionpaper.dto.AssignmentResponseDto;
import com.assessment.questionpaper.dto.SaveAnswerRequestDto;
import com.assessment.questionpaper.dto.SubmissionResponseDto;

import java.util.List;

@Validated
public interface TestAssignmentService {

    String assignTest(AssignmentRequestDto assignmentDto);

    boolean exists(String assignmentId);

    boolean isAssignmentExistForTest(String testId);

    void deleteAssignment(String assignmentId);

    void updateAssignment(String assignmentId, AssignmentRequestDto dto);

    AssignmentResponseDto getAssignment(String assignmentId);

    List<AssignmentResponseDto> listAssignmentsByEmail(String emailId);

    List<AssignmentResponseDto> listMyAssignments();

    boolean isValidAssignmentPasscode(String assignmentId, String username, String passcode);

    void saveStudentAnswer(String assignmentId, SaveAnswerRequestDto requestDto);

    void clearStudentAnswer(String assignmentId, SaveAnswerRequestDto requestDto);

    void markStudentAnswerForReview(String assignmentId, SaveAnswerRequestDto requestDto);

    void submitStudentAssignment(String assignmentId, String username);

    SubmissionResponseDto getStudentAssignmentSubmissionState(String assignmentId, String username);

    void deleteStudentAssignmentSubmission(String assignmentId, String username);

    List<AssignmentResponseDto> listStudentAssignmentsByUsername(String username);

    List<SubmissionResponseDto> listMySubmissions();

    void initiateAssignmentEvaluation(String assignmentId);

    void initiateStudentAssignmentEvaluation(String assignmentId, String username, Boolean forceEvaluation);

    SubmissionResponseDto viewStudentAssignmentResult(String assignmentId, String username);

    List<AssignmentResponseDto> listStudentAssignmentsByTest(String testId);

    void triggerScheduledStudentSubmissionEvaluation(String tenantId);
}
