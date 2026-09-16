package com.coalqc.api;

import com.coalqc.model.Defect;
import com.coalqc.service.DefectTracker;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class DefectController {

    private final DefectTracker tracker;

    public DefectController(DefectTracker tracker) {
        this.tracker = tracker;
    }

    public void registerRoutes(Javalin app) {
        app.post("/defects", this::create);
    }

    private void create(Context ctx) {
        CreateDefectRequest request = ctx.bodyAsClass(CreateDefectRequest.class);
        Defect defect = tracker.createDefect(request.title(), request.description(),
                request.reportedBy(), request.component(), request.severity());
        ctx.status(201).json(defect);
    }
}
