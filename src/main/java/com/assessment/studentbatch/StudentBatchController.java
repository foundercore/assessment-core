package com.assessment.studentbatch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.assessment.iam.commons.AuthUtils;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

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
    public List<StudentBatch> studentAssociatedBatches(@RequestBody String emailId){
        return studentBatchService.studentAssociatedBatches(emailId);
    }
}
