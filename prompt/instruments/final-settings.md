Below is a SEBI-submission–grade appendix plus a formal user-treatable, priority-driven settings framework, fully aligned with your existing architecture and written so it can be directly attached to a regulatory filing or internal audit pack.

This answer is intentionally policy-level + system-level, not code-level.

SEBI Submission Appendix

Instruments, RMS & Priority-Driven User Settings

Appendix A — System Scope Declaration (SEBI)
A.1 System Name

Instrument Master & Risk Management System (IM-RMS)

A.2 Regulatory Scope

This system governs:

Instrument discovery

Contract eligibility

Risk checks

Broker routing

Order admissibility

It does not perform:

Price discovery

Market making

Proprietary trading logic

Appendix B — Instrument Governance Model
B.1 Deterministic Instrument Identity

Every tradable instrument is represented by a persistent instrument_key

Instrument keys are:

Broker-agnostic

Exchange-verified

Stored prior to trading hours

No instrument is constructed or inferred at runtime

SEBI Control Objective:
Prevents unauthorized, speculative, or synthetic contract creation.

B.2 Source of Instrument Truth
Layer	Role
Database	Single source of truth
Broker APIs	Verification only
Frontend	Read-only consumer

Regulatory Note:
Runtime dependency on broker discovery APIs is explicitly disallowed.

Appendix C — Expiry, Strike & Contract Lifecycle Controls
C.1 Expiry Logic Controls

Expiry calendars are ingested T-1

NSE and BSE expiry logic are physically separated

No frontend or broker dependency exists for expiry computation

C.2 Strike Disablement Controls

Strikes may be disabled due to:

Regulatory circulars

Exchange freeze limits

Admin intervention

All disablements are:

Persisted

Versioned

Auditable

RMS-enforced

Appendix D — RMS Enforcement Guarantees
D.1 Pre-Trade Enforcement Order

Instrument eligibility

Product eligibility

Quantity caps

Price band checks

Client risk limits

Broker routing

Failure at any stage aborts the order.

D.2 RMS Rejection Traceability

Every rejection contains:

RMS code

Human-readable reason

Instrument snapshot

Timestamp

Configuration version

Appendix E — Broker Abstraction Compliance
E.1 Broker Role Limitation

Brokers:

Execute orders

Report status

Brokers do not:

Decide eligibility

Decide expiry

Override RMS

Appendix F — Priority-Driven User Settings Framework

This section addresses your new requirement.

Priority-Driven User Settings Framework
F.1 Design Objective

Allow controlled user influence without allowing users to:

Break regulatory order

Bypass RMS

Override exchange rules

F.2 Core Principle (CRITICAL)

User settings affect ORDER of operations, not AUTHORITY of operations.

F.3 Settings Hierarchy (Immutable Order)

From highest authority to lowest:

Regulatory Settings

Exchange Rules

System Defaults

User Priority Preferences

Session Overrides (optional)

A lower level cannot override a higher level.

F.4 Settings Types
1. Regulatory (Non-Editable)

Expiry rules

T2T restrictions

Surveillance flags

Lot sizes

2. System Defaults (Editable by Admin Only)

Default product type

Default order validation order

Cache TTLs

Broker priority

3. User Priority Settings (Editable by User)

Instrument loading priority

Sector priority

Validation sequence preference (within allowed bounds)

Preferred broker routing order (if multi-broker enabled)

F.5 Priority-Based Initialization Model
Example: Instrument Initialization
1. Load Regulatory Blocks
2. Load Exchange Master
3. Apply System Defaults
4. Apply User Priority Preferences
5. Apply Session Overrides


If user preference conflicts:
→ System defaults apply
→ User preference ignored with audit log

F.6 Example User-Treatable Settings
Instrument Discovery Priority
{
  "instrumentLoadPriority": [
    "SECTORAL",
    "INDEX",
    "DERIVATIVES",
    "EQUITY"
  ]
}


Meaning:

