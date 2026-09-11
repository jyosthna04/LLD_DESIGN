package com.designloop.evaluator;

import java.util.ArrayList;
import java.util.List;

/**
 * A common "shape" that both DeterministicEvaluator and LlmEvaluator
 * return. Keeping this shape identical for both means the code that
 * combines their results (FeedbackMerger) does not need to know or
 * care which evaluator produced which part.
 */
public class EvaluationResult {

    private int score; // 0 to 100
    private List<String> strengths = new ArrayList<>();
    private List<String> problems = new ArrayList<>();
    private List<String> suggestions = new ArrayList<>();
    private List<String> missingRequirements = new ArrayList<>();
    private String tradeoffsNote = "";

    // If an evaluator could not run (for example, the AI API failed),
    // it sets this to false. The merger then relies only on the
    // evaluator(s) that succeeded, instead of crashing the whole app.
    private boolean available = true;

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public List<String> getStrengths() {
        return strengths;
    }

    public void setStrengths(List<String> strengths) {
        this.strengths = strengths;
    }

    public List<String> getProblems() {
        return problems;
    }

    public void setProblems(List<String> problems) {
        this.problems = problems;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public List<String> getMissingRequirements() {
        return missingRequirements;
    }

    public void setMissingRequirements(List<String> missingRequirements) {
        this.missingRequirements = missingRequirements;
    }

    public String getTradeoffsNote() {
        return tradeoffsNote;
    }

    public void setTradeoffsNote(String tradeoffsNote) {
        this.tradeoffsNote = tradeoffsNote;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
