package com.assessment.studentbatch;

import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.assessment.iam.commons.AuthUtils;

import lombok.extern.slf4j.Slf4j;

@Validated
@Slf4j
@RestController
@RequestMapping("/api/v1/student")
public class StudentBatchController {

    @Autowired
    private StudentBatchService studentBatchService;

    @Autowired
    private StudentBatchRepository studentBatchRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('ROLE_USER_ADMIN')")
    public void createStudentBatch(@RequestParam("name") String name,
                                   @RequestParam("description") String description){
        StudentBatchId id = new StudentBatchId();
        id.setBatchId(UUID.randomUUID().toString());
        id.setTenantId(AuthUtils.getCurrentTenantId());

        StudentBatch batch = new StudentBatch();
        batch.setId(id);
        batch.setName(name);
        batch.setDescription(description);
        studentBatchRepository.save(batch);
    }

    @GetMapping("/batches")
    public List<StudentBatch> getStudentBatches(){
        Query query = new Query();
        query.addCriteria(Criteria.where("_id.tenantId").is(AuthUtils.getCurrentTenantId()));
        List<StudentBatch> batches = mongoTemplate.find(query, StudentBatch.class);
        return batches;
    }

    @GetMapping("/batch/{batch-id}")
    public StudentBatch getStudentBatch(@PathVariable("batch-id") String batchId){
        return studentBatchService.getStudentBatch(batchId);
    }

    @DeleteMapping("/batch/{batch-id}/remove")
    @PreAuthorize("hasRole('ROLE_USER_ADMIN')")
    public void deleteStudentBatch(@NotBlank @PathVariable("batch-id") String batchId){
        StudentBatchId id = new StudentBatchId();
        id.setBatchId(batchId);
        id.setTenantId(AuthUtils.getCurrentTenantId());
        studentBatchRepository.deleteById(id);
    }

    @PostMapping("/batch/{batch-id}/add-students")
    @PreAuthorize("hasAnyRole('ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public void addStudentsInBatch(@NotBlank @PathVariable("batch-id") String batchId, @RequestBody List<String> emails){
        StudentBatchId id = new StudentBatchId();
        id.setBatchId(batchId);
        id.setTenantId(AuthUtils.getCurrentTenantId());
        studentBatchService.addStudentsInBatch(id, emails);
    }

    @PostMapping("/batch/{batch-id}/remove-students")
    @PreAuthorize("hasAnyRole('ROLE_USER_ADMIN', 'ROLE_STAFF')")
    public void removeStudentsFromBatch(@NotBlank @PathVariable("batch-id") String batchId, @RequestBody List<String> emails){
        StudentBatchId id = new StudentBatchId();
        id.setBatchId(batchId);
        id.setTenantId(AuthUtils.getCurrentTenantId());
        studentBatchService.removeStudentsFromBatch(id, emails);
    }

    @PostMapping("/linked-batches")
	public List<StudentBatch> userAssociatedBatches(@RequestBody String emailId) {
        return studentBatchService.userAssociatedBatches(emailId);
    }
}
