# Option Chain - Admin & User SOPs

**Version:** 1.0  
**Last Updated:** 2025-12-30

---

## Part 1: Admin SOP

### 1.1 Daily Operations

#### Pre-Market (Before 9:00 AM IST)

- [ ] **Verify T-1 Pre-warm Completed**
  - Check scheduler logs for `PREWARM_COMPLETE`
  - Validate option chains for next-day expiry cached

- [ ] **Validate Token Health**
  - OPTIONCHAIN1 connectivity: ✅
  - OPTIONCHAIN2 connectivity: ✅
  - Rate limit headroom: >80%

- [ ] **Confirm Cache Status**
  - Cache hit rate: >70%
  - No stale data warnings

#### Post-Market (After 3:30 PM IST)

- [ ] Run expiry rollover cleanup
- [ ] Archive audit logs
- [ ] Trigger T-1 pre-warm for next day

---

### 1.2 Configuration Management

#### Global Settings

| Setting | Default | Admin Editable |
|---------|---------|----------------|
| Rate limit per token | 5/sec | ✅ |
| Cache TTL | 60 min | ✅ |
| BSE support | false | ✅ |
| Audit logging | true | ⚠️ (Compliance review) |

#### Token Management

- **Add Token**: Admin UI → Tokens → Add New
- **Rotate Token**: Swap priority in settings
- **Disable Token**: Mark inactive in registry

---

### 1.3 Audit Log Review

| Frequency | Action |
|-----------|--------|
| Daily | Check for API errors (5xx) |
| Weekly | Export audit summary |
| Monthly | Full audit review with compliance |

---

### 1.4 Incident Handling

| Severity | Definition | Response |
|----------|------------|----------|
| P1 | API completely down | Activate fallback, notify team |
| P2 | High rate limit hits | Rotate tokens |
| P3 | Cache miss spike | Investigate, prewarm |

---

## Part 2: User SOP

### 2.1 Accessing Option Chain

1. **Login** to User UI
2. **Navigate** to Option Chain page
3. **Select** symbol (e.g., NIFTY, BANKNIFTY)
4. **Choose** expiry date
5. **View** option chain table with Greeks

---

### 2.2 User Settings

#### What You Can Configure

| Setting | Description |
|---------|-------------|
| Fetch priority | CACHE → API order |
| Display preferences | Columns to show |
| Auto-refresh | Enable/disable |

#### What You Cannot Change

- Rate limits
- Token selection
- Audit logging
- Cache TTL

---

### 2.3 Exporting Data

- **CSV Export**: Click "Export" button
- **JSON Snapshot**: Available for audit-ready format
- **Historical**: Request from admin if needed

---

### 2.4 Troubleshooting

| Issue | Solution |
|-------|----------|
| No data displayed | Check symbol/expiry validity |
| Stale data | Clear cache (if allowed) |
| Slow loading | Check network, reduce columns |
| Error message | Note error code, contact support |

---

### 2.5 Compliance Reminders

> - All option chain requests are logged
> - Export data is timestamped and auditable
> - User settings cannot override regulatory controls

---

*Document Status: FINALIZED*
