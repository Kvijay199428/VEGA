# Database Documentation

The application uses **SQLite** as the primary data store, managed by **Flyway** for schema migrations.

## Database File
- **Location**: `database/vega_trade.db` (Relative to project root)
- **Dialect**: `SQLiteDialect`
- **Driver**: `org.sqlite.JDBC`

## Schema Migrations (Flyway)
Format: `V<Version>__<Description>.sql` located in `src/main/resources/db/migration`.

### Core Schema
| Version | Description | Purpose |
|---------|-------------|---------|
| `V2` | `create_audit_tables.sql` | Tables for system auditing. |
| `V3` | `create_admin_tables.sql` | Admin user and role management. |
| `V31` | `user_settings.sql` | User-specific preference storage. |
| `V32` | `settings_metadata.sql` | Metadata for configuration settings. |
| `V42` | `user_profile_snapshot.sql` | Snapshots of user profile data. |
| `V44` | `settings_admin.sql` | Administrative settings. |
| `V45` | `settings_definition.sql` | Definitions for dynamic settings. |

### Instrument & Market Data
| Version | Description | Purpose |
|---------|-------------|---------|
| `V10` | `instrument_master.sql` | Master list of all tradable instruments. |
| `V11` | `instrument_overlays.sql` | Overlay data for instruments. |
| `V12` | `product_risk_profile.sql` | Risk profiles associated with products. |
| `V27` | `sector_master.sql` | Sector definitions. |
| `V28` | `index_master.sql` | Market index definitions. |
| `V29` | `index_constituent.sql` | Mapping of instruments to indices. |
| `V41` | `option_chain.sql` | Storage for option chain snapshots. |

### Risk Management System (RMS)
| Version | Description | Purpose |
|---------|-------------|---------|
| `V23` | `client_risk_limits.sql` | Risk limits per client. |
| `V24` | `client_risk_state.sql` | Current risk state of clients. |
| `V30` | `sector_risk_limit.sql` | Risk limits per sector. |
| `V37` | `rms_rejection_code.sql` | Codes for RMS order rejections. |

### Orders & Trades
| Version | Description | Purpose |
|---------|-------------|---------|
| `V46` | `orders_core.sql` | Core order table. |
| `V47` | `order_charges.sql` | Charges/Taxes associated with orders. |
| `V48` | `order_latency_events.sql` | Latency tracking for HFT analysis. |
| `V49` | `trades_settlements.sql` | Trade execution and settlement data. |
| `V50` | `pnl.sql` | Profit and Loss calculations. |

### Configuration & Rules
- **Brokers**: `V25` (Broker Registry), `V26` (Broker Symbol Mapping).
- **Expiry**: `V22` (FO Lifecycle), `V34` (Exchange Expiry Rules), `V33` (Expired Instruments).
- **BSE**: `V36` (BSE Group Rules).
- **Price Bands**: `V21` (Price Band configuration).
