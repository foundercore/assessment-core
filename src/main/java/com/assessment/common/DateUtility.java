package com.assessment.common;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

public class DateUtility {
    public final static String WIDE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public final static String NARROW_FORMAT = "yyyy-MM-dd";

    private static final ThreadLocal<DateTimeFormatter> JODA_WIDE_SDF = ThreadLocal.withInitial(() -> DateTimeFormat.forPattern(WIDE_FORMAT));
    private static final ThreadLocal<DateTimeFormatter> JODA_NARROW_SDF = ThreadLocal.withInitial(() -> DateTimeFormat.forPattern(NARROW_FORMAT));

    public static Date convertWideStringToDate(String date){
        return JODA_WIDE_SDF.get().parseDateTime(date).toDate();
    }

    public static Date convertNarrowStringToDate(String date){
        return JODA_NARROW_SDF.get().parseDateTime(date).toDate();
    }

    public static String convertDateToWideString(Date date){
        return JODA_WIDE_SDF.get().print(date.getTime());
    }

    public static String convertDateToNarrowString(Date date){
        return JODA_NARROW_SDF.get().print(date.getTime());
    }

    public static void main(String[] args) {
        String dt = "2021-01-29 17:15:13";
        System.out.println(convertWideStringToDate(dt));
        System.out.println(convertDateToWideString(convertWideStringToDate(dt)));
        System.out.println(convertDateToNarrowString(convertWideStringToDate(dt)));
        System.out.println(convertNarrowStringToDate(convertDateToNarrowString(convertWideStringToDate(dt))));
    }
}
