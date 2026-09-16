package com.coalqc.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatusTest {

    @Test
    void loggedCanMoveToUnderInvestigationOrRejected() {
        assertTrue(Status.LOGGED.canTransitionTo(Status.UNDER_INVESTIGATION));
        assertTrue(Status.LOGGED.canTransitionTo(Status.REJECTED));
    }

    @Test
    void loggedCannotSkipStraightToClosed() {
        assertFalse(Status.LOGGED.canTransitionTo(Status.CLOSED));
    }

    @Test
    void fullHappyPathIsLegalStepByStep() {
        assertTrue(Status.LOGGED.canTransitionTo(Status.UNDER_INVESTIGATION));
        assertTrue(Status.UNDER_INVESTIGATION.canTransitionTo(Status.CLASSIFIED));
        assertTrue(Status.CLASSIFIED.canTransitionTo(Status.CORRECTIVE_ACTION));
        assertTrue(Status.CORRECTIVE_ACTION.canTransitionTo(Status.VERIFIED));
        assertTrue(Status.VERIFIED.canTransitionTo(Status.CLOSED));
    }

    @Test
    void closedAndRejectedAreTerminal() {
        for (Status target : Status.values()) {
            assertFalse(Status.CLOSED.canTransitionTo(target));
            assertFalse(Status.REJECTED.canTransitionTo(target));
        }
    }

    @Test
    void correctiveActionAndVerifiedCannotBeRejected() {
        assertFalse(Status.CORRECTIVE_ACTION.canTransitionTo(Status.REJECTED));
        assertFalse(Status.VERIFIED.canTransitionTo(Status.REJECTED));
    }

    @Test
    void underInvestigationAndClassifiedCanAlsoBeRejected() {
        assertTrue(Status.UNDER_INVESTIGATION.canTransitionTo(Status.REJECTED));
        assertTrue(Status.CLASSIFIED.canTransitionTo(Status.REJECTED));
    }

    @Test
    void noStatusCanTransitionToItself() {
        for (Status status : Status.values()) {
            assertFalse(status.canTransitionTo(status));
        }
    }

    @Test
    void cannotSkipIntermediateSteps() {
        assertFalse(Status.UNDER_INVESTIGATION.canTransitionTo(Status.CORRECTIVE_ACTION));
        assertFalse(Status.UNDER_INVESTIGATION.canTransitionTo(Status.VERIFIED));
        assertFalse(Status.CLASSIFIED.canTransitionTo(Status.VERIFIED));
        assertFalse(Status.CLASSIFIED.canTransitionTo(Status.CLOSED));
        assertFalse(Status.LOGGED.canTransitionTo(Status.CORRECTIVE_ACTION));
    }
}
