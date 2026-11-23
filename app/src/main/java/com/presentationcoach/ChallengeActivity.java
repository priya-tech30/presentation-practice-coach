package com.presentationcoach;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.presentationcoach.data.SessionManager;
import com.presentationcoach.gamification.ChallengeManager;
import com.presentationcoach.model.SessionData;

import java.util.List;

public class ChallengeActivity extends AppCompatActivity {

    private TextView sessionProgressText;
    private TextView sessionStatusText;

    private TextView threeDayProgressText;
    private TextView threeDayStatusText;

    private TextView highScoreProgressText;
    private TextView highScoreStatusText;

    private TextView tenSessionProgressText;
    private TextView tenSessionStatusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_challenges);

        TextView backButton =
                findViewById(R.id.backButton);

        sessionProgressText =
                findViewById(R.id.sessionProgressText);

        sessionStatusText =
                findViewById(R.id.sessionStatusText);

        threeDayProgressText =
                findViewById(R.id.threeDayProgressText);

        threeDayStatusText =
                findViewById(R.id.threeDayStatusText);

        highScoreProgressText =
                findViewById(R.id.highScoreProgressText);

        highScoreStatusText =
                findViewById(R.id.highScoreStatusText);

        tenSessionProgressText =
                findViewById(R.id.tenSessionProgressText);

        tenSessionStatusText =
                findViewById(R.id.tenSessionStatusText);

        backButton.setOnClickListener(v -> finish());

        loadChallenges();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (sessionProgressText != null) {
            loadChallenges();
        }
    }

    private void loadChallenges() {

        List<SessionData> sessions =
                SessionManager.getSessions(this);

        // =========================
        // COMPLETE A SESSION
        // =========================

        int sessionProgress =
                ChallengeManager.getSessionChallengeProgress(
                        sessions
                );

        int sessionTarget =
                ChallengeManager.getSessionChallengeTarget();

        updateChallenge(
                sessionProgressText,
                sessionStatusText,
                sessionProgress,
                sessionTarget
        );


        // =========================
        // THREE DAY STREAK
        // =========================

        int threeDayProgress =
                ChallengeManager.getThreeDayChallengeProgress(
                        sessions
                );

        int threeDayTarget =
                ChallengeManager.getThreeDayChallengeTarget();

        updateChallenge(
                threeDayProgressText,
                threeDayStatusText,
                threeDayProgress,
                threeDayTarget
        );


        // =========================
        // SCORE 80+
        // =========================

        int highScoreProgress =
                ChallengeManager.getHighScoreChallengeProgress(
                        sessions
                );

        int highScoreTarget =
                ChallengeManager.getHighScoreChallengeTarget();

        updateChallenge(
                highScoreProgressText,
                highScoreStatusText,
                highScoreProgress,
                highScoreTarget
        );


        // =========================
        // TEN SESSIONS
        // =========================

        int tenSessionProgress =
                ChallengeManager.getTenSessionChallengeProgress(
                        sessions
                );

        int tenSessionTarget =
                ChallengeManager.getTenSessionChallengeTarget();

        updateChallenge(
                tenSessionProgressText,
                tenSessionStatusText,
                tenSessionProgress,
                tenSessionTarget
        );
    }

    private void updateChallenge(
            TextView progressText,
            TextView statusText,
            int progress,
            int target
    ) {

        progressText.setText(
                progress + " / " + target
        );

        if (ChallengeManager.isCompleted(
                progress,
                target
        )) {

            statusText.setText("✓ Completed");

        } else {

            statusText.setText("In progress");
        }
    }
}
