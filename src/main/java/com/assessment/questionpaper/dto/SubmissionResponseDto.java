package com.assessment.questionpaper.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.assessment.questionpaper.entity.Metric;

import lombok.Data;

@Data
public class SubmissionResponseDto {

    private String submissionId;
    private String assignmentId;
    private String studentId;
    private String testId;
    private String testName;

    private boolean submitted;
    private Date submissionTime;
    private int attempt;    //should not > maxAttempts in test_assignment

    private List<SectionResponseDto> sections;
    private String evaluation;

    private SummaryResponseDto summary;

    private Date lastUpdatedOn;

	private int totalTestTimeTakenInSec;

    public void addSection(SectionResponseDto section){
        if (this.sections == null) this.sections = new ArrayList<>();
        sections.add(section);
    }

    @Data
    public static class SectionResponseDto{
        private String sectionId;
        private String sectionName;
        private List<AnswerResponseDto> answers;

        public void addAnswer(AnswerResponseDto answerResponseDto){
            if (this.answers == null) this.answers = new ArrayList<>();
            answers.add(answerResponseDto);
        }
    }

    @Data
    public static class AnswerResponseDto {
        private String questionId;

        /* enrich from question & assignment */
        private String name;
        private String type;
        private List<InputOption> inputOptions;
        private List<String> correctOptions;
        private String correctAnswerText;

        /* student response */
        private String answerText;
        private List<String> selectedOptions;
        private double markAllocated;
        private String answerStatus;
        private int timeElapsedInSec;
        private boolean markForReview;
        private String explanation;
    }

    @Data
    public static class InputOption {
        private String key;
        private String value;
    }

    @Data
    public static class SummaryResponseDto {
        private Metric metric;
        private List<SectionSummaryResponseDto> sections;
        private List<DifficultySummaryResponseDto> difficulty;
        private List<TopicSummaryResponseDto> topics;

        public void addSectionSummary(SectionSummaryResponseDto summary){
            if (this.sections == null) this.sections = new ArrayList<>();
            this.sections.add(summary);
        }

        public void addDifficultySummary(DifficultySummaryResponseDto summary){
            if (this.difficulty == null) this.difficulty = new ArrayList<>();
            this.difficulty.add(summary);
        }

        public void addTopicSummary(TopicSummaryResponseDto summary){
            if (this.topics == null) this.topics = new ArrayList<>();
            this.topics.add(summary);
        }
    }

    @Data
    public static class SectionSummaryResponseDto {
        private String sectionId;
        private String sectionName;
        private Metric metric;
    }

    @Data
    public static class DifficultySummaryResponseDto {
        private String difficultyLevel;
        private Metric metric;
    }

    @Data
    public static class TopicSummaryResponseDto {
        private String topic;
        private Metric metric;
    }
}
