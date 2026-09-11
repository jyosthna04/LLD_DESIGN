package com.designloop.evaluator;

import com.designloop.model.Evaluation;
import org.springframework.stereotype.Component;

/**
 * Takes the DeterministicEvaluator's result and the LlmEvaluator's
 * result and combines them into one Evaluation entity that gets saved
 * to the database and shown on the Feedback page.
 *
 * Deterministic checks count for 40% of the score, AI judgement for
 * 60% - because "is this a good design" is mostly a judgement call,
 * but hard rules (missing required classes, duplicates) still matter
 * and should not be ignored.
 *
 * If the AI evaluator was unavailable, we fall back to the
 * deterministic score alone rather than failing.
 */
@Component
public class FeedbackMerger {

    public Evaluation merge(EvaluationResult deterministic, EvaluationResult llm) {
        Evaluation evaluation = new Evaluation();

        int finalScore;
        boolean aiAvailable = llm.isAvailable();
        if (aiAvailable) {
            finalScore = Math.round(deterministic.getScore() * 0.4f + llm.getScore() * 0.6f);
        } else {
            finalScore = deterministic.getScore();
        }
        evaluation.setOverallScore(finalScore);
        evaluation.setAiAvailable(aiAvailable);

        StringBuilder strengths = new StringBuilder();
        appendList(strengths, deterministic.getStrengths());
        appendList(strengths, llm.getStrengths());
        evaluation.setStrengths(strengths.toString());

        StringBuilder problems = new StringBuilder();
        appendList(problems, deterministic.getProblems());
        appendList(problems, llm.getProblems());
        evaluation.setProblemsFound(problems.toString());

        StringBuilder suggestions = new StringBuilder();
        appendList(suggestions, llm.getSuggestions());
        appendList(suggestions, deterministic.getSuggestions());
        evaluation.setSuggestions(suggestions.toString());

        StringBuilder missing = new StringBuilder();
        appendList(missing, deterministic.getMissingRequirements());
        evaluation.setMissingRequirements(missing.toString());

        evaluation.setTradeoffsNote(llm.getTradeoffsNote() == null ? "" : llm.getTradeoffsNote());

        return evaluation;
    }

    private void appendList(StringBuilder sb, Iterable<String> items) {
        if (items == null) return;
        for (String item : items) {
            if (item != null && !item.isBlank()) {
                sb.append(item).append("\n");
            }
        }
    }
}
