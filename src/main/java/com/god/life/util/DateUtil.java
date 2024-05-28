package com.god.life.util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class DateUtil {

    public static String formattingTimeDifference(LocalDateTime createDate) {
        LocalDateTime now = LocalDateTime.now();

        long yearDifference = ChronoUnit.YEARS.between(createDate, now);
        if (yearDifference > 0) {
            return yearDifference + "년 전";
        }

        long monthDifference = ChronoUnit.MONTHS.between(createDate, now);
        if (monthDifference > 0) {
            return monthDifference + "개월 전";
        }

        long dayDifference = ChronoUnit.DAYS.between(createDate, now);
        if (dayDifference > 0) {
            return dayDifference + "일 전";
        }

        long hourDifference = ChronoUnit.HOURS.between(createDate, now);
        if (hourDifference > 0) {
            return hourDifference + "시간 전";
        }

        long minutesDifference = ChronoUnit.MINUTES.between(createDate, now);
        if (minutesDifference > 0) {
            return minutesDifference + "분 전";
        }

        long secondDifference = ChronoUnit.SECONDS.between(createDate, now);
        if (secondDifference > 0) {
            return secondDifference + "초 전";
        }

        return "방금 전";
    }

}
