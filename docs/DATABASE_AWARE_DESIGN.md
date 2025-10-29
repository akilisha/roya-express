# DatabaseAware Interface Design

## Problem

Currently the Database plugin provides raw JOOQ access but:
1. No standardized contracts between framework and plugin
2. No useful abstraction layer
3. Framework can't leverage database capabilities
4. Tight coupling between JOOQ and framework

## Solution: DatabaseAware Interface

Create a **contract interface** that both:
- **RequestImpl** implements (delegates to Database)
- **Database plugin** provides (concrete implementation)

This follows the **collaboration pattern** to avoid coupling.

## Proposed Interface

```java
/**
 * Database capability interface - implemented by both:
 * - RequestImpl (delegates to plugin)
 * - Database plugin (provides concrete implementation)
 */
public interface DatabaseAware {

    /**
     * Run database migrations from standard path.
     * 
     * Framework calls this -> delegates to plugin -> executes migrations
     */
    void migrate();

    /**
     * Generate model/entity classes.
     * 
     * For JOOQ: runs code generation, outputs to standard location
     * For JPA: generates entities
     * Framework doesn't care what, just knows it needs model classes
     */
    void generateModel();

    /**
     * Execute function with database context.
     * 
     * For JOOQ: provides DSLContext
     * Returns Entity automatically mapped from query result
     */
    <T, R> R withContext(java.util.function.Function<T, R> handler, Class<R> entityType);

    /**
     * Execute function within transaction.
     * 
     * For JOOQ: provides DSLContext with transaction semantics
     * Automatically commits on success, rolls back on error
     */
    <T, R> R withTransaction(java.util.function.Function<T, R> handler, Class<R> entityType);
}
```

## Implementation Pattern

### RequestImpl (Framework Side)

```java
public class RequestImpl implements Request, DatabaseAware {
    
    private final Services services;
    
    @Override
    public void migrate() {
        Database db = services.get(Database.class);
        if (db != null && db instanceof DatabaseAware aware) {
            aware.migrate(); // Delegate to plugin
        }
    }
    
    @Override
    public <T, R> R withContext(Function<T, R> handler, Class<R> entityType) {
        Database db = services.get(Database.class);
        if (db instanceof DatabaseAware aware) {
            return aware.withContext(handler, entityType);
        }
        throw new UnsupportedOperationException("Database not available");
    }
}
```

### Database Plugin (Implementation Side)

```java
public class DatabaseServiceImpl implements Database, DatabaseAware {
    
    @Override
    public void migrate() {
        // Execute migrations from src/main/resources/migrations/
        // Use Flyway or custom migration runner
        new Flyway().migrate();
    }
    
    @Override
    public void generateModel() {
        // Generate JOOQ classes to build/generated/jooq
        org.jooq.codegen.GenerationTool.main(jooqConfig);
    }
    
    @Override
    public <T, R> R withContext(Function<T, R> handler, Class<R> entityType) {
        // Execute with DSLContext, auto-map to entity
        DSLContext ctx = dsl();
        Object result = handler.apply((T) ctx);
        return mapToEntity(result, entityType);
    }
    
    @Override
    public <T, R> R withTransaction(Function<T, R> handler, Class<R> entityType) {
        // Execute in transaction
        return transaction(ctx -> {
            Object result = handler.apply((T) ctx);
            return mapToEntity(result, entityType);
        });
    }
}
```

## Usage in Handlers

```java
app.get("/users/:id", (req, res, next) -> {
    User user = ((DatabaseAware) req).withContext(ctx -> {
        // ctx is DSLContext (for JOOQ)
        return ctx.selectFrom(USERS)
            .where(USERS.ID.eq(req.params().get("id")))
            .fetchOneInto(User.class);
    }, User.class);
    
    res.json(user);
});
```

## Benefits

1. **Decoupling**: Framework doesn't know about JOOQ, just DatabaseAware
2. **Swappable**: Swap JOOQ for JPA by changing plugin implementation
3. **Consistent**: Standard methods across all database operations
4. **Powerful**: Framework can call migrate(), generateModel() automatically
5. **Type-safe**: withContext() and withTransaction() return typed entities

## Alternative Approaches Considered

### Option A: Direct service access (current)
```java
Database db = req.get(Database.class);
db.dsl().selectFrom(table("users")).fetch();
```
**Problem**: Framework must know about JOOQ DSLContext

### Option B: DatabaseAware (proposed)
```java
((DatabaseAware) req).withContext(ctx -> ctx.selectFrom(USERS).fetch(), User.class);
```
**Benefit**: Framework only knows DatabaseAware interface

## Questions to Resolve

1. **Entity mapping**: How does `mapToEntity()` work? Auto-detection or explicit?
2. **Migration location**: Should it be `src/main/resources/migrations/`?
3. **Generated classes location**: Should be `build/generated/jooq` or configurable?
4. **Error handling**: What happens if Database plugin not installed?
5. **Multiple databases**: How to specify which database to use?

## Next Steps

- [ ] Finalize interface design
- [ ] Implement in RequestImpl
- [ ] Implement in DatabaseServiceImpl  
- [ ] Add migration support (Flyway or custom)
- [ ] Add model generation support
- [ ] Create example usage
- [ ] Write tests

