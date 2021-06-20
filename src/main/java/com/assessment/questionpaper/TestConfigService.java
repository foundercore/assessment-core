package com.assessment.questionpaper;

import org.springframework.validation.annotation.Validated;

import com.assessment.question.Question;
import com.assessment.questionpaper.dto.QuestionPaperPaginatedResponse;
import com.assessment.questionpaper.dto.QuestionPaperRequestDto;
import com.assessment.questionpaper.dto.QuestionPaperResponseDto;
import com.assessment.questionpaper.dto.SearchQuestionPaperDto;
import com.assessment.questionpaper.entity.QuestionPaper;

import java.util.List;

@Validated
public interface TestConfigService {

    String createQuestionPaper(QuestionPaperRequestDto questionPaperRequestDto);

    boolean exists(String paperId);

    QuestionPaper getEntity(String paperId);

    void deleteQuestionPaper(String paperId);

    void softDeleteQuestionPaper(String paperId);

    void updateQuestionPaperMetadata(String paperId, QuestionPaperRequestDto questionPaperRequestDto);

    String addQuestionPaperSection(String paperId, QuestionPaperRequestDto.PaperSectionRequestDto paperSectionRequestDto);

    void updateQuestionPaperSectionMetadata(String paperId, String sectionId, QuestionPaperRequestDto.PaperSectionRequestDto paperSectionRequestDto);

    void removeQuestionPaperSection(String paperId, String sectionId);

    void addNewQuestionsInQuestionPaperSection(String paperId, String sectionId, List<QuestionPaperRequestDto.TestQuestionRequestDto> questionDtos);

    void removeQuestionsFromQuestionPaperSection(String paperId, String sectionId, List<String> questions);

    void updateQuestionsInQuestionPaperSection(String paperId, String sectionId, List<QuestionPaperRequestDto.TestQuestionRequestDto> questionDtos);

    void initiateQuestionPaperVerification(String paperId);

    void verifyQuestionPaper(String paperId);

    void rejectQuestionPaperVerification(String paperId, String rejectionReason);

    List<QuestionPaperResponseDto> getQuestionPapersPendingToVerify();

    QuestionPaperResponseDto getQuestionPaper(String paperId);

    QuestionPaperPaginatedResponse searchQuestionPapers(SearchQuestionPaperDto filter);

    void archiveQuestionPaper(String paperId);

    List<Question> getQuestionPaperLinkedQuestions(String paperId);

    Question getQuestionPaperLinkedQuestion(String paperId, String questionId);

    void publishQuestionPaper(String paperId);

    List<String> getQuestionPaperSubjects();

    void updateTestControlParams(String paperId, QuestionPaper.TestControlParams controlParams);
}
