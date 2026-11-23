package com.presentationcoach.model;

import java.util.List;

public class AnalysisResult {

    private int overallScore;
    private int paceScore;
    private int clarityScore;
    private int fillerScore;
    private int confidenceScore;

    private String transcript;
    private String feedback;
    private String workOn;
    private List<String> followUpQuestions;

    private int xpEarned;

    public AnalysisResult(
            int overallScore,
            int paceScore,
            int clarityScore,
            int fillerScore,
            int confidenceScore,
            String transcript,
            String feedback,
            String workOn,
            List<String> followUpQuestions,
            int xpEarned
    ) {
        this.overallScore = overallScore;
        this.paceScore = paceScore;
        this.clarityScore = clarityScore;
        this.fillerScore = fillerScore;
        this.confidenceScore = confidenceScore;
        this.transcript = transcript;
        this.feedback = feedback;
        this.workOn = workOn;
        this.followUpQuestions = followUpQuestions;
        this.xpEarned = xpEarned;
    }

    public int getOverallScore() {
        return overallScore;
    }

    public int getPaceScore() {
        return paceScore;
    }

    public int getClarityScore() {
        return clarityScore;
    }

    public int getFillerScore() {
        return fillerScore;
    }

    public int getConfidenceScore() {
        return confidenceScore;
    }

    public String getTranscript() {
        return transcript;
    }

    public String getFeedback() {
        return feedback;
    }

    public String getWorkOn() {
        return workOn;
    }

    public List<String> getFollowUpQuestions() {
        return followUpQuestions;
    }

    public int getXpEarned() {
        return xpEarned;
    }
}