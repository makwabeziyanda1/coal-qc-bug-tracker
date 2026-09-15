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
}
