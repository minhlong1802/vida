package com.example.vida.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    public static Date parseDate(String dateStr) throws ParseException {
        return null;
    }

    public static String formatDate(Date date) {
        return null;
    }

    public static Date addDays(Date date, int days) {
        return null;
    }

    public static Date subtractDays(Date date, int days) {
        return null;
    }

    public static long daysBetween(Date startDate, Date endDate) {
        return 0;
    }

    public static Object isBefore(Date date1, Date date2) {
        return null;
    }

    public static Object isAfter(Date date1, Date date2) {
        return null;
    }

    public static Date getCurrentDate() {
        return null;
    }

    public static Date setTime(Date date, int hours, int minutes, int seconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, hours);
        cal.set(Calendar.MINUTE, minutes);
        cal.set(Calendar.SECOND, seconds);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    public static String dateToString(Date date, String format) {
        try {
            return new SimpleDateFormat(format).format(date);
        } catch (Exception e) {
            return "";
        }
    }
}