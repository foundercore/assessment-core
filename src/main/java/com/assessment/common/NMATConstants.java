package com.assessment.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NMATConstants {
	// Map for the combination of the probable questions in NMAT test.
	// It represents : Section -> {KeyForNmat -> Question Count}
	public static Map<String, Map<String, Integer>> NMAT_QUESTION_CATAGORIES = new HashMap<>();

	static void populateNmatQuestionCatagories() {
		// make this read from JSON
		Map<String, Integer> quantQuestionsCombination = new HashMap<>();
		quantQuestionsCombination.put("Quantitative ReasoningArithmeticMEDIUM", 2);
		quantQuestionsCombination.put("Quantitative ReasoningArithmeticHARD", 2);
		quantQuestionsCombination.put("Quantitative ReasoningArithmeticVERY HARD", 4);
		quantQuestionsCombination.put("Quantitative ReasoningArithmeticEASY", 2);
		quantQuestionsCombination.put("Quantitative ReasoningAdvanced ArithmeticHARD", 1);
		quantQuestionsCombination.put("Quantitative ReasoningAdvanced ArithmeticVERY HARD", 2);
		quantQuestionsCombination.put("Quantitative ReasoningAdvanced ArithmeticEASY", 1);
		quantQuestionsCombination.put("Quantitative ReasoningGeometryEASY", 1);
		quantQuestionsCombination.put("Quantitative ReasoningAlgebra", 1);
		quantQuestionsCombination.put("Quantitative ReasoningAlgebraVERY HARD", 2);
		quantQuestionsCombination.put("Quantitative ReasoningAlgebraEASY", 1);
		quantQuestionsCombination.put("Quantitative ReasoningData Sufficiency", 2);
		quantQuestionsCombination.put("Quantitative ReasoningData SufficiencyHARD", 2);
		quantQuestionsCombination.put("Quantitative ReasoningData SufficiencyVERY HARD", 1);
		quantQuestionsCombination.put("Quantitative ReasoningData Interpretation", 4);
		quantQuestionsCombination.put("Quantitative ReasoningData InterpretationHARD", 4);
		quantQuestionsCombination.put("Quantitative ReasoningData InterpretationEASY", 4);
		NMAT_QUESTION_CATAGORIES.put(Nmat_Sections.QUANT_REASONING.getSectionName(), quantQuestionsCombination);

		// Logical Reasoning
		Map<String, Integer> logicalReasoningQuestionsCombination = new HashMap<>();
		logicalReasoningQuestionsCombination.put("Logical ReasoningReasoning - AnalyticalBlood RelationMEDIUM", 1);
		logicalReasoningQuestionsCombination.put("Logical ReasoningReasoning - AnalyticalMiscellaneousEASY", 1);
		logicalReasoningQuestionsCombination.put("Logical ReasoningVerbal LogicStatement ConclusionEASY", 1);
		logicalReasoningQuestionsCombination.put("Logical ReasoningReasoning - AnalyticalArrangementMEDIUM", 4);
		logicalReasoningQuestionsCombination.put("Logical ReasoningReasoning - AnalyticalPuzzlesHARD", 4);
		logicalReasoningQuestionsCombination.put("Logical ReasoningReasoning - AnalyticalMiscellaneousVERY HARD", 4);
		logicalReasoningQuestionsCombination.put("Logical ReasoningVerbal LogicSyllogismEASY", 1);
		logicalReasoningQuestionsCombination.put("Logical ReasoningVerbal LogicStrengthen WeakenEASY", 1);
		logicalReasoningQuestionsCombination.put("Logical ReasoningCritical ReasoningAssumptionEASY", 1);
		logicalReasoningQuestionsCombination.put("Logical ReasoningVerbal LogicStatement ConclusionMEDIUM", 1);
		logicalReasoningQuestionsCombination.put("Logical ReasoningCritical ReasoningStatement ArgumentVERY HARD", 1);
		logicalReasoningQuestionsCombination.put("Logical ReasoningCritical ReasoningInferenceEASY", 1);
		logicalReasoningQuestionsCombination.put("Logical ReasoningVerbal LogicStatement Course of ActionMEDIUM", 1);
		logicalReasoningQuestionsCombination.put("Logical ReasoningVerbal LogicStatement Course of ActionHARD", 1);
		logicalReasoningQuestionsCombination.put("Logical ReasoningCritical ReasoningDecision MakingEASY", 1);
		logicalReasoningQuestionsCombination.put("Logical ReasoningCritical ReasoningStatement ArgumentMEDIUM", 1);
		logicalReasoningQuestionsCombination.put("Logical ReasoningCritical ReasoningAssumptionVERY HARD", 1);
		logicalReasoningQuestionsCombination.put("Logical ReasoningVerbal LogicStrengthen WeakenHARD", 2);
		logicalReasoningQuestionsCombination.put("Logical ReasoningCritical ReasoningInferenceVERY HARD", 1);
		logicalReasoningQuestionsCombination.put("Logical ReasoningCritical ReasoningAssumptionHARD", 1);
		logicalReasoningQuestionsCombination.put("Logical ReasoningCritical ReasoningDecision MakingVERY HARD", 1);
		logicalReasoningQuestionsCombination.put("Logical ReasoningReading ComprehensionReading Comprehension EASY", 1);
		logicalReasoningQuestionsCombination.put("Logical ReasoningCritical ReasoningConclusionMEDIUM", 1);
		logicalReasoningQuestionsCombination.put("Logical ReasoningTotalVERY HARD", 1);
		logicalReasoningQuestionsCombination.put("Logical ReasoningCritical ReasoningInferenceHARD", 1);
		logicalReasoningQuestionsCombination.put("Logical ReasoningGrammarError DetectionEASY", 1);
		NMAT_QUESTION_CATAGORIES.put(Nmat_Sections.LOGICAL_REASONING.getSectionName(),
				logicalReasoningQuestionsCombination);

		// Verbal Reasoning
		Map<String, Integer> verbalReasoningQuestionsCombination = new HashMap<>();
		verbalReasoningQuestionsCombination.put("Verbal ReasoningReading ComprehensionReading Comprehension HARD", 4);
		verbalReasoningQuestionsCombination.put("Verbal ReasoningReading ComprehensionReading Comprehension VERY HARD",
				4);
		verbalReasoningQuestionsCombination.put("Verbal ReasoningReading ComprehensionReading Comprehension EASY", 4);
		verbalReasoningQuestionsCombination.put("Verbal ReasoningAnalogiesMEDIUM", 2);
		verbalReasoningQuestionsCombination.put("Verbal ReasoningAnalogiesHARD", 1);
		verbalReasoningQuestionsCombination.put("Verbal ReasoningAnalogiesVERY HARD", 1);
		verbalReasoningQuestionsCombination.put("Verbal ReasoningAnalogiesEASY", 1);
		verbalReasoningQuestionsCombination.put("Verbal ReasoningGrammarError DetectionMEDIUM", 3);
		verbalReasoningQuestionsCombination.put("Verbal ReasoningGrammarError DetectionHARD", 2);
		verbalReasoningQuestionsCombination.put("Verbal ReasoningGrammarError DetectionVERY HARD", 1);
		verbalReasoningQuestionsCombination.put("Verbal ReasoningGrammarError DetectionEASY", 1);
		verbalReasoningQuestionsCombination.put("Verbal ReasoningGrammarFill in the blanksMEDIUM", 1);
		verbalReasoningQuestionsCombination.put("Verbal ReasoningGrammarFill in the blanksVERY HARD", 1);
		verbalReasoningQuestionsCombination.put("Verbal ReasoningGrammarFill in the blanksEASY", 1);
		verbalReasoningQuestionsCombination.put("Verbal ReasoningPara-jumbleSentence JumblingMEDIUM", 1);
		verbalReasoningQuestionsCombination.put("Verbal ReasoningPara-jumbleSentence JumblingHARD", 1);
		verbalReasoningQuestionsCombination.put("Verbal ReasoningPara-jumbleSentence JumblingVERY HARD", 1);
		verbalReasoningQuestionsCombination.put("Verbal ReasoningPara-jumbleSentence JumblingEASY", 1);
		verbalReasoningQuestionsCombination.put("Verbal ReasoningVocabularySentence CompletionMEDIUM", 2);
		verbalReasoningQuestionsCombination.put("Verbal ReasoningVocabularySentence CompletionHARD", 1);
		verbalReasoningQuestionsCombination.put("Verbal ReasoningVocabularySentence CompletionVERY HARD", 1);
		verbalReasoningQuestionsCombination.put("Verbal ReasoningVocabularySentence CompletionEASY", 1);
		NMAT_QUESTION_CATAGORIES.put(Nmat_Sections.VERBAL_REASONING.getSectionName(),
				verbalReasoningQuestionsCombination);

	}

	public enum Nmat_Sections {
		QUANT_REASONING("Quantitative Skill"), LOGICAL_REASONING("Logical Reasoning"),
		VERBAL_REASONING("Language Skill");

		private String sectionName;

		Nmat_Sections(String sectionName) {
			this.sectionName = sectionName;
		}

		public String getSectionName() {
			return this.sectionName;
		}

		public static List<String> getAllSectionNames() {
			Nmat_Sections[] sections = values();
			List<String> sectionNames = new ArrayList<String>();
			for (Nmat_Sections section : sections) {
				sectionNames.add(section.getSectionName());
			}
			return sectionNames;
		}
	}
}
