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
    
    /**
     * Parses a date string and converts it to an Instant representing the start of the day (00:00 UTC).
     * 
     * @param dateString The date string in ISO format (YYYY-MM-DD)
     * @return Instant representing the start of the day, or null if dateString is null or empty
     */
    public static Instant parseStartDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateString).atStartOfDay(ZoneOffset.UTC).toInstant(); // 00:00 UTC
    }
    
    /**
     * Parses a date string and converts it to an Instant representing the end of the day (23:59:59.999 UTC).
     * 
     * @param dateString The date string in ISO format (YYYY-MM-DD)
     * @return Instant representing the end of the day, or null if dateString is null or empty
     */
    public static Instant parseEndDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateString)
                .plusDays(1)                              // move to next day
                .atStartOfDay(ZoneOffset.UTC)             // at 00:00 UTC
                .toInstant()
                .minusMillis(1);                          // back to 23:59:59.999
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
}
//TODO: @Jackson