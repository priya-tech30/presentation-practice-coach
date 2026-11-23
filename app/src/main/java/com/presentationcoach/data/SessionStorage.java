
package com.presentationcoach.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.presentationcoach.model.AnalysisResult;
import com.presentationcoach.model.SessionData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SessionStorage {

    private static final String PREF_NAME =
            "presentation_coach_sessions";

    private static final String SESSIONS_KEY =
            "sessions";

    // SAVE ALL SESSIONS

    public static void saveSessions(
            Context context,
            List<SessionData> sessions
    ) {

        try {

            JSONArray sessionsArray = new JSONArray();

            for (SessionData session : sessions) {

                JSONObject object = new JSONObject();

                object.put("audioFilePath", session.getAudioFilePath());
                object.put("practiceMode", session.getPracticeMode());
                object.put("timestamp", session.getTimestamp());
                object.put("durationSeconds", session.getDurationSeconds());

                // PAUSE INFORMATION
                object.put("pauseCount", session.getPauseCount());
                object.put(
                        "totalPauseDuration",
                        session.getTotalPauseDuration()
                );
                object.put(
                        "averagePauseDuration",
                        session.getAveragePauseDuration()
                );

                JSONArray pauseArray = new JSONArray();

                for (Double duration : session.getPauseDurations()) {
                    pauseArray.put(duration);
                }

                object.put("pauseDurations", pauseArray);

                // ANALYSIS RESULT
                AnalysisResult result = session.getAnalysisResult();

                if (result != null) {

                    object.put("overallScore", result.getOverallScore());
                    object.put("paceScore", result.getPaceScore());
                    object.put("clarityScore", result.getClarityScore());
                    object.put("fillerScore", result.getFillerScore());
                    object.put("confidenceScore", result.getConfidenceScore());

                    object.put("transcript", result.getTranscript());
                    object.put("feedback", result.getFeedback());
                    object.put("workOn", result.getWorkOn());

                    JSONArray questionsArray = new JSONArray();

                    if (result.getFollowUpQuestions() != null) {
                        for (String question : result.getFollowUpQuestions()) {
                            questionsArray.put(question);
                        }
                    }

                    object.put("followUpQuestions", questionsArray);
                    object.put("xpEarned", result.getXpEarned());
                }

                sessionsArray.put(object);
            }

            SharedPreferences preferences =
                    context.getSharedPreferences(
                            PREF_NAME,
                            Context.MODE_PRIVATE
                    );

            preferences.edit()
                    .putString(SESSIONS_KEY, sessionsArray.toString())
                    .apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // LOAD ALL SESSIONS

    public static List<SessionData> loadSessions(Context context) {

        List<SessionData> sessions = new ArrayList<>();

        try {

            SharedPreferences preferences =
                    context.getSharedPreferences(
                            PREF_NAME,
                            Context.MODE_PRIVATE
                    );

            String savedData =
                    preferences.getString(SESSIONS_KEY, null);

            if (savedData == null || savedData.isEmpty()) {
                return sessions;
            }

            JSONArray sessionsArray = new JSONArray(savedData);

            for (int i = 0; i < sessionsArray.length(); i++) {

                JSONObject object = sessionsArray.getJSONObject(i);

                // SESSION INFORMATION
                String audioFilePath =
                        object.optString("audioFilePath", null);

                String practiceMode =
                        object.optString("practiceMode", "Interview");

                long timestamp =
                        object.optLong("timestamp", 0);

                long durationSeconds =
                        object.optLong("durationSeconds", 0);

                // PAUSE INFORMATION
                int pauseCount =
                        object.optInt("pauseCount", 0);

                double totalPauseDuration =
                        object.optDouble("totalPauseDuration", 0);

                double averagePauseDuration =
                        object.optDouble("averagePauseDuration", 0);

                List<Double> pauseDurations = new ArrayList<>();

                JSONArray pauseArray =
                        object.optJSONArray("pauseDurations");

                if (pauseArray != null) {

                    for (int j = 0; j < pauseArray.length(); j++) {
                        pauseDurations.add(pauseArray.optDouble(j, 0));
                    }
                }

                // ANALYSIS INFORMATION
                int overallScore = object.optInt("overallScore", 0);
                int paceScore = object.optInt("paceScore", 0);
                int clarityScore = object.optInt("clarityScore", 0);
                int fillerScore = object.optInt("fillerScore", 0);
                int confidenceScore = object.optInt("confidenceScore", 0);

                String transcript = object.optString("transcript", "");
                String feedback = object.optString("feedback", "");
                String workOn = object.optString("workOn", "");

                int xpEarned = object.optInt("xpEarned", 0);

                // FOLLOW-UP QUESTIONS
                List<String> questions = new ArrayList<>();

                JSONArray questionsArray =
                        object.optJSONArray("followUpQuestions");

                if (questionsArray != null) {

                    for (int j = 0; j < questionsArray.length(); j++) {
                        questions.add(questionsArray.getString(j));
                    }
                }

                // CREATE ANALYSIS RESULT
                AnalysisResult result = new AnalysisResult(
                        overallScore,
                        paceScore,
                        clarityScore,
                        fillerScore,
                        confidenceScore,
                        transcript,
                        feedback,
                        workOn,
                        questions,
                        xpEarned
                );

                // CREATE SESSION WITH PAUSE DATA
                SessionData session = new SessionData(
                        audioFilePath,
                        practiceMode,
                        timestamp,
                        durationSeconds,
                        result,
                        pauseCount,
                        totalPauseDuration,
                        averagePauseDuration,
                        pauseDurations
                );

                sessions.add(session);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return sessions;
    }
}
