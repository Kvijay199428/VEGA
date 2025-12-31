# Admin SOP — Operations & Compliance

**Version:** 1.0  
**Last Updated:** 2025-12-30

---

## 1. Daily Operations Checklist

### Pre-Market (Before 9:00 AM IST)

- [ ] **Verify T-1 Instrument Ingestion**
  - Check ingestion logs for completion
  - Validate instrument count matches exchange feed
  - Confirm no ingestion errors

- [ ] **Confirm Expiry Calendar Updates**
  - Verify upcoming expiries are loaded
  - Check for holiday adjustments
  - Validate NSE/BSE calendars separately

- [ ] **Review Disabled Strikes List**
  - Check new disabled strikes from overnight job
  - Verify reasons are logged
  - Confirm RMS enforcement active

- [ ] **Validate RMS Health Checks**
  - All RMS services healthy
  - Cache layers populated
  - Broker connections verified

### Post-Market (After 3:30 PM IST)

- [ ] Run expiry rollover jobs
- [ ] Archive day's audit logs
- [ ] Backup instrument snapshots
- [ ] Run T-1 prewarm for next day

---

## 2. Change Management

### Required for All Configuration Changes

| Field | Requirement |
|-------|-------------|
| Change ID | Unique ticket number |
| Reason | Business/regulatory justification |
| Approval | Manager + Compliance signoff |
| Rollback Plan | Documented revert procedure |
| Effective Date | When change applies |

### Change Types

| Type | Approval Level |
|------|----------------|
| User Settings | Manager |
| System Defaults | Manager + Compliance |
| RMS Parameters | Compliance + Risk |
| Strike Disablement | Compliance |
| Broker Priority | Manager |

---

## 3. Incident Handling

### Severity Levels

| Level | Definition | Response Time |
|-------|------------|---------------|
| P1 | Trading halt, data loss | Immediate |
| P2 | Degraded trading | 15 minutes |
| P3 | Non-critical issues | 1 hour |
| P4 | Minor issues | Next business day |

### Incident Response Steps

1. **FREEZE** — Halt trading if instrument inconsistency detected
2. **PRESERVE** — Secure all audit logs immediately
3. **NOTIFY** — Alert compliance team
4. **INVESTIGATE** — Root cause analysis
5. **REMEDIATE** — Apply fix with change management
6. **DOCUMENT** — Full incident report

### Emergency Contacts

| Role | Responsibility |
|------|----------------|
| Trading Ops | Trading decisions |
| Tech Lead | System issues |
| Compliance Officer | Regulatory matters |
| Risk Manager | Risk-related incidents |

---

## 4. Audit Log Retention

| Log Type | Retention |
|----------|-----------|
| Order audit | 7 years |
| RMS rejections | 7 years |
| Admin actions | 7 years |
| Instrument snapshots | 5 years |
| Settings versions | 5 years |

---

## 5. Compliance Reporting

### Weekly Reports

- [ ] Order rejection summary
- [ ] Strike disablement activity
- [ ] Admin action audit
- [ ] System health metrics

### Monthly Reports

- [ ] Instrument lifecycle summary
- [ ] RMS enforcement statistics
- [ ] User settings usage patterns
- [ ] Regulatory compliance checklist

---

*Document Status: FINALIZED*
