# Sectoral Indices

The internal Sectoral Indices module tracks NSE sector performance and constituents.

## Overview
Defined in `com.vegatrader.upstox.api.sectoral.SectoralIndex`.

## Supported Indices (21 Total)

### Banking & Finance
- **Nifty Bank** (`nifty_bank`)
- **Nifty Financial Services** (`nifty_financial`)
- **Nifty Fin Services 25/50** (`nifty_fin_25_50`)
- **Nifty Private Bank** (`nifty_private_bank`)
- **Nifty PSU Bank** (`nifty_psu_bank`)

### Technology
- **Nifty IT** (`nifty_it`)
- **Nifty MidSmall IT & Telecom** (`nifty_midsmall_it_telecom`)

### Consumer
- **Nifty FMCG** (`nifty_fmcg`)
- **Nifty Consumer Durables** (`nifty_consumer_durables`)
- **Nifty Auto** (`nifty_auto`)

### Others
- **Health**: Pharma, Healthcare
- **Commodities**: Metal, Oil & Gas
- **Infra**: Realty, Energy
- **Misc**: Media, Chemicals

## Functionality
- **CSV Mapping**: Each index maps to a specific NSE constituent CSV file (e.g., `ind_niftybanklist.csv`).
- **Grouping**: Helpers to get indices by group (e.g., `getSectorsByGroup("BANKING")`).
- **URLs**: Auto-generates download URLs for constituent data.
