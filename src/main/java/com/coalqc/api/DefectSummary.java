package com.coalqc.api;

import com.coalqc.model.Severity;
import com.coalqc.model.Status;

import java.util.Map;

public record DefectSummary(Map<Status, Long> countsByStatus, Map<Severity, Long> countsBySeverity) {
}
