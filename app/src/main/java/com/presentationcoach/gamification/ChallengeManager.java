package com.presentationcoach.gamification;

import com.presentationcoach.model.AnalysisResult;
import com.presentationcoach.model.SessionData;

import java.util.List;

public class ChallengeManager {

    // ==========================================
    // DAILY SESSION CHALLENGE
    // ==========================================

    public static int getSessionChallengeProgress(
            List<SessionData> sessions) {

        if (sessions == null) {
            return 0;
        }

        return Math.min(sessions.size(), 1);
    }

    public static int getSessionChallengeTarget() {
        return 1;
    }

    public static int getSessionChallengeXP() {
        return 50;
    }


    // ==========================================
    // THREE DAY PRACTICE CHALLENGE
    // ==========================================

    public static int getThreeDayChallengeProgress(
            List<SessionData> sessions) {

        int streak =
                StreakManager.calculateCurrentStreak(sessions);

        return Math.min(streak, 3);
    }

    public static int getThreeDayChallengeTarget() {
        return 3;
    }

    public static int getThreeDayChallengeXP() {
        return 100;
    }


    // ==========================================
    // SCORE 80+ CHALLENGE
    // ==========================================

    public static int getHighScoreChallengeProgress(
            List<SessionData> sessions) {

        if (sessions == null) {
            return 0;
        }

        for (SessionData session : sessions) {

            if (session == null) {
                continue;
            }

            AnalysisResult result =
                    session.getAnalysisResult();

            if (result == null) {
                continue;
            }

            if (result.getOverallScore() >= 80) {
                return 1;
            }
        }

        return 0;
    }

    public static int getHighScoreChallengeTarget() {
        return 1;
    }

    public static int getHighScoreChallengeXP() {
        return 75;
    }


    // ==========================================
    // TEN SESSION CHALLENGE
    // ==========================================

    public static int getTenSessionChallengeProgress(
            List<SessionData> sessions) {

        if (sessions == null) {
            return 0;
        }

        return Math.min(sessions.size(), 10);
    }

    public static int getTenSessionChallengeTarget() {
        return 10;
    }

    public static int getTenSessionChallengeXP() {
        return 150;
    }


    // ==========================================
    // CHALLENGE COMPLETION
    // ==========================================

    public static boolean isCompleted(
            int progress,
            int target) {

        return progress >= target;
    }
}