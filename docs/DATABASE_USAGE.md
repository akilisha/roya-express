# Database Plugin Usage Guide

## Setup

### 1. Start PostgreSQL

```bash
docker-compose up -d
```

Schema auto-creates from `init-database.sql` on first startup.

### 2. Register Database Plugin

```java
var app = Roya.create();
var services = app.services();

// Register database
Database db = new DatabaseServiceImpl(
    "jdbc:postgresql://localhost:5432/roya",
    "postgres", 
    "postgres"
);
services.singleton(Database.class, () -> db);
```

## Usage

### Using Database in Handlers

```java
app.get("/users", (req, res, next) -> {
    // Explicit service lookup
    Database db = req.get(Database.class);
    
    // Use JOOQ DSL
    List<User> users = db.dsl()
        .select(USERS.ID, USERS.NAME, USERS.EMAIL)
        .from(USERS)
        .fetchInto(User.class);
    
    res.json(users);
});
```

### Transactions

```java
app.post("/users", (req, res, next) -> {
    Database db = req.get(Database.class);
    
    // Use transaction wrapper
    db.transaction(ctx -> {
        ctx.insertInto(USERS)
           .set(USERS.NAME, "Alice")
           .set(USERS.EMAIL, "alice@example.com")
           .execute();
        return null;
    });
    
    res.status(201).json(Map.of("message", "User created"));
});
```

## Admin Operations

### Run Migrations

```java
// In your app or admin endpoint
Database db = req.get(Database.class);
int count = db.migrate(); // Runs Flyway migrations from classpath:db/migration
```

### Generate JOOQ Classes

```java
// Call before starting app
Database db = ...;
db.generateModel(); // Reads schema, generates to build/generated/jooq
```

Then in your code:
```java
import static com.akilisha.oss.roya.plugins.database.jooq.Tables.*;

// Now you have type-safe table references
db.dsl()
    .select(USERS.NAME, USERS.EMAIL)
    .from(USERS)
    .where(USERS.AGE.greaterThan(18))
    .fetch();
```

## Architecture

**Framework**: roya-core - Zero database dependencies  
**Plugin**: roya-plugins/database - JOOQ + HikariCP + Flyway  
**Usage**: Explicit `req.get(Database.class)` - No magic!

