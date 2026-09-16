package com.coalqc.service;

import com.coalqc.model.Defect;
import com.coalqc.model.Severity;
import com.coalqc.model.Status;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DefectTracker {

    private final Map<Integer, Defect> defects = new LinkedHashMap<>();

    public Defect createDefect(String title, String description, String reportedBy,
                                String component, Severity severity) {
        Defect defect = new Defect(title, description, reportedBy, component, severity);
        defects.put(defect.getId(), defect);
        return defect;
    }

    public Defect getDefect(int id) {
        Defect defect = defects.get(id);
        if (defect == null) {
            throw new DefectNotFoundException(id);
        }
        return defect;
    }

    public List<Defect> listDefects() {
        return List.copyOf(defects.values());
    }

    public List<Defect> findByStatus(Status status) {
        return defects.values().stream()
                .filter(defect -> defect.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<Defect> findBySeverity(Severity severity) {
        return defects.values().stream()
                .filter(defect -> defect.getSeverity() == severity)
                .collect(Collectors.toList());
    }

    public List<Defect> findByComponent(String component) {
        return defects.values().stream()
                .filter(defect -> defect.getComponent().equals(component))
                .collect(Collectors.toList());
    }

    public Defect changeStatus(int id, Status newStatus) {
        Defect defect = getDefect(id);
        defect.transitionTo(newStatus);
        return defect;
    }

    public Defect recordSignOff(int id, String verifiedBy) {
        Defect defect = getDefect(id);
        defect.setVerifiedBy(verifiedBy);
        return defect;
    }
}
