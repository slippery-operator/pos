package com.increff.pos.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, h:mm a z");

    public static ZonedDateTime parseStartDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        LocalDate localDate = LocalDate.parse(dateStr); // parses "yyyy-MM-dd"
        return localDate.atStartOfDay(ZoneOffset.UTC);  // 00:00 UTC
    }
    public static ZonedDateTime parseEndDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        LocalDate localDate = LocalDate.parse(dateStr);
        return localDate.atTime(23, 59, 59, 999_999_999).atZone(ZoneOffset.UTC); // 23:59:59.999 UTC
    }
    public static ZonedDateTime getYesterday() {
        return ZonedDateTime.now(ZoneOffset.UTC).minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    public static String getCurrentFormattedTimestamp() {
        return ZonedDateTime.now().format(FORMATTER);
    }

    public static String format(ZonedDateTime dateTime) {
        return dateTime.format(FORMATTER);
    }
}