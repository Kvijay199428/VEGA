# User SOP — Trading Behavior

**Version:** 1.0  
**Last Updated:** 2025-12-30

---

## 1. Overview

This document outlines user responsibilities and expected behavior when interacting with the VEGA Trader platform.

---

## 2. User Settings

### What You Can Configure

| Setting | Description |
|---------|-------------|
| Instrument Load Priority | Order in which instrument categories appear |
| Preferred Sectors | Sectors highlighted in search/display |
| Broker Routing Priority | Preferred broker order (if multi-broker enabled) |
| Default Product Type | INTRADAY, DELIVERY, CNC, MIS |
| Confirmation Settings | Require confirmation before order placement |

### What You Cannot Change

- Exchange rules
- Expiry dates
- Strike availability
- Lot sizes
- Price bands
- RMS limits

---

## 3. Order Placement Rules

### Before You Place an Order

1. ✅ Verify instrument is eligible
2. ✅ Check available margin
3. ✅ Confirm quantity is within limits
4. ✅ Review price is within band

### After You Place an Order

- Order is validated through RMS
- If rejected, check rejection code and reason
- **RMS decisions are final and non-overridable**

---

## 4. Settings Application

### When Settings Apply

- Changes apply only to **future orders**
- In-flight orders use settings at submission time
- Session overrides reset at logout

### Settings Do NOT Guarantee

- Execution priority
- Order acceptance
- Price improvement
- Broker availability

---

## 5. Common Rejection Codes

| Code | Meaning | Action |
|------|---------|--------|
| RMS_QTY_CAP | Quantity exceeds limit | Reduce quantity |
| RMS_PRICE_BAND | Price outside limits | Adjust price |
| RMS_STRIKE_DISABLED | Strike not available | Choose different strike |
| RMS_EXPIRY_INVALID | Contract expired | Select valid expiry |
| RMS_CLIENT_MARGIN | Insufficient margin | Add funds |

---

## 6. Support

For issues with:
- **Order rejections** — Review rejection code first
- **Settings not applying** — Ensure you saved changes
- **Missing instruments** — May be post-ingestion; wait for next cycle
- **System errors** — Contact technical support

---

## 7. Compliance Reminder

> All trading activity is logged and auditable. User preferences cannot override regulatory or exchange rules.

---

*Document Status: FINALIZED*
