package com.coalqc.api;

import com.coalqc.model.Severity;
import com.coalqc.service.DefectTracker;
import io.javalin.testtools.JavalinTest;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefectApiTest {

    @Test
    void createDefectReturns201WithBody() {
        var app = Main.createApp(new DefectTracker());

        JavalinTest.test(app, (server, client) -> {
            Response response = client.post("/defects", """
                    {"title":"Crash on save","description":"NPE","reportedBy":"ziyanda","component":"editor","severity":"MAJOR"}
                    """);

            assertEquals(201, response.code());
            String body = response.body().string();
            assertTrue(body.contains("\"title\":\"Crash on save\""));
            assertTrue(body.contains("\"status\":\"LOGGED\""));
        });
    }

    @Test
    void getUnknownDefectReturns404() {
        var app = Main.createApp(new DefectTracker());

        JavalinTest.test(app, (server, client) -> {
            Response response = client.get("/defects/999");

            assertEquals(404, response.code());
        });
    }

    @Test
    void getKnownDefectReturns200() {
        var tracker = new DefectTracker();
        var defect = tracker.createDefect("A", "d", "z", "editor", Severity.MINOR);
        var app = Main.createApp(tracker);

        JavalinTest.test(app, (server, client) -> {
            Response response = client.get("/defects/" + defect.getId());

            assertEquals(200, response.code());
            assertTrue(response.body().string().contains("\"title\":\"A\""));
        });
    }

    @Test
    void listDefectsFiltersByStatus() {
        var tracker = new DefectTracker();
        tracker.createDefect("A", "d", "z", "editor", Severity.MINOR);
        var app = Main.createApp(tracker);

        JavalinTest.test(app, (server, client) -> {
            Response response = client.get("/defects?status=LOGGED");

            assertEquals(200, response.code());
            assertTrue(response.body().string().contains("\"title\":\"A\""));
        });
    }

    @Test
    void listDefectsWithBogusSeverityReturns400() {
        var app = Main.createApp(new DefectTracker());

        JavalinTest.test(app, (server, client) -> {
            Response response = client.get("/defects?severity=NOT_A_SEVERITY");

            assertEquals(400, response.code());
        });
    }

    @Test
    void changeStatusHappyPath() {
        var tracker = new DefectTracker();
        var defect = tracker.createDefect("A", "d", "z", "editor", Severity.MINOR);
        var app = Main.createApp(tracker);

        JavalinTest.test(app, (server, client) -> {
            Response response = client.patch("/defects/" + defect.getId() + "/status", """
                    {"status":"UNDER_INVESTIGATION"}
                    """);

            assertEquals(200, response.code());
            assertTrue(response.body().string().contains("\"status\":\"UNDER_INVESTIGATION\""));
        });
    }

    @Test
    void changeStatusToIllegalTargetReturns400() {
        var tracker = new DefectTracker();
        var defect = tracker.createDefect("A", "d", "z", "editor", Severity.MINOR);
        var app = Main.createApp(tracker);

        JavalinTest.test(app, (server, client) -> {
            Response response = client.patch("/defects/" + defect.getId() + "/status", """
                    {"status":"CLOSED"}
                    """);

            assertEquals(400, response.code());
        });
    }

    @Test
    void changeStatusOnUnknownDefectReturns404() {
        var app = Main.createApp(new DefectTracker());

        JavalinTest.test(app, (server, client) -> {
            Response response = client.patch("/defects/999/status", """
                    {"status":"UNDER_INVESTIGATION"}
                    """);

            assertEquals(404, response.code());
        });
    }
}
