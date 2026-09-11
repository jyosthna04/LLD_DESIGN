package com.designloop.evaluator;

import com.designloop.model.Problem;
import com.designloop.model.Submission;

/**
 * Anything that can look at a Submission and produce an EvaluationResult
 * implements this interface.
 *
 * Why this exists: today we have two evaluators (deterministic code
 * checks, and an LLM). Because both implement this same interface, the
 * rest of the application (AttemptService) does not need to know HOW
 * an evaluator works internally - it just calls evaluate(...) and gets
 * back a result in the same shape every time.
 *
 * If we want to add a third evaluator later (for example, a
 * "StyleEvaluator" that checks naming conventions), we just create a
 * new class that implements Evaluator. Nothing else in the app has to
 * change.
 */
public interface Evaluator {
    EvaluationResult evaluate(Problem problem, Submission submission);
}
