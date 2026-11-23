package com.presentationcoach.analysis;

import com.presentationcoach.model.AnalysisResult;

import java.util.Arrays;

public class MockAnalysisEngine {

    public static AnalysisResult analyze(String audioFilePath) {

        // Temporary transcript.
        // Later this will come from Whisper.
        String transcript =
                "My project is a presentation practice application " +
                        "designed to help students improve their speaking skills. " +
                        "The application records the user's answer and analyzes " +
                        "different aspects of their speech. It provides feedback " +
                        "about clarity, speaking pace, filler words and confidence. " +
                        "It can also generate follow-up questions so that students " +
                        "can practice answering questions like they would in a real viva.";

        // Temporary AI-style feedback.
        // Later this will come from Claude.
        String feedback =
                "Your answer had a clear structure and a confident opening. " +
                        "You explained the purpose of your project well. " +
                        "Try supporting your main points with specific examples " +
                        "to make your explanation more convincing.";

        String workOn =
                "• Reduce filler words\n" +
                        "• Keep your speaking pace steady\n" +
                        "• Add examples when explaining technical ideas";

        return new AnalysisResult(

                // Overall score
                78,

                // Speaking pace
                82,

                // Clarity
                81,

                // Filler control
                74,

                // Confidence
                76,

                // Transcript
                transcript,

                // AI feedback
                feedback,

                // Improvement points
                workOn,

                // Examiner follow-up questions
                Arrays.asList(
                        "Why did you choose this approach?",
                        "What are the limitations of your solution?",
                        "How would you improve it?"
                ),

                // XP
                45
        );
    }
}