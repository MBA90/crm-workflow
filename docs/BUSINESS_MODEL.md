# CRM Workflow Service — Business Model & Solution Overview

*A configurable, multi-step approval engine that routes sensitive CRM actions through a defined chain of
approvers before they take effect.*

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [The Problem](#2-the-problem)
3. [The Solution](#3-the-solution)
4. [Business Value](#4-business-value)
5. [How It Works — The Approval Journey](#5-how-it-works--the-approval-journey)
6. [Core Concepts (Domain Model)](#6-core-concepts-domain-model)
7. [Approval Rules](#7-approval-rules)
8. [Definition Lifecycle & Versioning](#8-definition-lifecycle--versioning)
9. [Request & Step States](#9-request--step-states)
10. [Outcome Events & the Outbox](#10-outcome-events--the-outbox)
11. [Audit Trail](#11-audit-trail)
12. [REST API Surface](#12-rest-api-surface)
13. [Scope](#13-scope)
14. [Modeled but Not Yet Enforced by the Engine](#14-modeled-but-not-yet-enforced-by-the-engine)
15. [Glossary](#15-glossary)

---

## 1. Executive Summary

In a CRM, some actions are too consequential to happen on a single unchecked click — approving a refund,
updating a high-value deal, creating or updating an account. The **CRM Workflow Service** captures each such
action as a **request** and drives it through a **multi-step approval chain** that the business defines,
recording every decision along the way, and only reaching a final `APPROVED` or `REJECTED` outcome once the
required approvers have acted.

Approval logic lives in **data, not code**: business users create **workflow definitions** (versioned
templates of approval steps) through the API and activate them at runtime — no redeploy required to change
a policy.

| For Business Leaders | For Technical Teams |
|----------------------|---------------------|
| Route high-risk actions through the approvers you choose, in the order you choose. | A Spring Boot REST microservice over PostgreSQL, schema managed by Liquibase. |
| Change approval policy at runtime by publishing and activating a new definition version. | Approval policy is versioned data; definitions and steps are created and activated via API. |
| Get an append-only history of who decided what, and when. | Every state transition is written to an `approval_history` ledger in the same transaction. |
| Support single-approver, unanimous, and quorum decisions, including parallel steps. | Outcome events are written to a transactional `outbox` table for downstream consumption. |

---

## 2. The Problem

When high-risk CRM operations have no controlled approval path, the business is exposed:

- **Uncontrolled actions.** A single user can create or update sensitive records with no second pair of eyes.
- **Hard-coded rules.** Embedding approval logic in code makes every policy change a code change.
- **No record of decisions.** "Who approved this, and when?" has no reliable answer.
- **Inconsistent governance.** Different actions are approved in different, ad-hoc ways.

The business needs approval to be **centralized, configurable, versioned, and fully recorded**.

---

## 3. The Solution

The CRM Workflow Service is a dedicated **approval and governance layer**. Rather than changing sensitive
data directly, a caller records the intended change as a **request**; the request must pass through the
approval **steps** defined for its action before a final outcome is reached.

The service is **policy-driven**:

- **The business defines the rules** as *workflow definitions* — versioned templates listing the ordered
  approval steps for a given entity type and action.
- **The engine enforces them** — snapshotting the definition's steps onto each request, activating steps in
  order, collecting approver decisions, and recording every transition.
- **Downstream systems are notified** — when a request reaches `APPROVED` or `REJECTED`, the engine writes a
  corresponding event to a transactional outbox table for downstream consumption.

The engine is **payload-agnostic**: the proposed change travels with the request as an opaque JSON
**payload** that the service stores but never interprets, keeping the engine reusable across entities and
actions.

---

## 4. Business Value

| Value | What the code actually provides |
|-------|---------------------------------|
| **Controlled actions** | A request only reaches `APPROVED` after every ordered step is satisfied by the required approvers. |
| **Runtime-configurable policy** | Definitions and their steps are created via API and activated without redeploying the service. |
| **Safe policy evolution** | Definitions are versioned per entity type + action; activating a new version deactivates the previous active one. |
| **Recorded accountability** | Each request-, step-, and decision-level transition is written to an append-only `approval_history` ledger. |
| **Flexible decision rules** | Steps support single-approver, unanimous (all), and quorum (N approving votes) rules, and steps sharing an order run in parallel. |
| **Concurrency safety** | For an action tied to an existing entity, a new request cannot be submitted while another request for that same entity is in progress. |

---

## 5. How It Works — The Approval Journey

```
  Caller                    Workflow Engine                     Approvers
     │                            │                                 │
     │  create request (DRAFT)    │                                 │
     │ ─────────────────────────▶ │  Validate: definition.action    │
     │                            │  matches request.action         │
     │                            │                                 │
     │  submit request            │                                 │
     │ ─────────────────────────▶ │  Snapshot the definition's      │
     │                            │  steps onto the request;        │
     │                            │  compute each step's SLA due-at; │
     │                            │  activate the first step order   │
     │                            │  → request becomes IN_PROGRESS   │
     │                            │                                 │
     │                            │  decide(step) ────────────────▶ │  approver votes approve / reject
     │                            │ ◀────────────────────────────── │
     │                            │  record the vote & history;      │
     │                            │  advance to the next step order  │
     │                            │  when the current one is met     │
     │                            │                                 │
     │ ◀── final outcome ──────── │  On last step approved →         │
     │     (outbox row written)   │  APPROVED (+ WORKFLOW_APPROVED)  │
     │                            │  On any reject →                 │
     │                            │  REJECTED (+ WORKFLOW_REJECTED)  │
```

1. **Create.** A caller creates a request against a specific `definitionId`, supplying the entity type,
   optional entity id, action, opaque payload, and the requester's id and name. The engine verifies the
   definition's action matches the request's action, and stores the request as `DRAFT`.
2. **Submit.** On submit, the engine loads the definition's steps (ordered by step order) and **snapshots**
   them onto the request as request steps. Each step's SLA due-at is computed as *submit time + the step's
   SLA hours*. Steps at the first (lowest) order become `ACTIVE`; the rest become `PENDING`. The request
   becomes `IN_PROGRESS`. If the request targets an existing entity, submission is blocked while another
   request for that same entity is already in progress.
3. **Decide.** An approver decides an `ACTIVE` step. Each decision is recorded as an individual vote; a given
   approver may vote at most once per step. A rejection immediately ends the request as `REJECTED`. An
   approval satisfies the step according to its rule (see [Approval Rules](#7-approval-rules)); when all steps
   at the current order are satisfied, the next-higher order's steps are activated.
4. **Outcome.** When the last step order is satisfied, the request becomes `APPROVED`. On approval or
   rejection, the engine writes a `WORKFLOW_APPROVED` or `WORKFLOW_REJECTED` row to the outbox table.

---

## 6. Core Concepts (Domain Model)

| Concept | What it represents in the code |
|---------|--------------------------------|
| **Workflow Definition** | A versioned template keyed by entity type + action (unique on entity type + action + version). Carries a name, version, and an active flag. |
| **Definition Step** | An ordered step of a definition: step order, name, required approver role, approval type, optional quorum count, an optional JSON condition, SLA hours, an on-reject action, and an optional return-to-step target. |
| **Workflow Request** | A single action awaiting approval: its definition, entity type, optional entity id, action, opaque JSON payload, overall status, current step pointer, requester id/name, and timestamps. |
| **Request Step** | A per-request snapshot of a definition step, carrying its own status, SLA due-at, and the decider snapshot once decided. |
| **Step Decision** | One approver's vote (approved or rejected) on a request step, unique per step + approver. |
| **Approval History** | An append-only record of a status transition — request id, optional step id, actor id/name, from/to status, optional comment, and timestamp. |
| **Outbox Event** | A row written when a request reaches `APPROVED` or `REJECTED`, carrying an event type and a JSON payload, for downstream consumption. |

### Relationship at a glance

```
Workflow Definition (versioned template; one active per entity type + action)
   └── Definition Steps (ordered: role · approval type · SLA hours · on-reject)

              │  snapshotted onto the request at submit time
              ▼

Workflow Request (a single action awaiting approval; carries the opaque payload)
   ├── Request Steps (snapshot of each definition step, with live status)
   │      └── Step Decisions (one vote per approver per step)
   ├── Approval History (append-only transition ledger)
   └── Outbox Events (WORKFLOW_APPROVED / WORKFLOW_REJECTED on terminal outcome)
```

---

## 7. Approval Rules

Each step declares an **approval type** that determines when it is satisfied. Steps that share the same
**step order** run in **parallel**; the engine advances to the next-higher order only once the current order
is satisfied.

| Approval type | How the engine satisfies it |
|---------------|-----------------------------|
| **SINGLE** | The step is satisfied by one approving decision. |
| **ALL** | Every request step at that step order must be `APPROVED` before the order advances. |
| **QUORUM** | The step is satisfied once the count of approving votes on it reaches its configured `quorumCount`. |

In all cases, **a single rejection on any active step immediately ends the whole request as `REJECTED`.**

---

## 8. Definition Lifecycle & Versioning

- **Creation.** Creating a definition automatically assigns the **next version number** for that entity type
  and action (starting at 1). A newly created definition is **inactive** by default.
- **Validation on create.** A `QUORUM` step must specify a `quorumCount`; a step whose on-reject action is
  `RETURN_TO_STEP` must specify a `returnToStep`. Otherwise creation is rejected.
- **Activation.** Activating a definition **deactivates the currently active definition** for the same entity
  type and action, so at most one version is active at a time.
- **Lookup.** Callers can fetch a definition by id, list all versions for an entity type + action (newest
  first), or fetch the single active definition for an entity type + action.

---

## 9. Request & Step States

**Overall request status** — the values the engine actually transitions through:

```
DRAFT ──submit──▶ IN_PROGRESS ──┬── all steps approved ──▶ APPROVED
                                └── any step rejected  ──▶ REJECTED
```

- **DRAFT** — created, not yet submitted.
- **IN_PROGRESS** — submitted; moving through its steps.
- **APPROVED** — the last step order was satisfied; `completedAt` is set and `WORKFLOW_APPROVED` is written.
- **REJECTED** — an approver rejected an active step; `completedAt` is set and `WORKFLOW_REJECTED` is written.

*(A `CANCELLED` status exists in the model but is not reached by any current operation.)*

**Request step status:**

```
PENDING ──order becomes current──▶ ACTIVE ──┬── satisfied ──▶ APPROVED
                                            └── rejected  ──▶ REJECTED
```

- **PENDING** — snapshotted but not yet active.
- **ACTIVE** — at the request's current step order; open for decisions.
- **APPROVED** / **REJECTED** — decided.

*(A `SKIPPED` step status exists in the model but is not set by any current operation.)*

---

## 10. Outcome Events & the Outbox

When a request reaches a terminal outcome, the engine writes an event row to the **`outbox`** table **in the
same database transaction** as the state change:

| Event type | Written when |
|------------|--------------|
| **WORKFLOW_APPROVED** | The request becomes `APPROVED`. |
| **WORKFLOW_REJECTED** | The request becomes `REJECTED`. |

Each event carries a JSON payload with the request id, entity type, entity id (when present), action, and the
timestamp, and is stored with `published = false`. This is the **write side** of the transactional-outbox
pattern: outcome events are captured durably alongside the decision. A query to read unpublished events
(`published = false`, oldest first) is available; **a publisher/relay that delivers these events to an
external broker and marks them published is not part of this service today.**

---

## 11. Audit Trail

Every meaningful transition is written to the **`approval_history`** table as a new row: the request id, the
optional request-step id, the actor's id and name, the from- and to-status, an optional comment, and the
timestamp. History rows are only ever inserted — creation, submission, each step decision, step activation,
and each terminal outcome all append their own entry — producing an append-only, time-ordered record of how
each request was decided.

---

## 12. REST API Surface

**Workflow Definitions** — `/api/workflow-definitions`

| Method & path | Purpose |
|---------------|---------|
| `POST /` | Create a definition (with its steps); assigned the next version, created inactive. |
| `GET /{definitionId}` | Fetch a definition and its steps. |
| `GET /?entityType=&action=` | List all versions for an entity type + action (newest first). |
| `GET /active?entityType=&action=` | Fetch the single active definition for an entity type + action. |
| `POST /{definitionId}/activate` | Activate this definition (deactivating the previously active one). |

**Workflow Requests** — `/api/workflow-requests`

| Method & path | Purpose |
|---------------|---------|
| `POST /` | Create a request in `DRAFT` against a given definition. |
| `POST /{requestId}/submit` | Submit a draft: snapshot steps, activate the first order, move to `IN_PROGRESS`. |
| `GET /{requestId}` | Fetch a request and its steps. |
| `GET /?statuses=` | List requests filtered by overall status. |
| `POST /steps/{requestStepId}/decide` | Record an approve/reject decision on an active step. |

---

## 13. Scope

Definitions are keyed by an **entity type** (`ACCOUNT`, `DEAL`, `REFUND`) and an **action** (`CREATE`,
`UPDATE`, `DELETE`). The database seeds the following definitions:

| Entity type | Action | Definition |
|-------------|--------|------------|
| ACCOUNT | CREATE | Account Creation Approval |
| ACCOUNT | UPDATE | Account Update Approval |
| DEAL | UPDATE | Deal Update Approval |
| REFUND | CREATE | Refund Approval |

Because policy is data, additional definitions for these entity types and actions can be created and
activated through the API without changing code.

---

## 14. Modeled but Not Yet Enforced by the Engine

For transparency, several fields exist in the data model but are **not currently acted upon** by the service
logic. They are captured here so the document does not overstate current behavior:

| Modeled element | Current status |
|-----------------|----------------|
| **SLA escalation** | Each step's SLA due-at is computed and stored, and an `escalatedAt` column exists, but no process escalates overdue steps. |
| **`RETURN_TO_STEP` on reject** | The on-reject action and return-to-step target are stored and validated, but at runtime any rejection ends the request as `REJECTED`. |
| **Conditional step skips** | A per-step JSON `condition` is stored, but it is not evaluated and no step is skipped. |
| **Delegation / assignment** | `assignedTo` and `delegateTo` columns exist on request steps but are not set or used by any operation. |
| **`CANCELLED` / `SKIPPED` states** | Defined in the model but not produced by any current operation. |
| **Outbox publishing** | Outcome events are written to the outbox, but no relay publishes them to an external broker. |
| **Identity enforcement** | Actor and approver ids and roles are stored (intended to hold identity-provider subjects and role names), but the service does not authenticate callers or enforce roles; these values are supplied by the caller. |

---

## 15. Glossary

| Term | Definition |
|------|------------|
| **Definition** | A versioned template listing the ordered approval steps for an entity type + action. |
| **Request** | A single action awaiting approval, carrying the opaque proposed change (payload). |
| **Step** | One stage in an approval flow, owned by a role and governed by an approval type. |
| **Payload** | The opaque JSON proposed-change document the engine stores but never interprets. |
| **Approval type** | The rule that decides when a step is satisfied: `SINGLE`, `ALL`, or `QUORUM`. |
| **Step order** | The integer ordering of steps; steps sharing an order run in parallel. |
| **SLA hours / due-at** | A step's allowed duration, and the resulting deadline computed at submit time. |
| **Outbox** | A table of outcome events written in the same transaction as the state change, for downstream consumption. |

---

*This document describes the current business capabilities of the CRM Workflow Service as implemented in the
codebase. For setup, configuration, and operational details, see the [README](../README.md).*
