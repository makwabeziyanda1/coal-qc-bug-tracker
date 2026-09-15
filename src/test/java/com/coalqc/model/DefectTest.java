package com.coalqc.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefectTest {

    private Defect newDefect() {
        return new Defect("Crash on save", "NPE when saving with empty title",
                "ziyanda", "editor", Severity.MAJOR);
    }

    @Test
    void newDefectStartsInLoggedStatus() {
        Defect defect = newDefect();

        assertEquals(Status.LOGGED, defect.getStatus());
    }

    @Test
    void legalTransitionSucceeds() {
        Defect defect = newDefect();

        defect.transitionTo(Status.UNDER_INVESTIGATION);

        assertEquals(Status.UNDER_INVESTIGATION, defect.getStatus());
    }

    @Test
    void illegalTransitionThrowsAndLeavesStatusUnchanged() {
        Defect defect = newDefect();

        assertThrows(IllegalStatusTransitionException.class, () -> defect.transitionTo(Status.CLOSED));
        assertEquals(Status.LOGGED, defect.getStatus());
    }

    @Test
    void cannotCloseWithoutVerifiedBy() {
        Defect defect = newDefect();
        defect.transitionTo(Status.UNDER_INVESTIGATION);
        defect.transitionTo(Status.CLASSIFIED);
        defect.transitionTo(Status.CORRECTIVE_ACTION);
        defect.transitionTo(Status.VERIFIED);

        assertThrows(IllegalStateException.class, () -> defect.transitionTo(Status.CLOSED));
        assertEquals(Status.VERIFIED, defect.getStatus());
    }

    @Test
    void closingSetsDateClosedWhenVerifiedByIsSet() {
        Defect defect = newDefect();
        defect.transitionTo(Status.UNDER_INVESTIGATION);
        defect.transitionTo(Status.CLASSIFIED);
        defect.transitionTo(Status.CORRECTIVE_ACTION);
        defect.transitionTo(Status.VERIFIED);
        defect.setVerifiedBy("qa-lead");

        defect.transitionTo(Status.CLOSED);

        assertEquals(Status.CLOSED, defect.getStatus());
        assertNotNull(defect.getDateClosed());
    }
}
