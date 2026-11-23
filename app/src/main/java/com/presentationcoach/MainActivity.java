package com.presentationcoach;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.presentationcoach.data.SessionManager;
import com.presentationcoach.model.SessionData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView homeLevelText;
    private ProgressBar homeXPProgress;
    private TextView homeXPText;

    private View recentSessionOne;
    private View recentSessionTwo;

    private TextView recentSessionOneTitle;
    private TextView recentSessionOneDate;
    private TextView recentSessionOneScore;

    private TextView recentSessionTwoTitle;
    private TextView recentSessionTwoDate;
    private TextView recentSessionTwoScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupNavigation();
        setupHomeViews();
        loadHomeData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHomeData();
    }

    private void setupHomeViews() {

        homeLevelText = findViewById(R.id.homeLevelText);
        homeXPProgress = findViewById(R.id.homeXPProgress);
        homeXPText = findViewById(R.id.homeXPText);

        recentSessionOne = findViewById(R.id.recentSessionOne);
        recentSessionTwo = findViewById(R.id.recentSessionTwo);

        recentSessionOneTitle =
                findViewById(R.id.recentSessionOneTitle);

        recentSessionOneDate =
                findViewById(R.id.recentSessionOneDate);

        recentSessionOneScore =
                findViewById(R.id.recentSessionOneScore);

        recentSessionTwoTitle =
                findViewById(R.id.recentSessionTwoTitle);

        recentSessionTwoDate =
                findViewById(R.id.recentSessionTwoDate);

        recentSessionTwoScore =
                findViewById(R.id.recentSessionTwoScore);
    }

    private void setupNavigation() {

        // START NEW SESSION
        LinearLayout startSessionButton =
                findViewById(R.id.startSessionButton);

        startSessionButton.setOnClickListener(v -> {
            Intent intent =
                    new Intent(MainActivity.this, RecordingActivity.class);
            startActivity(intent);
        });

        // TALK / RECORD
        TextView navRecord = findViewById(R.id.navRecord);

        navRecord.setOnClickListener(v -> {
            Intent intent =
                    new Intent(MainActivity.this, RecordingActivity.class);

            intent.putExtra("practice_mode", "Talk");
            startActivity(intent);
        });

        // HISTORY
        TextView navHistory = findViewById(R.id.navHistory);

        navHistory.setOnClickListener(v -> {
            Intent intent =
                    new Intent(MainActivity.this, SessionHistoryActivity.class);
            startActivity(intent);
        });

        // SEE ALL
        TextView seeAll = findViewById(R.id.seeAll);

        seeAll.setOnClickListener(v -> {
            Intent intent =
                    new Intent(MainActivity.this, SessionHistoryActivity.class);
            startActivity(intent);
        });

        // PROGRESS
        TextView navProgress = findViewById(R.id.navProgress);

        navProgress.setOnClickListener(v -> {
            Intent intent =
                    new Intent(MainActivity.this, ProgressActivity.class);
            startActivity(intent);
        });

        // PROFILE
        TextView navProfile = findViewById(R.id.navProfile);

        navProfile.setOnClickListener(v -> {
            Intent intent =
                    new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // PROFILE ICON
        TextView homeProfileButton =
                findViewById(R.id.homeProfileButton);

        homeProfileButton.setOnClickListener(v -> {
            Intent intent =
                    new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // LEVEL CARD
        LinearLayout levelCard =
                findViewById(R.id.levelCard);

        levelCard.setOnClickListener(v -> {
            Intent intent =
                    new Intent(MainActivity.this, GamificationActivity.class);
            startActivity(intent);
        });
    }

    private void loadHomeData() {

        List<SessionData> sessions =
                SessionManager.getSessions(this);

        updateLevelCard(sessions);
        updateRecentSessions(sessions);
    }

    // =========================
    // LEVEL + XP
    // =========================

    private void updateLevelCard(List<SessionData> sessions) {

        int totalXP = 0;

        for (SessionData session : sessions) {

            if (session.getAnalysisResult() != null) {
                totalXP += session
                        .getAnalysisResult()
                        .getXpEarned();
            }
        }

        int level = calculateLevel(totalXP);

        int previousLevelXP = getPreviousLevelXP(level);
        int nextLevelXP = getNextLevelXP(level);

        int currentLevelXP =
                totalXP - previousLevelXP;

        int requiredXP =
                nextLevelXP - previousLevelXP;

        if (currentLevelXP < 0) {
            currentLevelXP = 0;
        }

        homeLevelText.setText(
                "Level " + level
        );

        homeXPProgress.setMax(requiredXP);

        homeXPProgress.setProgress(currentLevelXP);

        homeXPText.setText(
                currentLevelXP + " / " + requiredXP + " XP"
        );
    }

    private int calculateLevel(int xp) {

        if (xp < 100) return 1;
        if (xp < 250) return 2;
        if (xp < 450) return 3;
        if (xp < 700) return 4;
        if (xp < 1000) return 5;
        if (xp < 1400) return 6;
        if (xp < 1850) return 7;
        if (xp < 2350) return 8;
        if (xp < 3000) return 9;

        return 10;
    }

    private int getPreviousLevelXP(int level) {

        switch (level) {

            case 1:
                return 0;

            case 2:
                return 100;

            case 3:
                return 250;

            case 4:
                return 450;

            case 5:
                return 700;

            case 6:
                return 1000;

            case 7:
                return 1400;

            case 8:
                return 1850;

            case 9:
                return 2350;

            default:
                return 3000;
        }
    }

    private int getNextLevelXP(int level) {

        switch (level) {

            case 1:
                return 100;

            case 2:
                return 250;

            case 3:
                return 450;

            case 4:
                return 700;

            case 5:
                return 1000;

            case 6:
                return 1400;

            case 7:
                return 1850;

            case 8:
                return 2350;

            case 9:
                return 3000;

            default:
                return 3500;
        }
    }

    // =========================
    // RECENT SESSIONS
    // =========================

    private void updateRecentSessions(
            List<SessionData> sessions) {

        // No sessions yet
        if (sessions == null || sessions.isEmpty()) {

            recentSessionOne.setVisibility(View.GONE);
            recentSessionTwo.setVisibility(View.GONE);

            return;
        }

        // Most recent session
        SessionData first =
                sessions.get(sessions.size() - 1);

        displaySession(
                first,
                recentSessionOneTitle,
                recentSessionOneDate,
                recentSessionOneScore
        );

        recentSessionOne.setVisibility(View.VISIBLE);

        // Second most recent session
        if (sessions.size() >= 2) {

            SessionData second =
                    sessions.get(sessions.size() - 2);

            displaySession(
                    second,
                    recentSessionTwoTitle,
                    recentSessionTwoDate,
                    recentSessionTwoScore
            );

            recentSessionTwo.setVisibility(View.VISIBLE);

        } else {

            recentSessionTwo.setVisibility(View.GONE);
        }
    }

    private void displaySession(
            SessionData session,
            TextView title,
            TextView date,
            TextView score) {

        String mode = session.getPracticeMode();

        if (mode == null || mode.trim().isEmpty()) {
            mode = "Talk";
        }

        title.setText(mode + " Practice");

        SimpleDateFormat formatter =
                new SimpleDateFormat(
                        "dd MMM • hh:mm a",
                        Locale.getDefault()
                );

        date.setText(
                formatter.format(
                        new Date(session.getTimestamp())
                )
        );

        if (session.getAnalysisResult() != null) {

            int sessionScore =
                    session.getAnalysisResult()
                            .getOverallScore();

            score.setText(
                    sessionScore + "%"
            );

        } else {

            score.setText("--");
        }
    }
}