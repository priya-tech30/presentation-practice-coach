package com.presentationcoach.gamification;

import com.presentationcoach.model.AnalysisResult;
import com.presentationcoach.model.SessionData;

import java.util.List;

public class GamificationManager {

    // -----------------------------------------
    // XP SETTINGS
    // -----------------------------------------

    private static final int XP_PER_SESSION = 45;

    private static final int XP_PERFECT_SCORE = 25;

    private static final int XP_GOOD_SCORE = 15;


    // -----------------------------------------
    // CALCULATE TOTAL XP
    // -----------------------------------------

    public static int calculateTotalXP(
            List<SessionData> sessions
    ) {

        int totalXP = 0;

        for (SessionData session : sessions) {

            AnalysisResult result =
                    session.getAnalysisResult();

            if (result == null) {
                continue;
            }

            // Base XP
            totalXP += XP_PER_SESSION;


            // Bonus XP
            int score =
                    result.getOverallScore();

            if (score >= 90) {

                totalXP += XP_PERFECT_SCORE;

            } else if (score >= 80) {

                totalXP += XP_GOOD_SCORE;
            }
        }

        return totalXP;
    }


    // -----------------------------------------
    // CALCULATE LEVEL
    // -----------------------------------------

    public static int calculateLevel(
            int totalXP
    ) {

        if (totalXP < 100) {
            return 1;
        }

        if (totalXP < 250) {
            return 2;
        }

        if (totalXP < 500) {
            return 3;
        }

        if (totalXP < 800) {
            return 4;
        }

        if (totalXP < 1200) {
            return 5;
        }

        return 6 + ((totalXP - 1200) / 500);
    }


    // -----------------------------------------
    // XP REQUIRED FOR CURRENT LEVEL
    // -----------------------------------------

    public static int getXPForCurrentLevel(
            int level
    ) {

        switch (level) {

            case 1:
                return 100;

            case 2:
                return 250;

            case 3:
                return 500;

            case 4:
                return 800;

            case 5:
                return 1200;

            default:
                return 1700;
        }
    }


    // -----------------------------------------
    // XP AT START OF CURRENT LEVEL
    // -----------------------------------------

    public static int getXPAtLevelStart(
            int level
    ) {

        switch (level) {

            case 1:
                return 0;

            case 2:
                return 100;

            case 3:
                return 250;

            case 4:
                return 500;

            case 5:
                return 800;

            default:
                return 1200;
        }
    }


    // -----------------------------------------
    // XP INSIDE CURRENT LEVEL
    // -----------------------------------------

    public static int getCurrentLevelXP(
            int totalXP
    ) {

        int level =
                calculateLevel(totalXP);

        int levelStart =
                getXPAtLevelStart(level);

        return totalXP - levelStart;
    }


    // -----------------------------------------
    // XP NEEDED FOR CURRENT LEVEL
    // -----------------------------------------

    public static int getXPNeededForLevel(
            int level
    ) {

        int start =
                getXPAtLevelStart(level);

        int end =
                getXPForCurrentLevel(level);

        return end - start;
    }


    // -----------------------------------------
    // BADGE COUNT
    // -----------------------------------------

    public static int calculateBadgeCount(
            List<SessionData> sessions
    ) {

        int badges = 0;

        // First session
        if (sessions.size() >= 1) {
            badges++;
        }

        // 5 sessions
        if (sessions.size() >= 5) {
            badges++;
        }

        // 10 sessions
        if (sessions.size() >= 10) {
            badges++;
        }


        // Score based badges
        for (SessionData session : sessions) {

            AnalysisResult result =
                    session.getAnalysisResult();

            if (result == null) {
                continue;
            }

            if (result.getOverallScore() >= 90) {
                badges++;
                break;
            }
        }

        return badges;
    }


    // -----------------------------------------
    // BADGE NAME
    // -----------------------------------------

    public static String getNextBadgeName(
            List<SessionData> sessions
    ) {

        if (sessions.size() < 1) {
            return "First Steps";
        }

        if (sessions.size() < 5) {
            return "Getting Started";
        }

        if (sessions.size() < 10) {
            return "Dedicated Speaker";
        }

        return "Confident Speaker";
    }


    // -----------------------------------------
    // MOTIVATIONAL MESSAGE
    // -----------------------------------------

    public static String getLevelMessage(
            int level
    ) {

        if (level == 1) {
            return "Your journey begins ✨";
        }

        if (level == 2) {
            return "You're finding your voice 💜";
        }

        if (level == 3) {
            return "You're becoming more confident 🔥";
        }

        if (level == 4) {
            return "You're a confident speaker ✨";
        }

        if (level == 5) {
            return "You're mastering your voice 👑";
        }

        return "You're unstoppable 💜";
    }
}