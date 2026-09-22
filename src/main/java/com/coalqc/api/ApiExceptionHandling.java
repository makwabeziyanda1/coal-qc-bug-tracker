package com.coalqc.api;

import com.coalqc.model.IllegalStatusTransitionException;
import com.coalqc.service.DefectNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.javalin.Javalin;

import java.util.Map;

public class ApiExceptionHandling {

    public static void register(Javalin app) {
        app.exception(DefectNotFoundException.class, (e, ctx) ->
                ctx.status(404).json(Map.of("error", e.getMessage())));

        app.exception(IllegalStatusTransitionException.class, (e, ctx) ->
                ctx.status(400).json(Map.of("error", e.getMessage())));

        app.exception(IllegalStateException.class, (e, ctx) ->
                ctx.status(400).json(Map.of("error", e.getMessage())));

        app.exception(IllegalArgumentException.class, (e, ctx) ->
                ctx.status(400).json(Map.of("error", e.getMessage())));

        app.exception(JsonProcessingException.class, (e, ctx) -> {
            Throwable cause = e.getCause();
            String message = cause instanceof IllegalArgumentException ? cause.getMessage() : "Malformed request body";
            ctx.status(400).json(Map.of("error", message));
        });
    }
}
