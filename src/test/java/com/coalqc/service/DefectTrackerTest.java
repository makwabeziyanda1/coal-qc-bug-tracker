package com.coalqc.service;

import com.coalqc.model.Defect;
import com.coalqc.model.IllegalStatusTransitionException;
import com.coalqc.model.Severity;
import com.coalqc.model.Status;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefectTrackerTest {

    private final DefectTracker tracker = new DefectTracker();

    @Test
    void createdDefectCanBeRetrievedById() {
        Defect defect = tracker.createDefect("Crash on save", "NPE", "ziyanda", "editor", Severity.MAJOR);

        assertEquals(defect, tracker.getDefect(defect.getId()));
    }

    @Test
    void gettingUnknownIdThrows() {
        assertThrows(DefectNotFoundException.class, () -> tracker.getDefect(999));
    }

    @Test
    void listDefectsReturnsAllCreatedDefectsInOrder() {
        Defect first = tracker.createDefect("A", "desc", "ziyanda", "editor", Severity.MINOR);
        Defect second = tracker.createDefect("B", "desc", "ziyanda", "server", Severity.CRITICAL);

        assertEquals(List.of(first, second), tracker.listDefects());
    }

    @Test
    void findByStatusFiltersCorrectly() {
        Defect logged = tracker.createDefect("A", "desc", "ziyanda", "editor", Severity.MINOR);
        Defect investigated = tracker.createDefect("B", "desc", "ziyanda", "server", Severity.CRITICAL);
        tracker.changeStatus(investigated.getId(), Status.UNDER_INVESTIGATION);

        assertEquals(List.of(logged), tracker.findByStatus(Status.LOGGED));
        assertEquals(List.of(investigated), tracker.findByStatus(Status.UNDER_INVESTIGATION));
    }

    @Test
    void findBySeverityFiltersCorrectly() {
        Defect minor = tracker.createDefect("A", "desc", "ziyanda", "editor", Severity.MINOR);
        Defect critical = tracker.createDefect("B", "desc", "ziyanda", "server", Severity.CRITICAL);

        assertEquals(List.of(critical), tracker.findBySeverity(Severity.CRITICAL));
        assertEquals(List.of(minor), tracker.findBySeverity(Severity.MINOR));
    }

    @Test
    void findByComponentFiltersCorrectly() {
        Defect editorBug = tracker.createDefect("A", "desc", "ziyanda", "editor", Severity.MINOR);
        tracker.createDefect("B", "desc", "ziyanda", "server", Severity.CRITICAL);

        assertEquals(List.of(editorBug), tracker.findByComponent("editor"));
    }

    @Test
    void changeStatusDelegatesToDefectTransitionRules() {
        Defect defect = tracker.createDefect("A", "desc", "ziyanda", "editor", Severity.MINOR);

        assertThrows(IllegalStatusTransitionException.class,
                () -> tracker.changeStatus(defect.getId(), Status.CLOSED));
    }

    @Test
    void recordSignOffSetsVerifiedBy() {
        Defect defect = tracker.createDefect("A", "desc", "ziyanda", "editor", Severity.MINOR);

        Defect signedOff = tracker.recordSignOff(defect.getId(), "qc-lead");

        assertEquals("qc-lead", signedOff.getVerifiedBy());
    }

    @Test
    void recordSignOffOnUnknownIdThrows() {
        assertThrows(DefectNotFoundException.class, () -> tracker.recordSignOff(999, "qc-lead"));
    }

    @Test
    void countByStatusGroupsCorrectly() {
        Defect first = tracker.createDefect("A", "desc", "ziyanda", "editor", Severity.MINOR);
        tracker.createDefect("B", "desc", "ziyanda", "server", Severity.CRITICAL);
        tracker.changeStatus(first.getId(), Status.UNDER_INVESTIGATION);

        Map<Status, Long> counts = tracker.countByStatus();

        assertEquals(1L, counts.get(Status.UNDER_INVESTIGATION));
        assertEquals(1L, counts.get(Status.LOGGED));
    }

    @Test
    void countBySeverityGroupsCorrectly() {
        tracker.createDefect("A", "desc", "ziyanda", "editor", Severity.MINOR);
        tracker.createDefect("B", "desc", "ziyanda", "server", Severity.CRITICAL);
        tracker.createDefect("C", "desc", "ziyanda", "server", Severity.CRITICAL);

        Map<Severity, Long> counts = tracker.countBySeverity();

        assertEquals(1L, counts.get(Severity.MINOR));
        assertEquals(2L, counts.get(Severity.CRITICAL));
    }

    @Test
    void findOpenCriticalsExcludesMinorAndMajor() {
        tracker.createDefect("A", "desc", "ziyanda", "editor", Severity.MINOR);
        tracker.createDefect("B", "desc", "ziyanda", "server", Severity.MAJOR);
        Defect critical = tracker.createDefect("C", "desc", "ziyanda", "server", Severity.CRITICAL);

        assertEquals(List.of(critical), tracker.findOpenCriticals());
    }

    @Test
    void findOpenCriticalsExcludesClosedAndRejected() {
        Defect rejected = tracker.createDefect("A", "desc", "ziyanda", "editor", Severity.CRITICAL);
        tracker.changeStatus(rejected.getId(), Status.REJECTED);

        Defect closed = tracker.createDefect("B", "desc", "ziyanda", "server", Severity.CRITICAL);
        tracker.changeStatus(closed.getId(), Status.UNDER_INVESTIGATION);
        tracker.changeStatus(closed.getId(), Status.CLASSIFIED);
        tracker.changeStatus(closed.getId(), Status.CORRECTIVE_ACTION);
        tracker.changeStatus(closed.getId(), Status.VERIFIED);
        tracker.recordSignOff(closed.getId(), "qc-lead");
        tracker.changeStatus(closed.getId(), Status.CLOSED);

        Defect stillOpen = tracker.createDefect("C", "desc", "ziyanda", "server", Severity.CRITICAL);

        assertEquals(List.of(stillOpen), tracker.findOpenCriticals());
    }
}
