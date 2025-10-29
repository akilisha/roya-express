#!/bin/bash

# Setup PostgreSQL database for JOOQ code generation

echo "Setting up PostgreSQL database for JOOQ code generation..."
echo ""

# Start postgres
echo "Starting PostgreSQL..."
docker-compose up -d

# Wait for postgres to be ready
echo "Waiting for PostgreSQL to be ready..."
sleep 5

# Create schema
echo "Creating schema..."
docker exec -i roya-postgres psql -U postgres -d roya << 'EOF'
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    age INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Insert sample data
INSERT INTO users (name, email, age) VALUES
    ('Alice', 'alice@example.com', 30),
    ('Bob', 'bob@example.com', 25),
    ('Charlie', 'charlie@example.com', 35)
ON CONFLICT (email) DO NOTHING;
EOF

echo ""
echo "✓ Database setup complete!"
echo "Tables available:"
docker exec -i roya-postgres psql -U postgres -d roya -c "\dt"
echo ""
echo "Sample data:"
docker exec -i roya-postgres psql -U postgres -d roya -c "SELECT * FROM users"

