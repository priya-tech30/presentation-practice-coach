package com.presentationcoach;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.presentationcoach.data.SessionManager;
import com.presentationcoach.model.AnalysisResult;
import com.presentationcoach.model.SessionData;

import java.util.List;
import java.util.Locale;

public class ProgressActivity extends AppCompatActivity {

    private TextView totalSessionsText;
    private TextView averageScoreText;
    private TextView bestScoreText;
    private TextView totalTimeText;

    private TextView averagePaceText;
    private TextView averageClarityText;
    private TextView averageConfidenceText;

    private TextView progressMessageText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_progress);

        // -----------------------------------------
        // FIND VIEWS
        // -----------------------------------------

        TextView backButton =
                findViewById(R.id.backButton);

        totalSessionsText =
                findViewById(R.id.totalSessionsText);

        averageScoreText =
                findViewById(R.id.averageScoreText);

        bestScoreText =
                findViewById(R.id.bestScoreText);

        totalTimeText =
                findViewById(R.id.totalTimeText);

        averagePaceText =
                findViewById(R.id.averagePaceText);

        averageClarityText =
                findViewById(R.id.averageClarityText);

        averageConfidenceText =
                findViewById(R.id.averageConfidenceText);

        progressMessageText =
                findViewById(R.id.progressMessageText);


        // -----------------------------------------
        // BACK
        // -----------------------------------------

        backButton.setOnClickListener(v -> finish());


        // -----------------------------------------
        // LOAD PROGRESS
        // -----------------------------------------

        loadProgress();
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (totalSessionsText != null) {
            loadProgress();
        }
    }


    private void loadProgress() {

        List<SessionData> sessions =
                SessionManager.getSessions(this);


        // -----------------------------------------
        // NO SESSIONS
        // -----------------------------------------

        if (sessions.isEmpty()) {

            totalSessionsText.setText("0");
            averageScoreText.setText("--");
            bestScoreText.setText("--");
            totalTimeText.setText("0 min");

            averagePaceText.setText("--");
            averageClarityText.setText("--");
            averageConfidenceText.setText("--");

            progressMessageText.setText(
                    "Complete your first session to start tracking progress ✨"
            );

            return;
        }


        // -----------------------------------------
        // VARIABLES
        // -----------------------------------------

        int totalScore = 0;
        int bestScore = 0;

        int totalPace = 0;
        int totalClarity = 0;
        int totalConfidence = 0;

        long totalSeconds = 0;


        // -----------------------------------------
        // CALCULATE
        // -----------------------------------------

        for (SessionData session : sessions) {

            AnalysisResult result =
                    session.getAnalysisResult();

            if (result == null) {
                continue;
            }


            int score =
                    result.getOverallScore();

            totalScore += score;

            if (score > bestScore) {
                bestScore = score;
            }


            totalPace +=
                    result.getPaceScore();

            totalClarity +=
                    result.getClarityScore();

            totalConfidence +=
                    result.getConfidenceScore();


            totalSeconds +=
                    session.getDurationSeconds();
        }


        int sessionCount =
                sessions.size();


        int averageScore =
                totalScore / sessionCount;

        int averagePace =
                totalPace / sessionCount;

        int averageClarity =
                totalClarity / sessionCount;

        int averageConfidence =
                totalConfidence / sessionCount;


        // -----------------------------------------
        // DISPLAY
        // -----------------------------------------

        totalSessionsText.setText(
                String.valueOf(sessionCount)
        );

        averageScoreText.setText(
                String.valueOf(averageScore)
        );

        bestScoreText.setText(
                String.valueOf(bestScore)
        );

        averagePaceText.setText(
                averagePace + "%"
        );

        averageClarityText.setText(
                averageClarity + "%"
        );

        averageConfidenceText.setText(
                averageConfidence + "%"
        );


        // -----------------------------------------
        // TOTAL TIME
        // -----------------------------------------

        long minutes =
                totalSeconds / 60;

        if (minutes == 0 && totalSeconds > 0) {
            totalTimeText.setText(
                    totalSeconds + " sec"
            );
        } else {
            totalTimeText.setText(
                    minutes + " min"
            );
        }


        // -----------------------------------------
        // MESSAGE
        // -----------------------------------------

        if (averageScore >= 80) {

            progressMessageText.setText(
                    "You're building strong speaking habits ✨"
            );

        } else if (averageScore >= 60) {

            progressMessageText.setText(
                    "You're making progress — keep practicing 💜"
            );

        } else {

            progressMessageText.setText(
                    "Every session makes you better. Keep going ✨"
            );
        }
    }
}