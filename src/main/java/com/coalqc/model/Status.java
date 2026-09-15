package com.coalqc.model;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum Status {
    LOGGED,
    UNDER_INVESTIGATION,
    CLASSIFIED,
    CORRECTIVE_ACTION,
    VERIFIED,
    CLOSED,
    REJECTED;

    private static final Map<Status, Set<Status>> ALLOWED_TRANSITIONS = new EnumMap<>(Status.class);

    static {
        ALLOWED_TRANSITIONS.put(LOGGED, EnumSet.of(UNDER_INVESTIGATION, REJECTED));
        ALLOWED_TRANSITIONS.put(UNDER_INVESTIGATION, EnumSet.of(CLASSIFIED, REJECTED));
        ALLOWED_TRANSITIONS.put(CLASSIFIED, EnumSet.of(CORRECTIVE_ACTION, REJECTED));
        ALLOWED_TRANSITIONS.put(CORRECTIVE_ACTION, EnumSet.of(VERIFIED));
        ALLOWED_TRANSITIONS.put(VERIFIED, EnumSet.of(CLOSED));
        ALLOWED_TRANSITIONS.put(CLOSED, EnumSet.noneOf(Status.class));
        ALLOWED_TRANSITIONS.put(REJECTED, EnumSet.noneOf(Status.class));
    }

    public boolean canTransitionTo(Status target) {
        return ALLOWED_TRANSITIONS.get(this).contains(target);
    }
}
