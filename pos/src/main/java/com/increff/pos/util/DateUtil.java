package com.increff.pos.util;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Utility class for date parsing and manipulation operations.
 * Centralizes date-related logic to avoid code duplication and improve maintainability.
 * Now uses ZonedDateTime instead of Instant to leverage Jackson's JSR-310 support.
 */
public class DateUtil {
    
    /**
     * Parses a date string and converts it to a ZonedDateTime representing the start of the day (00:00 UTC).
     * Leverages Jackson's JSR-310 support for better date handling.
     * 
     * @param dateString The date string in ISO format (YYYY-MM-DD)
     * @return ZonedDateTime representing the start of the day, or null if dateString is null or empty
     */
    public static ZonedDateTime parseStartDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateString).atStartOfDay(ZoneOffset.UTC); // 00:00 UTC
    }
    
    /**
     * Parses a date string and converts it to a ZonedDateTime representing the end of the day (23:59:59.999 UTC).
     * Leverages Jackson's JSR-310 support for better date handling.
     * 
     * @param dateString The date string in ISO format (YYYY-MM-DD)
     * @return ZonedDateTime representing the end of the day, or null if dateString is null or empty
     */
    public static ZonedDateTime parseEndDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateString)
                .plusDays(1)                              // move to next day
                .atStartOfDay(ZoneOffset.UTC)             // at 00:00 UTC
                .minusNanos(1);                           // back to 23:59:59.999999999
    }
    
    /**
     * Calculates the start of day ZonedDateTime for a given LocalDate.
     * 
     * @param date The LocalDate to convert
     * @return ZonedDateTime representing the start of the day (00:00 UTC)
     */
    public static ZonedDateTime calStartDateTime(LocalDate date) {
        return date.atStartOfDay(ZoneOffset.UTC);
    }
    
    /**
     * Calculates the end of day ZonedDateTime for a given LocalDate.
     * 
     * @param date The LocalDate to convert
     * @return ZonedDateTime representing the end of the day (23:59:59 UTC)
     */
    public static ZonedDateTime calEndDateTime(LocalDate date) {
        return date.atTime(23, 59, 59).atZone(ZoneOffset.UTC);
    }
}
// Jackson's JSR-310 module (jackson-datatype-jsr310) automatically handles ZonedDateTime serialization/deserialization
// No additional configuration needed since it's already included in pom.xml