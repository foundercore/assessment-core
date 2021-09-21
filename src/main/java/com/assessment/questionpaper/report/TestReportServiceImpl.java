package com.assessment.questionpaper.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.assessment.common.StringUtility;
import com.assessment.iam.commons.AuthUtils;
import com.assessment.iam.dtos.UserRole;
import com.assessment.iam.entities.User;
import com.assessment.iam.services.UserService;
import com.assessment.question.QuestionService;
import com.assessment.questionpaper.config.TestConfigRepository;
import com.assessment.questionpaper.dto.EvaluationState;
import com.assessment.questionpaper.entity.Assignment;
import com.assessment.questionpaper.entity.AssignmentId;
import com.assessment.questionpaper.entity.InstituteAnalysisMetadata.InstituteData;
import com.assessment.questionpaper.entity.QuestionPaper;
import com.assessment.questionpaper.entity.QuestionPaper.PaperSection;
import com.assessment.questionpaper.entity.QuestionPaperId;
import com.assessment.questionpaper.entity.Submission;
import com.assessment.questionpaper.entity.Submission.SectionSummary;
import com.assessment.questionpaper.report.dto.StudentTestAnalysisDto;
import com.assessment.questionpaper.report.dto.StudentTestAnalysisDto.SectionAnalysisResponseDto;
import com.assessment.questionpaper.report.dto.StudentTestAnalysisDto.StudentTestAnalysisResponseDto;
import com.assessment.questionpaper.report.dto.StudentTestReportResponseDto;
import com.assessment.questionpaper.report.dto.StudentTestReportResponseDto.SectionReport;
import com.assessment.studentbatch.StudentBatch;
import com.assessment.studentbatch.StudentBatchService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TestReportServiceImpl implements TestReportService {

	@Autowired
	TestConfigRepository testConfigRepository;

	@Autowired
	QuestionService questionService;

	@Autowired
	private StudentBatchService studentBatchService;

	@Autowired
	private UserService userService;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public List<Map<String, Object>> getStudentBatchRankingReport(String assignmentId, String batchId) {

		List<String> students = getBatchStudents(batchId);

		Query query = new Query();
		Criteria criteria = Criteria.where("_id.tenantId").is(AuthUtils.getCurrentTenantId());
		criteria.andOperator(Criteria.where("assignmentId").is(assignmentId), Criteria.where("studentId").in(students));
		query.addCriteria(criteria);
		query.with(Sort.by(Sort.Direction.DESC, "summary.metric.totalMarks"));

		List<Submission> submissions = mongoTemplate.find(query, Submission.class);

		List<Map<String, Object>> response = new ArrayList<>();
		for (Submission submission : submissions) {
			Map<String, Object> record = new HashMap<>();
			User user = userService.getUser(submission.getStudentId()).orElse(null);
			// report is just for students.
			if (user == null || !user.getRoles().contains(UserRole.ROLE_STUDENT.value())) {
				continue;
			}
			record.put("name", user.getDisplayName());
			record.put("email", user.getEmail());
			record.put("username", submission.getStudentId());
			record.put("marksReceived", submission.getSummary().getMetric().getMarksReceived());
			record.put("totalMarks", submission.getSummary().getMetric().getTotalMarks());
			response.add(record);
		}
		return response;
	}

	private List<String> getBatchStudents(String batchId) {
		List<String> students = new ArrayList<>();
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
		return students;
	}

	@Override
	public List<Map<String, Object>> getStudentAssignmentRanking(String assignmentId) {
		Query query = new Query();
		Criteria criteria = Criteria.where("_id.tenantId").is(AuthUtils.getCurrentTenantId());
		criteria.andOperator(Criteria.where("assignmentId").is(assignmentId));
		query.addCriteria(criteria);
		query.with(Sort.by(Sort.Direction.DESC, "summary.metric.totalMarks"));

		List<Submission> submissions = mongoTemplate.find(query, Submission.class);
		Map<Double, Double> percentileScore = null;
		if (submissions != null && submissions.size() > 0) {
			AssignmentId asssignemntId = new AssignmentId();
			asssignemntId.setTenantId(AuthUtils.getCurrentTenantId());
			asssignemntId.setAssignmentId(assignmentId);
			Assignment assignment = mongoTemplate.findById(asssignemntId, Assignment.class);
			if (assignment != null) {
				QuestionPaperId paperid = new QuestionPaperId();
				paperid.setTenantId(AuthUtils.getCurrentTenantId());
				paperid.setQuestionPaperId(assignment.getTestId());
				QuestionPaper questionPaper = mongoTemplate.findById(paperid, QuestionPaper.class);
				if (questionPaper != null) {
					percentileScore = questionPaper.getPercentileForTest();
				}
			}
		}
		List<Map<String, Object>> response = new ArrayList<>();
		for (Submission submission : submissions) {
			if (submission.isSubmitted() && submission.getEvaluation() != null
					&& submission.getEvaluation().equals(EvaluationState.COMPLETED.value())) {
				Map<String, Object> record = new HashMap<>();
				User user = userService.getUser(submission.getStudentId()).orElse(null);
				// report is just for students.
				if (user == null || !user.getRoles().contains(UserRole.ROLE_STUDENT.value())) {
					continue;
				}
				record.put("name", user.getDisplayName());
				record.put("email", user.getEmail());

				record.put("username", submission.getStudentId());
				record.put("marksReceived", submission.getSummary().getMetric().getMarksReceived());
				record.put("totalMarks", submission.getSummary().getMetric().getTotalMarks());
				if (percentileScore != null) {
					Object percentile = percentileScore.get(submission.getSummary().getMetric().getMarksReceived());
					record.put("percentile", percentile != null ? percentile : 0.0);
				}
				response.add(record);
			}
		}
		return response;
	}

	@Override
	public List<Map<String, Object>> getStudentAssignedToTest(String assignmentId) {    // TODO implement
		// get assignment
		// loop over all batches
		// loop over all students
		return null;
	}

	@Override
	public List<StudentTestReportResponseDto> getStudentTestReport(String testId) {
		if (StringUtility.isNullOrEmpty(testId)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Test Id");
		}
		// get question paper information
		QuestionPaperId paperid = new QuestionPaperId();
		paperid.setTenantId(AuthUtils.getCurrentTenantId());
		paperid.setQuestionPaperId(testId);
		QuestionPaper questionPaper = mongoTemplate.findById(paperid, QuestionPaper.class);
		if (questionPaper == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No test exists");
		}
		// get percentile information for test level
		Map<Double, Double> percentileScore = questionPaper.getPercentileForTest();
		Criteria filter = Criteria.where("_id.tenantId").is(AuthUtils.getCurrentTenantId())
				.andOperator(new Criteria("testId").is(testId));
		Query getAssignmentQuery = new Query();
		getAssignmentQuery.addCriteria(filter);
		List<Assignment> assignments = mongoTemplate.find(getAssignmentQuery, Assignment.class);
		if (assignments.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"No assignments in test : " + questionPaper.getName());
		}
		// List<Map<String, Object>> response = new ArrayList<>();
		List<StudentTestReportResponseDto> response = new ArrayList<>();
		for (Assignment assignment : assignments) {
			Query query = new Query();
			Criteria criteria = Criteria.where("_id.tenantId").is(AuthUtils.getCurrentTenantId());
			criteria.andOperator(Criteria.where("assignmentId").is(assignment.getId().getAssignmentId()));
			query.addCriteria(criteria);
			List<Submission> submissions = mongoTemplate.find(query, Submission.class);
			for (Submission submission : submissions) {
				if (submission.isSubmitted() && submission.getEvaluation() != null
						&& submission.getEvaluation().equals(EvaluationState.COMPLETED.value())) {
					StudentTestReportResponseDto record = new StudentTestReportResponseDto();
					User user = userService.getUser(submission.getStudentId()).orElse(null);
					// report is just for students.
					if (user == null || !user.getRoles().contains(UserRole.ROLE_STUDENT.value())) {
						continue;
					}

					record.setStudentName(user.getDisplayName());
					record.setStudentEmail(user.getEmail());
					if (percentileScore != null) {
						Double percentile = percentileScore.get(submission.getSummary().getMetric().getMarksReceived());
						record.setPercentile(percentile != null ? percentile : 0.0);
					}
					record.setMarkRecieved(submission.getSummary().getMetric().getMarksReceived());
					record.setTotalMark(submission.getSummary().getMetric().getTotalMarks());
					record.setSectionReports(new ArrayList<>());
					// add section level details
					for (SectionSummary sectionSummary : submission.getSummary().getSections()) {
						if (questionPaper.getSections().get(sectionSummary.getSectionId()) != null) {
							SectionReport sectionReport = new SectionReport();
							sectionReport.setSectionName(
									questionPaper.getSections().get(sectionSummary.getSectionId()).getName());
							sectionReport.setMarkRecieved(sectionSummary.getMetric().getMarksReceived());
							sectionReport.setTotalMark(sectionSummary.getMetric().getTotalMarks());

							// get percentile information for test level
							Map<Double, Double> percentileSectionScore = questionPaper
									.getPercentileForSection(sectionSummary.getSectionId());
							if (percentileSectionScore != null) {
								Double percentile = percentileSectionScore
										.get(submission.getSummary().getMetric().getMarksReceived());
								sectionReport.setPercentile(percentile != null ? percentile : 0.0);
							}
							record.getSectionReports().add(sectionReport);
						}
					}

					response.add(record);
				}
			}
		}

		return response;
	}

	@Override
	public StudentTestAnalysisResponseDto getStudentTestAnalysisReport(
			StudentTestAnalysisDto studentTestAnalysisData) {
		QuestionPaperId id = new QuestionPaperId();
		id.setTenantId(AuthUtils.getCurrentTenantId());
		id.setQuestionPaperId(studentTestAnalysisData.getTestId());
		QuestionPaper questionPaper = testConfigRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
						String.format("Question paper %s does not exist", studentTestAnalysisData.getTestId())));

		if (questionPaper.getControlParam() == null || !questionPaper.getControlParam().isPercentile()
				|| !questionPaper.getControlParam().isAllowInstituteAnalysis()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Question paper ' %s ' does not have percentile setup.", questionPaper.getName()));
		}
		StudentTestAnalysisResponseDto response = new StudentTestAnalysisResponseDto();
		response.setTestId(studentTestAnalysisData.getTestId());
		// get percentile information for test level
		Map<Double, Double> percentileScore = questionPaper.getPercentileForTest();
		if (percentileScore != null) {
			Double percentile = percentileScore.get(studentTestAnalysisData.getTestMark());
			response.setTestPercentile(percentile != null ? percentile : 0.0);
		}

		for (PaperSection section : questionPaper.getSections().values()) {
			SectionAnalysisResponseDto sectionResult = new SectionAnalysisResponseDto();
			sectionResult.setSectionId(section.getId());
			sectionResult.setSectionName(section.getName());
			// get percentile information for test level
			Map<Double, Double> percentileSectionScore = questionPaper.getPercentileForSection(section.getId());
			if (percentileSectionScore != null) {
				Double percentile = percentileSectionScore
						.get(studentTestAnalysisData.getSectionLevelMark().get(section.getId()));
				sectionResult.setSectionPercentile(percentile != null ? percentile : 0.0);
			}
			response.getSectionLevelPercentile().add(sectionResult);
		}

		// get institute eligibility criteria
		List<InstituteData> instituteDetails = questionPaper.getControlParam().getInstituteAnalysisMetadata()
				.getInstituteData();
		if (instituteDetails != null) {
			for (InstituteData instituteDetail : instituteDetails) {
				if (instituteDetail.getTestLevelPercentile() <= response.getTestPercentile()) {
					// add section level checks
					boolean allsectionLevelPass = instituteDetail.getSectionLevelPercentile().entrySet().stream()
							.allMatch(entry -> {
						for (SectionAnalysisResponseDto sectionLevelPercentile : response.getSectionLevelPercentile()) {
							if (entry.getValue() <= sectionLevelPercentile.getSectionPercentile()) {
								return true;
							}
						}
						return false;
					});
					// if all section level cutoff passed then add the institute
					if (allsectionLevelPass) {
						response.getInstituesSelectedIn().add(instituteDetail.getInstituteName());
					}
				}
			}
		}
		return response;
	}
}
