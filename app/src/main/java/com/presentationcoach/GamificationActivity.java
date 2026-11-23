package com.presentationcoach;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.presentationcoach.data.SessionManager;
import com.presentationcoach.gamification.GamificationManager;
import com.presentationcoach.gamification.StreakManager;
import com.presentationcoach.model.SessionData;

import java.util.List;

public class GamificationActivity extends AppCompatActivity {

    // GAMIFICATION VIEWS
    private TextView levelText;
    private TextView xpText;
    private TextView badgeCountText;
    private TextView badgeNameText;
    private TextView messageText;

    // STREAK VIEWS
    private TextView currentStreakText;
    private TextView streakMessageText;
    private TextView currentStreakStatText;
    private TextView longestStreakStatText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gamification);

        // BACK BUTTON
        TextView backButton = findViewById(R.id.backButton);

        // GAMIFICATION VIEWS
        levelText = findViewById(R.id.levelText);
        xpText = findViewById(R.id.xpText);
        badgeCountText = findViewById(R.id.badgeCountText);
        badgeNameText = findViewById(R.id.badgeNameText);
        messageText = findViewById(R.id.messageText);

        // STREAK VIEWS
        currentStreakText =
                findViewById(R.id.currentStreakText);

        streakMessageText =
                findViewById(R.id.streakMessageText);

        currentStreakStatText =
                findViewById(R.id.currentStreakStatText);

        longestStreakStatText =
                findViewById(R.id.longestStreakStatText);

        // BACK BUTTON
        backButton.setOnClickListener(v -> finish());

        // CHALLENGES BUTTON
        TextView challengesButton =
                findViewById(R.id.challengesButton);

        challengesButton.setOnClickListener(v -> {
            startActivity(
                    new android.content.Intent(
                            GamificationActivity.this,
                            ChallengeActivity.class
                    )
            );
        });

        // LOAD GAMIFICATION DATA
        loadGamification();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (levelText != null) {
            loadGamification();
        }
    }

    private void loadGamification() {

        // GET SAVED SESSIONS
        List<SessionData> sessions =
                SessionManager.getSessions(this);

        // =========================
        // XP + LEVEL
        // =========================

        int totalXP =
                GamificationManager.calculateTotalXP(sessions);

        int level =
                GamificationManager.calculateLevel(totalXP);

        int currentXP =
                GamificationManager.getCurrentLevelXP(totalXP);

        int neededXP =
                GamificationManager.getXPNeededForLevel(level);

        // =========================
        // BADGES
        // =========================

        int badgeCount =
                GamificationManager.calculateBadgeCount(sessions);

        String nextBadge =
                GamificationManager.getNextBadgeName(sessions);

        // =========================
        // STREAK
        // =========================

        int currentStreak =
                StreakManager.calculateCurrentStreak(sessions);

        int longestStreak =
                StreakManager.calculateLongestStreak(sessions);

        // =========================
        // UPDATE LEVEL
        // =========================

        levelText.setText(
                "LEVEL " + level
        );

        xpText.setText(
                currentXP + " / " + neededXP + " XP"
        );

        messageText.setText(
                GamificationManager.getLevelMessage(level)
        );

        // =========================
        // UPDATE BADGES
        // =========================

        badgeCountText.setText(
                String.valueOf(badgeCount)
        );

        badgeNameText.setText(
                "Next: " + nextBadge
        );

        // =========================
        // UPDATE STREAK
        // =========================

        currentStreakText.setText(
                currentStreak + " days"
        );

        currentStreakStatText.setText(
                "Current: " + currentStreak + " days"
        );

        longestStreakStatText.setText(
                "Best: " + longestStreak + " days"
        );

        // =========================
        // STREAK MESSAGE
        // =========================

        if (currentStreak == 0) {

            streakMessageText.setText(
                    "Practice today to start your streak ✨"
            );

        } else if (currentStreak == 1) {

            streakMessageText.setText(
                    "Great start! Come back tomorrow 🔥"
            );

        } else {

            streakMessageText.setText(
                    "You're on fire! Keep it going 🔥"
            );
        }
    }
}