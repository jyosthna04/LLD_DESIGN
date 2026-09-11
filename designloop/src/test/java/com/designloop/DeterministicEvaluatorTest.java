package com.designloop;

import com.designloop.evaluator.DeterministicEvaluator;
import com.designloop.evaluator.EvaluationResult;
import com.designloop.model.Problem;
import com.designloop.model.Submission;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * These tests check the DeterministicEvaluator - the part of the app
 * that uses plain Java code (no AI) to check objective facts about a
 * submission, like "did the learner include a required class?".
 */
class DeterministicEvaluatorTest {

    private final DeterministicEvaluator evaluator = new DeterministicEvaluator();

    private Problem parkingLotProblem() {
        Problem p = new Problem();
        p.setTitle("Parking Lot");
        p.setRequiredConcepts("Vehicle,ParkingLot,Slot,Ticket");
        return p;
    }

    // Test 1: An empty submission should score 0 and clearly say it's empty.
    @Test
    void emptySubmissionScoresZero() {
        Submission submission = new Submission();
        EvaluationResult result = evaluator.evaluate(parkingLotProblem(), submission);

        assertEquals(0, result.getScore());
        assertFalse(result.getProblems().isEmpty());
    }

    // Test 2: A valid submission that mentions every required concept and
    // describes relationships should score well and list no missing concepts.
    @Test
    void validSubmissionScoresHigh() {
        Submission submission = new Submission();
        submission.setClassesText("ParkingLot: manages slots and tickets\nVehicle: represents a car or bike\nSlot: a single parking space\nTicket: tracks entry time");
        submission.setRelationshipsText("ParkingLot has many Slots. Slot holds one Vehicle. Ticket is issued per Vehicle.");

        EvaluationResult result = evaluator.evaluate(parkingLotProblem(), submission);

        assertTrue(result.getScore() >= 90, "Expected a high score, got " + result.getScore());
        assertTrue(result.getMissingRequirements().isEmpty());
    }

    // Test 3: If the learner forgets a required class (Ticket), the
    // evaluator should flag exactly that concept as missing.
    @Test
    void missingRequiredClassIsDetected() {
        Submission submission = new Submission();
        submission.setClassesText("ParkingLot: manages slots\nVehicle: represents a car\nSlot: a parking space");
        submission.setRelationshipsText("ParkingLot has many Slots.");

        EvaluationResult result = evaluator.evaluate(parkingLotProblem(), submission);

        assertTrue(result.getMissingRequirements().contains("Ticket"));
    }

    // Test 4: If the same class name is listed twice, it should be flagged
    // as a duplicate instead of silently accepted.
    @Test
    void duplicateClassIsDetected() {
        Submission submission = new Submission();
        submission.setClassesText("ParkingLot: manages slots\nVehicle: a car\nVehicle: a bike\nSlot: a space\nTicket: entry record");
        submission.setRelationshipsText("ParkingLot has many Slots.");

        EvaluationResult result = evaluator.evaluate(parkingLotProblem(), submission);

        boolean foundDuplicateMessage = result.getProblems().stream()
                .anyMatch(p -> p.toLowerCase().contains("duplicate"));
        assertTrue(foundDuplicateMessage);
    }
}
