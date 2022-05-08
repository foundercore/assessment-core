package com.assessment.questionpaper.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.constraints.NotNull;

public enum QuestionPaperStatus {
	DRAFT("DRAFT"), PENDING_VERIFICATION("PENDING_VERIFICATION"), VERIFIED("VERIFIED"), PUBLISHED("PUBLISHED"),
	DELETED("DELETED"), ARCHIVED("ARCHIVED");

	private final String value;

	private QuestionPaperStatus(String value) {
		this.value = value;
	}

	public String toString() {
		return this.value + " " + this.name();
	}

	public String value() {
		return this.value;
	}

	public static QuestionPaperStatus valueOf(QuestionPaperStatus qt) {
		return valueOf(qt.value);
	}

	@NotNull
	public static QuestionPaperStatus resolve(String qt) {
		QuestionPaperStatus[] var1 = values();
		for (QuestionPaperStatus status : var1) {
			if (Objects.equals(status.value, qt)) {
				return status;
			}
		}

		return null;
	}

	public static List<String> getAllStatus() {
		List<String> types = new ArrayList<>();
		QuestionPaperStatus[] var1 = values();
		for (QuestionPaperStatus qt : var1) {
			types.add(qt.value);
		}
		return types;
	}
}
