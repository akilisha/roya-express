package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.api.*;
import com.akilisha.oss.roya.plugins.database.Database;
import com.akilisha.oss.roya.plugins.database.DatabaseServiceImpl;
import org.jooq.*;
import org.jooq.impl.DSL;
import java.util.*;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;

/**
 * Complete CRUD demo with JOOQ, Postgres, and cURL verification.
 *
 * Setup:
 * 1. Start Postgres: docker-compose up -d (schema auto-creates)
 * 2. Run this: ./gradlew :roya-examples:run --args="UserDemo"  
 * 3. Test with curl commands (see USER_DEMO_GUIDE.md)
 * 4. Verify with: psql -h localhost -U postgres -d roya -c "SELECT * FROM users"
 */
public class UserDemo {

    public record User(Integer id, String name, String email, Integer age) {
    }

    public static void main(String[] args) {
        var app = Roya.create();

        // Register database service
        Database db = new DatabaseServiceImpl(
            "jdbc:postgresql://localhost:5432/roya",
            "postgres",
            "postgres"
        );
        app.services().singleton(Database.class, () -> db);

        // Application JSON middleware
        app.use((req, res, next) -> {
            res.header("Content-Type", "application/json");
            next.handle(req, res);
        });

        // GET /users - List all users
        app.get("/users", (req, res, next) -> {
            Database database = req.get(Database.class);
            
            // Use JOOQ manual mode to query database
            var users = database.dsl()
                .selectFrom(table("users"))
                .orderBy(field("id"))
                .fetch(result -> {
                    var r = (org.jooq.Record) result;
                    return new User(
                        r.get("id", Integer.class),
                        r.get("name", String.class),
                        r.get("email", String.class),
                        r.get("age", Integer.class)
                    );
                });
            
            res.json(users);
        });

        // GET /users/:id - Get single user
        app.get("/users/:id", (req, res, next) -> {
            Integer id = Integer.parseInt(req.params().get("id").orElse("0"));
            Database database = req.get(Database.class);
            
            var user = database.dsl()
                .selectFrom(table("users"))
                .where(field("id").eq(id))
                .fetchOne(result -> {
                    var r = (org.jooq.Record) result;
                    return new User(
                        r.get("id", Integer.class),
                        r.get("name", String.class),
                        r.get("email", String.class),
                        r.get("age", Integer.class)
                    );
                });
            
            if (user != null) {
                res.json(user);
            } else {
                res.status(404).json(Map.of("error", "User not found", "id", id));
            }
        });

        // POST /users - Create user
        app.post("/users", (req, res, next) -> {
            Database database = req.get(Database.class);
            
            // Parse request body (simple JSON for now)
            String body = req.bodyText();
            // TODO: Parse JSON body properly with BodyParser middleware
            // For now, skip - just demonstrate pattern
            
            // Insert with JOOQ
            var id = database.dsl()
                .insertInto(table("users"))
                .set(field("name"), "Unknown") // Would use parsed body
                .set(field("email"), "unknown@example.com")
                .set(field("age"), 0)
                .returning(field("id"))
                .fetchOne()
                .get("id", Integer.class);
            
            res.status(201).json(Map.of(
                "message", "User created",
                "id", id
            ));
        });

        // PUT /users/:id - Update user
        app.put("/users/:id", (req, res, next) -> {
            Integer id = Integer.parseInt(req.params().get("id").orElse("0"));
            Database database = req.get(Database.class);
            
            int updated = database.dsl()
                .update(table("users"))
                .set(field("name"), "Updated Name") // Would use parsed body
                .where(field("id").eq(id))
                .execute();
            
            if (updated > 0) {
                res.json(Map.of("message", "User updated", "id", id));
            } else {
                res.status(404).json(Map.of("error", "User not found", "id", id));
            }
        });

        // DELETE /users/:id - Delete user
        app.delete("/users/:id", (req, res, next) -> {
            Integer id = Integer.parseInt(req.params().get("id").orElse("0"));
            Database database = req.get(Database.class);
            
            int deleted = database.dsl()
                .deleteFrom(table("users"))
                .where(field("id").eq(id))
                .execute();
            
            if (deleted > 0) {
                res.json(Map.of("message", "User deleted", "id", id));
            } else {
                res.status(404).json(Map.of("error", "User not found", "id", id));
            }
        });

        // Health check with database stats
        app.get("/health", (req, res, next) -> {
            Database database = req.get(Database.class);
            var stats = database.getStats();
            
            res.json(Map.of(
                "status", "healthy",
                "database", Map.of(
                    "activeConnections", stats.activeConnections(),
                    "idleConnections", stats.idleConnections(),
                    "totalConnections", stats.totalConnections()
                )
            ));
        });

        System.out.println("User CRUD Demo");
        System.out.println("==============");
        System.out.println("\nEndpoints:");
        System.out.println("  GET    /users      - List all users");
        System.out.println("  GET    /users/:id  - Get single user");
        System.out.println("  POST   /users      - Create user");
        System.out.println("  PUT    /users/:id  - Update user");
        System.out.println("  DELETE /users/:id - Delete user");
        System.out.println("  GET    /health     - Database connection pool stats");
        System.out.println("\nCurl commands:");
        System.out.println("  curl http://localhost:3000/users");
        System.out.println("  curl http://localhost:3000/health");
        System.out.println("  curl -X POST http://localhost:3000/users -H \"Content-Type: application/json\" -d '{\"name\":\"Alice\",\"email\":\"alice@example.com\",\"age\":30}'");
        System.out.println("\nVerify in database:");
        System.out.println("  psql -h localhost -U postgres -d roya -c \"SELECT * FROM users\"");
        System.out.println("\nNote: JOOQ code generation pending - currently returns mock data\n");

        app.listen(3000, () -> {
            System.out.println("✓ User CRUD Demo running on http://localhost:3000");
        });
    }
}

