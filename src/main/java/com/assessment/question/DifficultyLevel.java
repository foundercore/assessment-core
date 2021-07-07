package com.assessment.question;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.constraints.NotNull;

public enum DifficultyLevel {
    EASY("EASY"),
    MEDIUM("MEDIUM"),
	HARD("HARD"), VERY_HARD("VERY HARD")
    ;

    private final String value;

    private DifficultyLevel(String value) {
        this.value = value;
    }

    public String toString() {
        return this.value + " " + this.name();
    }

    public String value() {
        return this.value;
    }

    public static DifficultyLevel valueOf(DifficultyLevel qt) {
        return valueOf(qt.value);
    }

    @NotNull
    public static DifficultyLevel resolve(String qt) {
        DifficultyLevel[] var1 = values();
        for (DifficultyLevel status : var1) {
            if (Objects.equals(status.value, qt)) {
                return status;
            }
        }
        return null;
    }

    public static List<String> getAllEntities(){
        List<String> types = new ArrayList<>();
        DifficultyLevel[] var1 = values();
        for (DifficultyLevel qt : var1) {
            types.add(qt.value);
        }
        return types;
    }
}
