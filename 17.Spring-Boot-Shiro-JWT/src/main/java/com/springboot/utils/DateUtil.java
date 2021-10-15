package com.springboot.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

/**
 * Description: DateUtil
 *
 * @author hbl
 * @date 2021/05/26 0026 14:37
 */
public class DateUtil
{
    public static final String FULL_TIME_PATTERN = "yyyyMMddHHmmss";

    public static final String FULL_TIME_SPLIT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static String formatFullTime(LocalDateTime localDateTime)
    {
        return null;
    }

    public static String formatFullTime(LocalDateTime localDateTime, String pattern)
    {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        return localDateTime.format(dateTimeFormatter);
    }

    public static String formatCSTTime(String date, String format) throws ParseException
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyy", Locale.US);
        Date d = simpleDateFormat.parse(date);
        return DateUtil.getDateFormat(d, format);
    }

    private static String getDateFormat(Date date, String dateFormatType)
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormatType);
        return simpleDateFormat.format(date);
    }
}
