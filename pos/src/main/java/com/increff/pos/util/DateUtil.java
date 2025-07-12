package com.increff.pos.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Utility class for date parsing and manipulation operations.
 * Centralizes date-related logic to avoid code duplication and improve maintainability.
 */
public class DateUtil {

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

    public static Instant calStartInstant(LocalDate date) {
        ZonedDateTime startOfDay = date.atStartOfDay(ZoneOffset.UTC);
        Instant startInstant = startOfDay.toInstant();
        return startInstant;
    }
    public static Instant calEndInstant(LocalDate date) {
        ZonedDateTime endOfDay = date.atTime(23, 59, 59).atZone(ZoneOffset.UTC);
        Instant endInstant = endOfDay.toInstant();
        return endInstant;
    }
    public static ZonedDateTime toStartOfDay(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        return ZonedDateTime.parse(dateStr).withZoneSameInstant(ZoneOffset.UTC)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    public static ZonedDateTime toEndOfDay(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        return ZonedDateTime.parse(dateStr).withZoneSameInstant(ZoneOffset.UTC)
                .withHour(23).withMinute(59).withSecond(59).withNano(999_999_999);
    }

}
//TODO: @Jackson