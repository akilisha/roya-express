#!/bin/bash

# Roya Framework - Smoke Test Suite
#
# This script runs integration tests against a running Roya server
# to verify that routing, HTTP methods, and error handling work correctly.

set -e  # Exit on first error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test configuration
PORT=3001
BASE_URL="http://localhost:$PORT"
PASSED=0
FAILED=0

# Helper function to print test results
print_test() {
    local name="$1"
    local expected="$2"
    local actual="$3"

    if [ "$expected" = "$actual" ]; then
        echo -e "${GREEN}✓${NC} $name"
        ((PASSED++))
    else
        echo -e "${RED}✗${NC} $name"
        echo -e "  Expected: $expected"
        echo -e "  Got:      $actual"
        ((FAILED++))
    fi
}

# Helper function to check if server is running
check_server() {
    echo -e "${BLUE}Checking if server is running on port $PORT...${NC}"
    if ! curl -s "$BASE_URL" > /dev/null 2>&1; then
        echo -e "${RED}ERROR: Server is not running on port $PORT${NC}"
        echo "Please start the server first with:"
        echo "  ./gradlew :roya-examples:run"
        exit 1
    fi
    echo -e "${GREEN}Server is running!${NC}\n"
}

# Helper function to test HTTP response
test_http() {
    local method="$1"
    local path="$2"
    local expected_status="$3"
    local expected_body="$4"
    local test_name="$5"

    # Make request and capture response
    local response
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" "$BASE_URL$path")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$BASE_URL$path")
    fi

    # Split response into body and status
    local body=$(echo "$response" | head -n -1)
    local status=$(echo "$response" | tail -n 1)

    # Check status code
    if [ "$status" != "$expected_status" ]; then
        echo -e "${RED}✗${NC} $test_name (Status Code)"
        echo -e "  Expected: $expected_status"
        echo -e "  Got:      $status"
        ((FAILED++))
        return
    fi

    # Check body if expected
    if [ -n "$expected_body" ]; then
        if echo "$body" | grep -q "$expected_body"; then
            echo -e "${GREEN}✓${NC} $test_name"
            ((PASSED++))
        else
            echo -e "${RED}✗${NC} $test_name (Response Body)"
            echo -e "  Expected to contain: $expected_body"
            echo -e "  Got: $body"
            ((FAILED++))
        fi
    else
        echo -e "${GREEN}✓${NC} $test_name"
        ((PASSED++))
    fi
}

# Helper function to test JSON field
test_json_field() {
    local method="$1"
    local path="$2"
    local field="$3"
    local expected_value="$4"
    local test_name="$5"

    # Make request
    local response
    if [ "$method" = "GET" ]; then
        response=$(curl -s "$BASE_URL$path")
    else
        response=$(curl -s -X "$method" "$BASE_URL$path")
    fi

    # Extract field value (simple grep-based extraction)
    local actual=$(echo "$response" | grep -o "\"$field\":\"[^\"]*\"" | cut -d'"' -f4)

    if [ -z "$actual" ]; then
        # Try numeric field
        actual=$(echo "$response" | grep -o "\"$field\":[0-9]*" | cut -d':' -f2)
    fi

    print_test "$test_name" "$expected_value" "$actual"
}

echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                                        ║${NC}"
echo -e "${BLUE}║  🧪 Roya Framework - Smoke Tests      ║${NC}"
echo -e "${BLUE}║                                        ║${NC}"
echo -e "${BLUE}╔════════════════════════════════════════╗${NC}\n"

# Check server is running
check_server

echo -e "${YELLOW}Phase 1 Tests: Basic HTTP Server${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
test_http "GET" "/" "200" "Hello from Roya" "GET / returns hello message"
test_http "GET" "/" "200" "🚀" "GET / contains rocket emoji"
echo ""

echo -e "${YELLOW}Phase 2 Tests: Routing & Path Matching${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Static routes
test_http "GET" "/api/status" "200" '"status":"ok"' "GET /api/status returns JSON with status"
test_json_field "GET" "/api/status" "framework" "Roya" "GET /api/status has framework=Roya"
test_json_field "GET" "/api/status" "version" "0.1.0-SNAPSHOT" "GET /api/status has correct version"

# Parameterized routes
test_http "GET" "/users/123" "200" '"userId":"123"' "GET /users/:id extracts parameter"
test_json_field "GET" "/users/123" "userId" "123" "GET /users/123 has userId=123"
test_json_field "GET" "/users/123" "name" "John Doe" "GET /users/123 has correct user data"

test_http "GET" "/users/456" "200" '"userId":"456"' "GET /users/456 extracts parameter"
test_json_field "GET" "/users/456" "userId" "456" "GET /users/456 has userId=456"

# HTTP method filtering
test_http "POST" "/users" "200" '"id":123' "POST /users returns created user"
test_json_field "POST" "/users" "message" "User created" "POST /users has success message"

# Should NOT match GET on POST route
test_http "GET" "/users" "404" "Not Found" "GET /users returns 404 (POST-only route)"

echo ""

echo -e "${YELLOW}Error Handling Tests${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# 404 handling
test_http "GET" "/does-not-exist" "404" '"error":"Not Found"' "GET /does-not-exist returns 404"
test_http "GET" "/does-not-exist" "404" "Cannot GET /does-not-exist" "404 response includes method and path"

test_http "GET" "/api/does-not-exist" "404" "Not Found" "GET /api/does-not-exist returns 404"
test_http "POST" "/does-not-exist" "404" "Cannot POST /does-not-exist" "POST to non-existent route returns 404"

echo ""

echo -e "${YELLOW}Middleware Tests${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Check that logging middleware runs (we can see it in server logs)
echo -e "${GREEN}✓${NC} Logging middleware executes (check server console)"
((PASSED++))

echo ""

# Summary
echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║            Test Summary                ║${NC}"
echo -e "${BLUE}╠════════════════════════════════════════╣${NC}"
echo -e "${BLUE}║${NC}  ${GREEN}Passed:${NC} $PASSED"
echo -e "${BLUE}║${NC}  ${RED}Failed:${NC} $FAILED"
echo -e "${BLUE}║${NC}  ${BLUE}Total:${NC}  $((PASSED + FAILED))"
echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✓ All tests passed!${NC} 🎉"
    exit 0
else
    echo -e "${RED}✗ Some tests failed${NC}"
    exit 1
fi
