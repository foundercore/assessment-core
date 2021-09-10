package com.assessment.questionpaper.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import com.assessment.questionpaper.dto.QuestionPaperResponseDto;

import lombok.Data;

@Data
@Document(collection = QuestionPaper.COLLECTION_NAME)
public class QuestionPaper {
    public static final String COLLECTION_NAME = "question_paper";

    @Id
    private QuestionPaperId id;

    @NotNull
    @NotEmpty
    private String name;

    private String subject;
    private String type;
    private int totalDurationInMinutes;
    private int minimumDurationInMinutes;

    private String instructions;
    private int totalMarks;
    private String status;

    Map<String, PaperSection> sections;
    List<String> sectionOrder;

    private List<String> tags;

    /* activity logs */
    private String createdBy;
    private Date createdOn;
    @LastModifiedBy
    private String lastUpdatedBy;
    @LastModifiedDate
    private Date lastUpdatedOn;

    /* migration */
    List<PaperMigration> migration;

    /* flow control parameters */
    TestControlParams controlParam;

	private boolean calculatorRequired;

    public void addTags(String fileName) {
        if (this.tags == null) this.tags = new ArrayList<>();
        this.tags.add(fileName);
    }

    public void addSection(PaperSection section) {
        if (this.sections == null) this.sections = new LinkedHashMap<>();
        this.sections.put(section.getId(), section);
    }

    public void addMigration(PaperMigration migration) {
        if (this.migration == null) this.migration = new ArrayList<>();
        this.migration.add(migration);
    }

    public List<String> getSubsections(String sectionId){
        List<String> subsections = new ArrayList<>();
        if (sections != null && sections.containsKey(sectionId)){
            sections.values().forEach(s-> {
                if (sectionId.equalsIgnoreCase(s.getParentSection())){
                    subsections.add(s.getId());
                }
            });
        }
        return subsections;
    }

	public Map<Double, Double> getPercentileForSection(String sectionId) {
		if (this.getControlParam() != null && this.getControlParam().isPercentile()
				&& this.getControlParam().getPercentileScoreCard() != null
				&& this.getControlParam().getPercentileScoreCard().getSectionLevelPercentile() != null
				&& this.getControlParam().getPercentileScoreCard().getSectionLevelPercentile().get(sectionId) != null) {
			return this.getControlParam().getPercentileScoreCard().getSectionLevelPercentile().get(sectionId);
		}

		return null;
	}

	public Map<Double, Double> getPercentileForTest() {
		if (this.getControlParam() != null && this.getControlParam().getPercentileScoreCard() != null
				&& this.getControlParam().isPercentile()
				&& this.getControlParam().getPercentileScoreCard().getTestLevelPercentile() != null) {
			return this.getControlParam().getPercentileScoreCard().getTestLevelPercentile();
		}

		return null;
	}

    @Data
    public static class PaperSection {

        private String id;
        private String name;
        private String instructions;
        private int durationInMinutes;
        private String difficultyLevel;
        private Map<String, TestQuestion> questions;

        /* to support nested sub-sections */
        private String parentSection;
        List<String> subSectionOrder;

        public void addQuestion(TestQuestion question) {
            if (this.questions == null) this.questions = new LinkedHashMap<>();
            this.questions.put(question.getId(), question);
        }
    }

    @Data
    public static class TestQuestion {

        private String id;
        private double positiveMark = 0;
        private double negativeMark = 0;
        private double skipMark = 0;
		private int sequenceNumber = 0;
    }

    @Data
    public static class PaperMigration {
        private String status;
        private String activityBy;
        private Date activityOn;
        private String remarks;
    }

    @Data
    public static class TestControlParams {
        private boolean viewTestResult;
		private boolean sectionalTest;
		private boolean allowInstituteAnalysisMetadata;
		private boolean percentile;
		private boolean allowCalculator;
		private boolean doNotShowReport;
		private boolean shuffleQuestions;

		private PercentileScoreCard percentileScoreCard = new PercentileScoreCard();
		private InstituteAnalysisMetadata instituteAnalysisMetadata = new InstituteAnalysisMetadata();

        public QuestionPaperResponseDto.TestControlParamsResponseDto toResponseDto(){
            QuestionPaperResponseDto.TestControlParamsResponseDto response = new QuestionPaperResponseDto.TestControlParamsResponseDto();

            response.setDoNotShowReport(this.doNotShowReport);
            response.setPercentile(this.percentile);
            response.setAllowCalculator(this.allowCalculator);
            response.setShuffleQuestions(this.shuffleQuestions);
            response.setViewTestResult(this.viewTestResult);
			response.setSectionalTest(this.sectionalTest);
			response.setPercentileScoreCard(this.percentileScoreCard);
			response.setInstituteAnalysisMetadata(this.instituteAnalysisMetadata);
            return response;
        }
    }


}
