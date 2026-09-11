package com.designloop.service;

import com.designloop.evaluator.DeterministicEvaluator;
import com.designloop.evaluator.EvaluationResult;
import com.designloop.evaluator.FeedbackMerger;
import com.designloop.evaluator.LlmEvaluator;
import com.designloop.model.*;
import com.designloop.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Contains the business logic for the practice flow: starting an
 * attempt, saving a design in progress, submitting it for evaluation,
 * and retrying evaluation if it failed. Controllers call this class;
 * this class talks to the repositories (database) and the evaluators.
 */
@Service
public class AttemptService {

    private final ProblemRepository problemRepository;
    private final AttemptRepository attemptRepository;
    private final SubmissionRepository submissionRepository;
    private final EvaluationRepository evaluationRepository;
    private final DeterministicEvaluator deterministicEvaluator;
    private final LlmEvaluator llmEvaluator;
    private final FeedbackMerger feedbackMerger;

    public AttemptService(ProblemRepository problemRepository,
                           AttemptRepository attemptRepository,
                           SubmissionRepository submissionRepository,
                           EvaluationRepository evaluationRepository,
                           DeterministicEvaluator deterministicEvaluator,
                           LlmEvaluator llmEvaluator,
                           FeedbackMerger feedbackMerger) {
        this.problemRepository = problemRepository;
        this.attemptRepository = attemptRepository;
        this.submissionRepository = submissionRepository;
        this.evaluationRepository = evaluationRepository;
        this.deterministicEvaluator = deterministicEvaluator;
        this.llmEvaluator = llmEvaluator;
        this.feedbackMerger = feedbackMerger;
    }

    public Attempt startAttempt(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new NoSuchElementException("Problem not found: " + problemId));

        int previousAttempts = attemptRepository.countByProblem(problem);

        Attempt attempt = new Attempt();
        attempt.setProblem(problem);
        attempt.setStatus(AttemptStatus.STARTED);
        attempt.setAttemptNumber(previousAttempts + 1);
        attempt.setCreatedAt(LocalDateTime.now());
        attempt = attemptRepository.save(attempt);

        Submission submission = new Submission();
        submission.setAttempt(attempt);
        submissionRepository.save(submission);

        return attempt;
    }

    public Attempt getAttempt(Long attemptId) {
        return attemptRepository.findById(attemptId)
                .orElseThrow(() -> new NoSuchElementException("Attempt not found: " + attemptId));
    }

    public Submission getSubmission(Attempt attempt) {
        return submissionRepository.findByAttempt(attempt)
                .orElseThrow(() -> new NoSuchElementException("Submission not found for attempt: " + attempt.getId()));
    }

    /** Saves the design-in-progress without submitting it for evaluation. */
    public void saveDraft(Long attemptId, String classesText, String interfacesText,
                           String relationshipsText, String explanationText, String tradeoffsText) {
        Attempt attempt = getAttempt(attemptId);
        Submission submission = getSubmission(attempt);
        submission.setClassesText(classesText);
        submission.setInterfacesText(interfacesText);
        submission.setRelationshipsText(relationshipsText);
        submission.setExplanationText(explanationText);
        submission.setTradeoffsText(tradeoffsText);
        submissionRepository.save(submission);
    }

    /**
     * Submits the design for evaluation. Runs both evaluators, merges
     * their results, and stores an Evaluation. If something goes wrong
     * (for example the AI API is down), the attempt is marked FAILED
     * but the submission itself is never lost - the learner can retry
     * evaluation without retyping their design.
     */
    public Attempt submitAndEvaluate(Long attemptId, String classesText, String interfacesText,
                                      String relationshipsText, String explanationText, String tradeoffsText) {
        Attempt attempt = getAttempt(attemptId);

        if (attempt.getStatus() == AttemptStatus.COMPLETED) {
            throw new IllegalStateException("This attempt has already been submitted and evaluated.");
        }

        saveDraft(attemptId, classesText, interfacesText, relationshipsText, explanationText, tradeoffsText);
        attempt.setStatus(AttemptStatus.SUBMITTED);
        attemptRepository.save(attempt);

        return runEvaluation(attempt);
    }

    /** Re-runs evaluation on an attempt that previously FAILED. */
    public Attempt retryEvaluation(Long attemptId) {
        Attempt attempt = getAttempt(attemptId);
        return runEvaluation(attempt);
    }

    private Attempt runEvaluation(Attempt attempt) {
        attempt.setStatus(AttemptStatus.EVALUATING);
        attemptRepository.save(attempt);

        try {
            Submission submission = getSubmission(attempt);
            Problem problem = attempt.getProblem();

            EvaluationResult deterministicResult = deterministicEvaluator.evaluate(problem, submission);
            EvaluationResult llmResult = llmEvaluator.evaluate(problem, submission);

            Evaluation evaluation = feedbackMerger.merge(deterministicResult, llmResult);
            evaluation.setAttempt(attempt);
            evaluation.setCreatedAt(LocalDateTime.now());

            // If a previous failed attempt already has an evaluation row, reuse it.
            evaluationRepository.findByAttempt(attempt).ifPresent(existing -> evaluation.setId(existing.getId()));
            evaluationRepository.save(evaluation);

            attempt.setStatus(AttemptStatus.COMPLETED);
            attemptRepository.save(attempt);
            return attempt;

        } catch (Exception e) {
            // Anything unexpected (DB hiccup, evaluator bug, etc.) -> mark
            // FAILED instead of throwing a raw error at the learner. Their
            // submission stays safely in the database either way.
            attempt.setStatus(AttemptStatus.FAILED);
            attemptRepository.save(attempt);
            return attempt;
        }
    }

    public List<Attempt> getHistoryForProblem(Problem problem) {
        return attemptRepository.findByProblemOrderByAttemptNumberAsc(problem);
    }

    public Evaluation getEvaluation(Attempt attempt) {
        return evaluationRepository.findByAttempt(attempt).orElse(null);
    }

    public List<Problem> getAllProblems() {
        return problemRepository.findAll();
    }

    public Problem getProblem(Long id) {
        return problemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Problem not found: " + id));
    }
}
