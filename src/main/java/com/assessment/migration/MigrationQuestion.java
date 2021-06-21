package com.assessment.migration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import com.assessment.common.StringUtility;
import com.assessment.question.QuestionType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MigrationQuestion {
    private String id;
    private String type;
    private String isPassageSplit;
    private String question;
    private List<MigrationSchemaOption> options;
    private String subjectId;
    private String areaId;
    private String topicId;
    private String subTopicId;
    private String difficultyId;
    private String direction;
    private String passage;
    private double marks;
    private double negativeMarks;
    private String explanation;
    private String videoExplanation;

    public String getNewSchemaRecord(int maxOptions, String testId, String testName, String sectionId, String sectionName){
        StringBuilder builder = new StringBuilder();

        if (QuestionType.TITA.value().equalsIgnoreCase(this.type)){
            builder.append("\"").append(StringUtility.html2text(this.question)).append(" ").append(StringUtility.html2text(this.passage)).append("\"").append(",");
        }else{
            builder.append("\"").append(StringUtility.html2text(this.question)).append("\"").append(",");
        }
        builder.append("\"").append(this.type.toUpperCase()).append("\"").append(",");
        builder.append("\"").append(StringUtility.html2text(isNullThenEmpty(this.subjectId))).append("\"").append(",");
        builder.append("\"").append("").append("\"").append(",");
        if (QuestionType.PASSAGE.value().equalsIgnoreCase(this.type)){
            builder.append("\"").append(StringUtility.html2text(isNullThenEmpty(passage))).append("\"").append(",");
        }else {
            builder.append("\"").append("").append("\"").append(",");
        }
        builder.append("\"").append("").append("\"").append(",");

        int count = 0;
        StringBuilder correctOption = null;
        String answerText = "";
        if (this.options != null) {
            for (MigrationSchemaOption option : this.options) {
                ++count;
                builder.append("\"").append(StringUtility.html2text(option.getOption())).append("\"").append(",");
                if (option.isCorrect()){
                    if (correctOption == null){
                        correctOption = new StringBuilder().append(count);
                    }else {
                        correctOption.append("|").append(count);
                    }
                    if (QuestionType.TITA.value().equalsIgnoreCase(this.type)){
                        answerText = StringUtility.html2text(option.getOption());
                    }
                }
            }
        }
        if (count < maxOptions){
            int iter = maxOptions;
            while (iter > count){
                builder.append("\"").append("").append("\"").append(",");
                --iter;
            }
        }
        builder.append("\"").append(answerText).append("\"").append(",");
        builder.append("\"").append(isNullThenEmpty(correctOption)).append("\"").append(",");
        builder.append("\"").append(StringUtility.html2text(isNullThenEmpty(explanation))).append("\"").append(",");
        builder.append("\"").append(marks).append("\"").append(",");
        builder.append("\"").append(negativeMarks).append("\"").append(",");
        builder.append("\"").append("0").append("\"").append(",");
        builder.append("\"").append(parseDifficultyLevel(isNullThenEmpty(difficultyId))).append("\"").append(",");
        builder.append("\"").append(isNullThenEmpty(topicId)).append("\"").append(",");
        builder.append("\"").append(isNullThenEmpty(subTopicId)).append("\"").append(",");
        builder.append("\"").append("").append("\"").append(",");
        /* migration specific */
        builder.append("\"").append(isNullThenEmpty(testId)).append("\"").append(",");
        builder.append("\"").append(StringUtility.html2text(isNullThenEmpty(testName))).append("\"").append(",");
        builder.append("\"").append(isNullThenEmpty(sectionId)).append("\"").append(",");
        builder.append("\"").append(StringUtility.html2text(isNullThenEmpty(sectionName))).append("\"");
        builder.append("\n");
        return builder.toString();
    }

    private String parseDifficultyLevel(String difficultyId) {
        if ("2".equalsIgnoreCase(difficultyId)){
            return "MEDIUM";
        }else if ("3".equalsIgnoreCase(difficultyId)){
            return "HARD";
        }
        return "EASY";
    }

    public static String getHeaders(int maxOptions){
        StringBuilder builder = new StringBuilder();
        builder.append("\"").append("name").append("\"").append(",");
        builder.append("\"").append("type").append("\"").append(",");
        builder.append("\"").append("subject").append("\"").append(",");
        builder.append("\"").append("description").append("\"").append(",");
        builder.append("\"").append("passage").append("\"").append(",");
        builder.append("\"").append("tags").append("\"").append(",");
        int count = 0;
        while (maxOptions > count) {
            ++count;
            builder.append("\"").append("option_").append(count).append("\"").append(",");
        }
        builder.append("\"").append("correct_answer").append("\"").append(",");
        builder.append("\"").append("correct_options").append("\"").append(",");
        builder.append("\"").append("explanation").append("\"").append(",");
        builder.append("\"").append("positive_marks").append("\"").append(",");
        builder.append("\"").append("negative_marks").append("\"").append(",");
        builder.append("\"").append("skip_marks").append("\"").append(",");
        builder.append("\"").append("difficulty_level").append("\"").append(",");
        builder.append("\"").append("topic").append("\"").append(",");
        builder.append("\"").append("sub_topic").append("\"").append(",");
        builder.append("\"").append("reference").append("\"").append(",");
        /* migration specific */
        builder.append("\"").append("migration_test_id").append("\"").append(",");
        builder.append("\"").append("migration_test_name").append("\"").append(",");
        builder.append("\"").append("migration_section_id").append("\"").append(",");
        builder.append("\"").append("migration_section_name").append("\"");
        builder.append("\n");
        return builder.toString();
    }

    public Map<String, Object> getRecordAsMap(int maxOptions, String testId, String testName, String sectionId, String sectionName, boolean removeHtmlContent){
        Map<String, Object> record = new LinkedHashMap<>();

        if (QuestionType.TITA.value().equalsIgnoreCase(this.type)){
            record.put("name", getTitaQuestionName(removeHtmlContent));
        }else{
            record.put("name", StringUtility.html2text(this.question, removeHtmlContent));
        }
        record.put("type", this.type.toUpperCase());
        record.put("subject", StringUtility.html2text(isNullThenEmpty(this.subjectId), removeHtmlContent));
        record.put("description", "");
//        if (QuestionType.PASSAGE.value().equalsIgnoreCase(this.type)) {
        if (QuestionType.MCQ.value().equalsIgnoreCase(this.type) && StringUtils.isNotEmpty(this.passage)){
            record.put("passage", StringUtility.html2text(this.passage, removeHtmlContent));
        }else {
            record.put("passage", "");
        }
        record.put("tags", "");

        int count = 0;
        StringBuilder correctOption = null;
        String answerText = "";
        if (this.options != null) {
            for (MigrationSchemaOption option : this.options) {
                ++count;
                if (QuestionType.TITA.value().equalsIgnoreCase(this.type)){
                    record.put("option_" + count, "");
                }else {
                    record.put("option_" + count, StringUtility.html2text(option.getOption(), removeHtmlContent));
                }
                if (option.isCorrect()){
                    if (correctOption == null){
                        correctOption = new StringBuilder().append(count);
                    }else {
                        correctOption.append("|").append(count);
                    }
                    if (QuestionType.TITA.value().equalsIgnoreCase(this.type)){
                        answerText = StringUtility.html2text(option.getOption(), removeHtmlContent);
                    }
                }
            }
        }
        while (maxOptions > count){
            ++count;
            record.put("option_" + count, "");
        }
        record.put("correct_answer", answerText);
        if (QuestionType.TITA.value().equalsIgnoreCase(this.type)){
            record.put("correct_options", "");
        }else {
            record.put("correct_options", isNullThenEmpty(correctOption));
        }
        String finalExplanation = "";
        String part1 = StringUtility.html2text(isNullThenEmpty(explanation), removeHtmlContent);
        String part2 = StringUtility.html2text(isNullThenEmpty(videoExplanation), removeHtmlContent);
        part1 = isNullThenEmpty(part1);
        part2 = isNullThenEmpty(part2);
        if (StringUtils.isNotEmpty(part1)){
            finalExplanation += part1;
        }
        if (StringUtils.isNotEmpty(part2)){
            if (StringUtils.isNotEmpty(finalExplanation)){
                finalExplanation += "<br>" + part2;
            }else {
                finalExplanation = part2;
            }
        }
        record.put("explanation", finalExplanation);
//        record.put("explanation", StringUtility.html2text(isNullThenEmpty(explanation), removeHtmlContent));
        record.put("positive_marks", marks);
        record.put("negative_marks", negativeMarks);
        record.put("skip_marks", 0);
        record.put("difficulty_level", parseDifficultyLevel(isNullThenEmpty(difficultyId)));
        record.put("topic", isNullThenEmpty(topicId));
        record.put("sub_topic", isNullThenEmpty(subTopicId));
        record.put("reference", "");
        /* migration specific */
        record.put("migration_test_id", testId);
        record.put("migration_test_name", testName);
        record.put("migration_section_id", sectionId);
        record.put("migration_section_name", sectionName);

        return record;
    }

	private String getTitaQuestionName(boolean removeHtmlContent) {
		return StringUtility.html2text(this.question, removeHtmlContent).replaceAll("\\[quizky-text\\]", "") + " " + StringUtility.html2text(isNullThenEmpty(this.passage), removeHtmlContent);
	}

    public String isNullThenEmpty(String value){
        return value == null ? "": value;
    }

    public String isNullThenEmpty(StringBuilder value){
        return value == null ? "": value.toString();
    }
}