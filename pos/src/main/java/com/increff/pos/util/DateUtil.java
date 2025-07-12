package com.increff.pos.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

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
}