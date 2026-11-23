package com.presentationcoach;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.presentationcoach.data.SessionManager;
import com.presentationcoach.model.AnalysisResult;
import com.presentationcoach.model.SessionData;

import java.util.List;

public class SessionHistoryActivity extends AppCompatActivity {

    private LinearLayout sessionsContainer;
    private TextView emptyText;
    private TextView sessionCountText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_session_history);

        // -----------------------------------------
        // FIND UI ELEMENTS
        // -----------------------------------------

        TextView backButton =
                findViewById(R.id.backButton);

        sessionsContainer =
                findViewById(R.id.sessionsContainer);

        emptyText =
                findViewById(R.id.emptyText);

        sessionCountText =
                findViewById(R.id.sessionCountText);


        // -----------------------------------------
        // BACK BUTTON
        // -----------------------------------------

        backButton.setOnClickListener(v -> finish());


        // -----------------------------------------
        // LOAD SESSIONS
        // -----------------------------------------

        loadSessions();
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (sessionsContainer != null) {
            loadSessions();
        }
    }


    private void loadSessions() {

        sessionsContainer.removeAllViews();

        List<SessionData> sessions =
                SessionManager.getSessions(this);


        // -----------------------------------------
        // NO SESSIONS
        // -----------------------------------------

        if (sessions.isEmpty()) {

            emptyText.setVisibility(View.VISIBLE);

            sessionCountText.setText(
                    "0 sessions"
            );

            return;
        }


        // -----------------------------------------
        // SESSIONS EXIST
        // -----------------------------------------

        emptyText.setVisibility(View.GONE);

        sessionCountText.setText(
                sessions.size()
                        + (sessions.size() == 1
                        ? " session"
                        : " sessions")
        );


        // -----------------------------------------
        // CREATE SESSION CARDS
        // -----------------------------------------

        for (SessionData session : sessions) {

            addSessionCard(session);
        }
    }


    private void addSessionCard(SessionData session) {

        // Main card
        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.HORIZONTAL
        );

        card.setGravity(
                android.view.Gravity.CENTER_VERTICAL
        );

        card.setPadding(
                18,
                18,
                18,
                18
        );

        card.setBackgroundResource(
                R.drawable.bg_session_card_new
        );


        // -----------------------------------------
        // LEFT ICON
        // -----------------------------------------

        TextView icon =
                new TextView(this);

        icon.setText("🎙️");

        icon.setTextSize(22);

        icon.setGravity(
                android.view.Gravity.CENTER
        );

        icon.setBackgroundResource(
                R.drawable.bg_session_icon_new
        );

        LinearLayout.LayoutParams iconParams =
                new LinearLayout.LayoutParams(
                        52,
                        52
                );

        card.addView(
                icon,
                iconParams
        );


        // -----------------------------------------
        // MIDDLE CONTENT
        // -----------------------------------------

        LinearLayout middle =
                new LinearLayout(this);

        middle.setOrientation(
                LinearLayout.VERTICAL
        );

        LinearLayout.LayoutParams middleParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        middleParams.setMargins(
                14,
                0,
                10,
                0
        );


        // Practice mode
        TextView mode =
                new TextView(this);

        mode.setText(
                session.getPracticeMode()
        );

        mode.setTextColor(
                getColor(R.color.pc_text)
        );

        mode.setTextSize(15);

        mode.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );


        // Session info
        TextView info =
                new TextView(this);

        String duration =
                formatDuration(
                        session.getDurationSeconds()
                );

        info.setText(
                "Practice session · "
                        + duration
        );

        info.setTextColor(
                getColor(R.color.pc_text_muted)
        );

        info.setTextSize(11);

        LinearLayout.LayoutParams infoParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        infoParams.setMargins(
                0,
                5,
                0,
                0
        );


        middle.addView(mode);

        middle.addView(
                info,
                infoParams
        );

        card.addView(
                middle,
                middleParams
        );


        // -----------------------------------------
        // SCORE
        // -----------------------------------------

        AnalysisResult result =
                session.getAnalysisResult();

        TextView score =
                new TextView(this);

        score.setText(
                String.valueOf(
                        result.getOverallScore()
                )
        );

        score.setTextColor(
                getColor(R.color.pc_text)
        );

        score.setTextSize(18);

        score.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        score.setGravity(
                android.view.Gravity.CENTER
        );

        score.setBackgroundResource(
                R.drawable.bg_score_mint
        );

        LinearLayout.LayoutParams scoreParams =
                new LinearLayout.LayoutParams(
                        54,
                        46
                );

        card.addView(
                score,
                scoreParams
        );


        // -----------------------------------------
        // ADD CARD
        // -----------------------------------------

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(
                0,
                0,
                0,
                12
        );

        sessionsContainer.addView(
                card,
                cardParams
        );
    }


    private String formatDuration(
            long seconds
    ) {

        if (seconds <= 0) {
            return "Duration unavailable";
        }

        long minutes =
                seconds / 60;

        long remainingSeconds =
                seconds % 60;

        return String.format(
                java.util.Locale.getDefault(),
                "%02d:%02d",
                minutes,
                remainingSeconds
        );
    }
}