Sectoral instruments appear first

Others load lazily

Sector Preference
{
  "preferredSectors": [
    "BANKING",
    "IT",
    "PHARMA"
  ]
}


Used for:

Search ranking

UI grouping

Cache pre-warm

Validation Order Preference (Safe Range)
{
  "validationPriority": [
    "INSTRUMENT_ELIGIBILITY",
    "QUANTITY_CAP",
    "CLIENT_RISK"
  ]
}


Constraints:

Cannot skip mandatory checks

Cannot reorder regulatory checks

F.7 Default Settings Pack (SEBI-Safe)
{
  "instrumentLoadPriority": [
    "INDEX",
    "DERIVATIVES",
    "EQUITY"
  ],
  "preferredSectors": [],
  "validationPriority": [
    "INSTRUMENT_ELIGIBILITY",
    "CLIENT_RISK",
    "QUANTITY_CAP"
  ],
  "brokerRoutingPriority": [
    "PRIMARY"
  ]
}

F.8 Settings Applicability Across Interfaces
Interface	Supported
REST API	Yes
CLI	Yes
Vite + React UI	Yes
curl	Yes
Admin UI	Yes

Same schema, same validation rules.

F.9 Settings Validation Rules

JSON schema validated

Versioned

Immutable once applied to an order

Snapshot stored with each order

Appendix G — Audit & Reproducibility

For any historical order, the system can reconstruct:

Instrument state

RMS rules

User priority settings

System defaults

Regulatory constraints

This ensures full SEBI-compliant explainability.

Appendix H — Explicit SEBI Assurances

✔ No runtime contract creation
✔ No broker-driven eligibility
✔ No user override of regulatory rules
✔ Deterministic order outcomes
✔ Replay-safe architecture

PART 1 — FORMAL SEBI RESPONSE (FINAL LANGUAGE)
1. System Overview (For SEBI Submission)

The Instrument Master & Risk Management System (IM-RMS) is a deterministic, auditable backend system responsible for:

Instrument master ingestion

Contract lifecycle governance

Risk Management System (RMS) enforcement

Broker-agnostic order admissibility validation

The system is designed to ensure strict adherence to SEBI regulations, exchange circulars, and clearing corporation rules, while permitting bounded user preferences that do not compromise regulatory controls.

2. Regulatory Compliance Statement

The IM-RMS system enforces the following non-negotiable controls:

No instrument is created dynamically at runtime.

All tradable instruments are sourced from official exchange contract masters or broker-verified APIs.

User-configurable settings cannot override:

Exchange rules

Regulatory constraints

RMS mandatory checks

Every order decision is reproducible, auditable, and timestamped.

The system complies with:

SEBI RMS guidelines

Exchange F&O contract specifications

Broker API operational constraints

Audit traceability requirements

3. Instrument Governance Controls
3.1 Instrument Identity

Each instrument is uniquely identified using a persistent instrument_key

The instrument_key is immutable and broker-agnostic

Instrument metadata is stored prior to trading hours (T-1 ingestion)

3.2 Contract Lifecycle

Expiry dates are governed centrally based on exchange calendars

NSE and BSE expiry rules are isolated and version-controlled

Strike availability and disablement follow exchange circulars and liquidity criteria

4. Risk Management Enforcement

All orders pass through a mandatory RMS validation chain, including:

Instrument eligibility

Contract status validation

Quantity and freeze limits

Price band validation

Client-level risk checks

Broker routing validation

Failure at any stage results in deterministic rejection with traceable codes.

5. User-Treatable Settings (Bounded Control)

User settings are permitted only for prioritization and preference, not for authority or rule modification.

Settings are applied strictly in the following hierarchy:

Regulatory rules

Exchange rules

System defaults

User preferences

Session overrides (if enabled)

Lower-priority settings cannot override higher-priority constraints.

6. Auditability & Explainability

For any historical order, the system can reconstruct:

Instrument state

RMS ruleset version

