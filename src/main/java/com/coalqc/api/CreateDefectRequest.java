package com.coalqc.api;

import com.coalqc.model.Severity;

public record CreateDefectRequest(String title, String description, String reportedBy,
                                   String component, Severity severity) {
}
