@echo off
REM Roya Framework - Smoke Test Suite
REM Comprehensive integration tests for Phase 1 & Phase 2
REM Tests HTTP server, routing, path matching, and error handling

setlocal enabledelayedexpansion

echo ========================================
echo Roya Framework - Smoke Test Suite
echo ========================================
echo.

set BASE_URL=http://localhost:3001
set PASSED=0
set FAILED=0

REM Check if server is running
echo [CHECK] Testing server availability...
curl -s -o nul -w "%%{http_code}" %BASE_URL%/ > temp_status.txt
set /p STATUS=<temp_status.txt
del temp_status.txt

if not "%STATUS%"=="200" (
    echo [ERROR] Server is not running on %BASE_URL%
    echo [ERROR] Please start the server first: java -jar roya-server/target/roya-server-1.0-SNAPSHOT.jar
    exit /b 1
)
echo [OK] Server is running
echo.

REM Test 1: Basic root route
echo [TEST 1] GET / - Basic root route
curl -s %BASE_URL%/ > temp_response.txt
findstr /C:"Hello from Roya" temp_response.txt >nul
if %errorlevel% equ 0 (
    echo [PASS] Root route returns greeting
    set /a PASSED+=1
) else (
    echo [FAIL] Root route did not return expected greeting
    set /a FAILED+=1
)
del temp_response.txt
echo.

REM Test 2: JSON response
echo [TEST 2] GET /api/status - JSON response
curl -s %BASE_URL%/api/status > temp_response.txt
findstr /C:"Roya Framework" temp_response.txt >nul
if %errorlevel% equ 0 (
    echo [PASS] Status endpoint returns JSON
    set /a PASSED+=1
) else (
    echo [FAIL] Status endpoint did not return expected JSON
    set /a FAILED+=1
)
del temp_response.txt
echo.

REM Test 3: Path parameter extraction - userId 123
echo [TEST 3] GET /users/123 - Path parameter extraction
curl -s %BASE_URL%/users/123 > temp_response.txt
findstr /C:"userId" temp_response.txt >nul && findstr /C:"123" temp_response.txt >nul
if %errorlevel% equ 0 (
    echo [PASS] Parameter extraction working (userId=123)
    set /a PASSED+=1
) else (
    echo [FAIL] Parameter extraction failed
    set /a FAILED+=1
)
del temp_response.txt
echo.

REM Test 4: Path parameter extraction - userId 456
echo [TEST 4] GET /users/456 - Different path parameter
curl -s %BASE_URL%/users/456 > temp_response.txt
findstr /C:"userId" temp_response.txt >nul && findstr /C:"456" temp_response.txt >nul
if %errorlevel% equ 0 (
    echo [PASS] Parameter extraction working (userId=456)
    set /a PASSED+=1
) else (
    echo [FAIL] Parameter extraction failed for different parameter
    set /a FAILED+=1
)
del temp_response.txt
echo.

REM Test 5: HTTP method filtering - POST
echo [TEST 5] POST /users - HTTP method filtering
curl -s -X POST %BASE_URL%/users > temp_response.txt
findstr /C:"User created" temp_response.txt >nul
if %errorlevel% equ 0 (
    echo [PASS] POST method handler executed
    set /a PASSED+=1
) else (
    echo [FAIL] POST method handler did not execute
    set /a FAILED+=1
)
del temp_response.txt
echo.

REM Test 6: HTTP method filtering - GET should 404
echo [TEST 6] GET /users - Should return 404 (POST only)
curl -s -o nul -w "%%{http_code}" %BASE_URL%/users > temp_status.txt
set /p STATUS=<temp_status.txt
del temp_status.txt
if "%STATUS%"=="404" (
    echo [PASS] GET request correctly returns 404 for POST-only route
    set /a PASSED+=1
) else (
    echo [FAIL] Expected 404, got %STATUS%
    set /a FAILED+=1
)
echo.

REM Test 7: 404 handling for non-existent routes
echo [TEST 7] GET /does-not-exist - 404 error handling
curl -s -o nul -w "%%{http_code}" %BASE_URL%/does-not-exist > temp_status.txt
set /p STATUS=<temp_status.txt
del temp_status.txt
if "%STATUS%"=="404" (
    echo [PASS] Non-existent route returns 404
    set /a PASSED+=1
) else (
    echo [FAIL] Expected 404, got %STATUS%
    set /a FAILED+=1
)
echo.

REM Test 8: 404 error message format
echo [TEST 8] 404 error message format
curl -s %BASE_URL%/does-not-exist > temp_response.txt
findstr /C:"Not Found" temp_response.txt >nul && findstr /C:"Cannot GET" temp_response.txt >nul
if %errorlevel% equ 0 (
    echo [PASS] 404 error message properly formatted
    set /a PASSED+=1
) else (
    echo [FAIL] 404 error message not properly formatted
    set /a FAILED+=1
)
del temp_response.txt
echo.

REM Test 9: Content-Type header for JSON
echo [TEST 9] Content-Type header for JSON responses
curl -s -I %BASE_URL%/api/status > temp_headers.txt
findstr /C:"application/json" temp_headers.txt >nul
if %errorlevel% equ 0 (
    echo [PASS] JSON Content-Type header set correctly
    set /a PASSED+=1
) else (
    echo [FAIL] JSON Content-Type header not set
    set /a FAILED+=1
)
del temp_headers.txt
echo.

REM Test 10: HTTP status code 200 for successful requests
echo [TEST 10] HTTP 200 status for successful requests
curl -s -o nul -w "%%{http_code}" %BASE_URL%/ > temp_status.txt
set /p STATUS=<temp_status.txt
del temp_status.txt
if "%STATUS%"=="200" (
    echo [PASS] Successful request returns 200 OK
    set /a PASSED+=1
) else (
    echo [FAIL] Expected 200, got %STATUS%
    set /a FAILED+=1
)
echo.

REM Summary
echo ========================================
echo Test Summary
echo ========================================
echo Total Tests: %PASSED% + %FAILED%
echo [PASS] Passed: %PASSED%
if %FAILED% gtr 0 (
    echo [FAIL] Failed: %FAILED%
    echo.
    echo [RESULT] SMOKE TESTS FAILED
    exit /b 1
) else (
    echo [FAIL] Failed: %FAILED%
    echo.
    echo [RESULT] ALL SMOKE TESTS PASSED!
    exit /b 0
)
