package org.telegram.tutorbot.model.enums;

import java.time.DateTimeException;

public enum WeekDay {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;

    public static WeekDay getWeekDay(String weekDayNumber) {
        switch (weekDayNumber) {
            case "1" -> {
                return MONDAY;
            }
            case "2" -> {
                return TUESDAY;
            }
            case "3" -> {
                return WEDNESDAY;
            }
            case "4" -> {
                return THURSDAY;
            }
            case "5" -> {
                return FRIDAY;
            }
            case "6" -> {
                return SATURDAY;
            }
            case "7" -> {
                return SUNDAY;
            }
            default -> throw new DateTimeException("Invalid value for WeekDay: " + weekDayNumber);
        }
    }

    public static int getWeekDayNumber(WeekDay weekDay) {
        switch (weekDay) {
            case MONDAY -> {
                return 1;
            }
            case TUESDAY -> {
                return 2;
            }
            case WEDNESDAY -> {
                return 3;
            }
            case THURSDAY -> {
                return 4;
            }
            case FRIDAY -> {
                return 5;
            }
            case SATURDAY -> {
                return 6;
            }
            default -> {
                return 7;
            }
        }
    }
}
