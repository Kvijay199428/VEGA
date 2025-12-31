$test1 = Invoke-WebRequest -Uri "http://localhost:28020/api/v1/options/underlyings?segment=index&exchange=NSE" -UseBasicParsing
Write-Host "=== TEST 1: Underlyings (NSE) ==="
Write-Host "Status:" $test1.StatusCode
Write-Host "Content:" $test1.Content
Write-Host ""

$test2 = Invoke-WebRequest -Uri "http://localhost:28020/api/v1/options/expiries/NSE_INDEX%7CNifty%2050" -UseBasicParsing  
Write-Host "=== TEST 2: Expiries (Nifty 50) ==="
Write-Host "Status:" $test2.StatusCode
Write-Host "Content:" $test2.Content
Write-Host ""

$test3 = Invoke-WebRequest -Uri "http://localhost:28020/api/v1/options/chain?instrument_key=NSE_INDEX%7CNifty%2050&expiry_date=2025-12-30" -UseBasicParsing
Write-Host "=== TEST 3: Chain (Nifty 50) ==="
Write-Host "Status:" $test3.StatusCode
$json = $test3.Content | ConvertFrom-Json
Write-Host "Data Count:" $json.data.Length
Write-Host ""
