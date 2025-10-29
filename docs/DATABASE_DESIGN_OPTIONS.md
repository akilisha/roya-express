# Database Capability Design Options

## Option A: Explicit Database Access (Simplest)

**Pattern**: Request doesn't extend DatabaseAware, use explicit lookup

```java
// In handler
app.get("/users/:id", (req, res, next) -> {
    // Explicit database access
    Database db = req.get(Database.class); // Throws if not available
    User user = db.dsl().select(USERS.NAME).from(USERS)...;
    res.json(user);
});

// For migrations/admin
app.get("/admin/migrate", (req, res, next) -> {
    DatabaseAware db = req.get(Database.class); // Get as DatabaseAware
    db.migrate();
    res.json(Map.of("migrations", "executed"));
});
```

**Pros**: 
- ✅ Simple, explicit
- ✅ Framework doesn't know about Database
- ✅ Zero coupling

**Cons**:
- ❌ Have to handle plugin availability manually
- ❌ Not "magical" - explicit `req.get()`

## Option B: Request Extends DatabaseAware (Magic)

**Pattern**: Request interface extends DatabaseAware, RequestImpl delegates

```java
// In handler  
app.get("/users/:id", (req, res, next) -> {
    // Magic database access via Request
    User user = req.withContext(ctx -> 
        ctx.select(USERS.NAME).from(USERS)...,
        User.class
    );
    res.json(user);
});

// Migrations
req.migrate();
```

**Pros**:
- ✅ Clean usage: `req.withContext(...)`
- ✅ Request "has" database capability
- ✅ Developer doesn't need to think about plugin availability

**Cons**:
- ❌ How does RequestImpl find the Database plugin?
- ❌ What if Database plugin not installed? (throws exception?)
- ❌ Request is coupled to database concept

## Option C: Services Method for DatabaseAware Lookup (Best?)

**Pattern**: Services can lookup by interface, not just class

```java
// In Services interface, add:
Optional<DatabaseAware> findDatabaseAware();

// In RequestImpl:
@Override
public <T, R> R withContext(Function<T, ?> handler, Class<R> resultType) {
    return services.findDatabaseAware()
        .map(db -> db.withContext(handler, resultType))
        .orElseThrow(() -> new UnsupportedOperationException("Database not available"));
}
```

**Pros**:
- ✅ Request can provide database capabilities
- ✅ Clean delegation through Services
- ✅ Services registry knows about capabilities

**Cons**:
- ❌ Need to add findDatabaseAware() to Services API
- ❌ Still coupling Request to DatabaseAware concept

## My Preference: Option A (Explicit)

I think **Option A** (explicit database access) is cleanest because:

1. **Framework stays lightweight** - roya-core doesn't need DatabaseAware at all
2. **Clear intent** - `req.get(Database.class)` shows you're using database
3. **Type safety** - Compile-time error if Database not imported
4. **Express.js philosophy** - Everything is explicit, no hidden magic

The "magic" of `req.withContext()` is appealing, but I'm not sure it's worth the complexity of Services lookup mechanism.

**What do you think?**

