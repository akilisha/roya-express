# JOOQ Code Generation Setup

## Quick Manual Generation

### Option 1: Using Gradle (Simpler)
```bash
# Start database if not running
docker-compose up -d
Get-Content init-database.sql | docker exec -i roya-postgres psql -U postgres -d roya

# Download JOOQ jars
gradlew :roya-plugins:database:dependencies

# Generate (manual for now)
cd roya-plugins/database
java -cp "path/to/jooq-3.18.7.jar:jooq-codegen-3.18.7.jar:postgresql-42.7.1.jar" org.jooq.codegen.GenerationTool src/main/resources/jooq-config.xml
```

### Option 2: Use JOOQ Manual Mode
```java
// In your code, use JOOQ's manual mode
import static org.jooq.impl.DSL.*;

public List<User> getUsers() {
    return dsl().selectFrom(table("users"))
        .fetchInto(User.class);
}
```

## Current Status

**Database is ready:**
- ✅ Users table created
- ✅ Sample data inserted
- ✅ Schema accessible

**JOOQ generation pending:**
- ⏳ Configure Gradle plugin OR use manual generation
- ⏳ Generate `Users` table class
- ⏳ Generate `UsersRecord` class
- ⏳ Wire into UserDemo.java

## Alternative: Start with Manual Queries

For now, you can use JOOQ's manual mode without code generation:

```java
import static org.jooq.impl.DSL.*;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

// In UserDemo
Database db = req.get(Database.class);
DSLContext ctx = db.dsl();

// Manual query
Result<Record> result = ctx.selectFrom(table("users"))
    .where(field("email").eq("alice@example.com"))
    .fetch();

// Convert to map
Map<String, Object> user = result.get(0).intoMap();
```

This works immediately without code generation!

