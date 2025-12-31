Fund and Margin API Response Change
Effective Date: July 19, 2025

Overview
From July 19, 2025, the Fund and Margin API will return combined funds for both Equity and Commodity segments in the equity object. This change affects applications that process segment specific fund data separately.

For full documentation of the Fund and Margin API, please refer to the Get User Fund & Margin page.

What's Changing
Currently, the Fund and Margin API returns separate fund information for Equity and Commodity segments in their respective objects. After the change, the combined funds data for both segments will be returned exclusively in the equity object.

Note: To maintain backward compatibility and prevent breaking changes, the commodity field will still be included in the response structure but all values will be set to zero.

Current Response Structure
{
  "status": "success",
  "data": {
    "equity": {
      // Equity segment funds only
    },
    "commodity": {
      // Commodity segment funds only
    }
  }
}

New Response Structure (After July 19, 2025)
{
  "status": "success",
  "data": {
    "equity": {
      // Combined funds for both Equity and Commodity segments
    },
    "commodity": {
      // All values will be set to zero
    }
  }
}

Action Required
If your application relies on segment specific fund data, you will need to update your implementation to handle the combined data. All fund-related calculations should now be performed using the values from the equity object only.