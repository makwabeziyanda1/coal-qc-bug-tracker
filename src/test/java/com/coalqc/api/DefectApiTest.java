package com.coalqc.api;

import com.coalqc.model.Severity;
import com.coalqc.model.Status;
import com.coalqc.service.DefectTracker;
import io.javalin.testtools.JavalinTest;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    void listDefectsFiltersByComponent() {
        var tracker = new DefectTracker();
        tracker.createDefect("A", "d", "z", "editor", Severity.MINOR);
        tracker.createDefect("B", "d", "z", "server", Severity.MINOR);
        var app = Main.createApp(tracker);

        JavalinTest.test(app, (server, client) -> {
            Response response = client.get("/defects?component=server");

            assertEquals(200, response.code());
            String body = response.body().string();
            assertTrue(body.contains("\"title\":\"B\""));
            assertFalse(body.contains("\"title\":\"A\""));
        });
    }

    @Test
    void listDefectsWithNoFilterReturnsAll() {
        var tracker = new DefectTracker();
        tracker.createDefect("A", "d", "z", "editor", Severity.MINOR);
        tracker.createDefect("B", "d", "z", "server", Severity.CRITICAL);
        var app = Main.createApp(tracker);

        JavalinTest.test(app, (server, client) -> {
            Response response = client.get("/defects");

            assertEquals(200, response.code());
            String body = response.body().string();
            assertTrue(body.contains("\"title\":\"A\""));
            assertTrue(body.contains("\"title\":\"B\""));
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

    @Test
    void verifyEndpointSetsVerifiedBy() {
        var tracker = new DefectTracker();
        var defect = tracker.createDefect("A", "d", "z", "editor", Severity.MINOR);
        var app = Main.createApp(tracker);

        JavalinTest.test(app, (server, client) -> {
            Response response = client.patch("/defects/" + defect.getId() + "/verify", """
                    {"verifiedBy":"qc-lead"}
                    """);

            assertEquals(200, response.code());
            assertTrue(response.body().string().contains("\"verifiedBy\":\"qc-lead\""));
        });
    }

    @Test
    void verifyOnUnknownDefectReturns404() {
        var app = Main.createApp(new DefectTracker());

        JavalinTest.test(app, (server, client) -> {
            Response response = client.patch("/defects/999/verify", """
                    {"verifiedBy":"qc-lead"}
                    """);

            assertEquals(404, response.code());
        });
    }

    @Test
    void fullLifecycleReachesClosedAfterVerify() {
        var tracker = new DefectTracker();
        var defect = tracker.createDefect("A", "d", "z", "editor", Severity.MINOR);
        tracker.changeStatus(defect.getId(), Status.UNDER_INVESTIGATION);
        tracker.changeStatus(defect.getId(), Status.CLASSIFIED);
        tracker.changeStatus(defect.getId(), Status.CORRECTIVE_ACTION);
        tracker.changeStatus(defect.getId(), Status.VERIFIED);
        var app = Main.createApp(tracker);

        JavalinTest.test(app, (server, client) -> {
            Response beforeSignOff = client.patch("/defects/" + defect.getId() + "/status", """
                    {"status":"CLOSED"}
                    """);
            assertEquals(400, beforeSignOff.code());

            Response verifyResponse = client.patch("/defects/" + defect.getId() + "/verify", """
                    {"verifiedBy":"qc-lead"}
                    """);
            assertEquals(200, verifyResponse.code());

            Response afterSignOff = client.patch("/defects/" + defect.getId() + "/status", """
                    {"status":"CLOSED"}
                    """);
            assertEquals(200, afterSignOff.code());
            String body = afterSignOff.body().string();
            assertTrue(body.contains("\"status\":\"CLOSED\""));
            assertFalse(body.contains("\"dateClosed\":null"));
        });
    }
}
