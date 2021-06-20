package com.assessment.questionpaper.dto;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public enum EvaluationState {
    PENDING("PENDING"),
    INIT("INIT"),
    COMPLETED("COMPLETED")
    ;

    private final String value;

    private EvaluationState(String value) {
        this.value = value;
    }

    public String toString() {
        return this.value + " " + this.name();
    }

    public String value() {
        return this.value;
    }

    public static EvaluationState valueOf(EvaluationState qt) {
        return valueOf(qt.value);
    }

    @NotNull
    public static EvaluationState resolve(String qt) {
        EvaluationState[] var1 = values();
        for (EvaluationState status : var1) {
            if (Objects.equals(status.value, qt)) {
                return status;
            }
        }
        return null;
    }

    public static List<String> getAllEntities(){
        List<String> types = new ArrayList<>();
        EvaluationState[] var1 = values();
        for (EvaluationState qt : var1) {
            types.add(qt.value);
        }
        return types;
    }
}
