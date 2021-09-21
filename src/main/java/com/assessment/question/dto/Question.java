package com.assessment.question.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import com.assessment.question.dto.QuestionPaginatedResponse.QuestionResponseDto;

import lombok.Data;

@Data
@Document(collection = Question.COLLECTION_NAME)
public class Question {
	public static final String COLLECTION_NAME = "question";

	@Id
	private QuestionId id;

	@NotNull
	@NotEmpty
	private String name;

	private String description;

	@NotNull
	@NotEmpty
	private String type;

	// @NotNull
	// @NotEmpty
	private String subject;

	private String topic, subTopic;
	private String difficultyLevel;

	private String passageId;
	private String passageContent;

	private List<QuestionOption> options;

	private List<String> tags;
	private String reference;
	private String explanation;
	private String videoExplanationUrl;

	private QuestionAnswer answer;

	private double positiveMark = 0;
	private double negativeMark = 0;
	private double skipMark = 0;

	/* metadata */
	private String fileName;
	private Map<String, String> usedInPapers;

	/* migration fields */
	Map<String, Object> migration;

	private String createdBy;
	private Date createdOn;

	@LastModifiedBy
	private String lastUpdatedBy;

	@LastModifiedDate
	private Date lastUpdatedOn;

	public void addTags(String fileName) {
		if (this.tags == null)
			this.tags = new ArrayList<>();
		this.tags.add(fileName);
	}

	public void addMigrationProperty(String key, Object value) {
		if (this.migration == null)
			this.migration = new HashMap<>();
		this.migration.put(key, value);
	}

	public void markUsage(String questionPaperId, String status) {
		if (this.usedInPapers == null)
			this.usedInPapers = new LinkedHashMap<>();
		this.usedInPapers.put(questionPaperId, status);
	}

	public void unmarkUsage(String questionPaperId) {
		if (this.usedInPapers != null) {
			this.usedInPapers.remove(questionPaperId);
		}
	}

	@Data
	public static class QuestionOption {
		private String key;
		private String value;
	}

	@Data
	public static class QuestionAnswer {

		private List<String> options;
		private String answerText;
	}

	public QuestionResponseDto getResponseDto() {

		QuestionResponseDto responseDto = new QuestionResponseDto();
		responseDto.setId(this.id);
		responseDto.setName(this.name);
		responseDto.setDescription(this.description);
		responseDto.setType(this.type);
		responseDto.setSubject(this.subject);
		responseDto.setTopic(this.topic);
		responseDto.setSubTopic(this.subTopic);
		responseDto.setDifficultyLevel(this.difficultyLevel);
		responseDto.setPassageId(this.passageId);
		responseDto.setPassageContent(this.passageContent);
		responseDto.setOptions(this.options);
		responseDto.setTags(this.tags);
		responseDto.setReference(this.reference);
		responseDto.setExplanation(this.explanation);
		responseDto.setVideoExplanationUrl(this.videoExplanationUrl);
		responseDto.setAnswer(this.answer);
		responseDto.setPositiveMark(this.positiveMark);
		responseDto.setNegativeMark(this.negativeMark);
		responseDto.setSkipMark(this.skipMark);
		responseDto.setFileName(this.fileName);
		responseDto.setUsedInPapers(this.usedInPapers);
		responseDto.setMigration(this.migration);
		responseDto.setCreatedBy(this.createdBy);
		responseDto.setCreatedOn(this.createdOn);
		responseDto.setLastUpdatedBy(this.lastUpdatedBy);
		responseDto.setLastUpdatedOn(this.lastUpdatedOn);
		return responseDto;
	}
}
