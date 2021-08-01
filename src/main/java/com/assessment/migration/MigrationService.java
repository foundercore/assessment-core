package com.assessment.migration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.exceptions.CsvValidationException;

@Validated
public interface MigrationService {

    File questionSchemaMigration(MultipartFile file, boolean removeHtmlContent) throws IOException;

    void intiAutomateQuestion(Path inputFile, boolean removeHtmlContent, String errorDir, String outputDir, String archiveDir);

	void intiQuestionMetadataUpdate(Path inputFile, String errorDir, String outputDir, String archiveDir);

	void updateTestTags(MultipartFile file) throws IOException, CsvValidationException;
}
