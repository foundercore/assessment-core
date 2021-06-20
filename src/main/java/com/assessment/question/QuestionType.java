package com.assessment.question;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public enum QuestionType {
    AWA("AWA"),
    FILL_IN_THE_BLANKS("FILL_IN_THE_BLANKS"),
    FRACTIONS("FRACTIONS"),
    MCQ("MCQ"),
    PASSAGE("PASSAGE"),
    TITA("TITA"),
    TEXT_HIGHLIGHTING("TEXT_HIGHLIGHTING")
    ;

    private final String value;

    private QuestionType(String value) {
        this.value = value;
    }

    public String toString() {
        return this.value + " " + this.name();
    }

    public String value() {
        return this.value;
    }

    public static QuestionType valueOf(QuestionType qt) {
        return valueOf(qt.value);
    }

    @NotNull
    public static QuestionType resolve(String qt) {
        QuestionType[] var1 = values();
        for (QuestionType status : var1) {
            if (Objects.equals(status.value, qt)) {
                return status;
            }
        }

        return null;
    }

    public static List<String> getQuestionTypes(){
        List<String> types = new ArrayList<>();
        QuestionType[] var1 = values();
        for (QuestionType qt : var1) {
            types.add(qt.value);
        }
        return types;
    }
}
