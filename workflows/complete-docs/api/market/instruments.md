# Instrument Module

The Instrument module provides search, resolution, and master data management services.

## Service: Instrument Search
**Class**: `com.vegatrader.upstox.api.instrument.search.InstrumentSearchService`

Handles symbol resolution and autocomplete features.

### Key Features
- **Key Resolution**: Resolves `SYMBOL` + `SEGMENT` + `TYPE` -> `INSTRUMENT_KEY`.
- **Autocomplete**: Efficient prefix-based search for UI dropdowns.
- **Overlay Data**: Enriches search results with:
  - `misAllowed` / `mtfEnabled`: Product eligibility.
  - `suspended`: Suspension status.
- **Options Discovery**: Fetch entire option chain concepts or expiry dates for an underlying.

### Search DTO
`InstrumentSearchResult` contains:
- `instrumentKey` (e.g., `NSE_EQ|INE123...`)
- `symbol`, `name`
- `expiry`, `strikePrice`, `lotSize`
- `misAllowed`, `mtfEnabled`, `suspended`

## Service: Instrument Enrollment
**Class**: `com.vegatrader.upstox.api.instrument.enrollment.InstrumentEnrollmentService`

Manages the lifecycle of instrument masters (Download -> Parse -> Persist).
Typically runs as a daily BOD (Beginning of Day) job to sync with Upstox masters.
