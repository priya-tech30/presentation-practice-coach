
package com.presentationcoach.model;

import java.util.ArrayList;
import java.util.List;

public class SessionData {

    private String audioFilePath;
    private String practiceMode;
    private long timestamp;
    private long durationSeconds;
    private AnalysisResult analysisResult;

    // Pause analysis
    private int pauseCount;
    private double totalPauseDuration;
    private double averagePauseDuration;
    private List<Double> pauseDurations;

    // Original constructor retained for compatibility
    public SessionData(
            String audioFilePath,
            String practiceMode,
            long timestamp,
            long durationSeconds,
            AnalysisResult analysisResult
    ) {
        this(
                audioFilePath,
                practiceMode,
                timestamp,
                durationSeconds,
                analysisResult,
                0,
                0,
                0,
                new ArrayList<>()
        );
    }

    // New constructor with pause information
    public SessionData(
            String audioFilePath,
            String practiceMode,
            long timestamp,
            long durationSeconds,
            AnalysisResult analysisResult,
            int pauseCount,
            double totalPauseDuration,
            double averagePauseDuration,
            List<Double> pauseDurations
    ) {
        this.audioFilePath = audioFilePath;
        this.practiceMode = practiceMode;
        this.timestamp = timestamp;
        this.durationSeconds = durationSeconds;
        this.analysisResult = analysisResult;

        this.pauseCount = pauseCount;
        this.totalPauseDuration = totalPauseDuration;
        this.averagePauseDuration = averagePauseDuration;

        this.pauseDurations = pauseDurations == null
                ? new ArrayList<>()
                : new ArrayList<>(pauseDurations);
    }

    public String getAudioFilePath() {
        return audioFilePath;
    }

    public String getPracticeMode() {
        return practiceMode;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public AnalysisResult getAnalysisResult() {
        return analysisResult;
    }

    public int getPauseCount() {
        return pauseCount;
    }

    public double getTotalPauseDuration() {
        return totalPauseDuration;
    }

    public double getAveragePauseDuration() {
        return averagePauseDuration;
    }

    public List<Double> getPauseDurations() {
        return new ArrayList<>(pauseDurations);
    }
}
