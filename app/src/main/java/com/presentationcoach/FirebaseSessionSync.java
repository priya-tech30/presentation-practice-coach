
package com.presentationcoach.data;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.presentationcoach.model.AnalysisResult;
import com.presentationcoach.model.SessionData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseSessionSync {

    private static final String TAG = "FirebaseSessionSync";

    private static FirebaseAuth auth;
    private static FirebaseFirestore firestore;

    private static boolean initialized = false;
    private static boolean signingIn = false;

    private static final List<SessionData> pendingSessions =
            new ArrayList<>();

    private FirebaseSessionSync() {
    }

    public static synchronized void initialize(Context context) {

        if (initialized) {
            return;
        }

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        initialized = true;

        if (auth.getCurrentUser() != null) {
            Log.d(TAG, "Already authenticated");
            uploadPendingSessions();
        } else {
            signIn();
        }
    }

    private static synchronized void signIn() {

        if (signingIn || auth == null) {
            return;
        }

        signingIn = true;

        auth.signInAnonymously()
                .addOnSuccessListener(result -> {
                    synchronized (FirebaseSessionSync.class) {
                        signingIn = false;
                    }

                    Log.d(TAG, "Anonymous sign-in successful");
                    uploadPendingSessions();
                })
                .addOnFailureListener(error -> {
                    synchronized (FirebaseSessionSync.class) {
                        signingIn = false;
                    }

                    Log.e(TAG, "Anonymous sign-in failed", error);
                });
    }

    public static synchronized void uploadSession(SessionData session) {

        if (session == null) {
            return;
        }

        if (!initialized) {
            Log.e(TAG, "Call initialize before uploading");
            return;
        }

        if (auth.getCurrentUser() == null) {
            pendingSessions.add(session);
            signIn();
            return;
        }

        uploadToFirestore(session);
    }

    private static void uploadPendingSessions() {

        List<SessionData> sessionsToUpload;

        synchronized (FirebaseSessionSync.class) {
            sessionsToUpload = new ArrayList<>(pendingSessions);
            pendingSessions.clear();
        }

        for (SessionData session : sessionsToUpload) {
            uploadSession(session);
        }
    }

    private static void uploadToFirestore(SessionData session) {

        String userId = auth.getCurrentUser().getUid();
        String sessionId = String.valueOf(session.getTimestamp());

        Map<String, Object> data = new HashMap<>();

        data.put("audioFilePath", session.getAudioFilePath());
        data.put("practiceMode", session.getPracticeMode());
        data.put("timestamp", session.getTimestamp());
        data.put("durationSeconds", session.getDurationSeconds());

        data.put("pauseCount", session.getPauseCount());
        data.put("totalPauseDuration",
                session.getTotalPauseDuration());
        data.put("averagePauseDuration",
                session.getAveragePauseDuration());
        data.put("pauseDurations", session.getPauseDurations());

        AnalysisResult result = session.getAnalysisResult();

        if (result != null) {

            Map<String, Object> analysis = new HashMap<>();

            analysis.put("overallScore", result.getOverallScore());
            analysis.put("paceScore", result.getPaceScore());
            analysis.put("clarityScore", result.getClarityScore());
            analysis.put("fillerScore", result.getFillerScore());
            analysis.put("confidenceScore",
                    result.getConfidenceScore());

            analysis.put("transcript", result.getTranscript());
            analysis.put("feedback", result.getFeedback());
            analysis.put("workOn", result.getWorkOn());

            List<String> questions = result.getFollowUpQuestions();

            analysis.put("followUpQuestions",
                    questions == null ? new ArrayList<>() : questions);

            analysis.put("xpEarned", result.getXpEarned());

            data.put("analysisResult", analysis);
        }

        firestore.collection("users")
                .document(userId)
                .collection("sessions")
                .document(sessionId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused ->
                        Log.d(TAG, "Session uploaded: " + sessionId))
                .addOnFailureListener(error ->
                        Log.e(TAG, "Session upload failed", error));
    }

    public static void syncAllSessions(List<SessionData> sessions) {

        if (sessions == null) {
            return;
        }

        for (SessionData session : sessions) {
            uploadSession(session);
        }
    }
}
