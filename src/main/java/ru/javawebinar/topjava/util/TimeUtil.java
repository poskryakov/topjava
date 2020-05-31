package ru.javawebinar.topjava.util;

import java.time.LocalTime;
import java.util.Objects;

public class TimeUtil {

    /**
     * Checks if time is between two times.
     * <p>
     * The start time is included, but the end time is not.
     *
     * @param time  the time to check, not null
     * @param startInclusive  start time, not null, strictly before {@code endExclusive}
     * @param endExclusive  end time, not null, strictly after {@code startInclusive}
     * @return true if time is between start time and end time
     * @throws NullPointerException if any parameter is null
     */
    public static boolean isBetweenHalfOpen(LocalTime time, LocalTime startInclusive, LocalTime endExclusive) {
        Objects.requireNonNull(time, "Parameter time cannot be null.");
        Objects.requireNonNull(startInclusive, "Parameter startInclusive cannot be null.");
        Objects.requireNonNull(endExclusive, "Parameter endExclusive cannot be null.");

        return time.compareTo(startInclusive) >= 0 && time.compareTo(endExclusive) < 0;
    }
}
