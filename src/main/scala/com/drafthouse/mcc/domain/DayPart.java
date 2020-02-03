package com.drafthouse.mcc.domain;

import java.time.LocalTime;

/** The parts of the day in cinema local time */
public enum DayPart {

    MORNING(LocalTime.of(6, 0), LocalTime.of(11, 0)),
    MATINEE(LocalTime.of(11, 0), LocalTime.of(17, 0)),
    PRIME(LocalTime.of(17, 0), LocalTime.of(20, 30)),
    LATE(LocalTime.of(20, 30), LocalTime.of(23, 30)),
    VERY_LATE(LocalTime.of(23, 30), LocalTime.of(6, 0)),
    UNKNOWN(LocalTime.of(0, 0), LocalTime.of(0, 0));

    private LocalTime _startTime;
    private LocalTime _endTime;

    DayPart(LocalTime startTime, LocalTime endTime) {
        _startTime = startTime;
        _endTime = endTime;
    }

    public LocalTime getStartTime() { return _startTime; }
    public LocalTime getEndTime() { return _endTime; }

}
