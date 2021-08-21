package com.assessment.questionpaper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.assessment.common.DateUtility;
import com.assessment.common.StringUtility;
import com.assessment.iam.commons.AuthUtils;
import com.assessment.iam.entities.User;
import com.assessment.iam.services.UserService;
import com.assessment.question.DifficultyLevel;
import com.assessment.question.Question;
import com.assessment.question.QuestionService;
import com.assessment.question.QuestionType;
import com.assessment.questionpaper.config.TestConfigService;
import com.assessment.questionpaper.dto.AnswerState;
import com.assessment.questionpaper.dto.AssignmentRequestDto;
import com.assessment.questionpaper.dto.AssignmentResponseDto;
import com.assessment.questionpaper.dto.EvaluationState;
import com.assessment.questionpaper.dto.QuestionPaperResponseDto;
import com.assessment.questionpaper.dto.QuestionPaperStatus;
import com.assessment.questionpaper.dto.SaveAnswerRequestDto;
import com.assessment.questionpaper.dto.SubmissionResponseDto;
import com.assessment.questionpaper.entity.Assignment;
import com.assessment.questionpaper.entity.AssignmentId;
import com.assessment.questionpaper.entity.Metric;
import com.assessment.questionpaper.entity.QuestionPaper;
import com.assessment.questionpaper.entity.Submission;
import com.assessment.questionpaper.entity.Submission.SectionSummary;
import com.assessment.questionpaper.entity.SubmissionId;
import com.assessment.questionpaper.report.TestSubmissionRepository;
import com.assessment.studentbatch.StudentBatch;
import com.assessment.studentbatch.StudentBatchService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TestAssignmentServiceImpl implements TestAssignmentService {

    @Autowired
    TestAssignmentRepository assignmentRepository;

    @Autowired
    TestSubmissionRepository submissionRepository;

    @Autowired
    TestConfigService testConfigService;

    @Autowired
    private StudentBatchService studentBatchService;

    @Autowired
    QuestionService questionService;

    @Autowired
    private UserService userService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private Validator validator;

    @Override
    public String assignTest(AssignmentRequestDto dto) {

        if ((dto.getAssignedToBatch() == null || dto.getAssignedToBatch().isEmpty())
                && (dto.getAssignedToStudent() == null || dto.getAssignedToStudent().isEmpty())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Test not assigned to any batch/student");
        }
        if(!isTestAssignable(dto.getTestId())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Test not published. Id - %s", dto.getTestId()));
        }

        /* build */
        AssignmentId id = new AssignmentId();
        id.setTenantId(AuthUtils.getCurrentTenantId());
        id.setAssignmentId(UUID.randomUUID().toString());

        Assignment assignment = new Assignment();
        assignment.setId(id);
        assignment.setTestId(dto.getTestId());
        assignment.setAssignedToBatch(dto.getAssignedToBatch());
        assignment.setAssignedToStudent(dto.getAssignedToStudent());
        assignment.setPasscode(dto.getPasscode());
        assignment.setDescription(dto.getDescription());
        assignment.setScoringType(dto.getScoringType());
        if (StringUtils.isNotEmpty(dto.getReleaseDate())){
            assignment.setReleaseDate(DateUtility.convertWideStringToDate(dto.getReleaseDate()));
        }
        if (StringUtils.isNotEmpty(dto.getValidFrom())){
            assignment.setValidFrom(DateUtility.convertWideStringToDate(dto.getValidFrom()));
        }
        if (StringUtils.isNotEmpty(dto.getValidTo())){
            assignment.setValidTo(DateUtility.convertWideStringToDate(dto.getValidTo()));
        }
        assignment.setTags(dto.getTags());
        assignment.setCreatedOn(new Date());
        assignment.setCreatedBy(AuthUtils.getCurrentQualifiedUsername());
        assignmentRepository.save(assignment);
        return id.getAssignmentId();
    }

    private Assignment getAssignmentEntity(String assignmentId) {
        AssignmentId id = new AssignmentId();
        id.setTenantId(AuthUtils.getCurrentTenantId());
        id.setAssignmentId(assignmentId);
        return assignmentRepository.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Assignment %s not found.", assignmentId)));
    }

    @Override
    public AssignmentResponseDto getAssignment(String assignmentId) {
        Assignment assignment = getAssignmentEntity(assignmentId);
        AssignmentResponseDto dto = assignment.toResponseDto();
        dto.setTestName(testConfigService.getQuestionPaper(dto.getTestId()).getName());
        return dto;
    }

    @Override
    public boolean exists(String assignmentId) {
        AssignmentId id = new AssignmentId();
        id.setTenantId(AuthUtils.getCurrentTenantId());
        id.setAssignmentId(assignmentId);
        return assignmentRepository.existsById(id);
    }

    @Override
    public boolean isAssignmentExistForTest(String testId) {
        Criteria filter = Criteria.where("_id.tenantId").is(AuthUtils.getCurrentTenantId())
                .andOperator(new Criteria("testId").is(testId));
        Query query = new Query();
        query.addCriteria(filter);
        long assignments = mongoTemplate.count(query, Assignment.class);
        return assignments > 0;
    }

    @Override
    public void deleteAssignment(String assignmentId) {
        Assignment assignment = getAssignmentEntity(assignmentId);
        if (!isSubmissionExists(assignment.getId().getAssignmentId())){
            assignmentRepository.deleteById(assignment.getId());
        }else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can not delete assignment. Student submission exists.");
        }
    }

    private boolean isSubmissionExists(String assignmentId) {
        Criteria filter = Criteria.where("_id.tenantId").is(AuthUtils.getCurrentTenantId())
                .andOperator(new Criteria("assignmentId").is(assignmentId));
        Query query = new Query();
        query.addCriteria(filter);
        long submissions = mongoTemplate.count(query, Submission.class);
        return submissions > 0;
    }

    @Override
    public void updateAssignment(String assignmentId, AssignmentRequestDto dto) {
        Assignment assignment = getAssignmentEntity(assignmentId);

        if (StringUtils.isNotEmpty(dto.getTestId())){
            if(isTestAssignable(dto.getTestId())){
                assignment.setTestId(dto.getTestId());
            }else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Test not published. Id - %s", dto.getTestId()));
            }
        }

        if (dto.getAssignedToBatch() != null && !dto.getAssignedToBatch().isEmpty()){
            assignment.setAssignedToBatch(dto.getAssignedToBatch());
        }
        if (dto.getAssignedToStudent() != null && !dto.getAssignedToStudent().isEmpty()){
            assignment.setAssignedToStudent(dto.getAssignedToStudent());
        }
        if (StringUtils.isNotEmpty(dto.getPasscode())){
            assignment.setPasscode(dto.getPasscode());
        }
        if (StringUtils.isNotEmpty(dto.getDescription())){
            assignment.setDescription(dto.getDescription());
        }
        if (StringUtils.isNotEmpty(dto.getScoringType())){
            assignment.setScoringType(dto.getScoringType());
        }
        if (StringUtils.isNotEmpty(dto.getReleaseDate())){
            assignment.setReleaseDate(DateUtility.convertWideStringToDate(dto.getReleaseDate()));
        }
        if (StringUtils.isNotEmpty(dto.getValidFrom())){
            assignment.setValidFrom(DateUtility.convertWideStringToDate(dto.getValidFrom()));
        }
        if (StringUtils.isNotEmpty(dto.getValidTo())){
            assignment.setValidTo(DateUtility.convertWideStringToDate(dto.getValidTo()));
        }
        if (dto.getTags() != null && !dto.getTags().isEmpty()){
            assignment.setTags(dto.getTags());
        }
        assignmentRepository.save(assignment);
    }

    private boolean isTestAssignable(String testId) {
        QuestionPaper qp = testConfigService.getEntity(testId);
        return QuestionPaperStatus.PUBLISHED.value().equalsIgnoreCase(qp.getStatus());
    }

    @Override
    public List<AssignmentResponseDto> listStudentAssignmentsByTest(String testId) {
        QuestionPaper qp = testConfigService.getEntity(testId);
        Criteria filter = Criteria.where("_id.tenantId").is(AuthUtils.getCurrentTenantId())
                .andOperator(new Criteria("testId").is(testId));
        Query query = new Query();
        query.addCriteria(filter);
        List<Assignment> assignments = mongoTemplate.find(query, Assignment.class);

        List<AssignmentResponseDto> responseDtos = new ArrayList<>();
        if (!assignments.isEmpty()){
            for (Assignment assignment: assignments){
                AssignmentResponseDto dto = assignment.toResponseDto();
                dto.setTestName(qp.getName());
                responseDtos.add(dto);
            }
        }
        return responseDtos;
    }

    @Override
    public List<AssignmentResponseDto> listAssignmentsByEmail(String emailId) {
        /* get student details */
        User student = userService.getUserByEmail(emailId);
		/* get user associated batches. */
		List<StudentBatch> batches = studentBatchService.userAssociatedBatches(emailId);
        /* batch condition */
        Criteria batch = null;
        if (batches != null && !batches.isEmpty()){
            batch = Criteria.where("_id.tenantId").is(AuthUtils.getCurrentTenantId());
            Criteria c12 = new Criteria();
            List<Criteria> criteria = new ArrayList<>();
            for (StudentBatch sb: batches){
                criteria.add(Criteria.where("assignedToBatch").is(sb.getId().getBatchId()));
            }
            c12.orOperator(criteria.toArray(new Criteria[0]));
            batch.andOperator(c12);
        }

        /* individual condition */
		Criteria individual = Criteria.where("_id.tenantId").is(AuthUtils.getCurrentTenantId())
				.andOperator(Criteria.where("assignedToStudent").is(student.getUsername()));

        /* get individual assignments */
        Query query1 = new Query();
        query1.addCriteria(individual);
        List<Assignment> assignments = mongoTemplate.find(query1, Assignment.class);

        /* get batch assignments */
        if (batch != null) {
            Query query2 = new Query();
            query2.addCriteria(batch);
            List<Assignment> result2 = mongoTemplate.find(query2, Assignment.class);
			assignments.addAll(result2);

        }

        List<AssignmentResponseDto> responseDtos = new ArrayList<>();

        if (!assignments.isEmpty()){
			Set<String> idsProcessed = new HashSet<String>();
            for (Assignment assignment: assignments){
				if (idsProcessed.contains(assignment.getId().getAssignmentId())) {
					continue;
				} else {
					idsProcessed.add(assignment.getId().getAssignmentId());
				}
                AssignmentResponseDto dto = assignment.toResponseDto();
				QuestionPaper questionPaper = testConfigService.getEntity(dto.getTestId());
				dto.setTestName(questionPaper.getName());
				dto.setTestType(questionPaper.getType());
				dto.setTags(questionPaper.getTags());
                /* enrich student submission state */
				Submission submission = getSubmissionEntity(dto.getAssignmentId(), student.getUsername(), true);
				if (submission != null) {
					dto.setAttempted(submission.isSubmitted());
					if (EvaluationState.COMPLETED.value().equalsIgnoreCase(submission.getEvaluation())) {
						dto.setTotalMarks(submission.getSummary().getMetric().getTotalMarks());
						dto.setMarksReceived(submission.getSummary().getMetric().getMarksReceived());
					}
				}
                responseDtos.add(dto);
            }
        }
        /* return result */
        return responseDtos;
    }

    @Override
    public List<AssignmentResponseDto> listStudentAssignmentsByUsername(String username) {
        User user = userService.getUser(username).orElseThrow(()-> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User %s does not exist", username)));
        return listAssignmentsByEmail(user.getEmail());
    }

    @Override
    public List<AssignmentResponseDto> listMyAssignments() {
        User user = userService.getLoggedInUserDetails();
        return listAssignmentsByEmail(user.getEmail());
    }

    @Override
    public boolean isValidAssignmentPasscode(String assignmentId, String username, String passcode) {
        Assignment assignment = getAssignmentEntity(assignmentId);
        String email = userService.getUser(username).orElseThrow(()-> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Student not found. Student Id - %s", username))).getEmail();

        /* assigned to student check */
        if (assignment.getAssignedToStudent() != null && assignment.getAssignedToStudent().contains(username)){
            return assignment.getPasscode().equalsIgnoreCase(passcode);
        }

        /* assigned to batch check */
		List<StudentBatch> batches = studentBatchService.userAssociatedBatches(email);
        boolean isPartOfAssociatedBatch = false;
        for (StudentBatch batch: batches){
            if (assignment.getAssignedToBatch().contains(batch.getId().getBatchId())){
                isPartOfAssociatedBatch = true;
                break;
            }
        }
        if (isPartOfAssociatedBatch){
            return assignment.getPasscode().equalsIgnoreCase(passcode);
        }

        /* if all check fail, then student not part of assignment */
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Assignment %s not assigned to student %s", assignmentId, username));
    }

    private Submission getSubmissionEntity(String assignmentId, String studentId, boolean suppressException) {
        SubmissionId id = new SubmissionId();
        id.setTenantId(AuthUtils.getCurrentTenantId());
        id.setSubmissionId(getSubmissionKey(assignmentId, studentId));
        Submission submission = submissionRepository.findById(id).orElse(null);
        if (submission == null && !suppressException){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Student submission details not found. Student - %s, Assignment - %s", studentId, assignmentId));
        }
        return submission;
    }

    private String getSubmissionKey(String assignmentId, String studentId){
        return assignmentId + "|" + studentId;
    }

    private Submission initStudentSubmission(String assignmentId, String studentId) {
        Submission submission = new Submission();

        SubmissionId id = new SubmissionId();
        id.setTenantId(AuthUtils.getCurrentTenantId());
        id.setSubmissionId(getSubmissionKey(assignmentId, studentId));

        submission.setId(id);
        submission.setAssignmentId(assignmentId);
        submission.setStudentId(studentId);
        submission.setAttempt(1);
        submissionRepository.save(submission);
        return submissionRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Unable to initialize student submission. Assignment - %s, Student - %s", assignmentId, studentId)));
    }

    @Override
    public void saveStudentAnswer(String assignmentId, SaveAnswerRequestDto dto) {
        String studentId = AuthUtils.getCurrentQualifiedUsername();
        Submission submission = getSubmissionEntity(dto.getAssignmentId(), studentId, true);
        if (submission == null){
            submission = initStudentSubmission(dto.getAssignmentId(), studentId);
        }
        if (submission.isSubmitted()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Can't update test submission state. Test %s already submitted by student %s", assignmentId, studentId));
        }

        /* handle section presence */
        Submission.Section section;
        if (submission.getSections() == null || !submission.getSections().containsKey(dto.getSectionId())){
            section = new Submission.Section();
            section.setSectionId(dto.getSectionId());
        }else {
            section = submission.getSections().get(dto.getSectionId());
        }

        /* handle answer presence. Set/overwrite values */
        Submission.Answer answer;
        if (section.getAnswers() == null || !section.getAnswers().containsKey(dto.getQuestionId())){
            answer = new Submission.Answer();
            answer.setQuestionId(dto.getQuestionId());
            answer.setTimeElapsedInSec(dto.getTimeElapsedInSec());
        }else {
            answer = section.getAnswers().get(dto.getQuestionId());
            int previousElapsedTime = answer.getTimeElapsedInSec();
            answer.setTimeElapsedInSec(dto.getTimeElapsedInSec() + previousElapsedTime);
        }
        answer.setOptions(dto.getSelectedOptions());
        answer.setAnswerText(dto.getAnswerText());
        answer.setMarkForReview(dto.isMarkForReview());
        /* set submission values*/
        section.addAnswer(answer);
        submission.addSection(section);
		int previousTotalTimeTaken = submission.getTotalTestTimeTakenInSec();
		submission.setTotalTestTimeTakenInSec(dto.getTimeElapsedInSec() + previousTotalTimeTaken);

        /* update submission */
        submissionRepository.save(submission);
    }

    @Override
    public void clearStudentAnswer(String assignmentId, SaveAnswerRequestDto dto) {
        String studentId = AuthUtils.getCurrentQualifiedUsername();
        Submission submission = getSubmissionEntity(dto.getAssignmentId(), studentId, true);
        if (submission == null){
            submission = initStudentSubmission(dto.getAssignmentId(), studentId);
        }
        if (submission.isSubmitted()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Can't update test submission state. Test %s already submitted by student %s", assignmentId, studentId));
        }

        /* handle section presence */
        Submission.Section section;
        if (submission.getSections() == null || !submission.getSections().containsKey(dto.getSectionId())){
            section = new Submission.Section();
            section.setSectionId(dto.getSectionId());
        }else {
            section = submission.getSections().get(dto.getSectionId());
        }

        /* handle answer presence. Set/overwrite values */
        Submission.Answer answer;
        if (section.getAnswers() == null || !section.getAnswers().containsKey(dto.getQuestionId())){
            answer = new Submission.Answer();
            answer.setQuestionId(dto.getQuestionId());
            answer.setTimeElapsedInSec(dto.getTimeElapsedInSec());
        }else {
            answer = section.getAnswers().get(dto.getQuestionId());
            int previousElapsedTime = answer.getTimeElapsedInSec();
            answer.setTimeElapsedInSec(dto.getTimeElapsedInSec() + previousElapsedTime);
        }
        answer.setOptions(null);
        answer.setAnswerText("");
        answer.setMarkForReview(false);
        /* set submission values*/
        section.addAnswer(answer);
        submission.addSection(section);
		int previousTotalTimeTaken = submission.getTotalTestTimeTakenInSec();
		submission.setTotalTestTimeTakenInSec(dto.getTimeElapsedInSec() + previousTotalTimeTaken);

        /* update submission */
        submissionRepository.save(submission);
    }

    @Override
    public void markStudentAnswerForReview(String assignmentId, SaveAnswerRequestDto dto) {
        String studentId = AuthUtils.getCurrentQualifiedUsername();
        Submission submission = getSubmissionEntity(dto.getAssignmentId(), studentId, true);
        if (submission == null){
            submission = initStudentSubmission(dto.getAssignmentId(), studentId);
        }
        if (submission.isSubmitted()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Can't update test submission state. Test %s already submitted by student %s", assignmentId, studentId));
        }

        /* handle section presence */
        Submission.Section section;
        if (submission.getSections() == null || !submission.getSections().containsKey(dto.getSectionId())){
            section = new Submission.Section();
            section.setSectionId(dto.getSectionId());
        }else {
            section = submission.getSections().get(dto.getSectionId());
        }

        /* handle answer presence. Set/overwrite values */
        Submission.Answer answer;
        if (section.getAnswers() == null || !section.getAnswers().containsKey(dto.getQuestionId())){
            answer = new Submission.Answer();
            answer.setQuestionId(dto.getQuestionId());
            answer.setTimeElapsedInSec(dto.getTimeElapsedInSec());
        }else {
            answer = section.getAnswers().get(dto.getQuestionId());
            int previousElapsedTime = answer.getTimeElapsedInSec();
            answer.setTimeElapsedInSec(dto.getTimeElapsedInSec() + previousElapsedTime);
        }
        answer.setMarkForReview(true);
        /* set submission values */
        section.addAnswer(answer);
        submission.addSection(section);
		int previousTotalTimeTaken = submission.getTotalTestTimeTakenInSec();
		submission.setTotalTestTimeTakenInSec(dto.getTimeElapsedInSec() + previousTotalTimeTaken);

        /* update submission */
        submissionRepository.save(submission);
    }

    @Override
    public void submitStudentAssignment(String assignmentId, String username) {
        Submission submission = getSubmissionEntity(assignmentId, username, false);
        if (submission.isSubmitted()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Can't update test submission state. Test %s already submitted by student %s", assignmentId, username));
        }
        submission.setSubmitted(true);
        submission.setEvaluation(EvaluationState.PENDING.value());
        submission.setSubmissionTime(new Date());
        submissionRepository.save(submission);
    }

    @Override
    public SubmissionResponseDto getStudentAssignmentSubmissionState(String assignmentId, String username) {
        Submission submission = getSubmissionEntity(assignmentId, username, false);
        return buildSubmissionResponse(submission);
    }

    @Override
    public void deleteStudentAssignmentSubmission(String assignmentId, String username) {
        Submission submission = getSubmissionEntity(assignmentId, username, false);
        submissionRepository.deleteById(submission.getId());
    }

    private SubmissionResponseDto buildSubmissionResponse(Submission submission) {

        boolean enrichQuestionDetails = submission.isSubmitted();
        Map<String, Question> questionMap = new HashMap<>();
        if (enrichQuestionDetails) {
            List<Question> questions = testConfigService.getQuestionPaperLinkedQuestions(getAssignmentEntity(submission.getAssignmentId()).getTestId());
            if (questions != null) {
                questions.forEach(q -> questionMap.put(q.getId().getQuestionId(), q));
            }
        }
        QuestionPaper qp = testConfigService.getEntity(getAssignmentEntity(submission.getAssignmentId()).getTestId());

        SubmissionResponseDto responseDto = new SubmissionResponseDto();
        responseDto.setSubmissionId(submission.getId().getSubmissionId());
        responseDto.setAssignmentId(submission.getAssignmentId());
        responseDto.setStudentId(submission.getStudentId());
        responseDto.setSubmitted(submission.isSubmitted());
        responseDto.setSubmissionTime(submission.getSubmissionTime());
        responseDto.setAttempt(submission.getAttempt());
        responseDto.setEvaluation(submission.getEvaluation());
        responseDto.setLastUpdatedOn(submission.getLastUpdatedOn());
		responseDto.setTotalTestTimeTakenInSec(submission.getTotalTestTimeTakenInSec());
        /* test related details */
        responseDto.setTestId(qp.getId().getQuestionPaperId());
        responseDto.setTestName(qp.getName());
        /* summary */
        if (submission.getSummary() != null) {
            responseDto.setSummary(submission.getSummary().responseDto());
            for (SubmissionResponseDto.SectionSummaryResponseDto ssdto : responseDto.getSummary().getSections()) {
                ssdto.setSectionName(qp.getSections().get(ssdto.getSectionId()).getName());
            }
        }
        if (submission.getSections() != null){
            for (Submission.Section section: submission.getSections().values()){
                SubmissionResponseDto.SectionResponseDto sectionDto = new SubmissionResponseDto.SectionResponseDto();
                sectionDto.setSectionId(section.getSectionId());
                sectionDto.setSectionName(qp.getSections().get(section.getSectionId()).getName());
                if (section.getAnswers() != null){
                    for (Submission.Answer answer: section.getAnswers().values()){
                        SubmissionResponseDto.AnswerResponseDto dto = answer.responseDto();
                        String qid = answer.getQuestionId();

                        /* question related details */
                        if (enrichQuestionDetails && questionMap.containsKey(qid)){
                            dto.setName(questionMap.get(qid).getName());
                            dto.setType(questionMap.get(qid).getType());
                            dto.setCorrectAnswerText(questionMap.get(qid).getAnswer().getAnswerText());
                            dto.setCorrectOptions(questionMap.get(qid).getAnswer().getOptions());
                            dto.setExplanation(questionMap.get(qid).getExplanation());
							dto.setPassage(questionMap.get(qid).getPassageContent());
							dto.setSubject(questionMap.get(qid).getSubject());
                            dto.setTopic(questionMap.get(qid).getTopic());
                            dto.setSubTopic(questionMap.get(qid).getSubTopic());
							dto.setDifficultyLevel(questionMap.get(qid).getDifficultyLevel());
                            if (questionMap.get(qid).getOptions() != null){
                                List<SubmissionResponseDto.InputOption> options = new ArrayList<>();
                                for (Question.QuestionOption op: questionMap.get(qid).getOptions()){
                                    SubmissionResponseDto.InputOption option = new SubmissionResponseDto.InputOption();
                                    option.setKey(op.getKey());
                                    option.setValue(op.getValue());
                                    options.add(option);
                                }
                                dto.setInputOptions(options);
                            }
                        }
                        sectionDto.addAnswer(dto);
                    }
                }
                responseDto.addSection(sectionDto);
            }
        }
        return responseDto;
    }

    @Override
    public List<SubmissionResponseDto> listMySubmissions() {
        Query query = new Query();
        Criteria criteria = Criteria.where("_id.tenantId").is(AuthUtils.getCurrentTenantId());
        criteria.andOperator(Criteria.where("studentId").is(AuthUtils.getCurrentQualifiedUsername()));
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, "lastUpdatedOn"));

        List<Submission> submissions = mongoTemplate.find(query, Submission.class);
        List<SubmissionResponseDto> response = new ArrayList<>();
        if (!submissions.isEmpty()){
            for (Submission submission: submissions){
                SubmissionResponseDto dto = buildSubmissionResponse(submission);
                response.add(dto);
            }
        }
        return response;
    }

    @Override
    public synchronized void initiateAssignmentEvaluation(String assignmentId) {
        /* get assignment */
        Assignment assignment = getAssignmentEntity(assignmentId);

        /* get associated student */
        List<String> students = getAssociatedStudents(assignment);

        /* get student's submissions */
        List<Submission> submissions = new ArrayList<>();
        List<String> absentUsers = new ArrayList<>();
        for (String s: students){
            Submission submission = getSubmissionEntity(assignmentId, s, true);
            if (submission != null){
                submissions.add(submission);
            }else {
                /* mark as absent */
                absentUsers.add(s);
            }
        }
        /* validate if all submissions are submitted */
        for (Submission submission: submissions){
            if (!submission.isSubmitted()){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Test evaluation can not start. Submission not yet over for student %s", submission.getStudentId()));
            }
        }

        /* evaluate submissions & save into db */
        QuestionPaperResponseDto testConfig = testConfigService.getQuestionPaper(assignment.getTestId());
        Map<String, Question> questionMap = new HashMap<>();
        List<Question> questions = testConfigService.getQuestionPaperLinkedQuestions(testConfig.getQuestionPaperId());
        if (questions != null) {
            questions.forEach(q -> questionMap.put(q.getId().getQuestionId(), q));
        }
        /* iterate over each section, each question & update correct/incorrect, skipped, marks */
        for (Submission submission: submissions){
            if (!EvaluationState.PENDING.value().equalsIgnoreCase(submission.getEvaluation())){
                log.warn("[Skipped] Student assignment evaluation already initiated/completed. Assignment - {}, Student - {}, Evaluation State - {}", assignmentId, submission.getStudentId(), submission.getEvaluation());
                continue;
            }
            initStudentAssignmentEvaluation(submission, questionMap, testConfig);
        }

        /* update assignment modal for absent users */
        if (!absentUsers.isEmpty()){
            assignment.setAbsentUsers(absentUsers);
            mongoTemplate.save(assignment);
        }
    }

    @Override
    public synchronized void initiateStudentAssignmentEvaluation(String assignmentId, String studentId, Boolean forceEvaluation) {
        /* get assignment */
        Assignment assignment = getAssignmentEntity(assignmentId);

        /* get associated student */
        List<String> students = getAssociatedStudents(assignment);
        if (!students.contains(studentId)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignment not assigned to student");
        }

        /* get student's submissions */
        Submission submission = getSubmissionEntity(assignmentId, studentId, true);
        if (submission == null){
            assignment.addAbsentUsers(studentId);
            mongoTemplate.save(assignment);
            log.warn("Evaluation Completed. Student absent or not yet participated in assignment. Assignment - {}, Student - {}", assignmentId, studentId);
            return;
        }

        /* remove if student found in absent list. This will come if evaluation starts before student participates in exam */
        if (assignment.getAbsentUsers() != null){
            assignment.getAbsentUsers().remove(studentId);
        }

        /* assignment not yet submitted */
        if (!submission.isSubmitted()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Test evaluation can not start. Submission not yet over for student %s", submission.getStudentId()));
        }
        /* evaluation already started/completed */
        if (!EvaluationState.PENDING.value().equalsIgnoreCase(submission.getEvaluation()) && !forceEvaluation){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Student assignment evaluation already initiated/completed. Assignment - %s, Student - %s, Evaluation State - %s", assignmentId, submission.getStudentId(), submission.getEvaluation()));
        }

        /* load assignment questions & test config */
        QuestionPaperResponseDto testConfig = testConfigService.getQuestionPaper(assignment.getTestId());
        Map<String, Question> questionMap = new HashMap<>();
        List<Question> questions = testConfigService.getQuestionPaperLinkedQuestions(testConfig.getQuestionPaperId());
        if (questions != null) {
            questions.forEach(q -> questionMap.put(q.getId().getQuestionId(), q));
        }

        /* start evaluation */
        initStudentAssignmentEvaluation(submission, questionMap, testConfig);
    }

    @Override
    public SubmissionResponseDto viewStudentAssignmentResult(String assignmentId, String username) {

        Submission submission = getSubmissionEntity(assignmentId, username, false);
        if (EvaluationState.PENDING.value().equalsIgnoreCase(submission.getEvaluation())){
            log.warn("Student evaluation started. Assignment - {}, Student - {}", submission.getAssignmentId(), submission.getStudentId());
            initiateStudentAssignmentEvaluation(submission.getAssignmentId(), submission.getStudentId(), false);

            /* reload submission */
            submission = getSubmissionEntity(assignmentId, username, false);
        }
        return buildSubmissionResponse(submission);
    }

    @Override
    public void triggerScheduledStudentSubmissionEvaluation(String tenantId) {
        Query query = new Query();
        Criteria criteria = Criteria.where("_id.tenantId").is(tenantId);
        criteria.andOperator(Criteria.where("submitted").is(true), Criteria.where("evaluation").ne(EvaluationState.COMPLETED.value()));
        query.addCriteria(criteria);
        List<Submission> submissions = mongoTemplate.find(query, Submission.class);
        for (Submission submission : submissions) {
            try {
                initiateStudentAssignmentEvaluation(submission.getAssignmentId(), submission.getStudentId(), false);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private List<String> getAssociatedStudents(Assignment assignment) {
        List<String> students = new ArrayList<>();
        if (assignment.getAssignedToStudent() != null && !assignment.getAssignedToStudent().isEmpty()){
            students.addAll(assignment.getAssignedToStudent());
		}

		List<String> batches = assignment.getAssignedToBatch();
		if (batches != null) {
			for (String batchId : batches) {
				StudentBatch batch = studentBatchService.getStudentBatch(batchId);
				if (batch != null && batch.getStudents() != null) {
					for (String email : batch.getStudents()) {
						try {
							User user = userService.getUserByEmail(email);
							students.add(user.getUsername());
						} catch (Exception ignored) {
                        }
                    }
                }
            }
		}

        return students;
    }

    private synchronized void initStudentAssignmentEvaluation(Submission submission, Map<String, Question> questionMap, QuestionPaperResponseDto testConfig){
        submission.setEvaluation(EvaluationState.INIT.value());
        Map<String, List<Submission.Answer>> missing = new HashMap<>();
        for (QuestionPaperResponseDto.PaperSectionResponseDto sectionDto: testConfig.getSections()){
            if (sectionDto.getSubsections() != null && !sectionDto.getSubsections().isEmpty()){
                for (QuestionPaperResponseDto.PaperSectionResponseDto subSectionDto: sectionDto.getSubsections()){
                    sectionEvaluation(submission, subSectionDto, missing, questionMap);
                }
            }else {
                sectionEvaluation(submission, sectionDto, missing, questionMap);
            }
        }
        /* add missing details in section & save */
        for (Map.Entry<String, List<Submission.Answer>> entry: missing.entrySet()){
            String sectionId = entry.getKey();
            List<Submission.Answer> answers = entry.getValue();
            if (!submission.getSections().containsKey(sectionId)){
                Submission.Section section = new Submission.Section();
                section.setSectionId(sectionId);
                submission.addSection(section);
            }
            answers.forEach(item -> submission.getSections().get(sectionId).addAnswer(item));
        }
        /* handle marks calculation */
		Submission.Summary summary = generateSubmissionSummary(submission, testConfig);
        submission.setSummary(summary);
        /* mark submission completed & save */
        submission.setEvaluation(EvaluationState.COMPLETED.value());
        mongoTemplate.save(submission);
        log.info("Evaluation completed for student - {}, assignment - {}", submission.getStudentId(), submission.getAssignmentId());
    }

    private void sectionEvaluation(Submission submission, QuestionPaperResponseDto.PaperSectionResponseDto sectionDto, Map<String, List<Submission.Answer>> missing, Map<String, Question> questionMap){
        if (submission.getSections().containsKey(sectionDto.getId())){
            Submission.Section section = submission.getSections().get(sectionDto.getId());
            if (sectionDto.getQuestions() != null){
                for (QuestionPaperResponseDto.TestQuestionResponseDto questionDto: sectionDto.getQuestions()){
                    if (section.getAnswers().containsKey(questionDto.getId())){
                        Submission.Answer answer = section.getAnswers().get(questionDto.getId());
                        answer.setTotalMark(questionDto.getPositiveMark());
                        if ((answer.getOptions() == null || answer.getOptions().isEmpty()) && StringUtils.isEmpty(answer.getAnswerText())){
                            /* mark as skipped question */
                            answer.setAnswerStatus(AnswerState.SKIPPED.value());
                            answer.setMarkAllocated(questionDto.getSkipMark() * -1);
                            answer.setTotalMark(questionDto.getPositiveMark());
                        }else {
                            /* mark as correct/in-correct question */
                            if (QuestionType.MCQ.value().equalsIgnoreCase(questionDto.getType())
                                    || QuestionType.FILL_IN_THE_BLANKS.value().equalsIgnoreCase(questionDto.getType())
                                    || QuestionType.AWA.value().equalsIgnoreCase(questionDto.getType())
                                    || QuestionType.PASSAGE.value().equalsIgnoreCase(questionDto.getType())){
								boolean allMatch = false;
								if (answer.getOptions() != null && !answer.getOptions().isEmpty()
										&& questionMap.get(questionDto.getId()) != null) {
									List<String> correctOptions = questionMap.get(questionDto.getId()).getAnswer()
											.getOptions();
									if (answer.getOptions().equals(correctOptions)) {
										allMatch = true;
									}
                                }
                                if (allMatch){
                                    answer.setMarkAllocated(questionDto.getPositiveMark());
                                    answer.setAnswerStatus(AnswerState.CORRECT.value());
                                }else {
                                    answer.setMarkAllocated(questionDto.getNegativeMark() * -1);
                                    answer.setAnswerStatus(AnswerState.INCORRECT.value());
                                }
                            }else if (QuestionType.TITA.value().equalsIgnoreCase(questionDto.getType())){
								if (StringUtility
										.html2text(questionMap.get(questionDto.getId()).getAnswer().getAnswerText(),
												true)
										.equalsIgnoreCase(answer.getAnswerText())) {
                                    answer.setMarkAllocated(questionDto.getPositiveMark());
                                    answer.setAnswerStatus(AnswerState.CORRECT.value());
                                }else {
                                    answer.setMarkAllocated(questionDto.getNegativeMark() * -1);
                                    answer.setAnswerStatus(AnswerState.INCORRECT.value());
                                }
                            }
                        }
                    }else {
                        /* mark as skipped question */
                        Submission.Answer object = new Submission.Answer();
                        object.setQuestionId(questionDto.getId());
                        object.setAnswerStatus(AnswerState.SKIPPED.value());
                        object.setMarkAllocated(questionDto.getSkipMark() * -1);
                        object.setTotalMark(questionDto.getPositiveMark());
                        if (!missing.containsKey(section.getSectionId())){
                            missing.put(section.getSectionId(), new ArrayList<>());
                        }
                        missing.get(section.getSectionId()).add(object);
                    }
                }
            }
        }else {
            /* create new section in submission & mark all as skipped */
            if (sectionDto.getQuestions() != null) {
                for (QuestionPaperResponseDto.TestQuestionResponseDto questionDto : sectionDto.getQuestions()) {
                    Submission.Answer object = new Submission.Answer();
                    object.setQuestionId(questionDto.getId());
                    object.setAnswerStatus(AnswerState.SKIPPED.value());
                    object.setTimeElapsedInSec(0);
                    object.setMarkAllocated(questionDto.getSkipMark() * -1);
                    object.setTotalMark(questionDto.getPositiveMark());
                    if (!missing.containsKey(sectionDto.getId())){
                        missing.put(sectionDto.getId(), new ArrayList<>());
                    }
                    missing.get(sectionDto.getId()).add(object);
                }
            }
        }
    }

	private Submission.Summary generateSubmissionSummary(Submission submission,QuestionPaperResponseDto testConfig){
        Submission.Summary summary = new Submission.Summary();

        Submission.SectionSummary sectionSummary;
        Submission.TopicSummary topicSummary;
        Submission.DifficultySummary difficultySummary;
        Metric metric = new Metric();
        summary.setMetric(metric);

        for (Submission.Section section: submission.getSections().values()){
            for (Submission.Answer answer: section.getAnswers().values()){
                Question question = questionService.getQuestion(answer.getQuestionId(), false);
                String topic = getTopic(question.getTopic());
                String difficulty = getDifficultyLevel(question.getDifficultyLevel());
                if (summary.getSections() == null || !summary.containSectionSummary(section.getSectionId())){
                    sectionSummary = new Submission.SectionSummary();
                    sectionSummary.setSectionId(section.getSectionId());
                    sectionSummary.setMetric(new Metric());
                    summary.addSectionSummary(sectionSummary);
                }
                if (summary.getDifficulty() == null || !summary.containDifficultySummary(difficulty)){
                    difficultySummary = new Submission.DifficultySummary();
                    difficultySummary.setDifficultyLevel(difficulty);
                    difficultySummary.setMetric(new Metric());
                    summary.addDifficultySummary(difficultySummary);
                }
                if (summary.getTopics() == null || !summary.containTopicSummary(topic)){
                    topicSummary = new Submission.TopicSummary();
                    topicSummary.setTopic(topic);
                    topicSummary.setMetric(new Metric());
                    summary.addTopicSummary(topicSummary);
                }
                sectionSummary = summary.getSectionSummaryById(section.getSectionId());
                difficultySummary = summary.getDifficultySummaryById(difficulty);
                topicSummary = summary.getTopicSummaryById(topic);

                double marks = answer.getMarkAllocated();
                double totalMarks = answer.getTotalMark();
                int time = answer.getTimeElapsedInSec();

                if (AnswerState.CORRECT.value().equalsIgnoreCase(answer.getAnswerStatus())){
                    metric.addCorrect(marks, time);
                    metric.addAttempt();

                    sectionSummary.getMetric().addCorrect(marks, time);
                    sectionSummary.getMetric().addAttempt();

                    difficultySummary.getMetric().addCorrect(marks, time);
                    difficultySummary.getMetric().addAttempt();

                    topicSummary.getMetric().addCorrect(marks, time);
                    topicSummary.getMetric().addAttempt();
                }else if (AnswerState.INCORRECT.value().equalsIgnoreCase(answer.getAnswerStatus())){
                    metric.addIncorrect(marks, time);
                    metric.addAttempt();

                    sectionSummary.getMetric().addIncorrect(marks, time);
                    sectionSummary.getMetric().addAttempt();

                    difficultySummary.getMetric().addIncorrect(marks, time);
                    difficultySummary.getMetric().addAttempt();

                    topicSummary.getMetric().addIncorrect(marks, time);
                    topicSummary.getMetric().addAttempt();
                }else if (AnswerState.SKIPPED.value().equalsIgnoreCase(answer.getAnswerStatus())){
                    metric.addSkipped(marks, time);

                    sectionSummary.getMetric().addSkipped(marks, time);
                    difficultySummary.getMetric().addSkipped(marks, time);
                    topicSummary.getMetric().addSkipped(marks, time);
                }
                metric.addTotal(marks, time, totalMarks);
                sectionSummary.getMetric().addTotal(marks, time, totalMarks);
                difficultySummary.getMetric().addTotal(marks, time, totalMarks);
                topicSummary.getMetric().addTotal(marks, time, totalMarks);
            }
			SectionSummary tempSectionSummary = summary.getSectionSummaryById(section.getSectionId());
			Double marksReceived = tempSectionSummary.getMetric().getMarksReceived();
			Map<Double, Double> sectionLevelPercentile = getPercentileForSection(testConfig, section.getSectionId());
			if (sectionLevelPercentile != null && sectionLevelPercentile.get(marksReceived) != null) {
				tempSectionSummary.getMetric().setPercentileScore(sectionLevelPercentile.get(marksReceived));
			}

        }
        return summary;
    }

	public Map<Double, Double> getPercentileForSection(QuestionPaperResponseDto testConfig, String sectionId) {
		if (testConfig.getControlParam() != null && testConfig.getControlParam().getPercentileScoreCard() != null
				&& testConfig.getControlParam().getPercentileScoreCard().getSectionLevelPercentile() != null
				&& testConfig.getControlParam().getPercentileScoreCard().getSectionLevelPercentile()
						.get(sectionId) != null) {
			return testConfig.getControlParam().getPercentileScoreCard().getSectionLevelPercentile().get(sectionId);
		}

		return null;
	}

	public Map<Double, Double> getPercentileForTest(QuestionPaperResponseDto testConfig) {
		if (testConfig.getControlParam() != null && testConfig.getControlParam().getPercentileScoreCard() != null
				&& testConfig.getControlParam().getPercentileScoreCard().getTestLevelPercentile() != null) {
			return testConfig.getControlParam().getPercentileScoreCard().getTestLevelPercentile();
		}

		return null;
	}
    private String getDifficultyLevel(String input){
		if (DifficultyLevel.VERY_HARD.value().equalsIgnoreCase(input)) {
			return DifficultyLevel.VERY_HARD.value();
		} else if (DifficultyLevel.MEDIUM.value().equalsIgnoreCase(input)) {
            return DifficultyLevel.MEDIUM.value();
        }else if (DifficultyLevel.HARD.value().equalsIgnoreCase(input)){
            return DifficultyLevel.HARD.value();
        }
        return DifficultyLevel.EASY.value();
    }

    private String getTopic(String input){
        if (StringUtils.isEmpty(input)) return "unknown";
        return input.toLowerCase();
    }
}
