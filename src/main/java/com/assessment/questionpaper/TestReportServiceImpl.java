package com.assessment.questionpaper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.assessment.iam.commons.AuthUtils;
import com.assessment.iam.entities.User;
import com.assessment.iam.services.UserService;
import com.assessment.question.QuestionService;
import com.assessment.questionpaper.entity.Submission;
import com.assessment.studentbatch.StudentBatch;
import com.assessment.studentbatch.StudentBatchService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        for (Submission submission: submissions){
            Map<String, Object> record = new HashMap<>();
            User user = userService.getUser(submission.getStudentId()).orElse(null);
            if (user != null){
                record.put("name", user.getDisplayName());
                record.put("email", user.getEmail());
            }
            record.put("username", submission.getStudentId());
            record.put("marksReceived", submission.getSummary().getMetric().getMarksReceived());
            record.put("totalMarks", submission.getSummary().getMetric().getTotalMarks());
            response.add(record);
        }
        return response;
    }

    private List<String> getBatchStudents(String batchId){
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

        List<Map<String, Object>> response = new ArrayList<>();
        for (Submission submission: submissions){
            Map<String, Object> record = new HashMap<>();
            User user = userService.getUser(submission.getStudentId()).orElse(null);
            if (user != null){
                record.put("name", user.getDisplayName());
                record.put("email", user.getEmail());
            }
            record.put("username", submission.getStudentId());
            record.put("marksReceived", submission.getSummary().getMetric().getMarksReceived());
            record.put("totalMarks", submission.getSummary().getMetric().getTotalMarks());
            response.add(record);
        }
        return response;
    }

    @Override
    public List<Map<String, Object>> getStudentAssignedToTest(String assignmentId) {    //TODO implement
        //get assignment
        //loop over all batches
        //loop over all students
        return null;
    }
}
