package com.assessment.studentbatch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.assessment.iam.commons.AuthUtils;
import com.assessment.iam.dtos.AppRole;
import com.assessment.iam.entities.User;
import com.assessment.iam.services.UserService;

import java.util.ArrayList;
import java.util.List;

@Service
public class StudentBatchServiceImpl implements StudentBatchService {

    @Autowired
    private StudentBatchRepository studentBatchRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void addStudentsInBatch(StudentBatchId id, List<String> emails) {
        if (emails == null || emails.isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Students detail not supplied.");
        }

        StudentBatch batch = studentBatchRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Batch with id %s not found", id.getBatchId())));
        if (batch.getStudents() == null){
            batch.setStudents(new ArrayList<>());
        }
        List<String> addToBatch = new ArrayList<>();

        for (String email: emails){
            User user = userService.getUserByEmail(email);
            if (!user.getRoles().contains(AppRole.ROLE_STUDENT.value())){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a student. Email - "+ email);
            }

            if (!batch.getStudents().contains(email)){
                addToBatch.add(email);
            }
        }

        if (!addToBatch.isEmpty()){
            batch.getStudents().addAll(addToBatch);
            studentBatchRepository.save(batch);
        }

    }

    @Override
    public void removeStudentsFromBatch(StudentBatchId id, List<String> emails) {
        if (emails == null || emails.isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Students detail not supplied.");
        }

        StudentBatch batch = studentBatchRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Batch with id %s not found", id.getBatchId())));
        if (batch.getStudents() == null || batch.getStudents().isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Batch is empty.");
        }
        List<String> removeFromBatch = new ArrayList<>();

        for (String email: emails){
            User user = userService.getUserByEmail(email);
            if (!user.getRoles().contains(AppRole.ROLE_STUDENT.value())){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a student. Email - "+ email);
            }

            if (batch.getStudents().contains(email)){
                removeFromBatch.add(email);
            }
        }

        if (!removeFromBatch.isEmpty()){
            batch.getStudents().removeAll(removeFromBatch);
            studentBatchRepository.save(batch);
        }
    }

    @Override
    public List<StudentBatch> studentAssociatedBatches(String emailId) {

        if (emailId == null || emailId.isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student detail not supplied.");
        }
        User user = userService.getUserByEmail(emailId);
        if (!user.getRoles().contains(AppRole.ROLE_STUDENT.value())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a student. Email - "+ emailId);
        }

        Query query = new Query();
        query.addCriteria(Criteria.where("_id.tenantId").is(AuthUtils.getCurrentTenantId()));
        query.addCriteria(Criteria.where("students").is(emailId));

        List<StudentBatch> batches = mongoTemplate.find(query, StudentBatch.class);
        for (StudentBatch batch: batches){
            batch.setStudents(null);
        }
        return batches;
    }

    @Override
    public StudentBatch getStudentBatch(String batchId) {
        StudentBatchId id = new StudentBatchId();
        id.setBatchId(batchId);
        id.setTenantId(AuthUtils.getCurrentTenantId());
        return studentBatchRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Batch with id %s not found", batchId)));
    }
}
