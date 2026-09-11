package com.designloop;

import com.designloop.model.Attempt;
import com.designloop.model.AttemptStatus;
import com.designloop.model.Evaluation;
import com.designloop.model.Submission;
import com.designloop.repository.AttemptRepository;
import com.designloop.repository.ProblemRepository;
import com.designloop.service.AttemptService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * These tests exercise the whole practice flow through AttemptService,
 * backed by a real (in-memory H2) database, the same way the
 * controllers use it.
 *
 * Since no AI API key is configured for tests, the AI evaluator
 * automatically reports itself as "unavailable" - this is exactly the
 * behavior we want to test for "AI evaluation failure": the app should
 * still complete the attempt using the deterministic score alone,
 * instead of crashing.
 */
@SpringBootTest
class AttemptServiceTest {

    @Autowired
    private AttemptService attemptService;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private AttemptRepository attemptRepository;

    private Long anyProblemId() {
        // DataSeeder has already inserted the 3 starter problems by the
        // time tests run, so we just grab the first one.
        return problemRepository.findAll().get(0).getId();
    }

    // Test 5: Starting an attempt should create it with STARTED status
    // and attempt number 1 (or higher if attempts already exist).
    @Test
    void startingAnAttemptCreatesStartedStatus() {
        Attempt attempt = attemptService.startAttempt(anyProblemId());

        assertNotNull(attempt.getId());
        assertEquals(AttemptStatus.STARTED, attempt.getStatus());
    }

    // Test 6: Saving a submission (draft) should persist the text the
    // learner typed, without changing the attempt's status.
    @Test
    void savingADraftPersistsText() {
        Attempt attempt = attemptService.startAttempt(anyProblemId());

        attemptService.saveDraft(attempt.getId(), "ParkingLot: manages slots", "", "", "", "");

        Submission submission = attemptService.getSubmission(attempt);
        assertEquals("ParkingLot: manages slots", submission.getClassesText());
        assertEquals(AttemptStatus.STARTED, attemptService.getAttempt(attempt.getId()).getStatus());
    }

    // Test 7 & 8: Submitting an attempt should run evaluation and move
    // the attempt to COMPLETED, with an Evaluation saved for it - this
    // is what "successful evaluation" means in this app (AI being
    // unavailable does not count as failure - only unexpected errors do).
    @Test
    void submittingAttemptRunsEvaluationAndCompletes() {
        Attempt attempt = attemptService.startAttempt(anyProblemId());

        Attempt result = attemptService.submitAndEvaluate(
                attempt.getId(),
                "ParkingLot: manages slots and tickets\nVehicle: a car\nSlot: a space\nTicket: entry record",
                "", "ParkingLot has many Slots.", "Simple design.", "Kept it minimal.");

        assertEquals(AttemptStatus.COMPLETED, result.getStatus());
        Evaluation evaluation = attemptService.getEvaluation(result);
        assertNotNull(evaluation);
        assertFalse(evaluation.isAiAvailable(), "AI should be unavailable in tests (no API key configured)");
    }

    // Test 9: Submitting a second time on an already-COMPLETED attempt
    // should be rejected instead of silently overwriting the result.
    @Test
    void cannotSubmitAnAlreadyCompletedAttempt() {
        Attempt attempt = attemptService.startAttempt(anyProblemId());
        attemptService.submitAndEvaluate(attempt.getId(), "ParkingLot: manages slots", "", "", "", "");

        assertThrows(IllegalStateException.class, () ->
                attemptService.submitAndEvaluate(attempt.getId(), "ParkingLot: manages slots", "", "", "", ""));
    }

    // Test 10: Retrying evaluation on an attempt that previously FAILED
    // should move it back to COMPLETED, without losing the submission.
    @Test
    void retryAfterEvaluationFailureCompletesAttempt() {
        Attempt attempt = attemptService.startAttempt(anyProblemId());
        attemptService.saveDraft(attempt.getId(), "ParkingLot: manages slots", "", "", "", "");

        // Simulate a previous evaluation failure directly, the way the
        // service would leave it after catching an unexpected error.
        attempt.setStatus(AttemptStatus.FAILED);
        attemptRepository.save(attempt);

        Attempt result = attemptService.retryEvaluation(attempt.getId());

        assertEquals(AttemptStatus.COMPLETED, result.getStatus());
        assertNotNull(attemptService.getSubmission(result).getClassesText());
    }
}
