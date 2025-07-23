package com.increff.pos.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    public static ZonedDateTime getYesterday() {
        return ZonedDateTime.now(ZoneOffset.UTC).minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
    }
    public static ZonedDateTime toStartOfDayUTC(LocalDate localDate) {
        if (localDate == null) return null;
        return localDate.atStartOfDay(ZoneOffset.UTC);
    }
    public static ZonedDateTime toEndOfDayUTC(LocalDate localDate) {
        if (localDate == null) return null;
        return localDate.atTime(23, 59, 59, 999_999_999).atZone(ZoneOffset.UTC);
    }
    public static ZonedDateTime getStartOfDay(ZonedDateTime dateTime) {
        return dateTime.withHour(0).withMinute(0).withSecond(0).withNano(0);
    }
    public static ZonedDateTime getEndOfDay(ZonedDateTime dateTime) {
        return dateTime.withHour(23).withMinute(59).withSecond(59).withNano(999_999_999);
    }
}