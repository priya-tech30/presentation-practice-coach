package com.presentationcoach.gamification;
import com.presentationcoach.gamification.StreakManager;

import com.presentationcoach.model.SessionData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class StreakManager {

    // Get the number of consecutive practice days ending today
    public static int calculateCurrentStreak(List<SessionData> sessions) {

        if (sessions == null || sessions.isEmpty()) {
            return 0;
        }

        Set<String> practiceDays = getPracticeDays(sessions);

        String today = getDateString(System.currentTimeMillis());

        // No practice today = current streak is 0
        if (!practiceDays.contains(today)) {
            return 0;
        }

        int streak = 1;

        long previousDayMillis =
                System.currentTimeMillis() - (24L * 60L * 60L * 1000L);

        while (practiceDays.contains(getDateString(previousDayMillis))) {
            streak++;
            previousDayMillis -= (24L * 60L * 60L * 1000L);
        }

        return streak;
    }

    // Find the longest consecutive practice streak
    public static int calculateLongestStreak(List<SessionData> sessions) {

        if (sessions == null || sessions.isEmpty()) {
            return 0;
        }

        Set<String> practiceDays = getPracticeDays(sessions);

        List<String> sortedDays = new ArrayList<>(practiceDays);
        Collections.sort(sortedDays);

        int longestStreak = 1;
        int currentStreak = 1;

        for (int i = 1; i < sortedDays.size(); i++) {

            try {
                Date previous =
                        new SimpleDateFormat(
                                "yyyy-MM-dd",
                                Locale.getDefault()
                        ).parse(sortedDays.get(i - 1));

                Date current =
                        new SimpleDateFormat(
                                "yyyy-MM-dd",
                                Locale.getDefault()
                        ).parse(sortedDays.get(i));

                if (previous != null && current != null) {

                    long difference =
                            current.getTime() - previous.getTime();

                    long daysDifference =
                            difference / (24L * 60L * 60L * 1000L);

                    if (daysDifference == 1) {
                        currentStreak++;

                        if (currentStreak > longestStreak) {
                            longestStreak = currentStreak;
                        }

                    } else {
                        currentStreak = 1;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return longestStreak;
    }

    // Get all unique dates on which the user practiced
    private static Set<String> getPracticeDays(List<SessionData> sessions) {

        Set<String> practiceDays = new HashSet<>();

        for (SessionData session : sessions) {

            if (session == null) {
                continue;
            }

            long timestamp = session.getTimestamp();

            if (timestamp > 0) {
                practiceDays.add(getDateString(timestamp));
            }
        }

        return practiceDays;
    }

    // Convert timestamp into yyyy-MM-dd
    private static String getDateString(long timestamp) {

        SimpleDateFormat dateFormat =
                new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                );

        return dateFormat.format(new Date(timestamp));
    }
}