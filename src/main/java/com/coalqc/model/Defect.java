package com.coalqc.model;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

public class Defect {

    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);

    private final int id;
    private final String title;
    private final String description;
    private final String reportedBy;
    private final LocalDate dateLogged;
    private final String component;

    private Severity severity;
    private Status status;
    private String rootCause;
    private String correctiveAction;
    private String verifiedBy;
    private LocalDate dateClosed;

    public Defect(String title, String description, String reportedBy, String component, Severity severity) {
        this.id = NEXT_ID.getAndIncrement();
        this.title = title;
        this.description = description;
        this.reportedBy = reportedBy;
        this.component = component;
        this.dateLogged = LocalDate.now();
        this.severity = severity;
        this.status = Status.LOGGED;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public LocalDate getDateLogged() {
        return dateLogged;
    }

    public String getComponent() {
        return component;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public Status getStatus() {
        return status;
    }

    public void transitionTo(Status target) {
        if (!status.canTransitionTo(target)) {
            throw new IllegalStatusTransitionException(status, target);
        }
        if (target == Status.CLOSED && verifiedBy == null) {
            throw new IllegalStateException("Cannot close defect " + id + " without sign-off (verifiedBy)");
        }
        this.status = target;
        if (target == Status.CLOSED) {
            this.dateClosed = LocalDate.now();
        }
    }

    public String getRootCause() {
        return rootCause;
    }

    public void setRootCause(String rootCause) {
        this.rootCause = rootCause;
    }

    public String getCorrectiveAction() {
        return correctiveAction;
    }

    public void setCorrectiveAction(String correctiveAction) {
        this.correctiveAction = correctiveAction;
    }

    public String getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(String verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public LocalDate getDateClosed() {
        return dateClosed;
    }
}
