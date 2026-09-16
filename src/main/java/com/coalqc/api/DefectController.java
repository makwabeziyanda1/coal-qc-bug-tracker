package com.coalqc.api;

import com.coalqc.model.Defect;
import com.coalqc.model.Severity;
import com.coalqc.model.Status;
import com.coalqc.service.DefectTracker;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;

public class DefectController {

    private final DefectTracker tracker;

    public DefectController(DefectTracker tracker) {
        this.tracker = tracker;
    }

    public void registerRoutes(Javalin app) {
        app.post("/defects", this::create);
        app.get("/defects", this::list);
        app.get("/defects/{id}", this::get);
        app.patch("/defects/{id}/status", this::changeStatus);
    }

    private void create(Context ctx) {
        CreateDefectRequest request = ctx.bodyAsClass(CreateDefectRequest.class);
        Defect defect = tracker.createDefect(request.title(), request.description(),
                request.reportedBy(), request.component(), request.severity());
        ctx.status(201).json(defect);
    }

    private void list(Context ctx) {
        String status = ctx.queryParam("status");
        String severity = ctx.queryParam("severity");
        String component = ctx.queryParam("component");

        List<Defect> defects;
        if (status != null) {
            defects = tracker.findByStatus(Status.valueOf(status));
        } else if (severity != null) {
            defects = tracker.findBySeverity(Severity.valueOf(severity));
        } else if (component != null) {
            defects = tracker.findByComponent(component);
        } else {
            defects = tracker.listDefects();
        }
        ctx.json(defects);
    }

    private void get(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        ctx.json(tracker.getDefect(id));
    }

    private void changeStatus(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        StatusChangeRequest request = ctx.bodyAsClass(StatusChangeRequest.class);
        ctx.json(tracker.changeStatus(id, request.status()));
    }
}
