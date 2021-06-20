package com.assessment.question;

import com.opencsv.exceptions.CsvValidationException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Validated
public interface QuestionService {

    void linkQuestionsToPassage(String passageId, List<String> questionIds);

    void unlinkQuestionsToPassage(String passageId, List<String> questionIds);

    String generatePassageId(String passageContent, boolean suppressException);

    String createPassage(Passage passage);

    void updatePassage(String passageId, Passage passage);

    void bulkCreateQuestions(MultipartFile file) throws IOException, CsvValidationException;

    void initBulkCreationQuestion(String fileName) throws IOException, CsvValidationException;

    boolean exists(String id);

    Question getQuestion(String questionId, boolean loadPassageContent);

    void updateQuestion(Question question);

//    List<Question> getQuestionsByTag(String tag);
//
//    List<Question> getQuestionsByFileName(String fileName);

    List<String> getQuestionTags();

    void validateQuestion(Question question);

    String createQuestion(Question question);

    QuestionPaginatedResponse searchQuestions(SearchQuestion filter);

    void unmarkQuestionUsage(String questionPaperId, List<String> questions);

    void markQuestionUsage(String paperId, String status, List<String> questions);

    void deleteQuestion(String questionId);

    Map<String,?> bulkDeleteQuestions(List<String> questionIds);

    List<String> getQuestionSubjects();

    List<String> getQuestionTopics();

    List<String> getQuestionSubTopics();

    void updateQuestionTags(String questionId, List<String> tags);
}
