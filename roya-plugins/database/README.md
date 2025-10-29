# Database Plugin

Database plugin for Roya Framework - provides thin wrapper around JOOQ with HikariCP connection pooling.

## Philosophy

This plugin demonstrates Roya's **plugin-first architecture**:
- **Framework is database-agnostic** - roya-core doesn't include any database dependencies
- **JOOQ is a choice, not requirement** - you could swap for JPA, MyBatis, or any other library
- **Plugin system is unopinionated** - just implements `RoyaPlugin` and provides services

## Features

- ✅ JOOQ DSLContext for type-safe SQL
- ✅ HikariCP connection pooling optimized for virtual threads  
- ✅ PostgreSQL default (but can configure any JDBC database)
- ✅ Transaction management (`db.transaction()`)
- ✅ Connection pool monitoring (`db.getStats()`)

## Usage

### 1. Start PostgreSQL

```bash
docker-compose up -d
```

### 2. Register the Plugin

```java
var app = Roya.create();

// Install database plugin
app.plugin(new DatabasePlugin());

// Database is now available in handlers via service locator
app.get("/users/:id", (req, res, next) -> {
    Database db = req.get(Database.class);
    
    // Use JOOQ DSL directly
    User user = db.dsl()
        .selectFrom(Users.USERS)
        .where(Users.USERS.ID.eq(req.params().get("id").get()))
        .fetchOneInto(User.class);
    
    res.json(user);
});
```

### 3. Transaction Support

```java
db.transaction(ctx -> {
    // Multiple operations in transaction
    ctx.dsl().insertInto(Users.USERS)
        .set(Users.USERS.NAME, "Alice")
        .execute();
    
    ctx.dsl().update(Users.USERS)
        .set(Users.USERS.EMAIL, "alice@example.com")
        .where(Users.USERS.NAME.eq("Alice"))
        .execute();
    
    return null; // Transaction auto-commits on success, rolls back on error
});
```

## Configuration

Set system properties or environment variables:

```bash
# JDBC connection
-Ddatabase.url=jdbc:postgresql://localhost:5432/roya
-Ddatabase.username=postgres
-Ddatabase.password=postgres
```

## Architecture

```
┌─────────────────────────────────────┐
│         roya-core (lightweight)     │  ← No database dependencies!
├─────────────────────────────────────┤
│  • Middleware pipeline               │
│  • Routing                           │
│  • Request/Response                  │
│  • Service locator (req.get())       │
└──────────────┬───────────────────────┘
               │
               │ Plugin interface
               ↓
┌─────────────────────────────────────┐
│    Database Plugin (optional)       │  ← JOOQ + HikariCP
├─────────────────────────────────────┤
│  • Provides Database service         │
│  • Thin JOOQ wrapper                │
│  • Connection pooling                │
│  • Transaction management            │
└─────────────────────────────────────┘
```

## Extending

Want to use JPA instead of JOOQ? Just implement `RoyaPlugin`:

```java
public class JpaPlugin implements RoyaPlugin {
    public void register(Services services) {
        services.singleton(EntityManager.class, this::createEntityManager);
    }
    // ...
}
```

The framework doesn't care - it's all just services!

## Status

🚧 **In Progress** - Core structure complete
- [x] JOOQ integration
- [x] HikariCP connection pooling
- [x] Plugin registration
- [x] Transaction support
- [ ] JOOQ code generation (WIP)
- [ ] Example application (WIP)
- [ ] Comprehensive tests (WIP)

