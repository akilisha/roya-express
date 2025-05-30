package com.akilisha.oss.web.shared.datetime;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.TimeZone;

public class Clock {

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static Date fromNow(int amount, ChronoUnit unit) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime then = now.plus(amount, unit);
        ZonedDateTime later = then.atZone(ZoneId.systemDefault());
        return Date.from(later.toInstant());
    }

    public static Date fromNow(Duration duration) {
        Instant instant = Instant.now();
        ZoneId zoneId = TimeZone.getDefault().toZoneId();
        ZonedDateTime now = ZonedDateTime.ofInstant(instant, zoneId);
        ZonedDateTime later = now.plus(duration);
        return Date.from(later.toInstant());
    }
}
