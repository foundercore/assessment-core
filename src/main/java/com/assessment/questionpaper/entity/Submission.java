package com.assessment.questionpaper.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import com.assessment.questionpaper.dto.SubmissionResponseDto;

import lombok.Data;

@Data
@Document(collection = Submission.COLLECTION_NAME)
public class Submission {
    public static final String COLLECTION_NAME = "test_submission";

    @Id
    private SubmissionId id;

    private String assignmentId;
    private String studentId;

    private boolean submitted;
    private Date submissionTime;
    private int attempt;    //should not > maxAttempts in test_assignment

    private Map<String, Section> sections;
    private String evaluation;
	// private double totalMarks;
	// private double timeTakenInSec;

    private Summary summary;

    /* activity logs */
    @LastModifiedDate
    private Date lastUpdatedOn;

	private int totalTestTimeTakenInSec;

    public void addSection(Section section){
        if (this.sections == null) this.sections = new HashMap<>();
        sections.put(section.getSectionId(), section);
    }

    @Data
    public static class Section{
        private String sectionId;
//        private double totalMarks;
//        private double timeTakenInSec;
        private Map<String, Answer> answers;

        public void addAnswer(Answer answer){
            if (this.answers == null) this.answers = new HashMap<>();
            answers.put(answer.getQuestionId(), answer);
        }
    }

    @Data
    public static class Answer{
        private String questionId;
        private String answerText;
        private List<String> options;
        private double totalMark;
        private double markAllocated;
        private String answerStatus;    //correct, incorrect, skipped
        private int timeElapsedInSec;
        private boolean markForReview;

        public SubmissionResponseDto.AnswerResponseDto responseDto(){
            SubmissionResponseDto.AnswerResponseDto dto = new SubmissionResponseDto.AnswerResponseDto();
            dto.setQuestionId(this.questionId);
            dto.setAnswerText(this.answerText);
            dto.setSelectedOptions(this.options);
            dto.setTimeElapsedInSec(this.timeElapsedInSec);
            dto.setMarkForReview(this.markForReview);
            dto.setMarkAllocated(this.markAllocated);
            dto.setAnswerStatus(this.answerStatus);
            return dto;
        }
    }

    @Data
    public static class Summary {
        private Metric metric;
        List<SectionSummary> sections;
        List<DifficultySummary> difficulty;
        List<TopicSummary> topics;

        public void addSectionSummary(SectionSummary summary){
            if (this.sections == null) this.sections = new ArrayList<>();
            this.sections.add(summary);
        }
        public void addDifficultySummary(DifficultySummary summary){
            if (this.difficulty == null) this.difficulty = new ArrayList<>();
            this.difficulty.add(summary);
        }

        public void addTopicSummary(TopicSummary summary){
            if (this.topics == null) this.topics = new ArrayList<>();
            this.topics.add(summary);
        }

        public boolean containSectionSummary(String key){
            if (this.sections == null) return false;
            for (SectionSummary summary: sections){
                if (key.equalsIgnoreCase(summary.getSectionId())) return true;
            }
            return false;
        }

        public boolean containDifficultySummary(String key){
            if (this.difficulty == null) return false;
            for (DifficultySummary summary: difficulty){
                if (key.equalsIgnoreCase(summary.getDifficultyLevel())) return true;
            }
            return false;
        }

        public boolean containTopicSummary(String key){
            if (this.topics == null) return false;
            for (TopicSummary summary: topics){
                if (key.equalsIgnoreCase(summary.getTopic())) return true;
            }
            return false;
        }

        public SectionSummary getSectionSummaryById(String key){
            if (this.sections == null) return null;
            for (SectionSummary summary: sections){
                if (key.equalsIgnoreCase(summary.getSectionId())) {
                    return summary;
                }
            }
            return null;
        }

        public DifficultySummary getDifficultySummaryById(String key){
            if (this.difficulty == null) return null;
            for (DifficultySummary summary: difficulty){
                if (key.equalsIgnoreCase(summary.getDifficultyLevel())) {
                    return summary;
                }
            }
            return null;
        }

        public TopicSummary getTopicSummaryById(String key){
            if (this.topics == null) return null;
            for (TopicSummary summary: topics){
                if (key.equalsIgnoreCase(summary.getTopic())) {
                    return summary;
                }
            }
            return null;
        }

        public SubmissionResponseDto.SummaryResponseDto responseDto(){
            SubmissionResponseDto.SummaryResponseDto dto = new SubmissionResponseDto.SummaryResponseDto();
            dto.setMetric(this.metric);

            if (this.sections != null) {
                for (SectionSummary entry : this.sections) {
                    SubmissionResponseDto.SectionSummaryResponseDto section = new SubmissionResponseDto.SectionSummaryResponseDto();
                    section.setSectionId(entry.getSectionId());
                    section.setMetric(entry.getMetric());
                    dto.addSectionSummary(section);
                }
            }
            if (this.difficulty != null){
                for (DifficultySummary entry: this.difficulty){
                    SubmissionResponseDto.DifficultySummaryResponseDto difficultyDto = new SubmissionResponseDto.DifficultySummaryResponseDto();
                    difficultyDto.setDifficultyLevel(entry.getDifficultyLevel());
                    difficultyDto.setMetric(entry.getMetric());
                    dto.addDifficultySummary(difficultyDto);
                }
            }
            if (this.topics != null){
                for (TopicSummary entry: this.topics){
                    SubmissionResponseDto.TopicSummaryResponseDto topicDto = new SubmissionResponseDto.TopicSummaryResponseDto();
                    topicDto.setTopic(entry.getTopic());
                    topicDto.setMetric(entry.getMetric());
                    dto.addTopicSummary(topicDto);
                }
            }
            return dto;
        }
    }

    @Data
    public static class SectionSummary {
        private String sectionId;
        private Metric metric;
    }

    @Data
    public static class DifficultySummary {
        private String difficultyLevel;
        private Metric metric;
    }

    @Data
    public static class TopicSummary {
        private String topic;
        private Metric metric;
    }
}
