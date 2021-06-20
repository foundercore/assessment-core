package com.assessment.migration;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Validated
public interface MigrationService {

    File questionSchemaMigration(MultipartFile file, boolean removeHtmlContent) throws IOException;

    void intiAutomateQuestion(Path inputFile, boolean removeHtmlContent, String errorDir, String outputDir, String archiveDir);
}
