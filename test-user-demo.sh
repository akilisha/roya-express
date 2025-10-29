#!/bin/bash

# Test User CRUD Demo with cURL

echo "=================================="
echo "User CRUD Demo - cURL Tests"
echo "=================================="
echo ""

echo "1. Getting all users..."
curl -s http://localhost:3000/users | jq
echo ""

echo "2. Getting user with id=1..."
curl -s http://localhost:3000/users/1 | jq
echo ""

echo "3. Creating new user..."
curl -s -X POST http://localhost:3000/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@example.com","age":30}' | jq
echo ""

echo "4. Updating user with id=1..."
curl -s -X PUT http://localhost:3000/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice Updated","email":"alice.new@example.com","age":31}' | jq
echo ""

echo "5. Health check (connection pool stats)..."
curl -s http://localhost:3000/health | jq
echo ""

echo "6. Verifying users in database..."
docker exec -i roya-postgres psql -U postgres -d roya -c "SELECT * FROM users"
echo ""

echo "=================================="
echo "Tests complete!"
echo "=================================="

