package com.assessment.questionpaper.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.assessment.questionpaper.entity.InstituteAnalysisMetadata;
import com.assessment.questionpaper.entity.PercentileScoreCard;

import lombok.Data;

@Data
public class QuestionPaperResponseDto {
//    private String tenantId;
    private String questionPaperId;

    private String name;
    private String type;
    private String subject;
    private int totalDurationInMinutes;
    private int minimumDurationInMinutes;
    private String instructions;
    private int totalMarks;
    private String status;

    List<PaperSectionResponseDto> sections;
    List<String> sectionOrder;

    private List<String> tags;

    /* activity logs */
    private String createdBy;
    private Date createdOn;
    private String lastUpdatedBy;
    private Date lastUpdatedOn;

    /* migration */
    List<PaperMigrationResponseDto> migration;

    /* test control params */
    TestControlParamsResponseDto controlParam;

	private boolean calculatorRequired;

    public void addTags(String fileName) {
        if (this.tags == null) this.tags = new ArrayList<>();
        this.tags.add(fileName);
    }

    public void addSection(PaperSectionResponseDto section) {
        if (this.sections == null) this.sections = new ArrayList<>();
        this.sections.add(section);
    }

    public void addMigration(PaperMigrationResponseDto migration) {
        if (this.migration == null) this.migration = new ArrayList<>();
        this.migration.add(migration);
    }

    @Data
    public static class PaperSectionResponseDto {
        private String id;
        private String name;
        private int durationInMinutes;
        private String difficultyLevel;
        private String instructions;
        private String parentSection;
        private int totalQuestions;
        private List<TestQuestionResponseDto> questions;

        /* nested subsections */
        List<PaperSectionResponseDto> subsections;
        List<String> subSectionOrder;

        public void addQuestion(TestQuestionResponseDto question) {
            if (this.questions == null) this.questions = new ArrayList<>();
            this.questions.add(question);
        }

        public void updateQuestionCount(){
            this.totalQuestions = this.questions == null ? 0: this.questions.size();
        }
    }

    @Data
    public static class TestQuestionResponseDto {
        private String id;
        private String name;
		private String passageContent;
        private String type;
        private List<String> tags;
        private double positiveMark = 0;
        private double negativeMark = 0;
        private double skipMark = 0;
		private int sequenceNumber = 0;
    }

    @Data
    public static class PaperMigrationResponseDto {
        private String status;
        private String activityBy;
        private Date activityOn;
        private String remarks;
    }

    @Data
    public static class TestControlParamsResponseDto {
		private boolean doNotShowReport;
		private boolean percentile;
		private boolean allowCalculator;
		private boolean shuffleQuestions;
		private boolean sectionalTest;
		private boolean allowInstituteAnalysis;
		private boolean viewTestResult;
		private PercentileScoreCard percentileScoreCard = new PercentileScoreCard();
		private InstituteAnalysisMetadata instituteAnalysisMetadata = new InstituteAnalysisMetadata();

    }
}
