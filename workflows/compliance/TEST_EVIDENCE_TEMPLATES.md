# Compliance Test Evidence Templates

**Version:** 1.0  
**Last Updated:** 2025-12-30

---

## Template 1 — Instrument Validity Test

| Field | Value |
|-------|-------|
| **Test Case ID** | IM-01 |
| **Test Name** | Instrument Eligibility Check |
| **Instrument** | NIFTY 19650 CE |
| **Expiry** | 2025-01-30 |
| **Expected Result** | Accepted |
| **Actual Result** | |
| **Timestamp** | |
| **Tester** | |
| **Reviewer** | |
| **Evidence** | [Screenshot/Log link] |

---

## Template 2 — RMS Rejection Evidence

| Field | Value |
|-------|-------|
| **Test Case ID** | RMS-01 |
| **Order ID** | |
| **Rejection Code** | RMS-QTY-001 |
| **Reason** | Quantity Freeze Exceeded |
| **Instrument** | |
| **Requested Qty** | |
| **Max Allowed** | |
| **Instrument Snapshot** | Attached |
| **Settings Version** | |
| **Timestamp** | |
| **Tester** | |
| **Reviewer** | |

---

## Template 3 — Settings Enforcement Test

| Field | Value |
|-------|-------|
| **Test Case ID** | SET-01 |
| **Test Name** | Settings Hierarchy Enforcement |
| **User Preference** | Broker Priority: [ZERODHA, UPSTOX] |
| **Expected Behavior** | Applied |
| **Override Attempt** | Regulatory Rule |
| **Result** | Rejected (preference applied, regulation honored) |
| **Audit Log ID** | |
| **Timestamp** | |
| **Tester** | |
| **Reviewer** | |

---

## Template 4 — Expiry Validation Test

| Field | Value |
|-------|-------|
| **Test Case ID** | EXP-01 |
| **Instrument** | NIFTY JAN FUT |
| **Calculated Expiry** | |
| **Exchange Published** | |
| **Match** | Yes/No |
| **Holiday Adjustment** | Applied/Not Needed |
| **Timestamp** | |
| **Tester** | |

---

## Template 5 — Strike Disablement Test

| Field | Value |
|-------|-------|
| **Test Case ID** | STK-01 |
| **Underlying** | NIFTY |
| **Strike** | 19000 |
| **Option Type** | CE |
| **Disabled Reason** | Zero OI + Not in scheme |
| **RMS Enforcement** | Order rejected with RMS-STRIKE-004 |
| **Audit Log** | |
| **Timestamp** | |
| **Tester** | |

---

## Template 6 — Audit Replay Test

| Field | Value |
|-------|-------|
| **Test Case ID** | AUD-01 |
| **Order ID** | |
| **Order Timestamp** | |
| **Replay Timestamp** | |
| **Reconstructed Components** | |
| - Instrument State | Match/Mismatch |
| - RMS Rules | Match/Mismatch |
| - User Settings | Match/Mismatch |
| - System Defaults | Match/Mismatch |
| **Full Replay** | Pass/Fail |
| **Tester** | |
| **Reviewer** | |

---

*Document Status: FINALIZED*
