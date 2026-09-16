package com.coalqc.api;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.coalqc.service.DefectTracker;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;

public class Main {

    public static void main(String[] args) {
        createApp(new DefectTracker()).start(7000);
    }

    public static Javalin createApp(DefectTracker tracker) {
        Javalin app = Javalin.create(config -> config.jsonMapper(new JavalinJackson().updateMapper(mapper -> {
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        })));
        new DefectController(tracker).registerRoutes(app);
        ApiExceptionHandling.register(app);
        return app;
    }
}
