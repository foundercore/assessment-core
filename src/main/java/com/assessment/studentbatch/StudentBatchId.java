package com.assessment.studentbatch;

import lombok.Data;

import java.io.Serializable;

import com.assessment.iam.entities.TenantEntity;

@Data
public class StudentBatchId extends TenantEntity implements Serializable {
    private String batchId;
}
