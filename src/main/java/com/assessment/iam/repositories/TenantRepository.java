package com.assessment.iam.repositories;


import org.springframework.data.repository.CrudRepository;

import com.assessment.iam.entities.Tenant;

public interface TenantRepository extends CrudRepository<Tenant, String> {
}
