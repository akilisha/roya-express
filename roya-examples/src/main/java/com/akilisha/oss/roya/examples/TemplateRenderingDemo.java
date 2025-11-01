package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.core.HandlebarsViewOptions;

import java.util.Map;

/**
 * Template Rendering Demo - Showcases Handlebars template engine integration.
 * <p>
 * This demonstrates:
 * - Express-compatible app.set('view engine', 'hbs') and app.set('views', 'views')
 * - Type-safe app.view(HandlebarsViewOptions.create("views"))
 * - Direct app.engine("hbs", new HandlebarsEngine("views"))
 * - res.render(template, data) for rendering templates
 * <p>
 * Run the server and visit:
 * - http://localhost:3001/                     → Simple greeting
 * - http://localhost:3001/users                → User list template
 * - http://localhost:3001/product/123          → Product details with partials
 */
public class TemplateRenderingDemo {

    public static void main(String[] args) {
        var app = Roya.create();

        // ========== APPROACH 1: Express-style app.set() ==========
        // Most compatible with Express.js
        app.set("view engine", "hbs");
        app.set("views", "roya-examples/src/main/resources/views");

        // ========== APPROACH 2: Type-safe ViewOptions ==========
        // app.view(HandlebarsViewOptions.create("roya-examples/src/main/resources/views"));

        // ========== APPROACH 3: Direct engine registration ==========
        // app.engine("hbs", new HandlebarsEngine("roya-examples/src/main/resources/views"));

        // ========== TEMPLATE RENDERING ROUTES ==========

        // Simple greeting page
        app.get("/", (req, res, next) -> {
            res.render("home.hbs", Map.of(
                "title", "Welcome to Roya",
                "message", "Template rendering is working!"
            ));
        });

        // User list template
        app.get("/users", (req, res, next) -> {
            var users = java.util.List.of(
                Map.of("id", 1, "name", "Alice", "email", "alice@example.com"),
                Map.of("id", 2, "name", "Bob", "email", "bob@example.com"),
                Map.of("id", 3, "name", "Charlie", "email", "charlie@example.com")
            );
            
            res.render("users.hbs", Map.of(
                "title", "User Directory",
                "users", users
            ));
        });

        // Product details with partials
        app.get("/product/:id", (req, res, next) -> {
            var productId = req.params().get("id");
            var product = Map.of(
                "id", productId,
                "name", "Amazing Product",
                "price", "$99.99",
                "description", "This is an amazing product with lots of features!",
                "inStock", true
            );
            
            res.render("product.hbs", Map.of(
                "title", "Product Details",
                "product", product
            ));
        });

        // ========== START SERVER ==========
        app.listen(3001, () -> {
            System.out.println();
            System.out.println("╔═══════════════════════════════════════════════════════════╗");
            System.out.println("║   Template Rendering Demo Server                         ║");
            System.out.println("║                                                            ║");
            System.out.println("║   Server running on http://localhost:3001                 ║");
            System.out.println("║                                                            ║");
            System.out.println("║   Try these URLs:                                         ║");
            System.out.println("║   • http://localhost:3001/                                ║");
            System.out.println("║   • http://localhost:3001/users                           ║");
            System.out.println("║   • http://localhost:3001/product/123                     ║");
            System.out.println("║                                                            ║");
            System.out.println("╚═══════════════════════════════════════════════════════════╝");
            System.out.println();
        });
    }
}

