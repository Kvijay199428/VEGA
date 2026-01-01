# Archive Folder

This folder contains reference files that are not actively used in the application but kept for reference purposes.

## Files

### `login_bk.py`
- **Purpose**: Backup/reference copy of the original login script
- **Status**: NOT USED in active code
- **Active Location**: `backend/upstox_auth/login.py`
- **Note**: This file was used as a reference to create the centralized login script. Keep for historical reference only.

## Active Login Structure

All active login-related code is centralized in:
```
backend/upstox_auth/
├── login.py          # Main login script (--single, --multi-login flags)
└── upstox/           # Modular components (if any)
```

## Endpoints Using Login Scripts

- `POST /api/v1/auth/upstox/automated-single-login` → `backend/upstox_auth/login.py --single`
- `POST /api/v1/auth/upstox/launch-remaining-login` → `backend/upstox_auth/login.py --multi-login`

---
*Last Updated: 2025-12-23*
