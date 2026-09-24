# Coal QC Bug Tracker

A Java REST API for tracking bugs, modeled on coal analysis QC out-of-spec (OOS)
handling workflows rather than a generic issue tracker.

## Demo video

_TODO: link the 5-10 min walkthrough here._

## Concept

| Coal Analysis QC | Bug Tracker Equivalent |
|---|---|
| Sample logged in (batch/lot number, source) | Bug logged (ID, reporter, module/component) |
| Test run (ash content, calorific value, moisture, sulfur, etc.) | Defect reported with symptoms/steps to reproduce |
| Result flagged as out-of-spec (OOS) | Bug flagged as valid/confirmed |
| Initial assessment — real deviation or lab/testing error? | Triage — real bug or user error / duplicate? |
| Classify severity (minor spec drift vs. major contamination risk) | Classify severity/priority (cosmetic → critical) |
| Root cause investigation | Root cause analysis / assigned to dev |
| Corrective action (CA) — retest, adjust process | Fix implemented |
| Preventive action (PA) — prevent recurrence | Regression test added |
| Sign-off, close out, report to client | QC lead verifies, bug closed |

The state machine and its sign-off gate (below) are the point of the project:
a defect can't be closed just because someone flips a flag, the same way a QC
result can't be signed off without someone accountable for it.

## Status model

```
LOGGED ────────────────┐
  │                     │
  ▼                     │
UNDER_INVESTIGATION ────┤
  │                     ├──> REJECTED   (lab/testing error, not a real deviation)
  ▼                     │
CLASSIFIED ─────────────┘
  │
  ▼
CORRECTIVE_ACTION
  │
  ▼
VERIFIED ──(sign-off: verifiedBy set)──> CLOSED
```

Rules enforced by `Defect`/`Status`, not just described here:
- Only the moves drawn above are legal; anything else (e.g. `LOGGED` straight to
  `CLOSED`) throws `IllegalStatusTransitionException` (400 at the API).
- `REJECTED` is only reachable from the early states — once corrective action
  has started, a defect can't retroactively turn out to be a false alarm.
- A defect can't reach `CLOSED` without `verifiedBy` being set first (via
  `PATCH /defects/{id}/verify`) — mirrors a QC sign-off, not just a status flag.
- `dateClosed` is stamped automatically when a defect closes.

## REST API

Base URL: `http://localhost:7000`

| Method | Path | Body | Success | Errors |
|---|---|---|---|---|
| `POST` | `/defects` | `{title, description, reportedBy, component, severity}` | `201` + defect | `400` blank/missing field, missing or invalid `severity` |
| `GET` | `/defects` | — (optional `?status=`, `?severity=`, `?component=`) | `200` + list | `400` invalid `status`/`severity` value |
| `GET` | `/defects/summary` | — | `200` + `{countsByStatus, countsBySeverity, averageDaysToClose}` | — |
| `GET` | `/defects/open-criticals` | — | `200` + list of CRITICAL defects not CLOSED/REJECTED | — |
| `GET` | `/defects/{id}` | — | `200` + defect | `404` unknown id |
| `PATCH` | `/defects/{id}/status` | `{status}` | `200` + defect | `400` illegal transition, missing sign-off, missing/invalid `status`; `404` unknown id |
| `PATCH` | `/defects/{id}/verify` | `{verifiedBy}` | `200` + defect | `400` blank `verifiedBy`; `404` unknown id |

`Severity` is one of `MINOR`, `MAJOR`, `CRITICAL`. `Status` is one of the values
in the diagram above.

### Example: full lifecycle

```bash
# Create a defect
curl -X POST http://localhost:7000/defects \
  -H "Content-Type: application/json" \
  -d '{"title":"Ash content out of spec on batch B-311","description":"18.2% vs 14% max spec","reportedBy":"lab-analyst-3","component":"ash-analysis","severity":"CRITICAL"}'

# Illegal transition -- rejected, 400
curl -X PATCH http://localhost:7000/defects/1/status \
  -H "Content-Type: application/json" -d '{"status":"CLOSED"}'

# Walk the legal path
curl -X PATCH http://localhost:7000/defects/1/status -H "Content-Type: application/json" -d '{"status":"UNDER_INVESTIGATION"}'
curl -X PATCH http://localhost:7000/defects/1/status -H "Content-Type: application/json" -d '{"status":"CLASSIFIED"}'
curl -X PATCH http://localhost:7000/defects/1/status -H "Content-Type: application/json" -d '{"status":"CORRECTIVE_ACTION"}'
curl -X PATCH http://localhost:7000/defects/1/status -H "Content-Type: application/json" -d '{"status":"VERIFIED"}'

# Still can't close -- no sign-off yet, 400
curl -X PATCH http://localhost:7000/defects/1/status \
  -H "Content-Type: application/json" -d '{"status":"CLOSED"}'

# Sign off
curl -X PATCH http://localhost:7000/defects/1/verify \
  -H "Content-Type: application/json" -d '{"verifiedBy":"qc-lead-thandi"}'

# Now it closes -- dateClosed gets stamped automatically
curl -X PATCH http://localhost:7000/defects/1/status \
  -H "Content-Type: application/json" -d '{"status":"CLOSED"}'

# Reporting
curl http://localhost:7000/defects/summary
curl http://localhost:7000/defects/open-criticals
```

## Running it

```bash
mvn package -DskipTests
java -jar target/coal-qc-bug-tracker-1.0-SNAPSHOT.jar
```

Server listens on port 7000.

## Testing

```bash
mvn test
```

53 tests: the status transition graph (exhaustively — including that no status
can transition to itself and intermediate steps can't be skipped), the
`Defect`/`DefectTracker` domain and service layers (including the summary
counts, open-criticals filter, and average-time-to-close aggregation), and
REST integration tests covering every endpoint's happy path, validation,
error responses, and empty-state edge cases.

## Known limitations

- In-memory storage only — data doesn't survive a restart.
- No authentication — any caller can create or transition any defect.
- No pagination on `GET /defects`.

None of these were needed to demonstrate the triage/state-machine domain, but
a production version would need all three.

## Verification

WTC-J9T6L7HB
