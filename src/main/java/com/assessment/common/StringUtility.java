package com.assessment.common;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;

import net.openhft.hashing.LongHashFunction;

public class StringUtility {

    public static String decodeBase64(String base64Str) {
        return new String(Base64.getDecoder().decode(base64Str));
    }

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

	public String isNullThenEmpty(String value) {
		return value == null ? "" : value;
	}

    public static String generateCommonLangPassword() {
        String upperCaseLetters = RandomStringUtils.random(2, 65, 90, true, true);
        String lowerCaseLetters = RandomStringUtils.random(2, 97, 122, true, true);
        String numbers = RandomStringUtils.randomNumeric(2);
        String specialChar = RandomStringUtils.random(2, 33, 47, false, false);
        String totalChars = RandomStringUtils.randomAlphanumeric(2);
        String combinedChars = upperCaseLetters.concat(lowerCaseLetters)
                .concat(numbers)
                .concat(specialChar)
                .concat(totalChars);
        List<Character> pwdChars = combinedChars.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        Collections.shuffle(pwdChars);
        return pwdChars.stream()
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

    public static long zeroAllocationHash(String string) {
        return LongHashFunction.xx().hashChars(string);
    }

    public static String getClientTimezone(){
        return ConfigUtility.instance().getProperty("app.client.timezone-id");
    }

    public static String html2text(String html) {
		if (html == null)
			return StringUtils.EMPTY;
        return Jsoup.parse(html).text();
    }

    public static String html2text(String html, boolean removeHtml) {
        if (removeHtml && StringUtils.isNotEmpty(html)){
            return html2text(html);
        }else {
            return html;
        }
    }

	public static Double parseStringToOptionalDouble(String value) {
		return value == null || value.isEmpty() || value.equals("null") ? 0.0 : Double.valueOf(value);
	}
}
