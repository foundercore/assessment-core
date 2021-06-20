package com.assessment.questionpaper.dto;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public enum AnswerState {
    CORRECT("CORRECT"),
    INCORRECT("INCORRECT"),
    SKIPPED("SKIPPED")
    ;

    private final String value;

    private AnswerState(String value) {
        this.value = value;
    }

    public String toString() {
        return this.value + " " + this.name();
    }

    public String value() {
        return this.value;
    }

    public static AnswerState valueOf(AnswerState qt) {
        return valueOf(qt.value);
    }

    @NotNull
    public static AnswerState resolve(String qt) {
        AnswerState[] var1 = values();
        for (AnswerState status : var1) {
            if (Objects.equals(status.value, qt)) {
                return status;
            }
        }
        return null;
    }

    public static List<String> getAllEntities(){
        List<String> types = new ArrayList<>();
        AnswerState[] var1 = values();
        for (AnswerState qt : var1) {
            types.add(qt.value);
        }
        return types;
    }
}
