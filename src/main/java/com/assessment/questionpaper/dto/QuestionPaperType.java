package com.assessment.questionpaper.dto;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

public enum QuestionPaperType {
	DEFAULT("DEFAULT"), FULL_LENGTH("FULL_LENGTH"), NMAT("NMAT"), QUICK_FOCUSED_EXCERCISE("QUICK FOCUSED EXCERCISE"),
	PRACTICE_EXERCISE("PRACTICE EXERCISE"), SECTIONAL("SECTIONAL"), UNIT_EXCERCISE("UNIT EXCERCISE"),
	CLASS_EXCERCISE("CLASS EXCERCISE");

	private final String questionPaperType;

	private QuestionPaperType(String questionPaperType) {
		this.questionPaperType = questionPaperType;
	}

	public String toString() {
		return this.questionPaperType + " " + this.name();
	}

	public String questionPaperType() {
		return this.questionPaperType;
	}

	public static QuestionPaperType valueOf(QuestionPaperType qt) {
		return valueOf(qt.questionPaperType);
	}

	@NotNull
	public static QuestionPaperType resolve(String qt) {
		if (qt == null) {
			return null;
		}
		QuestionPaperType[] var1 = values();
		for (QuestionPaperType status : var1) {
			if (status.questionPaperType.trim().toLowerCase().equals(qt.trim().toLowerCase())) {
				return status;
			}
		}
		return null;
	}

	public static List<String> getQuestionPaperTypes() {
		List<String> types = new ArrayList<>();
		QuestionPaperType[] var1 = values();
		for (QuestionPaperType qt : var1) {
			types.add(qt.questionPaperType);
		}
		return types;
	}
}
