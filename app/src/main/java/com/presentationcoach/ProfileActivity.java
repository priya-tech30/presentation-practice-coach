package com.presentationcoach;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.presentationcoach.data.SessionManager;
import com.presentationcoach.model.SessionData;

import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private TextView nameText;
    private TextView avatarText;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        prefs = getSharedPreferences("profile", MODE_PRIVATE);

        nameText = findViewById(R.id.profileName);
        avatarText = findViewById(R.id.profileAvatar);

        TextView backButton = findViewById(R.id.backButton);
        TextView editButton = findViewById(R.id.editProfileButton);

        backButton.setOnClickListener(v -> finish());

        editButton.setOnClickListener(v -> showEditDialog());

        loadProfile();
        loadStats();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (prefs != null) {
            loadProfile();
            loadStats();
        }
    }

    private void loadProfile() {

        String name = prefs.getString("name", "Zia");
        String avatar = prefs.getString("avatar", "Z");

        nameText.setText(name);
        avatarText.setText(avatar);
    }

    private void showEditDialog() {

        EditText input = new EditText(this);
        input.setHint("Enter your name");
        input.setText(nameText.getText().toString());
        input.setSingleLine();

        new AlertDialog.Builder(this)
                .setTitle("Edit Profile")
                .setView(input)
                .setPositiveButton("SAVE", (dialog, which) -> {

                    String name = input.getText()
                            .toString()
                            .trim();

                    if (name.isEmpty()) {
                        name = "Zia";
                    }

                    String avatar =
                            name.substring(0, 1).toUpperCase();

                    prefs.edit()
                            .putString("name", name)
                            .putString("avatar", avatar)
                            .apply();

                    loadProfile();
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void loadStats() {

        List<SessionData> sessions =
                SessionManager.getSessions(this);

        int sessionCount = sessions.size();

        int totalScore = 0;
        int bestScore = 0;
        int totalXP = 0;

        for (SessionData session : sessions) {

            if (session.getAnalysisResult() != null) {

                int score =
                        session.getAnalysisResult()
                                .getOverallScore();

                totalScore += score;

                if (score > bestScore) {
                    bestScore = score;
                }

                totalXP += session.getAnalysisResult()
                        .getXpEarned();
            }
        }

        int averageScore =
                sessionCount > 0
                        ? totalScore / sessionCount
                        : 0;

        TextView sessionsText =
                findViewById(R.id.profileSessions);

        TextView averageText =
                findViewById(R.id.profileAverage);

        TextView levelText =
                findViewById(R.id.profileLevel);

        TextView xpText =
                findViewById(R.id.profileXP);

        sessionsText.setText(String.valueOf(sessionCount));
        averageText.setText(String.valueOf(averageScore));

        int level = calculateLevel(totalXP);

        levelText.setText("Level " + level);
        xpText.setText(totalXP + " XP");
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
}