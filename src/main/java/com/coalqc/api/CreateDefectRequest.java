package com.coalqc.api;

import com.coalqc.model.Severity;

public record CreateDefectRequest(String title, String description, String reportedBy,
                                   String component, Severity severity) {

    public CreateDefectRequest {
        requireNonBlank(title, "title");
        requireNonBlank(description, "description");
        requireNonBlank(reportedBy, "reportedBy");
        requireNonBlank(component, "component");
        if (severity == null) {
            throw new IllegalArgumentException("severity is required");
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}
