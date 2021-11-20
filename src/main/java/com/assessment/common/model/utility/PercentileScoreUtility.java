/**
 * 
 */
package com.assessment.common.model.utility;

import java.util.Map;

import com.assessment.questionpaper.dto.QuestionPaperResponseDto;
import com.assessment.questionpaper.entity.QuestionPaper.TestControlParams;

/**
 * @author kunwar
 *
 */
public class PercentileScoreUtility {

	public static Map<Double, Double> getPercentileForSection(QuestionPaperResponseDto testConfig, String sectionId) {
		if (testConfig.getControlParam() != null && testConfig.getControlParam().getPercentileScoreCard() != null
				&& testConfig.getControlParam().getPercentileScoreCard().getSectionLevelPercentile() != null
				&& testConfig.getControlParam().getPercentileScoreCard().getSectionLevelPercentile()
						.get(sectionId) != null) {
			return testConfig.getControlParam().getPercentileScoreCard().getSectionLevelPercentile().get(sectionId);
		}

		return null;
	}

	public static Map<Double, Double> getPercentileForTest(QuestionPaperResponseDto testConfig) {
		if (testConfig.getControlParam() != null && testConfig.getControlParam().getPercentileScoreCard() != null
				&& testConfig.getControlParam().getPercentileScoreCard().getTestLevelPercentile() != null) {
			return testConfig.getControlParam().getPercentileScoreCard().getTestLevelPercentile();
		}

		return null;
	}

	public static Map<Double, Double> getPercentileForSection(TestControlParams controlParam, String sectionId) {
		if (controlParam != null && controlParam.isPercentile() && controlParam.getPercentileScoreCard() != null
				&& controlParam.getPercentileScoreCard().getSectionLevelPercentile() != null
				&& controlParam.getPercentileScoreCard().getSectionLevelPercentile().get(sectionId) != null) {
			return controlParam.getPercentileScoreCard().getSectionLevelPercentile().get(sectionId);
		}

		return null;
	}

	public static Map<Double, Double> getPercentileForTest(TestControlParams controlParam) {
		if (controlParam != null && controlParam.getPercentileScoreCard() != null && controlParam.isPercentile()
				&& controlParam.getPercentileScoreCard().getTestLevelPercentile() != null) {
			return controlParam.getPercentileScoreCard().getTestLevelPercentile();
		}

		return null;
	}

	public static Double getPercentileForSectionMark(QuestionPaperResponseDto testConfig, String sectionId,
			Double marksReceived) {
		if (testConfig.getControlParam() != null && testConfig.getControlParam().getPercentileScoreCard() != null
				&& testConfig.getControlParam().getPercentileScoreCard().getSectionLevelPercentile() != null
				&& testConfig.getControlParam().getPercentileScoreCard().getSectionLevelPercentile()
						.get(sectionId) != null) {

			Map<Double, Double> sectionLevelPercentile = testConfig.getControlParam().getPercentileScoreCard()
					.getSectionLevelPercentile().get(sectionId);
			if (sectionLevelPercentile != null && sectionLevelPercentile.get(marksReceived) != null) {
				return sectionLevelPercentile.get(marksReceived);
			}
		}

		return 0.0;
	}
}
