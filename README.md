# Coal QC Bug Tracker

A Java bug-triage tracker modeled on coal analysis QC out-of-spec (OOS) handling workflows.

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
| Close out, sign-off, report to client | Bug closed, verified, release notes |

## Status model

```
LOGGED -> UNDER_INVESTIGATION -> CLASSIFIED -> CORRECTIVE_ACTION -> VERIFIED -> CLOSED
                                                                  \-> REJECTED
```

## Build

```bash
mvn test
```
