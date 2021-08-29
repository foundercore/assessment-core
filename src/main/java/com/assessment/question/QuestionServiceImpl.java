package com.assessment.question;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

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

import com.assessment.common.CsvFileDecoder;
import com.assessment.common.FileUtility;
import com.assessment.common.IFileDecoder;
import com.assessment.common.StringUtility;
import com.assessment.common.TimeUtility;
import com.assessment.common.XLReader;
import com.assessment.iam.commons.AuthUtils;
import com.assessment.questionpaper.config.TestConfigRepository;
import com.assessment.questionpaper.dto.QuestionPaperStatus;
import com.assessment.questionpaper.entity.QuestionPaper;
import com.assessment.questionpaper.entity.QuestionPaper.PaperSection;
import com.assessment.questionpaper.entity.QuestionPaperId;
import com.google.common.io.Files;
import com.opencsv.exceptions.CsvValidationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private PassageRepository passageRepository;
    
    @Autowired
    TestConfigRepository testConfigRepository;

    @Autowired
    private Validator validator;

    @Autowired
    private MongoTemplate mongoTemplate;

    public static final String QUESTION_COLLECTION = "question";

    @Override
    public String generatePassageId(String passageContent, boolean suppressException) {
        if (StringUtils.isEmpty(passageContent)){
            if (!suppressException) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passage content is empty");
            return "";
        }
        return String.valueOf(StringUtility.zeroAllocationHash(passageContent));
    }

    @Override
    public String createPassage(Passage passage) {
        PassageId id = new PassageId();
        id.setPassageId(generatePassageId(passage.getContent(), false));
        id.setTenantId(AuthUtils.getCurrentTenantId());
        passage.setId(id);

        Set<ConstraintViolation<Passage>> violations = validator.validate(passage);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        passageRepository.save(passage);
        return passage.getId().getPassageId();
    }

    @Override
    public void updatePassage(String passageId, Passage passage) {
        PassageId id = new PassageId();
        id.setPassageId(passageId);
        id.setTenantId(AuthUtils.getCurrentTenantId());

        Passage currentPassage = passageRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Passage with id %s not found", passageId)));
//        currentPassage.setTopic(passage.getTopic());
        currentPassage.setContent(passage.getContent());

        Set<ConstraintViolation<Passage>> violations = validator.validate(passage);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        passageRepository.save(currentPassage);
    }

    @Override
    public void linkQuestionsToPassage(String passageId, List<String> questionIds) {
        String tenantId = AuthUtils.getCurrentTenantId();

        /* check if passage exist */
        PassageId passageId1 = new PassageId();
        passageId1.setPassageId(passageId);
        passageId1.setTenantId(tenantId);
        passageRepository.findById(passageId1).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Passage with id %s not found", passageId)));

        /* list all questions & set passageId */
        List<Question> questions = new ArrayList<>();
        for (String questionId: questionIds){
            QuestionId id = new QuestionId();
            id.setQuestionId(questionId);
            id.setTenantId(tenantId);

            Question question = questionRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Question with id %s not found", questionId)));
            question.setPassageId(passageId);
            questions.add(question);
        }
        questionRepository.saveAll(questions);
    }

    @Override
    public void unlinkQuestionsToPassage(String passageId, List<String> questionIds) {
        String tenantId = AuthUtils.getCurrentTenantId();

        /* check if passage exist */
        PassageId passageId1 = new PassageId();
        passageId1.setPassageId(passageId);
        passageId1.setTenantId(tenantId);
        passageRepository.findById(passageId1).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Passage with id %s not found", passageId)));

        /* list all questions & unset passageId */
        List<Question> questions = new ArrayList<>();
        for (String questionId: questionIds){
            QuestionId id = new QuestionId();
            id.setQuestionId(questionId);
            id.setTenantId(tenantId);

            Question question = questionRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Question with id %s not found", questionId)));
            if (passageId.equalsIgnoreCase(question.getPassageId())){
                question.setPassageId("");
                questions.add(question);
            }else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Question with id %s not linked to passage", questionId));
            }
        }
        questionRepository.saveAll(questions);
    }

    @Override
    public void bulkCreateQuestions(MultipartFile file) throws IOException, CsvValidationException {
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

            initBulkCreationQuestion(fileName);
        } catch (IOException | CsvValidationException e) {
            throw e;
        } finally {
            if (tempFile != null && tempFile.exists()){
                FileUtility.delete(tempFile);
            }
        }
    }

    @Override
    public void initBulkCreationQuestion(String fileName) throws IOException, CsvValidationException {
        /* read file & load all question */
        StringBuilder error = new StringBuilder();
        List<Question> questions = new ArrayList<>();
        IFileDecoder decoder;
        if (fileName.endsWith(".csv")){
            decoder = new CsvFileDecoder(fileName);
        } else if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")){
            decoder = new XLReader(fileName);
        }else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Unsupported file format. File - %s", fileName));
        }
        int rowCount = 0;
        while (decoder.hasNext()){
            rowCount++;
            Map<String, Object> record = decoder.next();

            /* prepare & validate question modal */
            Question question = new Question();
            QuestionId id = new QuestionId();

            /* passage field added due to avoid id conflict */
            /* migration fields added to do migration without missing anything from existing source. New files won't have these fields so it won't impact */
			String idContent = String.valueOf(record.get("name")) + record.get("passage")
					+ record.get("migration_test_id") + record.get("migration_section_id")
					+ readOption(record.get("option_1")) + readOption(record.get("option_2"))
					+ readOption(record.get("option_3")) + readOption(record.get("option_4"));

            id.setQuestionId(String.valueOf(StringUtility.zeroAllocationHash(idContent)));
            id.setTenantId(AuthUtils.getCurrentTenantId());
            question.setId(id);
            if (exists(id.getQuestionId())){
                log.info("Question {} already exist in system. File - {}, Position - {}", id.getQuestionId(), fileName, rowCount);
                continue;
            }

            Question.QuestionAnswer answer = new Question.QuestionAnswer();
            List<Question.QuestionOption> options = new ArrayList<>();

            for (Map.Entry<String, Object> entry: record.entrySet()){
                String key = entry.getKey().toLowerCase();
                Object value = entry.getValue();
                switch (key){
                    case "name":
                        question.setName(String.valueOf(value));
                        break;
                    case "type":
                        question.setType(String.valueOf(value));
                        break;
                    case "subject":
                        question.setSubject(String.valueOf(value));
                        break;
                    case "topic":
                        question.setTopic(String.valueOf(value));
                        break;
                    case "sub_topic":
                        question.setSubTopic(String.valueOf(value));
                        break;
                    case "difficulty_level":
                        question.setDifficultyLevel(String.valueOf(value));
                        break;
                    case "reference":
                        question.setReference(String.valueOf(value));
                        break;
                    case "description":
                        question.setDescription(String.valueOf(value));
                        break;
                    case "passage":
                        String content = String.valueOf(value).trim();
                        if (!StringUtils.isEmpty(content)){
                            question.setPassageId(savePassageContent(content));
                            question.setPassageContent("");
                        }
                        break;
                    case "tags":
                        if (!StringUtils.isEmpty(String.valueOf(value).trim())) {
                            question.setTags(Arrays.asList(String.valueOf(value).split("\\|")));
                        }
                        break;
                    case "correct_answer":
                        if (!StringUtils.isEmpty(String.valueOf(value).trim())) {
                            answer.setAnswerText(String.valueOf(value));
                        }
                        break;
                    case "correct_options":
                        if (!StringUtils.isEmpty(String.valueOf(value).trim())) {
                            answer.setOptions(Arrays.asList(String.valueOf(value).split("\\|")));
                        }
                        break;
                    case "explanation":
                        if (!StringUtils.isEmpty(String.valueOf(value).trim())) {
                            question.setExplanation(String.valueOf(value));
                        }
                        break;
                    case "positive_marks":
                        if (!StringUtils.isEmpty(String.valueOf(value).trim())) {
                            question.setPositiveMark(Double.parseDouble(String.valueOf(value)));
                        }
                        break;
                    case "negative_marks":
                        if (!StringUtils.isEmpty(String.valueOf(value).trim())) {
                            question.setNegativeMark(Double.parseDouble(String.valueOf(value)));
                        }
                        break;
                    case "skip_marks":
                        if (!StringUtils.isEmpty(String.valueOf(value).trim())) {
                            question.setSkipMark(Double.parseDouble(String.valueOf(value)));
                        }
                        break;
                    case "migration_test_id":
                    case "migration_test_name":
                    case "migration_section_id":
                    case "migration_section_name":
                        if (!StringUtils.isEmpty(String.valueOf(value).trim())) {
                            String k = key.split("migration_")[1];
                            question.addMigrationProperty(k, value);
                        }
                        break;
                    default:
                        if (key.startsWith("option") && key.contains("_") && key.split("_").length == 2){
                            if(!StringUtils.isEmpty(String.valueOf(value).trim())) {
                                String[] arr = key.split("_");
                                Question.QuestionOption option = new Question.QuestionOption();
                                option.setKey(arr[1]);
                                option.setValue(String.valueOf(value));
                                options.add(option);
                            }
                        }else {
                            String message = String.format("Unknown key %s found in question bulk upload file %s", key, fileName);
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
                        }
                }
            }
            if (!options.isEmpty()){
                question.setOptions(options);
            }
            question.setAnswer(answer);
//                question.addTags(fileName);
//            question.setFileName(file.getOriginalFilename());
            question.setFileName(new File(fileName).getName());
            question.setCreatedBy(AuthUtils.getCurrentQualifiedUsername());
            question.setCreatedOn(new Date());

            /* validate question modal */
            try{
                /* constraints violations */
                Set<ConstraintViolation<Question>> violations = validator.validate(question);
                if (!violations.isEmpty()) {
                    throw new ConstraintViolationException(violations);
                }

                /* business checks */
                validateQuestion(question);
                /* keep question */
                questions.add(question);
            }catch (Exception e){
                error.append("Question - ").append(rowCount).append(", Error - ").append(e.getMessage()).append("\n");
            }
        }
        Map<String, Question> unique = new HashMap<>();
        questions.forEach(q-> {
            if (!unique.containsKey(q.getId().getQuestionId())){
                unique.put(q.getId().getQuestionId(), q);
            }
        });
        /* save questions */
        if (error.toString().trim().isEmpty()){
            log.info("File - {}, Saved - {}, Duplicates - {}", fileName, unique.size(), questions.size()-unique.size());
            questionRepository.saveAll(unique.values());
        }else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, error.toString());
        }
    }

	private String readOption(Object option) {
		return StringUtils.isNotEmpty(String.valueOf(option)) ? String.valueOf(option).trim() : "";
	}

    @Override
    public boolean exists(String questionId) {
        QuestionId id = new QuestionId();
        id.setQuestionId(questionId);
        id.setTenantId(AuthUtils.getCurrentTenantId());
        return questionRepository.existsById(id);
    }

    @Override
    public Question getQuestion(String questionId, boolean loadPassageContent) {
        QuestionId id = new QuestionId();
        id.setQuestionId(questionId);
        id.setTenantId(AuthUtils.getCurrentTenantId());
        Question question = questionRepository.findById(id).orElse(null);
        if (loadPassageContent && question != null && StringUtils.isNotEmpty(question.getPassageId())){
            PassageId pid = new PassageId();
            pid.setTenantId(question.getId().getTenantId());
            pid.setPassageId(question.getPassageId());
            passageRepository.findById(pid).ifPresent(passage -> question.setPassageContent(passage.getContent()));
        }
        return question;
    }

    private String savePassageContent(String passageContent) {
        String passageId = generatePassageId(passageContent, true);

        PassageId id = new PassageId();
        id.setPassageId(passageId);
        id.setTenantId(AuthUtils.getCurrentTenantId());

        Passage passage = passageRepository.findById(id).orElse(null);
        if (passage == null){
            passage = new Passage();
            passage.setId(id);
            passage.setContent(passageContent);
            passageRepository.save(passage);
        }
        return passageId;
    }

    @Override
    public void updateQuestion(Question question) {
        Question currentQuestion = questionRepository.findById(question.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Question with id %s not found", question.getId().getQuestionId())));

        if (!StringUtils.isEmpty(question.getPassageContent())){
            question.setPassageId(savePassageContent(question.getPassageContent()));
            question.setPassageContent("");
        }
        validateQuestion(question);

		boolean isQuestionAssocatedToNonDraftQuestionPaper = false;
        /* check if question part of verified/published/archived question-paper */
        if (currentQuestion.getUsedInPapers() != null){
            for (Map.Entry<String, String> entry: currentQuestion.getUsedInPapers().entrySet()){
                String paperId = entry.getKey();
                String status = entry.getValue();
                if (!QuestionPaperStatus.DRAFT.value().equalsIgnoreCase(status)){
					isQuestionAssocatedToNonDraftQuestionPaper = true;
					break;
					// throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					// String.format("Can't update question with id %s. It is part of %s
					// question-paper %s",
					// question.getId().getQuestionId(), status, paperId));

                }
            }
        }

        /* build question modal & save */
		// if question is associated to a test which is not in Draft State , do not
		// update these fields
		if (!isQuestionAssocatedToNonDraftQuestionPaper) {
			// edit every thing. keep this for future reference.
		}
		currentQuestion.setName(question.getName());
		currentQuestion.setDescription(question.getDescription());
		currentQuestion.setType(question.getType());
		currentQuestion.setPassageId(question.getPassageId());
		currentQuestion.setPassageContent(question.getPassageContent());
		currentQuestion.setOptions(question.getOptions());
		currentQuestion.setAnswer(question.getAnswer());
		currentQuestion.setPositiveMark(question.getPositiveMark());
		currentQuestion.setNegativeMark(question.getNegativeMark());
		currentQuestion.setSkipMark(question.getSkipMark());
		currentQuestion.setReference(question.getReference());
		currentQuestion.setExplanation(question.getExplanation());
        currentQuestion.setTopic(question.getTopic());
        currentQuestion.setSubTopic(question.getSubTopic());
		currentQuestion.setTags(question.getTags());
		currentQuestion.setSubject(question.getSubject());
		currentQuestion.setDifficultyLevel(question.getDifficultyLevel());
		currentQuestion.setVideoExplanationUrl(question.getVideoExplanationUrl());

        questionRepository.save(currentQuestion);
    }

//    @Override
//    public List<Question> getQuestionsByTag(String tag) {
//        if (StringUtils.isEmpty(tag)){
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question tag is empty");
//        }
//        Query query = new Query();
//        query.addCriteria(Criteria.where("_id.tenantId").is(AuthUtils.getCurrentTenantId()));
//        query.addCriteria(Criteria.where("tags").is(tag));
//        query.with(Sort.by("passageId").ascending());
//        return updatePassageContent(mongoTemplate.find(query, Question.class));
//    }
//
//    @Override
//    public List<Question> getQuestionsByFileName(String fileName) {
//        if (StringUtils.isEmpty(fileName)){
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filename is empty");
//        }
//        Query query = new Query();
//        query.addCriteria(Criteria.where("_id.tenantId").is(AuthUtils.getCurrentTenantId()));
//        query.addCriteria(Criteria.where("fileName").is(fileName));
//        query.with(Sort.by("passageId").ascending());
//        return updatePassageContent(mongoTemplate.find(query, Question.class));
//    }

    private List<Question> updatePassageContent(List<Question> questions){
        for (Question question: questions){
            /* set passage content */
            if (StringUtils.isNotEmpty(question.getPassageId())) {
                PassageId pid = new PassageId();
                pid.setTenantId(question.getId().getTenantId());
                pid.setPassageId(question.getPassageId());
                passageRepository.findById(pid).ifPresent(passage -> question.setPassageContent(passage.getContent()));
            }
        }
        return questions;
    }

    @Override
    public List<String> getQuestionTags() {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id.tenantId").is(AuthUtils.getCurrentTenantId()));
        return mongoTemplate.findDistinct(query, "tags", Question.COLLECTION_NAME, String.class);
    }

    @Override
    public void validateQuestion(Question question){

        /* validate question type */
        if (!QuestionType.getQuestionTypes().contains(question.getType())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Question Type %s not valid", question.getType()));
        }

        /* validate passage-id */
        if (QuestionType.PASSAGE.value().equalsIgnoreCase(question.getType())) {
            if (StringUtils.isEmpty(question.getPassageId())){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Passage content not supplied for question type %s", question.getType()));
            }
            PassageId passageId = new PassageId();
            passageId.setPassageId(question.getPassageId());
            passageId.setTenantId(AuthUtils.getCurrentTenantId());
            passageRepository.findById(passageId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Passage with id %s not found", question.getPassageId())));
        }

        /* validate options */
        if ((QuestionType.MCQ.value().equalsIgnoreCase(question.getType())
                || QuestionType.FILL_IN_THE_BLANKS.value().equalsIgnoreCase(question.getType())
                || QuestionType.PASSAGE.value().equalsIgnoreCase(question.getType())
        )
                && (question.getOptions() == null || question.getOptions().isEmpty())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Options not supplied for question type %s", question.getType()));
        }
        /* none options should be null/empty  */
        if (QuestionType.MCQ.value().equalsIgnoreCase(question.getType())
                || QuestionType.FILL_IN_THE_BLANKS.value().equalsIgnoreCase(question.getType())
                || QuestionType.PASSAGE.value().equalsIgnoreCase(question.getType())){
            for (Question.QuestionOption option: question.getOptions()){
                if (StringUtils.isEmpty(option.getKey()) || StringUtils.isEmpty(option.getValue())){
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Invalid option key/value for question type %s", question.getType()));
                }
            }
        }
        /* none options should have same key/value */
        if (QuestionType.MCQ.value().equalsIgnoreCase(question.getType())
                || QuestionType.FILL_IN_THE_BLANKS.value().equalsIgnoreCase(question.getType())
                || QuestionType.PASSAGE.value().equalsIgnoreCase(question.getType())){
            List<String> keys = new ArrayList<>();
            List<String> values = new ArrayList<>();
            for (Question.QuestionOption option: question.getOptions()){
                if (keys.contains(option.getKey())){
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Options keys %s must be unique", option.getKey()));
                }
                keys.add(option.getKey());

                if (values.contains(option.getValue())){
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Options values %s must be unique", option.getValue()));
                }
                values.add(option.getValue());
            }
        }

        /* answer validation */
        if ((QuestionType.MCQ.value().equalsIgnoreCase(question.getType())
                || QuestionType.FILL_IN_THE_BLANKS.value().equalsIgnoreCase(question.getType())
                || QuestionType.PASSAGE.value().equalsIgnoreCase(question.getType())
        )
                && (question.getAnswer().getOptions() == null || question.getOptions().isEmpty())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Answer Options not supplied for question type %s", question.getType()));
        }

        /* tita question's answer */
        if (QuestionType.TITA.value().equalsIgnoreCase(question.getType()) && (StringUtils.isEmpty(question.getAnswer().getAnswerText()))){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Answer text not supplied for question type %s", question.getType()));
        }
    }

    @Override
    public String createQuestion(Question question) {
        QuestionId id = new QuestionId();
        id.setQuestionId(String.valueOf(StringUtility.zeroAllocationHash(question.getName() + question.getPassageContent())));
        id.setTenantId(AuthUtils.getCurrentTenantId());
        question.setId(id);

        Set<ConstraintViolation<Question>> violations = validator.validate(question);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        if (question.getPassageContent() != null && !question.getPassageContent().trim().isEmpty()){
            String pid = savePassageContent(question.getPassageContent());
            question.setPassageId(pid);
            question.setPassageContent("");
        }

        validateQuestion(question);
        if (exists(id.getQuestionId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question already exist");
        question.setCreatedBy(AuthUtils.getCurrentQualifiedUsername());
        question.setCreatedOn(new Date());
        question.setUsedInPapers(new HashMap<>());
        questionRepository.save(question);
        return question.getId().getQuestionId();
    }

    @Override
    public QuestionPaginatedResponse searchQuestions(SearchQuestion filter) {
        if (filter.getPageNumber() <= 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Invalid page number - %s", filter.getPageNumber()));
        }
        
        List<String> exclusionQuestionId = new ArrayList<>();
        if(StringUtils.isNotBlank(filter.getTestIdToBeExcluded())) {
        	QuestionPaperId id = new QuestionPaperId();
            id.setTenantId(AuthUtils.getCurrentTenantId());
            id.setQuestionPaperId(filter.getTestIdToBeExcluded());
            QuestionPaper questionPaperToExclude = testConfigRepository.findById(id).orElse(null);
            if(questionPaperToExclude != null ) {
            	for (PaperSection section : questionPaperToExclude.getSections().values()) {
            		if(section!= null && 
            				section.getQuestions()!= null 
            				&& section.getQuestions().size() > 0)
	            		section.getQuestions().values()
	            		.forEach(e -> exclusionQuestionId.add(e.getId()));
				}
            }
        }
        
        Query query = new Query();

        List<Criteria> criterias = new ArrayList<>();

        if (StringUtils.isNotEmpty(filter.getQuestionId())){
            criterias.add(Criteria.where("_id.questionId").is(filter.getQuestionId()));
        }
        
        if(!exclusionQuestionId.isEmpty()) {
        	criterias.add(Criteria.where("_id.questionId").nin(exclusionQuestionId));
        }
        if (StringUtils.isNotEmpty(filter.getType())){
            criterias.add(Criteria.where("type").is(filter.getType()));
        }
        if (StringUtils.isNotEmpty(filter.getSubject())){
            criterias.add(Criteria.where("subject").is(filter.getSubject()));
        }
        if (StringUtils.isNotEmpty(filter.getFilename())){
            criterias.add(Criteria.where("fileName").is(filter.getFilename()));
        }
        if (StringUtils.isNotEmpty(filter.getTopic())){
            criterias.add(Criteria.where("topic").is(filter.getTopic()));
        }
        if (StringUtils.isNotEmpty(filter.getSubTopic())){
            criterias.add(Criteria.where("subTopic").is(filter.getSubTopic()));
        }
        if (StringUtils.isNotEmpty(filter.getMigrationTestId())){
            criterias.add(Criteria.where("migration.test_id").is(filter.getMigrationTestId()));
        }
        if (StringUtils.isNotEmpty(filter.getMigrationTestName())){
            criterias.add(Criteria.where("migration.test_name").is(filter.getMigrationTestName()));
        }
        if (StringUtils.isNotEmpty(filter.getMigrationSectionId())){
            criterias.add(Criteria.where("migration.section_id").is(filter.getMigrationSectionId()));
        }
        if (StringUtils.isNotEmpty(filter.getMigrationSectionName())){
            criterias.add(Criteria.where("migration.section_name").is(filter.getMigrationSectionName()));
        }
        if (filter.getTags() != null && !filter.getTags().isEmpty()){
            Criteria tc = new Criteria();
            List<Criteria> tags = new ArrayList<>();
            for (String tag : filter.getTags()) {
                tags.add(Criteria.where("tags").is(tag));
            }
            tc.orOperator(tags.toArray(new Criteria[0]));
            criterias.add(tc);
        }
        Criteria rangeCriteria = null;
        if (StringUtils.isNotEmpty(filter.getUpdateStartTime())){
            rangeCriteria = Criteria.where("lastUpdatedOn")
                    .gte(TimeUtility.convertTxtToDateTime(filter.getUpdateStartTime(), TimeUtility.WIDE_FORMAT, StringUtility.getClientTimezone()));
        }
        if (StringUtils.isNotEmpty(filter.getUpdateEndTime())){
            if (rangeCriteria != null){
                rangeCriteria.lte(TimeUtility.convertTxtToDateTime(filter.getUpdateEndTime(), TimeUtility.WIDE_FORMAT, StringUtility.getClientTimezone()));
                criterias.add(rangeCriteria);
            }else {
                Criteria etc = Criteria.where("lastUpdatedOn")
                        .lte(TimeUtility.convertTxtToDateTime(filter.getUpdateEndTime(), TimeUtility.WIDE_FORMAT, StringUtility.getClientTimezone()));
                criterias.add(etc);
            }
        }

        Criteria queryFinalCriteria = Criteria.where("_id.tenantId").is(AuthUtils.getCurrentTenantId());

        if (StringUtils.isNotEmpty(filter.getNameRegexPattern())){
            Criteria description = Criteria.where("description").regex(Pattern.quote(filter.getNameRegexPattern()), "idx");
            Criteria filename = Criteria.where("fileName").regex(Pattern.quote(filter.getNameRegexPattern()), "idx");
            Criteria name = Criteria.where("name").regex(Pattern.quote(filter.getNameRegexPattern()), "idx");
            Criteria nc = new Criteria().orOperator(name, description, filename);
            criterias.add(nc);
        }
        if (!criterias.isEmpty()){
            Criteria [] array = criterias.toArray(new Criteria[0]);
            queryFinalCriteria.andOperator(array);
        }
        query.addCriteria(queryFinalCriteria);

        /* sort */
        if (StringUtils.isNotEmpty(filter.getSortColumn()) && Arrays.asList("asc", "desc").contains(String.valueOf(filter.getSortOrder()).toLowerCase())){
            if ("asc".equalsIgnoreCase(filter.getSortOrder())){
                query.with(Sort.by(Sort.Direction.ASC, filter.getSortColumn()));
            }else if ("desc".equalsIgnoreCase(filter.getSortOrder())){
                query.with(Sort.by(Sort.Direction.DESC, filter.getSortColumn()));
            }
        }
        if (!"questionId".equalsIgnoreCase(filter.getSortColumn())){
            query.with(Sort.by(Sort.Direction.ASC, "_id.questionId"));
        }
        int limit = filter.getPageSize() > 0 && filter.getPageSize() <= 100 ? filter.getPageSize() : 100;

        /* get page specific records */
        int actualPage = filter.getPageNumber() > 0 ? filter.getPageNumber(): 1;
        long skipRecords = 0;
        if (actualPage > 1){
            if (!filter.isNext()) {
                actualPage--;
            }
            skipRecords = (long) (actualPage - 1) * limit;
        }

        /* get total record */
        long totalRecords = mongoTemplate.count(query, Question.class);

        /* limit */
        if (skipRecords > 0){
            query.skip(skipRecords);
        }
        query.limit(limit);

        QuestionPaginatedResponse response = new QuestionPaginatedResponse();
        response.setPageSize(limit);
        response.setTotalRecords(totalRecords);
        response.setPageNumber(actualPage);
        /* execute query */
        List<Question> questions;
        if (totalRecords > 0) {
            questions = updatePassageContent(mongoTemplate.find(query, Question.class));
//            response.setPaginatedRowId(questions.get(questions.size()-1).getId().getQuestionId());
        }else {
            questions = new ArrayList<>();
        }
        response.setQuestions(questions);
        return response;
    }

    @Override
    public void unmarkQuestionUsage(String questionPaperId, List<String> questions) {
        List<Question> entities = new ArrayList<>();
        for (String q: questions){
            Question question = getQuestion(q, false);
            if (question != null){
                question.unmarkUsage(questionPaperId);
                entities.add(question);
            }
        }
        if (!entities.isEmpty()){
            questionRepository.saveAll(entities);
        }
    }

    @Override
    public void markQuestionUsage(String questionPaperId, String status, List<String> questions) {
        List<Question> entities = new ArrayList<>();
        for (String q: questions){
            Question question = getQuestion(q, false);
            if (question != null){
                question.markUsage(questionPaperId, status);
                entities.add(question);
            }
        }
        if (!entities.isEmpty()){
            questionRepository.saveAll(entities);
        }
    }

    @Override
    public void deleteQuestion(String questionId) {
        Question question = getQuestion(questionId, false);
        if (question == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Question with id %s does not exist", questionId));

        /* avoid deletion if part of question papers */
        if (question.getUsedInPapers() != null && !question.getUsedInPapers().isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Can't delete question with id %s. Question is part of %s question papers", questionId, question.getUsedInPapers().size()));
        }
        questionRepository.deleteById(question.getId());
    }

    @Override
    public Map<String, ?> bulkDeleteQuestions(List<String> questionIds) {
        Map<String, Object> operationStatus = new HashMap<>();
        List<String> success = new ArrayList<>();
        Map<String, String> failed = new HashMap<>();
        operationStatus.put("success", success);
        operationStatus.put("failed", failed);
        for (String q: questionIds){
            try{
                deleteQuestion(q);
                success.add(q);
            }catch (Exception e){
                failed.put(q, e.getMessage());
            }
        }
        return operationStatus;
    }

    @Override
    public List<String> getQuestionSubjects() {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id.tenantId").is(AuthUtils.getCurrentTenantId()));
        return mongoTemplate.findDistinct(query, "subject", Question.COLLECTION_NAME, String.class);
    }

    @Override
    public List<String> getQuestionTopics() {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id.tenantId").is(AuthUtils.getCurrentTenantId()));
        return mongoTemplate.findDistinct(query, "topic", Question.COLLECTION_NAME, String.class);
    }

    @Override
    public List<String> getQuestionSubTopics() {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id.tenantId").is(AuthUtils.getCurrentTenantId()));
        return mongoTemplate.findDistinct(query, "subTopic", Question.COLLECTION_NAME, String.class);
    }

    @Override
    public void updateQuestionTags(String questionId, List<String> tags) {
        QuestionId id = new QuestionId();
        id.setQuestionId(questionId);
        id.setTenantId(AuthUtils.getCurrentTenantId());
        Question question = questionRepository.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question does not exist"));
        question.setTags(tags);
        questionRepository.save(question);
    }

	@Override
	public void metadataQuestionBulkUpdate(MultipartFile file) throws IOException, CsvValidationException {
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

			initMetadataQuestionBulkUpdate(fileName);
		} catch (IOException | CsvValidationException e) {
			throw e;
		} finally {
			if (tempFile != null && tempFile.exists()) {
				FileUtility.delete(tempFile);
			}
		}
	}

	@Override
	public void initMetadataQuestionBulkUpdate(String filePath) throws IOException, CsvValidationException {
		/* read file & load all question */
		StringBuilder error = new StringBuilder();
		List<Question> questions = new ArrayList<>();
		IFileDecoder decoder;
		if (filePath.endsWith(".csv")) {
			decoder = new CsvFileDecoder(filePath);
		} else if (filePath.endsWith(".xlsx") || filePath.endsWith(".xls")) {
			decoder = new XLReader(filePath);
		} else {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Unsupported file format. File - %s", filePath));
		}
		Path fileName = Paths.get(filePath).getFileName();
		String questionPaperId = fileName.toString().substring(0, fileName.toString().indexOf('-')).trim();
		String questionPaperFileName = questionPaperId + ".xlsx";

		log.info("updating question for {}. File name {}", questionPaperFileName, fileName.toString());

		int rowCount = 0;
		// get all questions associated to this test based on the file name
		Query getQuestionByFileName = new Query();
		getQuestionByFileName.addCriteria(Criteria.where("fileName").is(questionPaperFileName));
		List<Question> questionsForFile = mongoTemplate.find(getQuestionByFileName, Question.class);
		if (questionsForFile == null || questionsForFile.isEmpty()) {
			log.error("No Question found for file {}", questionPaperFileName);
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, questionPaperFileName);
		}
		// get all passages
		List<Passage> qPassages = mongoTemplate.findAll(Passage.class);
		while (decoder.hasNext()) {
			rowCount++;
			Map<String, Object> record = decoder.next();

			String questionName = String.valueOf(record.get("Name"));
			String passage = String.valueOf(record.get("Passage"));
			// get Passage object associated with Passage Id
			Passage matchedPassage = StringUtils.isEmpty(passage) ? null
					: qPassages.stream()
							.filter(x -> passage.replaceAll(" ", "").equalsIgnoreCase(
									StringUtility.html2text(x.getContent(), true).replaceAll(" ", "")))
							.findFirst()
					.orElse(null);
			// get question from the list if matched successfully based on Name and Passage
			List<Question> mappedQuestion = questionsForFile.stream().filter(q -> {
				if ((matchedPassage == null
						|| (q.getPassageId() != null && q.getPassageId().equals(matchedPassage.getId().getPassageId())))
						&& (StringUtils.isEmpty(questionName)
								|| questionName.replaceAll(" ", "").equals(StringUtility
										.html2text(q.getName().replaceAll("<br>", ""), true).replaceAll(" ", "")))) {
					return true;
				}
				return false;
			}).collect(Collectors.toList());

			if (mappedQuestion == null || mappedQuestion.isEmpty() || mappedQuestion.size() != 1) {
				log.info("Question : - '{}' is not unique. File - {}, Position - {}", questionName, fileName, rowCount);
				continue;
			}

			/* prepare & validate question modal */
			Question question = mappedQuestion.get(0);

			for (Map.Entry<String, Object> entry : record.entrySet()) {
				String key = entry.getKey().toLowerCase();
				Object value = entry.getValue();
				switch (key) {
				case "subject":
					question.setSubject(String.valueOf(value));
					break;
				case "topic":
					question.setTopic(String.valueOf(value));
					break;
				case "sub topic":
					question.setSubTopic(String.valueOf(value));
					break;
				case "difficulty level":
					question.setDifficultyLevel(getDifficultyLevel(String.valueOf(value)));
					break;
				}
			}
			question.setLastUpdatedBy(AuthUtils.getCurrentQualifiedUsername());
			question.setLastUpdatedOn(new Date());

			/* validate question modal */
			try {
				/* constraints violations */
				Set<ConstraintViolation<Question>> violations = validator.validate(question);
				if (!violations.isEmpty()) {
					throw new ConstraintViolationException(violations);
				}

				/* keep question */
				questions.add(question);
			} catch (Exception e) {
				error.append("Question - ").append(rowCount).append(", Error - ").append(e.getMessage()).append("\n");
			}
		}
		Map<String, Question> unique = new HashMap<>();
		questions.forEach(q -> {
			if (!unique.containsKey(q.getId().getQuestionId())) {
				unique.put(q.getId().getQuestionId(), q);
			}
		});
		/* save questions */
		if (error.toString().trim().isEmpty()) {
			log.info("File - {}, Saved - {}, Duplicates - {}", filePath, unique.size(),
					questions.size() - unique.size());
			questionRepository.saveAll(unique.values());
		} else {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, error.toString());
		}
	}


	private String getDifficultyLevel(String difficultyId) {
		if ("1.0".equals(difficultyId)) {
			return DifficultyLevel.EASY.value();
		} else if ("2.0".equals(difficultyId)) {
			return DifficultyLevel.MEDIUM.value();
		} else if ("3.0".equals(difficultyId)) {
			return DifficultyLevel.HARD.value();
		} else if ("4.0".equals(difficultyId)) {
			return DifficultyLevel.VERY_HARD.value();
		}
		return "EASY";
	}

	@Override
	public List<String> updateQuestionVideoUrl() throws IOException, CsvValidationException {
		List<String> updates = new ArrayList<>();
		try {
			Criteria explanationWithVideo = Criteria.where("explanation").regex(Pattern.quote("vimeo"), "idx");
			Query query = new Query(explanationWithVideo);
			List<Question> questionsToUpdateVideoExplanation = mongoTemplate.find(query, Question.class);
			// int parallel = 20;
			// ThreadPoolExecutor executor = new ThreadPoolExecutor(parallel, parallel, 10,
			// TimeUnit.DAYS,
			// new ArrayBlockingQueue<>(10_000));
			// questionsToUpdateVideoExplanation.forEach(question -> executor.execute(() ->
			// {
			// String videoUrl = question.getExplanation().substring(
			// question.getExplanation().indexOf("https://vimeo"),
			// question.getExplanation().length());
			// // actual update happens here.
			// question.setVideoExplanationUrl(videoUrl);
			// updates.add("Question : " + question.getId().getQuestionId() + " updated with
			// URL : " + videoUrl);
			// // questionRepository.save(question);
			// }));
			for (Question question : questionsToUpdateVideoExplanation) {
				String videoUrl = question.getExplanation().substring(
						question.getExplanation().indexOf("https://vimeo"), question.getExplanation().length());
				// actual update happens here.
				question.setVideoExplanationUrl(videoUrl);
				updates.add("Question : " + question.getId().getQuestionId() + " updated with URL : " + videoUrl);

			}
			/* save questions */
			questionRepository.saveAll(questionsToUpdateVideoExplanation);
		} finally {

		}

		return updates;

	}

}
