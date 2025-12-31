# Vega Trader Backend Documentation

Welcome to the comprehensive documentation for the Vega Trader Java Backend.

## Overview
This documentation covers the entire `backend/java/vega-trader` project, including configuration, API endpoints, and internal mechanisms.

## Directory Structure
- **[Resources & Configuration](resources/README.md)**: Configuration files, database schema, and environment settings.
- **[API Documentation](api/README.md)**: Detailed guide to all Upstox API integration endpoints.
- **[Internal Mechanisms](internal/README.md)**: Feature-specific documentation for Rate Limiting, Error Handling, and other internal systems.
  - [Rate Limiting](internal/rate-limiting.md)
  - [Error Handling](internal/error-handling.md)
  - [Sectoral Indices](internal/sectoral-indices.md)


## Getting Started
ensure you have `Java 21` and `Maven` installed.

1. **Configure Application**: Check `resources/configuration.md` to set up your `application.properties`.
2. **Database Setup**: Refer to `resources/database.md` for SQLite and Flyway migrations.
3. **Run Application**:
   ```bash
   mvn spring-boot:run
   ```
