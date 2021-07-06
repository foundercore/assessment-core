package com.assessment.migration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.assessment.common.FileUtility;
import com.assessment.common.XLWriter;
import com.assessment.iam.commons.AuthUtils;
import com.assessment.question.QuestionService;
import com.assessment.question.QuestionType;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MigrationServiceImpl implements MigrationService {

    @Autowired
    QuestionService questionService;

    @Override
    public File questionSchemaMigration(MultipartFile file, boolean removeHtmlContent) throws IOException {
        String directory = Files.createTempDir().getAbsolutePath() + File.separator + AuthUtils.getCurrentUsername()
                + File.separator + UUID.randomUUID().toString();
        File tempFile = null;
        File output = null;

        try {
            /* save file */
            Files.createParentDirs(new File(directory + File.separator + "tmp.log"));
            String fileName = file.getOriginalFilename();
            fileName = directory + File.separator + fileName;
            FileUtility.saveFile(file, fileName);
            tempFile = new File(fileName);

//            String outFile = directory + File.separator + file.getOriginalFilename() + "output.csv";
            String outFile = directory + File.separator + file.getOriginalFilename() + "output.xlsx";
            output = new File(outFile);

            /* read file & load all question */
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
            MigrationInSchema schema = objectMapper.readValue(tempFile, MigrationInSchema.class);
            if (schema == null){
                log.error("Unable to parse file - {}", tempFile.getName());
                throw new RuntimeException("Unable to parse file - " + tempFile.getName());
            }
            if (schema.getSections() == null){
                log.error("No section found in file - {}", tempFile.getName());
                throw new RuntimeException("No section found in file - " + tempFile.getName());
            }
            int maxOptionCount= getMaxOptionAvailable(schema);
            log.info("Max Option Count - {}", maxOptionCount);

            /* write a csv file */
//            String outputContent = convertQuestionSchema(schema, maxOptionCount);
//            FileOutputStream outputStream = new FileOutputStream(output);
//            byte[] bytes = outputContent.getBytes(StandardCharsets.UTF_8);
//            outputStream.write(bytes);
//            outputStream.close();

            List<Map<String, Object>> result = convertQuestionSchemaAndReturnNewSchemaRecords(schema, maxOptionCount, removeHtmlContent);
            /* write a xlsx file */
            XLWriter.write(output, result);

            log.info("Migration Successful. Input - {}, Output - {}", tempFile.getName(), output.getAbsolutePath());
        } catch (Exception e) {
        	log.error("Unable to process file - {}", tempFile.getName());
            throw e;
        } finally {
            if (tempFile != null && tempFile.exists()){
                FileUtility.delete(tempFile);
            }
        }
        return output;
    }

    @Override
    public void intiAutomateQuestion(Path inputFilePath, boolean removeHtmlContent, String errorDir, String outputDir, String archiveDir) {
        File inputJsonFile = null;
        File output = null;

        try {
            /* save file */
            inputJsonFile = inputFilePath.toFile();

//            String outFile = outputDir + File.separator + inputJsonFile.getName() + "output.xlsx";
            String of = inputJsonFile.getName().replace(".json", ".xlsx");
            String outFile = outputDir + File.separator + of;
            output = new File(outFile);

            /* read file & load all question */
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
            MigrationInSchema schema = objectMapper.readValue(inputJsonFile, MigrationInSchema.class);
            if (schema == null){
                log.error("Unable to parse file - {}", inputFilePath.toFile().getName());
                throw new RuntimeException("Unable to parse file - " + inputFilePath.toFile().getName());
            }
            if (schema.getSections() == null){
                log.error("No section found in file - {}", inputFilePath.toFile().getName());
                throw new RuntimeException("No section found in file - " + inputFilePath.toFile().getName());
            }
            int maxOptionCount= getMaxOptionAvailable(schema);
            log.info("Max Option Count - {}", maxOptionCount);

            List<Map<String, Object>> result = convertQuestionSchemaAndReturnNewSchemaRecords(schema, maxOptionCount, removeHtmlContent);
            /* write a xlsx file */
            XLWriter.write(output, result);
            log.info("Schema Migration Successful. Input - {}, Output - {}", inputJsonFile.getName(), output.getAbsolutePath());

            /* question creation */
            questionService.initBulkCreationQuestion(output.getAbsolutePath());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (inputJsonFile != null) {
                String errorFile = errorDir + File.separator + inputJsonFile.getName();
                try {
                    Files.move(inputJsonFile, new File(errorFile));
                } catch (IOException ignored) {
                }
            }
        } finally {
            if (inputJsonFile != null && inputJsonFile.exists()){
                String archiveFile = archiveDir + File.separator + inputJsonFile.getName();
                try {
                    Files.move(inputJsonFile, new File(archiveFile));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private List<Map<String, Object>> convertQuestionSchemaAndReturnNewSchemaRecords(MigrationInSchema schema, int maxOptionCount, boolean removeHtmlContent) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (MigrationSection section: schema.getSections()){
            for (MigrationQuestion question: section.getQuestions()){
                if ("bn".equalsIgnoreCase(question.getType())){
                    question.setType(QuestionType.TITA.value());
                }
                if ("an".equalsIgnoreCase(question.getType())){
                    question.setType(QuestionType.TITA.value());
                }
                if ("ne".equalsIgnoreCase(question.getType())){
                    question.setType(QuestionType.TITA.value());
                }
//                if (QuestionType.MCQ.value().equalsIgnoreCase(question.getType()) && StringUtils.isNotEmpty(question.getPassage())){
//                    question.setType(QuestionType.PASSAGE.value());
//                }
                result.add(question.getRecordAsMap(maxOptionCount, schema.getId(), schema.getName(), section.getId(), section.getName(), removeHtmlContent));
            }
        }
        return result;
    }


    private String convertQuestionSchema(MigrationInSchema schema, int maxOptionCount) {
        StringBuilder content = new StringBuilder();
        content.append(MigrationQuestion.getHeaders(maxOptionCount));
        for (MigrationSection section: schema.getSections()){
            for (MigrationQuestion question: section.getQuestions()){
                if ("bn".equalsIgnoreCase(question.getType())){
                    question.setType(QuestionType.TITA.value());
                }
                if ("an".equalsIgnoreCase(question.getType())){
                    question.setType(QuestionType.TITA.value());
                }
                content.append(question.getNewSchemaRecord(maxOptionCount, schema.getId(), schema.getName(), section.getId(), section.getName()));
            }
        }
        return content.toString();
    }

    private int getMaxOptionAvailable(MigrationInSchema schema) {
        int count = 0;
        for (MigrationSection section: schema.getSections()){
            for (MigrationQuestion question: section.getQuestions()){
                if (question.getOptions().size() > count){
                    count = question.getOptions().size();
                }
            }
        }
        return count;
    }

	@Override
	public void intiQuestionMetadataUpdate(Path inputExcelPath, String errorDir, String outputDir, String archiveDir) {
		File inputJsonFile = null;
		File output = null;

		try {
			inputJsonFile = inputExcelPath.toFile();
			String outFile = outputDir + File.separator + inputJsonFile.getName();
			/* move file to output folder */
			if (inputJsonFile != null) {

				try {
					Files.move(inputJsonFile, new File(outFile));
				} catch (IOException ignored) {
				}
			}
			/* read file from output folder */
			output = new File(outFile);
			/* question creation */
			questionService.initMetadataQuestionBulkUpdate(output.getAbsolutePath());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			if (output != null) {
				String errorFile = errorDir + File.separator + output.getName();
				try {
					Files.move(output, new File(errorFile));
				} catch (IOException ignored) {
				}
			}
		} finally {
			if (output != null && output.exists()) {
				String archiveFile = archiveDir + File.separator + output.getName();
				try {
					Files.move(output, new File(archiveFile));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
