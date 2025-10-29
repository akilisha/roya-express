# User CRUD Demo - Quick Start Guide

Complete CRUD example with cURL and database verification.

## Step 1: Start PostgreSQL

```bash
docker-compose up -d
```

Wait for postgres to be healthy.

## Step 2: Create Database Schema

```bash
# Connect to postgres and create schema
docker exec -i roya-postgres psql -U postgres -d roya << 'EOF'
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    age INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
EOF
```

Or manually:
```bash
docker exec -it roya-postgres psql -U postgres -d roya
# Then paste the schema from roya-plugins/database/src/main/resources/schema.sql
```

## Step 3: Run the Demo

```bash
./gradlew :roya-examples:run --args="UserDemo"
```

Server starts on http://localhost:3000

## Step 4: Test with cURL

### Get all users
```bash
curl http://localhost:3000/users
```

### Get single user
```bash
curl http://localhost:3000/users/1
```

### Create user
```bash
curl -X POST http://localhost:3000/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@example.com","age":30}'
```

### Update user
```bash
curl -X PUT http://localhost:3000/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice Updated","email":"alice.new@example.com","age":31}'
```

### Delete user
```bash
curl -X DELETE http://localhost:3000/users/1
```

### Health check (database pool stats)
```bash
curl http://localhost:3000/health
```

## Step 5: Verify in Database

### Connect to database
```bash
docker exec -it roya-postgres psql -U postgres -d roya
```

### Query users table
```sql
SELECT * FROM users;
```

### Check specific user
```sql
SELECT * FROM users WHERE id = 1;
```

### Verify email uniqueness
```sql
SELECT email, COUNT(*) FROM users GROUP BY email;
```

### Exit psql
```sql
\q
```

## Expected Results

### cURL Response
```json
{
  "users": [
    {"id": 1, "name": "Alice", "email": "alice@example.com", "age": 30}
  ]
}
```

### Database Verification
```
 roya=# SELECT * FROM users;
  id | name | email               | age | created_at
 ----+------+---------------------+-----+---------------
   1 | Alice| alice@example.com   |  30 | 2025-01-13...
```

## Note

Currently returns mock data until JOOQ code generation is fully configured.
Once JOOQ is set up, this will use real database queries.

