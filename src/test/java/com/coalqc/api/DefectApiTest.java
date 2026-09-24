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

    // POST /defects

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
    void createDefectWithBlankTitleReturns400() {
        var app = Main.createApp(new DefectTracker());

        JavalinTest.test(app, (server, client) -> {
            Response response = client.post("/defects", """
                    {"title":"  ","description":"NPE","reportedBy":"ziyanda","component":"editor","severity":"MAJOR"}
                    """);

            assertEquals(400, response.code());
        });
    }

    @Test
    void createDefectWithMissingSeverityReturns400() {
        var app = Main.createApp(new DefectTracker());

        JavalinTest.test(app, (server, client) -> {
            Response response = client.post("/defects", """
                    {"title":"Crash on save","description":"NPE","reportedBy":"ziyanda","component":"editor"}
                    """);

            assertEquals(400, response.code());
        });
    }

    @Test
    void createDefectWithInvalidSeverityReturns400() {
        var app = Main.createApp(new DefectTracker());

        JavalinTest.test(app, (server, client) -> {
            Response response = client.post("/defects", """
                    {"title":"Crash on save","description":"NPE","reportedBy":"ziyanda","component":"editor","severity":"BOGUS"}
                    """);

            assertEquals(400, response.code());
        });
    }

    // GET /defects (list + filters)

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
    void listDefectsWithBogusSeverityReturns400() {
        var app = Main.createApp(new DefectTracker());

        JavalinTest.test(app, (server, client) -> {
            Response response = client.get("/defects?severity=NOT_A_SEVERITY");

            assertEquals(400, response.code());
        });
    }

    // GET /defects/summary

    @Test
    void summaryReturnsCountsByStatusAndSeverity() {
        var tracker = new DefectTracker();
        var first = tracker.createDefect("A", "d", "z", "editor", Severity.MINOR);
        tracker.createDefect("B", "d", "z", "server", Severity.CRITICAL);
        tracker.changeStatus(first.getId(), Status.UNDER_INVESTIGATION);
        var app = Main.createApp(tracker);

        JavalinTest.test(app, (server, client) -> {
            Response response = client.get("/defects/summary");

            assertEquals(200, response.code());
            String body = response.body().string();
            assertTrue(body.contains("\"UNDER_INVESTIGATION\":1"));
            assertTrue(body.contains("\"LOGGED\":1"));
            assertTrue(body.contains("\"MINOR\":1"));
            assertTrue(body.contains("\"CRITICAL\":1"));
            assertTrue(body.contains("\"averageDaysToClose\":0.0"));
        });
    }

    @Test
    void summaryOnEmptyTrackerReturnsEmptyCountsNotAnError() {
        var app = Main.createApp(new DefectTracker());

        JavalinTest.test(app, (server, client) -> {
            Response response = client.get("/defects/summary");

            assertEquals(200, response.code());
            String body = response.body().string();
            assertTrue(body.contains("\"countsByStatus\":{}"));
            assertTrue(body.contains("\"countsBySeverity\":{}"));
            assertTrue(body.contains("\"averageDaysToClose\":0.0"));
        });
    }

    // GET /defects/open-criticals

    @Test
    void openCriticalsReturnsOnlyUnresolvedCriticalDefects() {
        var tracker = new DefectTracker();
        tracker.createDefect("Critical batch failure", "d", "z", "ash-analysis", Severity.CRITICAL);
        tracker.createDefect("Minor drift", "d", "z", "moisture-analysis", Severity.MINOR);
        var app = Main.createApp(tracker);

        JavalinTest.test(app, (server, client) -> {
            Response response = client.get("/defects/open-criticals");

            assertEquals(200, response.code());
            String body = response.body().string();
            assertTrue(body.contains("\"title\":\"Critical batch failure\""));
            assertFalse(body.contains("\"title\":\"Minor drift\""));
        });
    }

    @Test
    void openCriticalsReturnsEmptyListWhenNoneAreCritical() {
        var tracker = new DefectTracker();
        tracker.createDefect("Minor drift", "d", "z", "moisture-analysis", Severity.MINOR);
        var app = Main.createApp(tracker);

        JavalinTest.test(app, (server, client) -> {
            Response response = client.get("/defects/open-criticals");

            assertEquals(200, response.code());
            assertEquals("[]", response.body().string());
        });
    }

    // GET /defects/{id}

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
    void getUnknownDefectReturns404() {
        var app = Main.createApp(new DefectTracker());

        JavalinTest.test(app, (server, client) -> {
            Response response = client.get("/defects/999");

            assertEquals(404, response.code());
        });
    }

    // PATCH /defects/{id}/status

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
    void changeStatusWithMissingStatusReturns400() {
        var tracker = new DefectTracker();
        var defect = tracker.createDefect("A", "d", "z", "editor", Severity.MINOR);
        var app = Main.createApp(tracker);

        JavalinTest.test(app, (server, client) -> {
            Response response = client.patch("/defects/" + defect.getId() + "/status", "{}");

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

    // PATCH /defects/{id}/verify

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
    void verifyWithBlankVerifiedByReturns400() {
        var tracker = new DefectTracker();
        var defect = tracker.createDefect("A", "d", "z", "editor", Severity.MINOR);
        var app = Main.createApp(tracker);

        JavalinTest.test(app, (server, client) -> {
            Response response = client.patch("/defects/" + defect.getId() + "/verify", """
                    {"verifiedBy":"  "}
                    """);

            assertEquals(400, response.code());
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

    // Full lifecycle

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
