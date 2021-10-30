package com.assessment.questionpaper.entity;

import lombok.Data;

@Data
public class Metric {

	private int totalQuestions = 0;
	private double totalMarks = 0;
	private int correct = 0;
	private int incorrect = 0;
	private int skipped = 0;
	private int attempted = 0;

	private double marksReceived = 0;
	private double positiveMarks = 0;
	private double negativeMarks = 0;
	private double skippedMarks = 0;

	private double totalTimeInSec = 0;
	private double correctTimeInSec = 0;
	private double incorrectTimeInSec = 0;
	private double skippedTimeInSec = 0;
	private double percentileScore = 0;

	public void addTotal(double markAllocated, int timeElapsedInSec, double totalMarks) {
		this.totalQuestions++;
		this.marksReceived += markAllocated;
		this.totalTimeInSec += timeElapsedInSec;
		this.totalMarks += totalMarks;
	}

	public void addCorrect(double markAllocated, int timeElapsedInSec) {
		this.correct++;
		this.positiveMarks += markAllocated;
		this.correctTimeInSec += timeElapsedInSec;
	}

	public void addIncorrect(double markAllocated, int timeElapsedInSec) {
		this.incorrect++;
		this.negativeMarks += markAllocated;
		this.incorrectTimeInSec += timeElapsedInSec;
	}

	public void addSkipped(double markAllocated, int timeElapsedInSec) {
		this.skipped++;
		this.skippedMarks += markAllocated;
		this.skippedTimeInSec += timeElapsedInSec;
	}

	public void addAttempt() {
		this.attempted++;
	}
}