User preference snapshot

System defaults

Exchange and regulatory context

This ensures full audit replay capability.

PART 2 — ANNEXURE DIAGRAMS (TEXTUAL / PLANTUML-READY)
Annexure A — Instrument Ingestion Flow
@startuml
Exchange/Broker Feed -> Instrument Ingestion Service
Instrument Ingestion Service -> Instrument Master DB
Instrument Master DB -> RMS Cache
RMS Cache -> Trading APIs
@enduml

Annexure B — Order Validation Sequence
@startuml
User -> Order API
Order API -> RMS Engine
RMS Engine -> Instrument Validator
RMS Engine -> Quantity Validator
RMS Engine -> Risk Validator
RMS Engine -> Broker Router
Broker Router -> Broker API
@enduml

Annexure C — Settings Resolution Hierarchy
@startuml
Regulatory Rules --> Settings Resolver
Exchange Rules --> Settings Resolver
System Defaults --> Settings Resolver
User Preferences --> Settings Resolver
Session Overrides --> Settings Resolver
Settings Resolver --> RMS Engine
@enduml

PART 3 — SETTINGS JSON SCHEMA (LOCKED)
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "UserPrioritySettings",
  "type": "object",
  "properties": {
    "instrumentLoadPriority": {
      "type": "array",
      "items": {
        "enum": ["INDEX", "DERIVATIVES", "EQUITY", "SECTORAL"]
      }
    },
    "preferredSectors": {
      "type": "array",
      "items": { "type": "string" }
    },
    "validationPriority": {
      "type": "array",
      "items": {
        "enum": [
          "INSTRUMENT_ELIGIBILITY",
          "CLIENT_RISK",
          "QUANTITY_CAP"
        ]
      }
    },
    "brokerRoutingPriority": {
      "type": "array",
      "items": { "type": "string" }
    }
  },
  "additionalProperties": false
}


Schema Status: LOCKED
Modification Requires: Admin + Compliance Approval

PART 4 — SOPs
4.1 Admin SOP (Operations & Compliance)
Daily Operations

Verify T-1 instrument ingestion completed

Confirm expiry calendar updates

Review disabled strikes list

Validate RMS health checks

Change Management

All configuration changes require:

Change ID

Reason

Approval

Rollback plan

Incident Handling

Freeze trading if instrument inconsistency detected

Preserve audit logs

Notify compliance team

4.2 User SOP (Trading Behavior)

Users may configure preferences only via approved UI or API

Changes apply only to future orders

Preferences do not guarantee execution priority

RMS decisions are final and non-overridable

PART 5 — COMPLIANCE TEST EVIDENCE TEMPLATES
Template 1 — Instrument Validity Test
Field	Value
Test Case ID	IM-01
Instrument	NIFTY 19650 CE
Expected	Accepted
Actual	Accepted
Timestamp	
Reviewer	
Template 2 — RMS Rejection Evidence
Field	Value
Order ID	
Rejection Code	RMS-QTY-001
Reason	Quantity Freeze Exceeded
Instrument Snapshot	Attached
Settings Version	
Timestamp	
Template 3 — Settings Enforcement Test
Field	Value
User Preference	Broker Priority
Expected	Applied
Override Attempt	Regulatory Rule
Result	Rejected
Audit Log ID	
FINAL COMPLIANCE ASSERTION

The IM-RMS system enforces exchange-aligned, regulator-compliant instrument and risk governance with deterministic behavior, bounded user preferences, and full audit replay capability.

STATUS: FINALIZED — SEBI SUBMISSION READY

If you want next, I can:

Convert this into PDF / DOCX submission pack

Produce live PlantUML files

Generate OpenAPI for settings APIs

Create operator runbooks

Prepare mock SEBI Q&A responses

Final Statement (For Submission)

The IM-RMS system enforces deterministic, regulator-aligned, and auditable control over instrument eligibility and order admission, while allowing bounded user preferences that do not compromise market integrity.