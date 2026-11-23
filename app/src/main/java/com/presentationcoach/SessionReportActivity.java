
package com.presentationcoach;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.presentationcoach.analysis.LocalNLPAnalyzer;
import com.presentationcoach.data.SessionManager;
import com.presentationcoach.model.AnalysisResult;
import com.presentationcoach.model.SessionData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SessionReportActivity extends AppCompatActivity {

    private static final String SERVER_URL =
            "http://10.207.121.85:8000";

    private boolean sessionSaved = false;

    private TextView overallScoreText;
    private TextView scoreMessageText;
    private TextView paceScoreText;
    private TextView clarityScoreText;
    private TextView fillerScoreText;
    private TextView confidenceScoreText;
    private TextView transcriptText;
    private TextView feedbackText;
    private TextView workOnText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_report);

        TextView backButton = findViewById(R.id.backButton);

        overallScoreText = findViewById(R.id.overallScoreText);
        scoreMessageText = findViewById(R.id.scoreMessageText);
        paceScoreText = findViewById(R.id.paceScoreText);
        clarityScoreText = findViewById(R.id.clarityScoreText);
        fillerScoreText = findViewById(R.id.fillerScoreText);
        confidenceScoreText = findViewById(R.id.confidenceScoreText);
        transcriptText = findViewById(R.id.transcriptText);
        feedbackText = findViewById(R.id.feedbackText);
        workOnText = findViewById(R.id.workOnText);

        backButton.setOnClickListener(v -> finish());

        String audioPath =
                getIntent().getStringExtra("audio_file_path");

        String practiceMode =
                getIntent().getStringExtra("practice_mode");

        if (practiceMode == null || practiceMode.trim().isEmpty()) {
            practiceMode = "Talk";
        }

        final String finalPracticeMode = practiceMode;

        long durationSeconds =
                getIntent().getLongExtra("duration_seconds", 0);

        if (audioPath == null || audioPath.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "Audio file not found.",
                    Toast.LENGTH_LONG
            ).show();
            finish();
            return;
        }

        File audioFile = new File(audioPath);

        if (!audioFile.exists()) {
            Toast.makeText(
                    this,
                    "Recorded audio file does not exist.",
                    Toast.LENGTH_LONG
            ).show();
            finish();
            return;
        }

        transcriptText.setText("Transcribing your answer with AI...");
        feedbackText.setText("Please wait while your speech is analyzed.");
        workOnText.setText("Analyzing your speaking performance...");

        transcribeAudio(
                audioFile,
                finalPracticeMode,
                durationSeconds
        );
    }

    // =========================================================
    // TRANSCRIBE AUDIO
    // =========================================================

    private void transcribeAudio(
            File audioFile,
            String practiceMode,
            long durationSeconds
    ) {

        new Thread(() -> {

            HttpURLConnection connection = null;

            try {
                URL url = new URL(SERVER_URL + "/transcribe");
                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(120000);

                String boundary = "----PresentationCoachBoundary";

                connection.setRequestProperty(
                        "Content-Type",
                        "multipart/form-data; boundary=" + boundary
                );

                OutputStream outputStream = connection.getOutputStream();

                String lineEnd = "\r\n";
                String twoHyphens = "--";

                String fileHeader =
                        twoHyphens + boundary + lineEnd
                                + "Content-Disposition: form-data; name=\"file\"; filename=\""
                                + audioFile.getName() + "\"" + lineEnd
                                + "Content-Type: audio/mp4" + lineEnd
                                + lineEnd;

                outputStream.write(fileHeader.getBytes("UTF-8"));

                FileInputStream fileInputStream =
                        new FileInputStream(audioFile);

                byte[] buffer = new byte[8192];
                int bytesRead;

                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                fileInputStream.close();

                outputStream.write(
                        (lineEnd + twoHyphens + boundary
                                + twoHyphens + lineEnd).getBytes("UTF-8")
                );

                outputStream.flush();
                outputStream.close();

                int responseCode = connection.getResponseCode();

                InputStream responseStream =
                        responseCode >= 200 && responseCode < 300
                                ? connection.getInputStream()
                                : connection.getErrorStream();

                String responseText = readResponse(responseStream);

                if (responseCode < 200 || responseCode >= 300) {
                    throw new Exception(
                            "Server error " + responseCode + ": " + responseText
                    );
                }

                JSONObject json = new JSONObject(responseText);

                if (!json.optBoolean("success", false)) {
                    throw new Exception(
                            json.optString("message", "Transcription failed.")
                    );
                }

                String transcript =
                        json.optString("transcript", "").trim();

                if (transcript.isEmpty()) {
                    throw new Exception("Whisper returned an empty transcript.");
                }

                int pauseCount = json.optInt("pause_count", 0);

                double totalPauseDuration =
                        json.optDouble("total_pause_duration", 0);

                double averagePauseDuration =
                        json.optDouble("average_pause_duration", 0);

                List<Double> pauseDurations = new ArrayList<>();

                JSONArray pauses = json.optJSONArray("pauses");

                if (pauses != null) {
                    for (int i = 0; i < pauses.length(); i++) {
                        JSONObject pause = pauses.optJSONObject(i);

                        if (pause != null) {
                            pauseDurations.add(
                                    pause.optDouble("duration", 0)
                            );
                        }
                    }
                }

                final String pauseAnalysis = buildPauseAnalysis(
                        pauseCount,
                        totalPauseDuration,
                        averagePauseDuration,
                        pauseDurations
                );

                // LOCAL ANALYSIS: scores and XP remain unchanged.
                AnalysisResult localResult =
                        LocalNLPAnalyzer.analyze(
                                transcript,
                                durationSeconds
                        );

                // GEMINI ANALYSIS
                AnalysisResult finalResult = localResult;
                boolean geminiWorked = false;

                runOnUiThread(() -> {
                    feedbackText.setText(
                            "Your speech has been transcribed. Gemini is preparing your personalized feedback..."
                    );
                });

                try {
                    finalResult = requestGeminiFeedback(
                            transcript,
                            practiceMode,
                            durationSeconds,
                            pauseCount,
                            totalPauseDuration,
                            averagePauseDuration,
                            localResult
                    );

                    geminiWorked = true;

                } catch (Exception geminiError) {
                    geminiError.printStackTrace();
                }

                final AnalysisResult resultToDisplay = finalResult;
                final boolean geminiSuccess = geminiWorked;

                runOnUiThread(() -> {

                    saveAndDisplayResult(
                            audioFile.getAbsolutePath(),
                            practiceMode,
                            durationSeconds,
                            resultToDisplay,
                            pauseCount,
                            totalPauseDuration,
                            averagePauseDuration,
                            pauseDurations,
                            pauseAnalysis
                    );

                    if (!geminiSuccess) {
                        Toast.makeText(
                                SessionReportActivity.this,
                                "Gemini unavailable. Local feedback was saved instead.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });

            } catch (Exception e) {

                e.printStackTrace();

                runOnUiThread(() -> {

                    transcriptText.setText(
                            "Unable to transcribe this recording."
                    );

                    feedbackText.setText("Whisper connection failed.");

                    workOnText.setText(
                            "Please check that FastAPI is running and your phone and laptop are on the same Wi-Fi."
                    );


                    Toast.makeText(
                            SessionReportActivity.this,
                            "Whisper error: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

        }).start();
    }

    // =========================================================
    // GEMINI FEEDBACK
    // =========================================================

    private AnalysisResult requestGeminiFeedback(
            String transcript,
            String practiceMode,
            long durationSeconds,
            int pauseCount,
            double totalPauseDuration,
            double averagePauseDuration,
            AnalysisResult localResult
    ) throws Exception {

        HttpURLConnection connection = null;

        try {
            URL url = new URL(SERVER_URL + "/feedback");
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(75000);

            connection.setRequestProperty(
                    "Content-Type",
                    "application/json; charset=UTF-8"
            );

            connection.setRequestProperty(
                    "Accept",
                    "application/json"
            );

            int wordCount = transcript.trim().isEmpty()
                    ? 0
                    : transcript.trim().split("\\s+").length;

            double wordsPerMinute = durationSeconds > 0
                    ? (wordCount * 60.0) / durationSeconds
                    : 0;

            JSONObject request = new JSONObject();

            request.put("transcript", transcript);
            request.put("practice_mode", practiceMode);
            request.put("duration_seconds", durationSeconds);
            request.put("words_per_minute", wordsPerMinute);
            request.put("filler_words", new JSONArray());
            request.put("pause_count", pauseCount);
            request.put("total_pause_duration", totalPauseDuration);
            request.put("average_pause_duration", averagePauseDuration);

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(request.toString().getBytes("UTF-8"));
            outputStream.flush();
            outputStream.close();

            int responseCode = connection.getResponseCode();

            InputStream responseStream =
                    responseCode >= 200 && responseCode < 300
                            ? connection.getInputStream()
                            : connection.getErrorStream();

            String responseText = readResponse(responseStream);

            if (responseCode < 200 || responseCode >= 300) {
                throw new Exception(
                        "Gemini server error " + responseCode + ": " + responseText
                );
            }

            JSONObject response = new JSONObject(responseText);

            if (!response.optBoolean("success", false)) {
                throw new Exception("Gemini feedback generation failed.");
            }

            String feedback =
                    response.optString("feedback", "").trim();

            if (feedback.isEmpty()) {
                throw new Exception("Gemini returned empty feedback.");
            }

            JSONArray strengthsArray =
                    response.optJSONArray("strengths");

            JSONArray areasArray =
                    response.optJSONArray("areas_to_improve");

            JSONArray suggestionsArray =
                    response.optJSONArray("suggestions");

            StringBuilder completeFeedback = new StringBuilder();

            completeFeedback.append("✨ GEMINI AI FEEDBACK\n\n");
            completeFeedback.append(feedback);

            if (strengthsArray != null && strengthsArray.length() > 0) {
                completeFeedback.append("\n\n💜 YOUR STRENGTHS\n");

                for (int i = 0; i < strengthsArray.length(); i++) {
                    completeFeedback.append("• ")
                            .append(strengthsArray.optString(i))
                            .append("\n");
                }
            }

            StringBuilder improvementPoints = new StringBuilder();

            improvementPoints.append("🎯 AREAS TO IMPROVE\n\n");

            if (areasArray != null) {
                for (int i = 0; i < areasArray.length(); i++) {
                    improvementPoints.append("• ")
                            .append(areasArray.optString(i))
                            .append("\n");
                }
            }

            if (suggestionsArray != null && suggestionsArray.length() > 0) {
                improvementPoints.append("\n💡 SUGGESTIONS\n\n");

                for (int i = 0; i < suggestionsArray.length(); i++) {
                    improvementPoints.append("• ")
                            .append(suggestionsArray.optString(i))
                            .append("\n");
                }
            }

            List<String> followUpQuestions = new ArrayList<>();

            // Keep local scores and XP. Replace only textual feedback.
            return new AnalysisResult(
                    localResult.getOverallScore(),
                    localResult.getPaceScore(),
                    localResult.getClarityScore(),
                    localResult.getFillerScore(),
                    localResult.getConfidenceScore(),
                    localResult.getTranscript(),
                    completeFeedback.toString().trim(),
                    improvementPoints.toString().trim(),
                    followUpQuestions,
                    localResult.getXpEarned()
            );

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // =========================================================
    // READ SERVER RESPONSE
    // =========================================================

    private String readResponse(InputStream inputStream) throws Exception {

        if (inputStream == null) {
            return "";
        }

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, "UTF-8")
        );

        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();

        return response.toString();
    }

    // =========================================================
    // BUILD PAUSE REPORT
    // =========================================================

    private String buildPauseAnalysis(
            int pauseCount,
            double totalDuration,
            double averageDuration,
            List<Double> pauseDurations
    ) {

        StringBuilder report = new StringBuilder();

        report.append("⏸ PAUSE ANALYSIS\n\n");

        report.append("Detected pauses: ")
                .append(pauseCount)
                .append("\n");

        report.append("Total pause duration: ")
                .append(totalDuration)
                .append(" seconds\n");

        report.append("Average pause duration: ")
                .append(averageDuration)
                .append(" seconds\n");

        if (pauseCount > 0) {

            report.append("\nIndividual pauses:\n");

            for (int i = 0; i < pauseDurations.size(); i++) {
                report.append("Pause ")
                        .append(i + 1)
                        .append(": ")
                        .append(pauseDurations.get(i))
                        .append(" seconds\n");
            }

        } else {
            report.append("\nNo significant pauses detected.");
        }

        return report.toString();
    }

    // =========================================================
    // SAVE + DISPLAY RESULT
    // =========================================================

    private void saveAndDisplayResult(
            String audioPath,
            String practiceMode,
            long durationSeconds,
            AnalysisResult result,
            int pauseCount,
            double totalPauseDuration,
            double averagePauseDuration,
            List<Double> pauseDurations,
            String pauseAnalysis
    ) {

        SessionData session = new SessionData(
                audioPath,
                practiceMode,
                System.currentTimeMillis(),
                durationSeconds,
                result,
                pauseCount,
                totalPauseDuration,
                averagePauseDuration,
                pauseDurations
        );

        if (!sessionSaved) {
            SessionManager.addSession(this, session);
            sessionSaved = true;
        }

        overallScoreText.setText(
                String.valueOf(result.getOverallScore())
        );

        paceScoreText.setText(
                result.getPaceScore() + "%"
        );

        clarityScoreText.setText(
                result.getClarityScore() + "%"
        );

        fillerScoreText.setText(
                result.getFillerScore() + "%"
        );

        confidenceScoreText.setText(
                result.getConfidenceScore() + "%"
        );

        scoreMessageText.setText(
                getScoreMessage(result.getOverallScore())
        );

        transcriptText.setText(
                result.getTranscript()
        );

        feedbackText.setText(
                result.getFeedback()
                        + "\n\n"
                        + pauseAnalysis
        );

        workOnText.setText(
                result.getWorkOn()
        );


    }

    // =========================================================
    // SCORE MESSAGE
    // =========================================================

    private String getScoreMessage(int score) {

        if (score >= 90) {
            return "Excellent speaking performance! ✨";

        } else if (score >= 80) {
            return "Great work! You're getting stronger 🔥";

        } else if (score >= 70) {
            return "Good progress! Keep practicing 💜";

        } else if (score >= 60) {
            return "You're improving. Keep going ✨";

        } else {
            return "Every practice session makes you better 💗";
        }
    }
}
