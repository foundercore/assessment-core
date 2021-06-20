package com.assessment.iam.configurations;

import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.mapping.event.*;

import com.assessment.exception.ConfigurationException;
import com.assessment.iam.commons.AuthUtils;
import com.assessment.iam.entities.Tenant;
import com.assessment.iam.entities.User;
import com.assessment.iam.exceptions.CrossTenantOperationException;

@Slf4j
@Configuration
public class CrossTenantEventListener extends AbstractMongoEventListener<Object> {

    @Override
    public void onBeforeSave(BeforeSaveEvent<Object> event) {
        log.debug("Cross tenant access check: event onBeforeSave");
        Object entity = event.getSource();
        if (entity instanceof Tenant || entity instanceof User) {
            //ignore.
        } else if (isSpringSessionCollection(event)){

        } else {
            Object entityDocument = event.getDocument();
            checkCrossTenantOperation(entityDocument);
        }
    }

    private boolean isSpringSessionCollection(MongoMappingEvent event) {
        return "sessions".equalsIgnoreCase(event.getCollectionName());
    }

    @Override
    public void onAfterLoad(AfterLoadEvent<Object> event) {
        log.debug("Cross tenant access check: event onAfterLoad");
        Object entityType = event.getType();
        if (entityType == Tenant.class || entityType == User.class) {
            //ignore.
        } else if (isSpringSessionCollection(event)){
            //ignore.
        } else {
            Object entityDocument = event.getSource();
            checkCrossTenantOperation(entityDocument);
        }
    }

    @Override
    public void onBeforeDelete(BeforeDeleteEvent<Object> event) {
        log.debug("Cross tenant access check: event onBeforeDelete");
        Object entityType = event.getType();
        if (entityType == Tenant.class || entityType == User.class) {
            //ignore.
        } else if (isSpringSessionCollection(event)){

        } else {
            Document entityDocument = event.getSource();
            checkCrossTenantOperation(entityDocument);
        }
    }

    private void checkCrossTenantOperation(Object entity) {
        try {
            Document entityDocument = (Document) entity;
            Document id = (Document) entityDocument.get("_id");
            if (id == null) {
                throw new ConfigurationException("Entity has invalid id, null encountered.");
            }
            String requestedTenantId = (String) id.get("tenantId");
            if (requestedTenantId == null) {
                throw new ConfigurationException("Entity has invalid tenantId, null encountered.");
            }
            log.debug("Requested Tenant Id: " + requestedTenantId);
            String currentTenantId = AuthUtils.getCurrentTenantId();
            log.debug("Current Tenant Id: " + currentTenantId);
            if (!requestedTenantId.equals(currentTenantId)) {
                throw new CrossTenantOperationException("Access denied!");
            }
        } catch (CrossTenantOperationException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigurationException("Entity does not have tenantId field in its primary key.", e);
        }
    }
}