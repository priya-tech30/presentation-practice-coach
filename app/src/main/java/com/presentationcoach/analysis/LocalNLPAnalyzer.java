
package com.presentationcoach.analysis;

import com.presentationcoach.model.AnalysisResult;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocalNLPAnalyzer {

    public static AnalysisResult analyze(String transcript, long durationSeconds) {

        if (transcript == null || transcript.trim().isEmpty()
                || transcript.equalsIgnoreCase("No transcript available.")) {

            return new AnalysisResult(
                    0, 0, 0, 0, 0,
                    "No transcript available.",
                    "Speech analysis could not be completed because no transcript was received.",
                    "• Record your answer again.",
                    Arrays.asList("Please try recording your answer again."),
                    0
            );
        }

        transcript = transcript.trim();

        // WORD COUNT
        Matcher wordMatcher = Pattern.compile("[\\p{L}\\p{N}']+")
                .matcher(transcript);

        int wordCount = 0;

        while (wordMatcher.find()) {
            wordCount++;
        }

        // HIGH-CONFIDENCE FILLER DETECTION
        String fillerPattern =
                "(?<![\\p{L}\\p{N}])(" +
                        "um+|uh+|erm+|er+|ah+|hmm+|" +
                        "mm+\\s*-?\\s*hm+|" +
                        "you\\s+know|i\\s+mean|you\\s+see|" +
                        "let\\s+me\\s+think|" +
                        "how\\s+do\\s+i\\s+say\\s+this|" +
                        "what\\s+was\\s+i\\s+saying|" +
                        "what'?s\\s+the\\s+word" +
                        ")(?![\\p{L}\\p{N}-])";

        Matcher fillerMatcher = Pattern.compile(
                fillerPattern,
                Pattern.CASE_INSENSITIVE
        ).matcher(transcript);

        List<String> detectedFillers = new ArrayList<>();

        while (fillerMatcher.find()) {
            detectedFillers.add(fillerMatcher.group().trim());
        }

        int fillerCount = detectedFillers.size();

        // CONTEXT-DEPENDENT FILLERS
        // Display these separately; do not automatically penalize them.

        String possiblePattern =
                "(?<![\\p{L}\\p{N}])(" +
                        "basically|actually|literally|honestly|" +
                        "like|well|okay|right|so|" +
                        "kind\\s+of|sort\\s+of|i\\s+guess" +
                        ")(?![\\p{L}\\p{N}])";

        Matcher possibleMatcher = Pattern.compile(
                possiblePattern,
                Pattern.CASE_INSENSITIVE
        ).matcher(transcript);

        List<String> possibleFillers = new ArrayList<>();

        while (possibleMatcher.find()) {
            possibleFillers.add(possibleMatcher.group().trim());
        }

        // FILLER SCORE
        double fillerPercentage =
                wordCount > 0
                        ? fillerCount * 100.0 / wordCount
                        : 0;

        int fillerScore;

        if (fillerPercentage <= 2) {
            fillerScore = 95;
        } else if (fillerPercentage <= 5) {
            fillerScore = 85;
        } else if (fillerPercentage <= 8) {
            fillerScore = 75;
        } else {
            fillerScore = 60;
        }

        // SPEAKING PACE
        int wordsPerMinute = 0;
        int paceScore = 0;

        if (durationSeconds > 0) {

            wordsPerMinute =
                    (int) (wordCount * 60.0 / durationSeconds);

            if (wordsPerMinute >= 110 && wordsPerMinute <= 160) {
                paceScore = 95;
            } else if (wordsPerMinute >= 90 && wordsPerMinute <= 180) {
                paceScore = 85;
            } else if (wordsPerMinute >= 70 && wordsPerMinute <= 200) {
                paceScore = 75;
            } else {
                paceScore = 60;
            }
        }

        // EXISTING UI COMPATIBILITY
        // These are estimates, not direct voice measurements.
        int clarityScore = 75;
        int confidenceScore = 75;

        if (fillerCount == 0) {
            confidenceScore += 5;
        }

        if (wordCount >= 80) {
            confidenceScore += 5;
        }

        if (confidenceScore > 100) {
            confidenceScore = 100;
        }

        // PERSONALIZED FEEDBACK
        StringBuilder feedback = new StringBuilder();

        feedback.append("Your response contains ")
                .append(wordCount)
                .append(" words. ");

        if (durationSeconds > 0) {
            feedback.append("Your speaking pace is approximately ")
                    .append(wordsPerMinute)
                    .append(" words per minute. ");
        } else {
            feedback.append(
                    "Speaking pace could not be calculated because recording duration is unavailable. "
            );
        }

        if (paceScore >= 85) {
            feedback.append("Your speaking pace is within a comfortable range. ");
        } else if (paceScore > 0) {
            feedback.append("Try maintaining a steadier speaking pace. ");
        }

        if (fillerCount > 0) {
            feedback.append("We detected ")
                    .append(fillerCount)
                    .append(" clear filler expressions. ");
        } else {
            feedback.append("No common hesitation fillers were detected. ");
        }

        // DISPLAY DETECTED FILLER WORDS
        if (!detectedFillers.isEmpty()) {

            feedback.append("Detected fillers: ");

            for (int i = 0; i < detectedFillers.size(); i++) {

                if (i > 0) {
                    feedback.append(", ");
                }

                feedback.append(detectedFillers.get(i));
            }

            feedback.append(". ");
        }

        // DISPLAY CONTEXT-DEPENDENT EXPRESSIONS
        if (!possibleFillers.isEmpty()) {

            feedback.append("Other expressions to review: ");

            for (int i = 0; i < possibleFillers.size(); i++) {

                if (i > 0) {
                    feedback.append(", ");
                }

                feedback.append(possibleFillers.get(i));
            }

            feedback.append(". These may be meaningful words depending on context.");
        }

        // AREAS TO IMPROVE
        StringBuilder workOn = new StringBuilder();

        if (fillerCount > 0) {
            workOn.append("• Reduce unnecessary hesitation words\n");
        }

        if (!possibleFillers.isEmpty()) {
            workOn.append("• Review repeated conversational expressions\n");
        }

        if (paceScore > 0 && paceScore < 85) {
            workOn.append("• Maintain a steady speaking pace\n");
        }

        if (workOn.length() == 0) {
            workOn.append("• Maintain your current speaking style\n")
                    .append("• Continue practicing consistently");
        }

        // OVERALL SCORE
        int overallScore =
                (paceScore + fillerScore +
                        clarityScore + confidenceScore) / 4;

        // XP
        int xpEarned = 25 + (overallScore / 5);

        // FOLLOW-UP QUESTIONS
        List<String> questions = Arrays.asList(
                "Can you explain the main idea of your answer in more detail?",
                "What challenges did you face while explaining this topic?",
                "How would you improve your answer?"
        );

        return new AnalysisResult(
                overallScore,
                paceScore,
                clarityScore,
                fillerScore,
                confidenceScore,
                transcript,
                feedback.toString(),
                workOn.toString(),
                questions,
                xpEarned
        );
    }
}
