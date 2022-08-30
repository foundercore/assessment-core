package com.assessment.questionpaper.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.assessment.common.FileUtility;
import com.assessment.common.IFileDecoder;
import com.assessment.common.NMATConstants;
import com.assessment.common.NMATConstants.Nmat_Sections;
import com.assessment.common.StringUtility;
import com.assessment.common.TimeUtility;
import com.assessment.common.XLReader;
import com.assessment.iam.commons.AuthUtils;
import com.assessment.iam.dtos.UserRole;
import com.assessment.question.QuestionService;
import com.assessment.question.dto.Question;
import com.assessment.questionpaper.dto.QuestionPaperPaginatedResponse;
import com.assessment.questionpaper.dto.QuestionPaperRequestDto;
import com.assessment.questionpaper.dto.QuestionPaperRequestDto.TestControlParamsRequestDto;
import com.assessment.questionpaper.dto.QuestionPaperResponseDto;
import com.assessment.questionpaper.dto.QuestionPaperStatus;
import com.assessment.questionpaper.dto.QuestionPaperType;
import com.assessment.questionpaper.dto.SearchQuestionPaperDto;
import com.assessment.questionpaper.entity.InstituteAnalysisMetadata;
import com.assessment.questionpaper.entity.InstituteAnalysisMetadata.InstituteData;
import com.assessment.questionpaper.entity.PercentileScoreCard;
import com.assessment.questionpaper.entity.QuestionPaper;
import com.assessment.questionpaper.entity.QuestionPaper.TestControlParams;
import com.assessment.questionpaper.entity.QuestionPaperId;
import com.google.common.io.Files;
import com.opencsv.exceptions.CsvValidationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TestConfigServiceImpl implements TestConfigService {

	@Autowired
	TestConfigRepository testConfigRepository;

	@Autowired
	QuestionService questionService;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Validator validator;

	@Override
	public String createQuestionPaper(QuestionPaperRequestDto questionPaperRequestDto) {
		/* validations */
		validateQuestionPaperDto(questionPaperRequestDto);

		/* entity building */
		QuestionPaper questionPaper = buildQuestionPaper(questionPaperRequestDto);
		QuestionPaperId id = new QuestionPaperId();
		id.setTenantId(AuthUtils.getCurrentTenantId());
		id.setQuestionPaperId(UUID.randomUUID().toString());

		questionPaper.setId(id);
		questionPaper.setStatus(QuestionPaperStatus.DRAFT.value());
		questionPaper.setCreatedBy(AuthUtils.getCurrentQualifiedUsername());
		questionPaper.setCreatedOn(new Date());

		testConfigRepository.save(questionPaper);
		updateQuestionRepository(questionPaper);
		return questionPaper.getId().getQuestionPaperId();
	}

	@Override
	public boolean exists(String paperId) {
		QuestionPaperId id = new QuestionPaperId();
		id.setTenantId(AuthUtils.getCurrentTenantId());
		id.setQuestionPaperId(paperId);
		return testConfigRepository.existsById(id);
	}

	@Override
	public QuestionPaper getEntity(String paperId) {
		QuestionPaperId id = new QuestionPaperId();
		id.setTenantId(AuthUtils.getCurrentTenantId());
		id.setQuestionPaperId(paperId);
		return testConfigRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
				String.format("Question paper %s does not exist", paperId)));
	}

	private QuestionPaper buildQuestionPaper(QuestionPaperRequestDto dto) {
		QuestionPaper questionPaper = new QuestionPaper();
		questionPaper.setName(dto.getName());
		questionPaper.setSubject(dto.getSubject() == null ? "" : dto.getSubject().trim());
		questionPaper.setType(dto.getType());
		questionPaper.setInstructions(dto.getInstructions());
		questionPaper.setTotalMarks(dto.getTotalMarks());
		questionPaper.setTotalDurationInMinutes(dto.getTotalDurationInMinutes());
		questionPaper.setMinimumDurationInMinutes(dto.getMinimumDurationInMinutes());
		questionPaper.setTags(dto.getTags());
		questionPaper.setSectionOrder(dto.getSectionOrder());

		if (dto.getSections() != null) {
			for (QuestionPaperRequestDto.PaperSectionRequestDto sectionDto : dto.getSections()) {
				QuestionPaper.PaperSection section = buildQuestionPaperSection(sectionDto);
				questionPaper.addSection(section);
			}
			adjustSectionNestedRelationship(questionPaper);
		}
		return questionPaper;
	}

	private QuestionPaper.PaperSection buildQuestionPaperSection(QuestionPaperRequestDto.PaperSectionRequestDto dto) {
		QuestionPaper.PaperSection section = new QuestionPaper.PaperSection();
		section.setId(UUID.randomUUID().toString());
		section.setName(dto.getName());
		section.setDurationInMinutes(dto.getDurationInMinutes());
		section.setDifficultyLevel(dto.getDifficultyLevel());
		section.setInstructions(dto.getInstructions());
		section.setParentSection(dto.getParentSection());
		section.setSubSectionOrder(dto.getSubSectionOrder());
		if (dto.getQuestions() != null) {
			for (QuestionPaperRequestDto.TestQuestionRequestDto testQuestionRequestDto : dto.getQuestions()) {
				QuestionPaper.TestQuestion question = buildTestPaperQuestion(testQuestionRequestDto);
				section.addQuestion(question);
			}
		}
		return section;
	}

	private QuestionPaper.TestQuestion buildTestPaperQuestion(QuestionPaperRequestDto.TestQuestionRequestDto dto) {
		QuestionPaper.TestQuestion question = new QuestionPaper.TestQuestion();
		question.setId(dto.getId());
		question.setPositiveMark(dto.getPositiveMark());
		question.setNegativeMark(dto.getNegativeMark());
		question.setSkipMark(dto.getSkipMark());
		question.setSequenceNumber(dto.getSequenceNumber());
		return question;
	}

	private void validateQuestionPaperDto(QuestionPaperRequestDto dto) {

		/* question-paper level validation */
		if (dto.getTotalDurationInMinutes() < 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("totalDurationInMinutes %s can't be negative", dto.getTotalDurationInMinutes()));
		}
		if (dto.getTotalMarks() < 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("totalMarks %s can't be negative", dto.getTotalMarks()));
		}

		/* question level validation */
		List<String> questions = new ArrayList<>();
		List<String> names = new ArrayList<>();
		if (dto.getSections() != null) {
			dto.getSections().forEach(paperSectionRequestDto -> {
				if (names.contains(paperSectionRequestDto.getName())) {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
							String.format("Section with name %s already exist", paperSectionRequestDto.getName()));
				}
				if (paperSectionRequestDto.getQuestions() != null) {
					paperSectionRequestDto.getQuestions().forEach(questionDto -> {
						validateTestQuestion(questionDto);
						if (questions.contains(questionDto.getId())) {
							throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(
									"Question %s occurs multiple times in question paper", questionDto.getId()));
						}
						questions.add(questionDto.getId());
					});
				}
				names.add(paperSectionRequestDto.getName());
			});
		}
	}

	private void validateTestQuestion(QuestionPaperRequestDto.TestQuestionRequestDto questionDto) {
		if (questionService.exists(questionDto.getId())) {
			if (questionDto.getPositiveMark() < 0) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						String.format("Invalid positiveMark for question %s ", questionDto.getId()));
			}
			if (questionDto.getNegativeMark() < 0) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						String.format("Invalid negativeMark for question %s ", questionDto.getId()));
			}
			if (questionDto.getSkipMark() < 0) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						String.format("Invalid skipMark for question %s ", questionDto.getId()));
			}
		} else {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Question with Id %s does not exist", questionDto.getId()));
		}
	}

	@Override
	public void deleteQuestionPaper(String paperId) {
		QuestionPaperId id = new QuestionPaperId();
		id.setTenantId(AuthUtils.getCurrentTenantId());
		id.setQuestionPaperId(paperId);
		QuestionPaper questionPaper = testConfigRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
						String.format("Question paper %s does not exist", paperId)));

		List<String> questions = new ArrayList<>();
		if (questionPaper.getSections() != null) {
			questionPaper.getSections().forEach((k, section) -> {
				if (section.getQuestions() != null) {
					section.getQuestions().forEach((x, q) -> questions.add(q.getId()));
				}
			});
		}
		if (!questions.isEmpty()) {
			questionService.unmarkQuestionUsage(paperId, questions);
		}
		testConfigRepository.deleteById(id);
	}

	@Override
	public void softDeleteQuestionPaper(String paperId) {
		QuestionPaperId id = new QuestionPaperId();
		id.setTenantId(AuthUtils.getCurrentTenantId());
		id.setQuestionPaperId(paperId);
		QuestionPaper questionPaper = testConfigRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
						String.format("Question paper %s does not exist", paperId)));
		questionPaper.setStatus(QuestionPaperStatus.DELETED.value());
		testConfigRepository.save(questionPaper);

		/* questions un-marking */
		List<String> questions = new ArrayList<>();
		if (questionPaper.getSections() != null) {
			questionPaper.getSections().forEach((k, section) -> {
				if (section.getQuestions() != null) {
					section.getQuestions().forEach((x, q) -> questions.add(q.getId()));
				}
			});
		}
		if (!questions.isEmpty()) {
			questionService.unmarkQuestionUsage(paperId, questions);
		}
	}

	@Override
	public void updateQuestionPaperMetadata(String paperId, QuestionPaperRequestDto dto) {
		QuestionPaperId id = new QuestionPaperId();
		id.setTenantId(AuthUtils.getCurrentTenantId());
		id.setQuestionPaperId(paperId);
		QuestionPaper questionPaper = testConfigRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
						String.format("Question paper %s does not exist", paperId)));

		if (QuestionPaperStatus.DELETED.value().equalsIgnoreCase(questionPaper.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Question paper %s does not exist.", paperId));
		}

		if (dto.getTotalDurationInMinutes() <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("totalDurationInMinutes %s can't be negative/zero", dto.getTotalDurationInMinutes()));
		}
		if (dto.getTotalMarks() <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("totalMarks %s can't be negative/zero", dto.getTotalMarks()));
		}

		if (StringUtils.isNotEmpty(dto.getName())) {
			questionPaper.setName(dto.getName());
		}
		if (StringUtils.isNotEmpty(dto.getSubject())) {
			questionPaper.setSubject(dto.getSubject());
		}
		if (StringUtils.isNotEmpty(dto.getType())) {
			questionPaper.setType(dto.getType());
		}
		if (StringUtils.isNotEmpty(dto.getInstructions())) {
			questionPaper.setInstructions(dto.getInstructions());
		}
		if (dto.getTags() != null && !dto.getTags().isEmpty()) {
			questionPaper.setTags(dto.getTags());
		}
		if (dto.getTotalMarks() > 0) {
			questionPaper.setTotalMarks(dto.getTotalMarks());
		}
		if (dto.getTotalDurationInMinutes() > 0) {
			questionPaper.setTotalDurationInMinutes(dto.getTotalDurationInMinutes());
		}
		if (dto.getMinimumDurationInMinutes() > 0) {
			questionPaper.setMinimumDurationInMinutes(dto.getMinimumDurationInMinutes());
		}
		if (dto.getSectionOrder() != null && !dto.getSectionOrder().isEmpty()) {
			questionPaper.setSectionOrder(dto.getSectionOrder());
		}
		questionPaper.setCalculatorRequired(dto.isCalculatorRequired());

		testConfigRepository.save(questionPaper);
	}

	@Override
	public String addQuestionPaperSection(String paperId,
			QuestionPaperRequestDto.PaperSectionRequestDto paperSectionRequestDto) {
		QuestionPaperId id = new QuestionPaperId();
		id.setTenantId(AuthUtils.getCurrentTenantId());
		id.setQuestionPaperId(paperId);
		QuestionPaper questionPaper = testConfigRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
						String.format("Question paper %s does not exist", paperId)));
		if (QuestionPaperStatus.DELETED.value().equalsIgnoreCase(questionPaper.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Question paper %s does not exist.", paperId));
		}
		if (!QuestionPaperStatus.DRAFT.value().equalsIgnoreCase(questionPaper.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Can't update question paper %s. Status not draft.", paperId));
		}
		/* validate all questions */
		if (paperSectionRequestDto.getQuestions() != null) {
			paperSectionRequestDto.getQuestions().forEach(this::validateTestQuestion);
		}

		List<String> names = new ArrayList<>();
		List<String> questions = new ArrayList<>();
		if (questionPaper.getSections() != null) {
			questionPaper.getSections().forEach((k, section) -> {
				names.add(section.getName());
				if (section.getQuestions() != null) {
					section.getQuestions().forEach((x, q) -> questions.add(q.getId()));
				}
			});
		}
		if (names.contains(paperSectionRequestDto.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Section with name %s already exist", paperSectionRequestDto.getName()));
		}
		if (paperSectionRequestDto.getQuestions() != null) {
			for (QuestionPaperRequestDto.TestQuestionRequestDto dto : paperSectionRequestDto.getQuestions()) {
				if (questions.contains(dto.getId())) {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
							String.format("Question %s already exist in other section", dto.getId()));
				}
			}
		}

		/* build & save */
		QuestionPaper.PaperSection section = buildQuestionPaperSection(paperSectionRequestDto);
		questionPaper.addSection(section);
		adjustSectionNestedRelationship(questionPaper);
		testConfigRepository.save(questionPaper);
		if (!questions.isEmpty()) {
			questionService.markQuestionUsage(paperId, questionPaper.getStatus(), questions);
		}
		return section.getId();
	}

	private void adjustSectionNestedRelationship(QuestionPaper questionPaper) {
		Map<String, String> nameIdMap = new HashMap<>();
		questionPaper.getSections().values().forEach(s -> nameIdMap.put(s.getName().toLowerCase(), s.getId()));

		for (QuestionPaper.PaperSection section : questionPaper.getSections().values()) {
			String parent = section.getParentSection();
			if (StringUtils.isNotEmpty(parent) && nameIdMap.containsKey(parent.toLowerCase())) {
				section.setParentSection(nameIdMap.get(parent));
			}
		}
	}

	@Override
	public void updateQuestionPaperSectionMetadata(String paperId, String sectionId,
			QuestionPaperRequestDto.PaperSectionRequestDto dto) {
		QuestionPaperId id = new QuestionPaperId();
		id.setTenantId(AuthUtils.getCurrentTenantId());
		id.setQuestionPaperId(paperId);
		QuestionPaper questionPaper = testConfigRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
						String.format("Question paper %s does not exist", paperId)));
		if (QuestionPaperStatus.DELETED.value().equalsIgnoreCase(questionPaper.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Question paper %s does not exist.", paperId));
		}
		if (!QuestionPaperStatus.DRAFT.value().equalsIgnoreCase(questionPaper.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Can't update question paper %s. Status not draft.", paperId));
		}
		if (questionPaper.getSections() == null || !questionPaper.getSections().containsKey(sectionId)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Section with Id %s does not exist", sectionId));
		}
		/* build & save */
		if (StringUtils.isNotEmpty(dto.getName())) {
			questionPaper.getSections().get(sectionId).setName(dto.getName());
		}
		if (StringUtils.isNotEmpty(dto.getInstructions())) {
			questionPaper.getSections().get(sectionId).setInstructions(dto.getInstructions());
		}
		if (dto.getDurationInMinutes() > 0) {
			questionPaper.getSections().get(sectionId).setDurationInMinutes(dto.getDurationInMinutes());
		}
		if (StringUtils.isNotEmpty(dto.getDifficultyLevel())) {
			questionPaper.getSections().get(sectionId).setDifficultyLevel(dto.getDifficultyLevel());
		}
		if (StringUtils.isNotEmpty(dto.getParentSection())) {
			questionPaper.getSections().get(sectionId).setParentSection(dto.getParentSection());
		}
		if (dto.getSubSectionOrder() != null) {
			questionPaper.getSections().get(sectionId).setSubSectionOrder(dto.getSubSectionOrder());
		}
		testConfigRepository.save(questionPaper);
	}

	@Override
	public void removeQuestionPaperSection(String paperId, String sectionId) {
		QuestionPaperId id = new QuestionPaperId();
		id.setTenantId(AuthUtils.getCurrentTenantId());
		id.setQuestionPaperId(paperId);
		QuestionPaper questionPaper = testConfigRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
						String.format("Question paper %s does not exist", paperId)));
		if (QuestionPaperStatus.DELETED.value().equalsIgnoreCase(questionPaper.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Question paper %s does not exist.", paperId));
		}
		if (!QuestionPaperStatus.DRAFT.value().equalsIgnoreCase(questionPaper.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Can't update question paper %s. Status not draft.", paperId));
		}
		if (questionPaper.getSections() == null || !questionPaper.getSections().containsKey(sectionId)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Section with Id %s does not exist", sectionId));
		}

		QuestionPaper.PaperSection section = questionPaper.getSections().get(sectionId);
		/* remove & save */
		List<String> questions = new ArrayList<>();
		if (section.getQuestions() != null) {
			questions.addAll(new ArrayList<>(section.getQuestions().keySet()));
		}
		/* adjust section order list if it's not a sub-section */
		String sectionName = section.getName();
		if (questionPaper.getSectionOrder() != null && StringUtils.isEmpty(section.getParentSection())) {
			questionPaper.getSectionOrder().remove(sectionName);
		}

		/* handle nested relationship */
		for (String subsection : questionPaper.getSubsections(sectionId)) {
			if (questionPaper.getSections().get(subsection).getQuestions() != null) {
				questions.addAll(new ArrayList<>(questionPaper.getSections().get(subsection).getQuestions().keySet()));
				questionPaper.getSections().remove(subsection);
			}
		}

		/* if it's a sub-section then adjust sub-section order list */
		String parentId = section.getParentSection();
		if (StringUtils.isNotEmpty(parentId) && questionPaper.getSections().containsKey(parentId)) {
			QuestionPaper.PaperSection parentSection = questionPaper.getSections().get(paperId);
			if (parentSection.getSubSectionOrder() != null) {
				parentSection.getSubSectionOrder().remove(sectionName);
			}
		}

		/* remove section after subsection processing over */
		questionPaper.getSections().remove(sectionId);

		/* save remaining modal */
		testConfigRepository.save(questionPaper);
		if (!questions.isEmpty()) {
			questionService.unmarkQuestionUsage(paperId, questions);
		}
	}

	@Override
	public void addNewQuestionsInQuestionPaperSection(String paperId, String sectionId,
			List<QuestionPaperRequestDto.TestQuestionRequestDto> questionDtos) {
		QuestionPaperId id = new QuestionPaperId();
		id.setTenantId(AuthUtils.getCurrentTenantId());
		id.setQuestionPaperId(paperId);
		QuestionPaper questionPaper = testConfigRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
						String.format("Question paper %s does not exist", paperId)));
		if (QuestionPaperStatus.DELETED.value().equalsIgnoreCase(questionPaper.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Question paper %s does not exist.", paperId));
		}
		if (!QuestionPaperStatus.DRAFT.value().equalsIgnoreCase(questionPaper.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Can't update question paper %s. Status not draft.", paperId));
		}
		if (questionPaper.getSections() == null || !questionPaper.getSections().containsKey(sectionId)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Section with Id %s does not exist", sectionId));
		}

		/* validate question dto */
		questionDtos.forEach(this::validateTestQuestion);

		/* if incoming questions already exist */
		List<String> questions = new ArrayList<>();
		questionPaper.getSections().forEach((k, section) -> {
			if (section.getQuestions() != null) {
				section.getQuestions().forEach((x, q) -> questions.add(q.getId()));
			}
		});
		for (QuestionPaperRequestDto.TestQuestionRequestDto dto : questionDtos) {
			if (questions.contains(dto.getId())) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						String.format("Question %s already exist in question paper", dto.getId()));
			}
		}

		/* build & save */
		QuestionPaper.PaperSection section = questionPaper.getSections().get(sectionId);
		questionDtos.forEach(q -> section.addQuestion(buildTestPaperQuestion(q)));
		testConfigRepository.save(questionPaper);
		questionService.markQuestionUsage(paperId, questionPaper.getStatus(), questions);
	}

	@Override
	public void removeQuestionsFromQuestionPaperSection(String paperId, String sectionId, List<String> questions) {
		QuestionPaperId id = new QuestionPaperId();
		id.setTenantId(AuthUtils.getCurrentTenantId());
		id.setQuestionPaperId(paperId);
		QuestionPaper questionPaper = testConfigRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
						String.format("Question paper %s does not exist", paperId)));
		if (QuestionPaperStatus.DELETED.value().equalsIgnoreCase(questionPaper.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Question paper %s does not exist.", paperId));
		}
		if (!QuestionPaperStatus.DRAFT.value().equalsIgnoreCase(questionPaper.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Can't update question paper %s. Status not draft.", paperId));
		}
		if (questionPaper.getSections() == null || !questionPaper.getSections().containsKey(sectionId)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Section with Id %s does not exist", sectionId));
		}
		QuestionPaper.PaperSection section = questionPaper.getSections().get(sectionId);
		for (String question : questions) {
			if (section.getQuestions() == null || !section.getQuestions().containsKey(question)) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(
						"Question with Id %s does not exist in paper %s, section %s", question, paperId, sectionId));
			}
		}

		/* remove question */
		questions.forEach(q -> section.getQuestions().remove(q));
		testConfigRepository.save(questionPaper);
		questionService.unmarkQuestionUsage(paperId, questions);
	}

	@Override
	public void updateQuestionsInQuestionPaperSection(String paperId, String sectionId,
			List<QuestionPaperRequestDto.TestQuestionRequestDto> questionDtos) {
		QuestionPaperId id = new QuestionPaperId();
		id.setTenantId(AuthUtils.getCurrentTenantId());
		id.setQuestionPaperId(paperId);
		QuestionPaper questionPaper = testConfigRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
						String.format("Question paper %s does not exist", paperId)));
		if (QuestionPaperStatus.DELETED.value().equalsIgnoreCase(questionPaper.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Question paper %s does not exist.", paperId));
		}
		// if
		// (!QuestionPaperStatus.DRAFT.value().equalsIgnoreCase(questionPaper.getStatus())){
		// throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
		// String.format("Can't update question paper %s. Status not draft.", paperId));
		// }
		if (questionPaper.getSections() == null || !questionPaper.getSections().containsKey(sectionId)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Section with Id %s does not exist", sectionId));
		}
		/* validate question dto */
		questionDtos.forEach(this::validateTestQuestion);

		/* if questions exists in section */
		QuestionPaper.PaperSection section = questionPaper.getSections().get(sectionId);
		for (QuestionPaperRequestDto.TestQuestionRequestDto dto : questionDtos) {
			if (section.getQuestions() == null || !section.getQuestions().containsKey(dto.getId())) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(
						"Question with Id %s does not exist in paper %s, section %s", dto.getId(), paperId, sectionId));
			}
		}

		/* build and save */
		questionDtos.forEach(q -> section.addQuestion(buildTestPaperQuestion(q)));
		testConfigRepository.save(questionPaper);
	}

	@Override
	public void initiateQuestionPaperVerification(String paperId) {
		QuestionPaperId id = new QuestionPaperId();
		id.setTenantId(AuthUtils.getCurrentTenantId());
		id.setQuestionPaperId(paperId);
		QuestionPaper questionPaper = testConfigRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
						String.format("Question paper %s does not exist", paperId)));
		if (QuestionPaperStatus.DELETED.value().equalsIgnoreCase(questionPaper.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Question paper %s does not exist.", paperId));
		}
		if (!QuestionPaperStatus.DRAFT.value().equalsIgnoreCase(questionPaper.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Can't init verification for question paper %s. Status not draft.", paperId));
		}
		Set<ConstraintViolation<QuestionPaper>> violations = validator.validate(questionPaper);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		/* state migration validations */
		validateTestForStateMigration(questionPaper);

		questionPaper.setStatus(QuestionPaperStatus.PENDING_VERIFICATION.value());
		QuestionPaper.PaperMigration migration = new QuestionPaper.PaperMigration();
		migration.setStatus(QuestionPaperStatus.PENDING_VERIFICATION.value());
		migration.setActivityBy(AuthUtils.getCurrentQualifiedUsername());
		migration.setActivityOn(new Date());
		migration.setRemarks("");
		questionPaper.addMigration(migration);
		testConfigRepository.save(questionPaper);
	}

	private void validateTestForStateMigration(QuestionPaper questionPaper) {
		if (questionPaper.getSections() == null || questionPaper.getSections().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(
					"Question paper sections missing. Paper id - %s", questionPaper.getId().getQuestionPaperId()));
		}
		for (QuestionPaper.PaperSection section : questionPaper.getSections().values()) {
			if (StringUtils.isEmpty(questionPaper.getName())) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						String.format("Section name missing. Paper id - %s, Section id - %s",
								questionPaper.getId().getQuestionPaperId(), section.getId()));
			}
			if (!QuestionPaperType.NMAT.name().equals(questionPaper.getType())
					&& questionPaper.getSubsections(section.getId()).isEmpty()
					&& (section.getQuestions() == null || section.getQuestions().isEmpty())) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						String.format("Questions missing in section. Paper id - %s, Section - %s",
								questionPaper.getId().getQuestionPaperId(), section.getId()));
			}
		}

		/* question level validation */
		List<String> questions = new ArrayList<>();
		List<String> names = new ArrayList<>();

		questionPaper.getSections().forEach((k1, section) -> {
			/* duplicate section name */
			if (names.contains(section.getName().toLowerCase())) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						String.format("Duplicate Section name %s ", section.getName()));
			}
			if (section.getQuestions() != null) {
				section.getQuestions().forEach((k2, question) -> {
					/* question exists */
					if (questionService.exists(question.getId())) {
						/* question marking valid */
						if (question.getPositiveMark() < 0) {
							throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
									String.format("Invalid positiveMark for question %s ", question.getId()));
						}
						if (question.getNegativeMark() < 0) {
							throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
									String.format("Invalid negativeMark for question %s ", question.getId()));
						}
						if (question.getSkipMark() < 0) {
							throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
									String.format("Invalid skipMark for question %s ", question.getId()));
						}
					} else {
						throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
								String.format("Question with Id %s does not exist", question.getId()));
					}
					/* duplicate question */
					if (questions.contains(question.getId())) {
						throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
								String.format("Question %s occurs multiple times in question paper", question.getId()));
					}
					questions.add(question.getId());
				});
			}
			names.add(section.getName().toLowerCase());
		});
		if (!QuestionPaperType.NMAT.name().equals(questionPaper.getType()) && questions.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String
					.format("Questions missing in paper. Paper id - %s", questionPaper.getId().getQuestionPaperId()));
		}
	}

	@Override
	public void verifyQuestionPaper(String paperId) {
		QuestionPaperId id = new QuestionPaperId();
		id.setTenantId(AuthUtils.getCurrentTenantId());
		id.setQuestionPaperId(paperId);
		QuestionPaper questionPaper = testConfigRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
						String.format("Question paper %s does not exist", paperId)));
		if (QuestionPaperStatus.DELETED.value().equalsIgnoreCase(questionPaper.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Question paper %s does not exist.", paperId));
		}
		if (!QuestionPaperStatus.PENDING_VERIFICATION.value().equalsIgnoreCase(questionPaper.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Can't verify question paper %s. Verification not initiated.", paperId));
		}
		Set<ConstraintViolation<QuestionPaper>> violations = validator.validate(questionPaper);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		/* state migration validations */
		validateTestForStateMigration(questionPaper);

		/* self verification block if not admin/tenant-admin */
		QuestionPaper.PaperMigration activity = questionPaper.getMigration()
				.get(questionPaper.getMigration().size() - 1);
		if (activity.getActivityBy().equalsIgnoreCase(AuthUtils.getCurrentQualifiedUsername()) && !isAdminUser()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Self verification not allowed. Paper id - %s", paperId));
		}

		QuestionPaper.PaperMigration migration = new QuestionPaper.PaperMigration();
		migration.setStatus(QuestionPaperStatus.VERIFIED.value());
		migration.setActivityBy(AuthUtils.getCurrentQualifiedUsername());
		migration.setActivityOn(new Date());
		migration.setRemarks("verification successful");
		questionPaper.addMigration(migration);

		questionPaper.setStatus(QuestionPaperStatus.VERIFIED.value());
		testConfigRepository.save(questionPaper);
		updateQuestionRepository(questionPaper);
	}

	private boolean isAdminUser() {
		return AuthUtils.getCurrentUserRoles().contains(UserRole.ROLE_TENANT_ADMIN.value())
				|| AuthUtils.getCurrentUserRoles().contains(UserRole.ROLE_USER_ADMIN.value());
	}

	@Override
	public void rejectQuestionPaperVerification(String paperId, String rejectionReason) {
		QuestionPaperId id = new QuestionPaperId();
		id.setTenantId(AuthUtils.getCurrentTenantId());
		id.setQuestionPaperId(paperId);
		QuestionPaper questionPaper = testConfigRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
						String.format("Question paper %s does not exist", paperId)));
		if (QuestionPaperStatus.DELETED.value().equalsIgnoreCase(questionPaper.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Question paper %s does not exist.", paperId));
		}
		if (!QuestionPaperStatus.PENDING_VERIFICATION.value().equalsIgnoreCase(questionPaper.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(
					"Can't process verification request for question paper %s. Verification not initiated.", paperId));
		}

		QuestionPaper.PaperMigration migration = new QuestionPaper.PaperMigration();
		migration.setStatus(QuestionPaperStatus.DRAFT.value());
		migration.setActivityBy(AuthUtils.getCurrentQualifiedUsername());
		migration.setActivityOn(new Date());
		migration.setRemarks(rejectionReason);
		questionPaper.addMigration(migration);

		questionPaper.setStatus(QuestionPaperStatus.DRAFT.value());
		testConfigRepository.save(questionPaper);
	}

	@Override
	public List<QuestionPaperResponseDto> getQuestionPapersPendingToVerify() {
		Query query = new Query();

		/* prepare request filters */
		query.addCriteria(Criteria.where("_id.tenantId").is(AuthUtils.getCurrentTenantId()));
		query.addCriteria(Criteria.where("status").is(QuestionPaperStatus.PENDING_VERIFICATION.value()));

		List<QuestionPaper> questionPapers = mongoTemplate.find(query, QuestionPaper.class);
		List<QuestionPaperResponseDto> dtos = new ArrayList<>();

		for (QuestionPaper qp : questionPapers) {
			dtos.add(buildQuestionPaperResponse(qp, false));
		}

		return dtos;
	}

	@Override
	public void archiveQuestionPaper(String paperId) {
		QuestionPaperId id = new QuestionPaperId();
		id.setTenantId(AuthUtils.getCurrentTenantId());
		id.setQuestionPaperId(paperId);
		QuestionPaper questionPaper = testConfigRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
						String.format("Question paper %s does not exist", paperId)));
		if (QuestionPaperStatus.DELETED.value().equalsIgnoreCase(questionPaper.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Question paper %s does not exist.", paperId));
		}
		if (!QuestionPaperStatus.PUBLISHED.value().equalsIgnoreCase(questionPaper.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Can't archive question paper. Status not published."));
		}
		questionPaper.setStatus(QuestionPaperStatus.ARCHIVED.value());

		QuestionPaper.PaperMigration migration = new QuestionPaper.PaperMigration();
		migration.setStatus(QuestionPaperStatus.ARCHIVED.name());
		migration.setActivityBy(AuthUtils.getCurrentQualifiedUsername());
		migration.setActivityOn(new Date());
		migration.setRemarks("Question paper archived. No longer an active test.");
		questionPaper.addMigration(migration);

		testConfigRepository.save(questionPaper);
		updateQuestionRepository(questionPaper);
	}

	@Override
	public QuestionPaperResponseDto getQuestionPaper(String paperId) {
		QuestionPaperId id = new QuestionPaperId();
		id.setTenantId(AuthUtils.getCurrentTenantId());
		id.setQuestionPaperId(paperId);
		QuestionPaper questionPaper = testConfigRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
						String.format("Question paper %s does not exist", paperId)));
		return buildQuestionPaperResponse(questionPaper, true);
	}

	@Override
	public List<Question> getQuestionPaperLinkedQuestions(String paperId) {
		QuestionPaperId id = new QuestionPaperId();
		id.setTenantId(AuthUtils.getCurrentTenantId());
		id.setQuestionPaperId(paperId);
		QuestionPaper questionPaper = testConfigRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
						String.format("Question paper %s does not exist", paperId)));

		List<Question> questions = new ArrayList<>();
		if (questionPaper.getSections() != null) {
			questionPaper.getSections().forEach((k, section) -> {
				if (section.getQuestions() != null) {
					section.getQuestions().forEach((q, question) -> {
						Question entity = questionService.getQuestion(question.getId(), true);
						if (entity != null) {
							entity.setPositiveMark(question.getPositiveMark());
							entity.setNegativeMark(question.getNegativeMark());
							entity.setSkipMark(question.getSkipMark());
							questions.add(entity);
						} else {
							log.warn("Question {} does not exist in question repository", question.getId());
						}
					});
				}
			});
		}
		return questions;
	}

	@Override
	public Question getQuestionPaperLinkedQuestion(String paperId, String questionId) {
		QuestionPaperId id = new QuestionPaperId();
		id.setTenantId(AuthUtils.getCurrentTenantId());
		id.setQuestionPaperId(paperId);
		QuestionPaper questionPaper = testConfigRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
						String.format("Question paper %s does not exist", paperId)));

		List<Question> questions = new ArrayList<>();
		if (questionPaper.getSections() != null) {
			questionPaper.getSections().forEach((k, section) -> {
				if (section.getQuestions() != null) {
					section.getQuestions().forEach((q, question) -> {
						if (question.getId().equalsIgnoreCase(questionId) && questions.isEmpty()) {
							Question entity = questionService.getQuestion(question.getId(), true);
							if (entity != null) {
								entity.setPositiveMark(question.getPositiveMark());
								entity.setNegativeMark(question.getNegativeMark());
								entity.setSkipMark(question.getSkipMark());
								questions.add(entity);
							} else {
								log.warn("Question {} does not exist in question repository", question.getId());
							}
						}
					});
				}
			});
		}
		if (questions.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Question %s not found in repository. Paper %s ", questionId, paperId));
		}
		return questions.get(0);
	}

	@Override
	public void publishQuestionPaper(String paperId) {
		QuestionPaperId id = new QuestionPaperId();
		id.setTenantId(AuthUtils.getCurrentTenantId());
		id.setQuestionPaperId(paperId);
		QuestionPaper questionPaper = testConfigRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
						String.format("Question paper %s does not exist", paperId)));
		if (QuestionPaperStatus.DELETED.value().equalsIgnoreCase(questionPaper.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Question paper %s does not exist.", paperId));
		}
		if (!QuestionPaperStatus.VERIFIED.value().equalsIgnoreCase(questionPaper.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Can't publish question paper %s. Verification not done.", paperId));
		}
		Set<ConstraintViolation<QuestionPaper>> violations = validator.validate(questionPaper);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		/* state migration validations */
		validateTestForStateMigration(questionPaper);

		QuestionPaper.PaperMigration migration = new QuestionPaper.PaperMigration();
		migration.setStatus(QuestionPaperStatus.PUBLISHED.value());
		migration.setActivityBy(AuthUtils.getCurrentQualifiedUsername());
		migration.setActivityOn(new Date());
		migration.setRemarks("test published");
		questionPaper.addMigration(migration);

		questionPaper.setStatus(QuestionPaperStatus.PUBLISHED.value());
		testConfigRepository.save(questionPaper);
		updateQuestionRepository(questionPaper);
	}

	@Override
	public List<String> getQuestionPaperSubjects() {
		Query query = new Query();
		query.addCriteria(Criteria.where("_id.tenantId").is(AuthUtils.getCurrentTenantId()));
		return mongoTemplate.findDistinct(query, "subject", QuestionPaper.COLLECTION_NAME, String.class);
	}

	@Override
	public List<String> getQuestionPaperTypes() {
		Query query = new Query();
		query.addCriteria(Criteria.where("_id.tenantId").is(AuthUtils.getCurrentTenantId()));
		return mongoTemplate.findDistinct(query, "type", QuestionPaper.COLLECTION_NAME, String.class);
	}

	@Override
	public void updateTestControlParams(String paperId, TestControlParamsRequestDto controlParams) {
		QuestionPaperId id = new QuestionPaperId();
		id.setTenantId(AuthUtils.getCurrentTenantId());
		id.setQuestionPaperId(paperId);
		QuestionPaper questionPaper = testConfigRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
						String.format("Question paper %s does not exist", paperId)));
		if (QuestionPaperStatus.DELETED.value().equalsIgnoreCase(questionPaper.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Question paper %s does not exist.", paperId));
		}
		if (controlParams == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Control params missing. Nothing to update.");
		}

		try {
			questionPaper.setControlParam(getControlParamWithPercentileData(controlParams, questionPaper));
			testConfigRepository.save(questionPaper);
		} catch (CsvValidationException | IOException e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
					"Control params invalid. Error Message : " + e.getMessage());
		}

	}

	private TestControlParams getControlParamWithPercentileData(TestControlParamsRequestDto controlParamsRequestDto,
			QuestionPaper questionPaper) throws CsvValidationException, IOException {
		TestControlParams controlParams = questionPaper.getControlParam() != null ? questionPaper.getControlParam()
				: new TestControlParams();
		// if file exists just update the file
		if (controlParamsRequestDto.getPercentileFile() != null) {
			controlParams.setPercentileScoreCard(
					readPercentileFile(controlParamsRequestDto.getPercentileFile(), questionPaper));
		} else {
			// update individual attributes
			controlParams.setAllowCalculator(controlParamsRequestDto.isAllowCalculator());
			controlParams.setDoNotShowReport(controlParamsRequestDto.isDoNotShowReport());
			controlParams.setPercentile(controlParamsRequestDto.isPercentile());
			controlParams.setSectionalTest(controlParamsRequestDto.isSectionalTest());
			controlParams.setShuffleQuestions(controlParamsRequestDto.isShuffleQuestions());
			controlParams.setAllowInstituteAnalysis(controlParamsRequestDto.isAllowInstituteAnalysis());
			;
		}
		return controlParams;
	}

	public PercentileScoreCard readPercentileFile(MultipartFile file, QuestionPaper questionPaper)
			throws IOException, CsvValidationException {
		String directory = Files.createTempDir().getAbsolutePath() + File.separator + AuthUtils.getCurrentUsername()
				+ File.separator + UUID.randomUUID().toString();
		File tempFile = null;

		try {
			/* save file */
			Files.createParentDirs(new File(directory + File.separator + "tmp.log"));
			String fileName = file.getOriginalFilename();
			fileName = directory + File.separator + fileName;
			FileUtility.saveFile(file, fileName);
			tempFile = new File(fileName);
			/* read percentile file */
			return initReadingPercentileFile(fileName, questionPaper);
		} catch (IOException e) {
			log.error("Error reading percentile file.", e);
			throw e;
		} finally {
			if (tempFile != null && tempFile.exists()) {
				FileUtility.delete(tempFile);
			}
		}
	}

	public PercentileScoreCard initReadingPercentileFile(String fileName, QuestionPaper questionPaper)
			throws IOException, CsvValidationException {
		/* read file & load all Percentile score */
		IFileDecoder decoder;
		if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
			decoder = new XLReader(fileName);
		} else {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Unsupported file format. File - %s", fileName));
		}
		int rowCount = 0;
		PercentileScoreCard percentileScoreCard = new PercentileScoreCard();
		percentileScoreCard.setFileName(Paths.get(fileName).getFileName().toString());
		percentileScoreCard.setTestId(questionPaper.getId().getQuestionPaperId());
		percentileScoreCard.setTestName(questionPaper.getName());

		Map<String, String> sectionNames = new HashMap<String, String>();
		questionPaper.getSections().values().forEach(section -> {
			sectionNames.put(section.getName(), section.getId());
		});
		while (decoder.hasNext()) {
			rowCount++;
			Map<String, Object> record = decoder.next();

			/*
			 * read the test marks!!!
			 */
			Double testMark = StringUtility.parseStringToOptionalDouble(String.valueOf(record.get("Test Marks")));
			Double testPercentileMark = StringUtility
					.parseStringToOptionalDouble(String.valueOf(record.get("Test Percentile")));
			if (testMark != null) {
				percentileScoreCard.getTestLevelPercentile().put(testMark, testPercentileMark);
			}

			/*
			 * read section marks
			 */
			for (Entry<String, String> sectionName : sectionNames.entrySet()) {
				Double sectionMark = StringUtility
						.parseStringToOptionalDouble(String.valueOf(record.get(sectionName.getKey() + " Marks")));
				Double sectionPercentileMark = StringUtility
						.parseStringToOptionalDouble(String.valueOf(record.get(sectionName.getKey() + " Percentile")));
				if (sectionMark != null) {
					if (percentileScoreCard.getSectionLevelPercentile().get(sectionName.getValue()) != null) {
						percentileScoreCard.getSectionLevelPercentile().get(sectionName.getValue()).put(sectionMark,
								sectionPercentileMark);
					} else {
						percentileScoreCard.getSectionLevelPercentile().put(sectionName.getValue(), new HashMap<>());
						percentileScoreCard.getSectionLevelPercentile().get(sectionName.getValue()).put(sectionMark,
								sectionPercentileMark);
					}
				}
			}

		}
		decoder.close();
		return percentileScoreCard;

	}

	private QuestionPaperResponseDto buildQuestionPaperResponse(QuestionPaper qp, boolean detailed) {

		QuestionPaperResponseDto dto = new QuestionPaperResponseDto();
		dto.setQuestionPaperId(qp.getId().getQuestionPaperId());
		dto.setName(qp.getName());
		dto.setSubject(qp.getSubject());
		dto.setType(qp.getType());
		dto.setStatus(qp.getStatus());
		dto.setInstructions(qp.getInstructions());
		dto.setTotalMarks(qp.getTotalMarks());
		dto.setTotalDurationInMinutes(qp.getTotalDurationInMinutes());
		dto.setMinimumDurationInMinutes(qp.getMinimumDurationInMinutes());
		dto.setTags(qp.getTags());
		dto.setCreatedBy(qp.getCreatedBy());
		dto.setCreatedOn(qp.getCreatedOn());
		dto.setLastUpdatedBy(qp.getLastUpdatedBy());
		dto.setLastUpdatedOn(qp.getLastUpdatedOn());
		dto.setCalculatorRequired(qp.isCalculatorRequired());
		if (qp.getControlParam() != null) {
			dto.setControlParam(qp.getControlParam().toResponseDto());
		}
		dto.setSectionOrder(qp.getSectionOrder());
		// making this as non reachable code as we will work on this later.
		if (qp.getType() != null && qp.getType().equals(QuestionPaperType.NMAT.name()) && false) {
			// get section wise 36 question
			List<Question> nmatQuestions = this.questionService.getAllNmatQuestion();
			Map<String, List<Question>> questionsMappedByPassageId = nmatQuestions.stream()
					.collect(Collectors.groupingBy(Question::getKeyForNamt));
			for (QuestionPaper.PaperSection ps : qp.getSections().values()) {
				QuestionPaperResponseDto.PaperSectionResponseDto section = new QuestionPaperResponseDto.PaperSectionResponseDto();
				section.setId(ps.getId());
				section.setName(ps.getName());
				section.setInstructions(ps.getInstructions());
				section.setDurationInMinutes(ps.getDurationInMinutes());
				section.setDifficultyLevel(ps.getDifficultyLevel());
				section.setSubSectionOrder(ps.getSubSectionOrder());
				// TODO Randomize it
				List<Question> sectionQuestion = getNmatSectionQuestion(ps.getName(), questionsMappedByPassageId);
				int questionOrder = 1;
				for (Question tq : sectionQuestion) {
					QuestionPaperResponseDto.TestQuestionResponseDto question = new QuestionPaperResponseDto.TestQuestionResponseDto();
					question.setId(tq.getId().getQuestionId());
					question.setPositiveMark(tq.getPositiveMark());
					question.setNegativeMark(tq.getNegativeMark());
					question.setSkipMark(tq.getSkipMark());
					question.setName(tq.getName());
					question.setPassageContent(tq.getPassageContent());
					question.setType(tq.getType());
					question.setTags(tq.getTags());
					question.setSequenceNumber(questionOrder++);
					section.addQuestion(question);
					if (section.getQuestions().size() == 36) {
						break;
					}
				}
				section.updateQuestionCount();
				/* add to response section if not a child section */
				dto.addSection(section);
			}
		} else if (qp.getSections() != null && detailed) {

			List<Question> questions = getQuestionPaperLinkedQuestions(qp.getId().getQuestionPaperId());
			Map<String, Question> questionMap = new HashMap<>();
			if (questions != null) {
				questions.forEach(q -> questionMap.put(q.getId().getQuestionId(), q));
			}

			Map<String, List<QuestionPaperResponseDto.PaperSectionResponseDto>> subsectionMap = new HashMap<>();
			for (QuestionPaper.PaperSection ps : qp.getSections().values()) {
				QuestionPaperResponseDto.PaperSectionResponseDto section = new QuestionPaperResponseDto.PaperSectionResponseDto();
				section.setId(ps.getId());
				section.setName(ps.getName());
				section.setInstructions(ps.getInstructions());
				section.setDurationInMinutes(ps.getDurationInMinutes());
				section.setDifficultyLevel(ps.getDifficultyLevel());
				section.setParentSection(ps.getParentSection());
				section.setSubSectionOrder(ps.getSubSectionOrder());

				if (ps.getQuestions() != null) {
					for (QuestionPaper.TestQuestion tq : ps.getQuestions().values()) {
						QuestionPaperResponseDto.TestQuestionResponseDto question = new QuestionPaperResponseDto.TestQuestionResponseDto();
						question.setId(tq.getId());
						question.setPositiveMark(tq.getPositiveMark());
						question.setNegativeMark(tq.getNegativeMark());
						question.setSkipMark(tq.getSkipMark());
						question.setSequenceNumber(tq.getSequenceNumber());
						/* enrichment as asked */
						if (questionMap.containsKey(tq.getId())) {
							question.setName(questionMap.get(tq.getId()).getName());
							question.setPassageContent(questionMap.get(tq.getId()).getPassageContent());
							question.setType(questionMap.get(tq.getId()).getType());
							question.setTags(questionMap.get(tq.getId()).getTags());
						}
						section.addQuestion(question);
					}
				}
				section.updateQuestionCount();

				/* prepare subsection details */
				String parentSection = ps.getParentSection();
				if (StringUtils.isNotEmpty(parentSection)) {
					List<QuestionPaperResponseDto.PaperSectionResponseDto> subsections;
					if (!subsectionMap.containsKey(parentSection)) {
						subsections = new ArrayList<>();
						subsectionMap.put(parentSection, subsections);
					}
					subsectionMap.get(parentSection).add(section);
				} else {
					/* add to response section if not a child section */
					dto.addSection(section);
				}
			}
			/* handle nested relationship */
			if (!subsectionMap.isEmpty()) {
				for (QuestionPaperResponseDto.PaperSectionResponseDto section : dto.getSections()) {
					if (subsectionMap.containsKey(section.getId())) {
						section.setSubsections(subsectionMap.get(section.getId()));
					}
				}
			}
		}
		if (qp.getMigration() != null && detailed) {
			for (QuestionPaper.PaperMigration migration : qp.getMigration()) {
				QuestionPaperResponseDto.PaperMigrationResponseDto migrationDto = new QuestionPaperResponseDto.PaperMigrationResponseDto();
				migrationDto.setStatus(migration.getStatus());
				migrationDto.setActivityBy(migration.getActivityBy());
				migrationDto.setActivityOn(migration.getActivityOn());
				migrationDto.setRemarks(migration.getRemarks());
				dto.addMigration(migrationDto);
			}
		}
		return dto;
	}

	private List<Question> getNmatSectionQuestion(String sectionName,
			Map<String, List<Question>> questionsMappedByPassageId) {
		List<Question> sectionQuestions = new ArrayList<>();
		Map<String, Integer> questionsCatagorisation = null;
		if (Nmat_Sections.QUANT_REASONING.getSectionName().equals(sectionName)) {
			questionsCatagorisation = NMATConstants.NMAT_QUESTION_CATAGORIES
					.get(Nmat_Sections.QUANT_REASONING.getSectionName());
		} else if (Nmat_Sections.VERBAL_REASONING.getSectionName().equals(sectionName)) {
			questionsCatagorisation = NMATConstants.NMAT_QUESTION_CATAGORIES
					.get(Nmat_Sections.VERBAL_REASONING.getSectionName());
		} else if (Nmat_Sections.LOGICAL_REASONING.getSectionName().equals(sectionName)) {
			questionsCatagorisation = NMATConstants.NMAT_QUESTION_CATAGORIES
					.get(Nmat_Sections.LOGICAL_REASONING.getSectionName());
		}
		if (questionsCatagorisation != null) {
			// iterate over all the pre defined categorization
			for (Entry<String, Integer> questionCatagory : questionsCatagorisation.entrySet()) {
				// iterate over all the categorized questions
				for (Entry<String, List<Question>> mappedQuestions : questionsMappedByPassageId.entrySet()) {
					// if the catagory starts with the predefined key proceed
					if (mappedQuestions.getKey().startsWith(questionCatagory.getKey())) {
						List<Question> shuffledQuestion = mappedQuestions.getValue();
						Collections.shuffle(shuffledQuestion);

						if (shuffledQuestion.size() > questionCatagory.getValue()) {
							for (int i = 0; i < questionCatagory.getValue(); i++) {
								Question question = shuffledQuestion.get(i);
								sectionQuestions.add(question);
								mappedQuestions.getValue().remove(question);
							}
						} else {
							throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String
									.format("Insufficiant questions for section : " + sectionName + " in NMAT test."));
						}
					}
				}
			}
		}
		return sectionQuestions;
	}

	@Override
	public QuestionPaperPaginatedResponse searchQuestionPapers(SearchQuestionPaperDto filter) {
		if (filter.getPageNumber() <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Invalid page number - %s", filter.getPageNumber()));
		}
		Query query = new Query();

		/* prepare request filters */
		query.addCriteria(Criteria.where("_id.tenantId").is(AuthUtils.getCurrentTenantId()));
		if (StringUtils.isNotEmpty(filter.getQuestionPaperId())) {
			query.addCriteria(Criteria.where("_id.questionPaperId").is(filter.getQuestionPaperId()));
		}
		if (StringUtils.isNotEmpty(filter.getStatus())) {
			query.addCriteria(Criteria.where("status").is(filter.getStatus()));
		} else {
			query.addCriteria(Criteria.where("status").ne(QuestionPaperStatus.DELETED.value()));
		}
		if (StringUtils.isNotEmpty(filter.getSubject())) {
			query.addCriteria(Criteria.where("subject").is(filter.getSubject()));
		}
		if (filter.getTags() != null && !filter.getTags().isEmpty()) {
			Criteria tc = new Criteria();
			List<Criteria> tags = new ArrayList<>();
			for (String tag : filter.getTags()) {
				tags.add(Criteria.where("tags").is(tag));
			}
			tc.orOperator(tags.toArray(new Criteria[0]));
			query.addCriteria(tc);
		}
		Criteria rangeCriteria = null;
		if (StringUtils.isNotEmpty(filter.getUpdateStartTime())) {
			rangeCriteria = Criteria.where("lastUpdatedOn").gte(TimeUtility.convertTxtToDateTime(
					filter.getUpdateStartTime(), TimeUtility.WIDE_FORMAT, StringUtility.getClientTimezone()));
			query.addCriteria(rangeCriteria);
		}
		if (StringUtils.isNotEmpty(filter.getUpdateEndTime())) {
			if (rangeCriteria != null) {
				rangeCriteria.lte(TimeUtility.convertTxtToDateTime(filter.getUpdateEndTime(), TimeUtility.WIDE_FORMAT,
						StringUtility.getClientTimezone()));
			} else {
				query.addCriteria(Criteria.where("lastUpdatedOn").lte(TimeUtility.convertTxtToDateTime(
						filter.getUpdateEndTime(), TimeUtility.WIDE_FORMAT, StringUtility.getClientTimezone())));
			}
		}
		if (StringUtils.isNotEmpty(filter.getNameRegexPattern())) {
			query.addCriteria(Criteria.where("name").regex(filter.getNameRegexPattern(), "i"));
		}

		/* sort */
		if (StringUtils.isNotEmpty(filter.getSortColumn())
				&& Arrays.asList("asc", "desc").contains(String.valueOf(filter.getSortOrder()).toLowerCase())) {
			if ("asc".equalsIgnoreCase(filter.getSortOrder())) {
				query.with(Sort.by(Sort.Direction.ASC, filter.getSortColumn()));
			} else if ("desc".equalsIgnoreCase(filter.getSortOrder())) {
				query.with(Sort.by(Sort.Direction.DESC, filter.getSortColumn()));
			}
		}
		if (!"lastUpdatedOn".equalsIgnoreCase(filter.getSortColumn())) {
			query.with(Sort.by(Sort.Direction.DESC, "lastUpdatedOn"));
		}

		int limit = filter.getPageSize() > 0 && filter.getPageSize() <= 100 ? filter.getPageSize() : 100;

		/* get page specific records */
		int actualPage = filter.getPageNumber() > 0 ? filter.getPageNumber() : 1;
		long skipRecords = 0;
		if (actualPage > 1) {
			if (!filter.isNext()) {
				actualPage--;
			}
			skipRecords = (long) (actualPage - 1) * limit;
		}

		/* get total record */
		long totalRecords = mongoTemplate.count(query, QuestionPaper.class);

		/* limit */
		if (skipRecords > 0) {
			query.skip(skipRecords);
		}
		query.limit(limit);

		/* response schema */
		List<QuestionPaperResponseDto> dtos = new ArrayList<>();
		QuestionPaperPaginatedResponse response = new QuestionPaperPaginatedResponse();
		response.setPageSize(limit);
		response.setTotalRecords(totalRecords);
		response.setPageNumber(actualPage);
		response.setTests(dtos);
		/* execute query & prepare response */
		if (totalRecords > 0) {
			List<QuestionPaper> questionPapers = mongoTemplate.find(query, QuestionPaper.class);
			// Do not need the whole object

			questionPapers.forEach(qp -> dtos.add(buildQuestionPaperResponse(qp, false)));
			// response.setPaginatedRowId(dtos.get(dtos.size()-1).getQuestionPaperId());

		}
		return response;
	}

	private void updateQuestionRepository(QuestionPaper questionPaper) {
		List<String> questions = new ArrayList<>();
		if (questionPaper.getSections() != null) {
			questionPaper.getSections().forEach((k, section) -> {
				if (section.getQuestions() != null) {
					section.getQuestions().forEach((x, q) -> questions.add(q.getId()));
				}
			});
			questionService.markQuestionUsage(questionPaper.getId().getQuestionPaperId(), questionPaper.getStatus(),
					questions);
		}
	}

	@Override
	public void updateTestInstituteAnalysisMetadata(@NotBlank String paperId, MultipartFile file) {
		QuestionPaperId id = new QuestionPaperId();
		id.setTenantId(AuthUtils.getCurrentTenantId());
		id.setQuestionPaperId(paperId);
		QuestionPaper questionPaper = testConfigRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
						String.format("Question paper %s does not exist", paperId)));

		if (QuestionPaperStatus.DELETED.value().equalsIgnoreCase(questionPaper.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Question paper %s does not exist.", paperId));
		}

		if (file == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File missing. Please upload correct file.");
		}
		try {
			TestControlParams controlParams = questionPaper.getControlParam() != null ? questionPaper.getControlParam()
					: new TestControlParams();
			controlParams.setInstituteAnalysisMetadata(initReadingInstituteAnalysisMetadataFile(file, questionPaper));
			questionPaper.setControlParam(controlParams);
			testConfigRepository.save(questionPaper);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
					"Control params invalid. Error Message : " + e.getMessage());
		}
	}

	public InstituteAnalysisMetadata initReadingInstituteAnalysisMetadataFile(MultipartFile file,
			QuestionPaper questionPaper) throws IOException, CsvValidationException {

		String directory = Files.createTempDir().getAbsolutePath() + File.separator + AuthUtils.getCurrentUsername()
				+ File.separator + UUID.randomUUID().toString();
		File tempFile = null;
		/* read file & load all Percentile score */
		IFileDecoder decoder = null;
		InstituteAnalysisMetadata instituteAnalysisMetadata = new InstituteAnalysisMetadata();
		try {
			/* save file */
			Files.createParentDirs(new File(directory + File.separator + "tmp.log"));
			String fileName = file.getOriginalFilename();
			fileName = directory + File.separator + fileName;
			FileUtility.saveFile(file, fileName);
			tempFile = new File(fileName);
			/* read percentile file */
			if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
				decoder = new XLReader(fileName);
			} else {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						String.format("Unsupported file format. File - %s", fileName));
			}
			// Setup initial data
			instituteAnalysisMetadata.setFileName(Paths.get(fileName).getFileName().toString());
			instituteAnalysisMetadata.setTestId(questionPaper.getId().getQuestionPaperId());
			instituteAnalysisMetadata.setTestName(questionPaper.getName());
			instituteAnalysisMetadata.setInstituteData(new ArrayList<InstituteData>());

			Map<String, String> sectionNames = new HashMap<String, String>();
			questionPaper.getSections().values().forEach(section -> {
				sectionNames.put(section.getName(), section.getId());
			});
			while (decoder.hasNext()) {
				Map<String, Object> record = decoder.next();
				InstituteData instituteData = new InstituteData();
				/*
				 * read the Institute Name
				 */
				String instituteName = String.valueOf(record.get("Institute Name"));
				if (instituteName != null) {
					instituteData.setInstituteName(instituteName);
				}
				Double testPercentileMark = StringUtility
						.parseStringToOptionalDouble(String.valueOf(record.get("Test Percentile")));
				if (testPercentileMark != null) {
					instituteData.setTestLevelPercentile(testPercentileMark);
				}
				/*
				 * read section percentile
				 */
				for (Entry<String, String> sectionName : sectionNames.entrySet()) {
					if (record.containsKey(sectionName.getKey() + " Percentile")) {
						Double sectionPercentileMark = StringUtility.parseStringToOptionalDouble(
								String.valueOf(record.get(sectionName.getKey() + " Percentile")));
						if (sectionPercentileMark != null) {
							instituteData.getSectionLevelPercentile().put(sectionName.getValue(),
									sectionPercentileMark);
						}
					}
				}
				instituteAnalysisMetadata.getInstituteData().add(instituteData);
			}
			decoder.close();
		} catch (IOException e) {
			log.error("Error reading institute Analysis Metadata file.", e);
			throw e;
		} finally {
			if (tempFile != null && tempFile.exists()) {
				FileUtility.delete(tempFile);
			}

			if (decoder != null)
				decoder.close();
		}
		return instituteAnalysisMetadata;

	}
}
