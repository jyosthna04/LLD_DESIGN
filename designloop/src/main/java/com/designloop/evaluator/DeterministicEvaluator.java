package com.designloop.evaluator;

import com.designloop.model.Problem;
import com.designloop.model.Submission;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Checks things that plain Java code can decide with certainty -
 * no AI needed. For example: is the submission empty? Did the learner
 * mention every class the problem requires? Did they type the same
 * class name twice by mistake?
 */
@Component
public class DeterministicEvaluator implements Evaluator {

    @Override
    public EvaluationResult evaluate(Problem problem, Submission submission) {
        EvaluationResult result = new EvaluationResult();
        int score = 100;

        String fullText = submission.getFullText();
        boolean isEmpty = fullText == null || fullText.trim().isEmpty();

        if (isEmpty) {
            result.setScore(0);
            result.getProblems().add("Your submission is empty. Add at least one class before submitting.");
            return result;
        }

        // Check 1: required concepts/classes for this problem
        List<String> missing = new ArrayList<>();
        if (problem.getRequiredConcepts() != null && !problem.getRequiredConcepts().isBlank()) {
            String[] required = problem.getRequiredConcepts().split(",");
            String lowerText = fullText.toLowerCase();
            for (String concept : required) {
                String c = concept.trim();
                if (!c.isEmpty() && !lowerText.contains(c.toLowerCase())) {
                    missing.add(c);
                }
            }
        }
        if (!missing.isEmpty()) {
            score -= 15 * missing.size();
            result.getMissingRequirements().addAll(missing);
            for (String m : missing) {
                result.getProblems().add("Missing required concept: " + m + ". Your design should include a class or component named (or clearly representing) '" + m + "'.");
            }
        } else {
            result.getStrengths().add("All required concepts for this problem are present in your design.");
        }

        // Check 2: no classes typed at all
        String classesText = submission.getClassesText();
        if (classesText == null || classesText.isBlank()) {
            score -= 30;
            result.getProblems().add("No classes were defined. Every LLD design needs at least one class.");
        } else {
            // Check 3: duplicate class names
            // We expect lines like "ClassName: responsibility ..."
            Set<String> seen = new HashSet<>();
            List<String> duplicates = new ArrayList<>();
            for (String line : classesText.split("\\r?\\n")) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;
                String name = trimmed.contains(":") ? trimmed.substring(0, trimmed.indexOf(":")).trim() : trimmed;
                String key = name.toLowerCase();
                if (!key.isEmpty()) {
                    if (!seen.add(key)) {
                        duplicates.add(name);
                    }
                }
            }
            if (!duplicates.isEmpty()) {
                score -= 10 * duplicates.size();
                for (String d : duplicates) {
                    result.getProblems().add("Duplicate class detected: '" + d + "' appears more than once. Each class should be listed only once.");
                }
            }
        }

        // Check 4: relationships mentioned at all (basic sanity check, only
        // matters when there's more than one class)
        long classCount = classesText == null ? 0 : classesText.lines().filter(l -> !l.isBlank()).count();
        if (classCount > 1 && (submission.getRelationshipsText() == null || submission.getRelationshipsText().isBlank())) {
            score -= 10;
            result.getProblems().add("You defined multiple classes but did not describe any relationships between them. Explain how your classes connect (e.g. 'ParkingLot has many Slots').");
        }

        if (score < 0) score = 0;
        if (score > 100) score = 100;
        result.setScore(score);
        return result;
    }
}
