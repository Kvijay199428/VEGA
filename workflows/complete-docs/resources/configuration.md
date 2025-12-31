# Configuration Documentation

This document explains the configuration settings found in `src/main/resources/application.properties`.

## Server Configuration
| Property | Value | Description |
|----------|-------|-------------|
| `server.port` | `28021` | The port on which the Spring Boot application runs. Dedicated to MarketDataStreamerV3. |
| `server.servlet.context-path` | `/api` | Base path for all REST API endpoints. |
| `spring.application.name` | `vega-trader` | Name of the application. |

## Timezone
| Property | Value | Description |
|----------|-------|-------------|
| `application.timezone` | `Asia/Kolkata` | Application-wide timezone setting. |
| `spring.jackson.time-zone` | `Asia/Kolkata` | Timezone for JSON serialization/deserialization. |

## Database (SQLite)
| Property | Value | Description |
|----------|-------|-------------|
| `spring.datasource.url` | `jdbc:sqlite:database/vega_trade.db` | Path to the SQLite database file. |
| `spring.datasource.driver-class-name` | `org.sqlite.JDBC` | JDBC driver for SQLite. |
| `spring.jpa.database-platform` | `org.hibernate.community.dialect.SQLiteDialect` | Hibernate dialect for SQLite. |
| `spring.jpa.hibernate.ddl-auto` | `update` | Automatically updates the database schema matching the entities. |
| `spring.flyway.enabled` | `true` | Enables Flyway for database migrations. |
| `spring.flyway.locations` | `classpath:db/migration` | Location of migration scripts. |

## Upstox API Configuration
| Property | Description |
|----------|-------------|
| `upstox.base-url` | Base URL for Upstox API v2 (`https://api.upstox.com/v2`). |
| `upstox.auth-url` | URL for user authorization dialog. |
| `upstox.token-url` | URL for exchanging auth code for access token. |
| `upstox.redirect-uri` | Callback URL for OAuth flow (`http://localhost:28021/api/v1/auth/upstox/callback`). |

## Market Data & Streams
| Property | Value/Default | Description |
|----------|---------------|-------------|
| `market-data-streamer.enabled` | `true` | Enables the V3 Market Data Streamer. |
| `portfolio-streamer.enabled` | `true` | Enables the Portfolio Streamer (V2). |
| `marketdata.reconnect-delay-ms` | `5000` | Delay before attempting to reconnect WebSocket. |
| `marketdata.buffer-capacity` | `50000` | Capacity of the internal buffer for market updates. |

## Rate Limits (Token Bucket)
Implemented using Guava RateLimiter.
| Property | Limit (req/sec) | Description |
|----------|-----------------|-------------|
| `api.ratelimiter.standard.rate` | `50` | Standard API rate limit. |
| `api.ratelimiter.multiorder.rate` | `4` | Specific rate limit for multi-order operations. |

## Logging
- **Console Pattern**: `%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n`
- **WS Log File**: `d:/projects/VEGA TRADER/prompt/upstox_endpoints/market_data_feed_v3/log/ws.log`

## Security (JWT)
- **Secret**: Configured via `jwt.secret`. Must be at least 256-bit (32 chars).
- **Expiration**: `86400000` ms (24 hours).
